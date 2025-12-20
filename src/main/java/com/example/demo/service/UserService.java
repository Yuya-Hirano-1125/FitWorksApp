package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.BackgroundUnlockDto;
import com.example.demo.dto.MissionStatusDto;
import com.example.demo.entity.User;
import com.example.demo.entity.UserItem;
import com.example.demo.repository.TrainingRecordRepository;
import com.example.demo.repository.UserItemRepository; // ★追加
import com.example.demo.repository.UserRepository;
@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final TrainingRecordRepository trainingRecordRepository; 
    private final PasswordEncoder passwordEncoder; 
    

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private UserItemRepository userItemRepository;

    @Autowired
    private SmsService smsService;
    
 // ★設定: 変更禁止期間（日数）
    private static final int USERNAME_CHANGE_COOLDOWN_DAYS = 7;

    @Value("${app.base-url}")
    private String baseUrl;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TrainingRecordRepository trainingRecordRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.trainingRecordRepository = trainingRecordRepository;
    }
    
    // --- ★修正: 互換性のためのラッパーメソッド ---
    // AuthControllerがこのシグネチャ(引数3つ)で呼び出しているため追加
    @Transactional
    public void registerUser(String username, String password, String email) {
        // 電話番号はnullとして処理
        registerNewUser(username, email, null, password);
    }
    
    
    

    // --- ユーザー登録処理 (既存) ---
    @Transactional
    public void registerNewUser(String username, String email, String phoneNumber, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("そのユーザー名は既に使用されています。");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("そのメールアドレスは既に使用されています。");
        }
        if (phoneNumber != null && !phoneNumber.isEmpty() && userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalArgumentException("その電話番号は既に使用されています。");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPhoneNumber(phoneNumber);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setLevel(1);
        newUser.setExperiencePoints(0);
        newUser.setTheme("default");

        userRepository.save(newUser);
        sendRegistrationEmail(newUser.getEmail(), newUser.getUsername());
    }

    // --- パスワードリセット関連 ---
    @Transactional
    public boolean processForgotPasswordBySms(String phoneNumber) {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String code = String.format("%06d", new Random().nextInt(999999));
            user.setResetPasswordToken(code);
            user.setTokenExpiration(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);
            
            // ★重要: ここで SmsService の sendVerificationCode を呼んでいます
            smsService.sendVerificationCode(user.getPhoneNumber(), code);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public boolean processForgotPassword(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String token = UUID.randomUUID().toString();
            user.setResetPasswordToken(token);
            user.setTokenExpiration(LocalDateTime.now().plusHours(24));
            userRepository.save(user);
            String resetLink = baseUrl + "/reset-password?token=" + token;
            sendResetEmail(user.getEmail(), resetLink);
            return true;
        }
        return false;
    }

    public User getByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token)
                .filter(u -> u.getTokenExpiration() != null && u.getTokenExpiration().isAfter(LocalDateTime.now()))
                .orElse(null);
    }

    @Transactional
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setTokenExpiration(null);
        userRepository.save(user);
    }

    // --- フレンド機能 ---
    public List<User> searchUsers(String currentUsername, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // ★ここでのエラーはUserRepositoryの修正で直ります
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(keyword);
        return users.stream()
                .filter(u -> !u.getUsername().equals(currentUsername))
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean sendFriendRequest(String currentUsername, Long targetUserId) {
        Optional<User> senderOpt = userRepository.findByUsername(currentUsername);
        Optional<User> receiverOpt = userRepository.findById(targetUserId);

        if (senderOpt.isPresent() && receiverOpt.isPresent()) {
            User sender = senderOpt.get();
            User receiver = receiverOpt.get();
            if (sender.getId().equals(receiver.getId())) return false;
            if (sender.getFriends().contains(receiver)) return false;
            if (receiver.getReceivedFriendRequests().contains(sender)) return false;

            receiver.addReceivedFriendRequest(sender);
            userRepository.save(receiver);
            return true;
        }
        return false;
    }

    @Transactional
    public void approveFriendRequest(String currentUsername, Long senderId) {
        Optional<User> receiverOpt = userRepository.findByUsername(currentUsername);
        Optional<User> senderOpt = userRepository.findById(senderId);

        if (receiverOpt.isPresent() && senderOpt.isPresent()) {
            User receiver = receiverOpt.get();
            User sender = senderOpt.get();

            if (receiver.getReceivedFriendRequests().contains(sender)) {
                receiver.removeReceivedFriendRequest(sender);
                receiver.addFriend(sender);
                sender.addFriend(receiver);
                userRepository.save(receiver);
                userRepository.save(sender);
            }
        }
    }

    @Transactional
    public void rejectFriendRequest(String currentUsername, Long senderId) {
        Optional<User> receiverOpt = userRepository.findByUsername(currentUsername);
        Optional<User> senderOpt = userRepository.findById(senderId);

        if (receiverOpt.isPresent() && senderOpt.isPresent()) {
            User receiver = receiverOpt.get();
            User sender = senderOpt.get();
            receiver.removeReceivedFriendRequest(sender);
            userRepository.save(receiver);
        }
    }

    public List<User> getFriendRanking(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return new ArrayList<>();
        
        User currentUser = userOpt.get();
        Set<User> friends = currentUser.getFriends();
        
        List<User> rankingList = new ArrayList<>(friends);
        rankingList.add(currentUser);
        rankingList = rankingList.stream().distinct().collect(Collectors.toList());

        rankingList.sort(Comparator.comparingInt(User::getLevel).reversed()
                .thenComparing(Comparator.comparingInt(User::getXp).reversed()));
        
        return rankingList;
    }

    // --- その他ヘルパー ---
    private void sendRegistrationEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@fitworks.com");
            message.setTo(toEmail);
            message.setSubject("【FitWorks】登録完了のお知らせ");
            message.setText(username + "様、登録ありがとうございます。");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("メール送信失敗: " + e.getMessage());
        }
    }

    private void sendResetEmail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@fitworks.com");
            message.setTo(toEmail);
            message.setSubject("【FitWorks】パスワード再設定");
            message.setText("リンクをクリックして再設定してください:\n" + resetLink);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("メール送信失敗: " + e.getMessage());
        }
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public void save(User user) {
        userRepository.save(user);
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) return false;
        User user = optionalUser.get();
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
    public void addExperience(String username, int xp) {
         userRepository.findByUsername(username).ifPresent(user -> {
             user.addXp(xp); 
             userRepository.save(user); 
         });
    }

    public MissionStatusDto getDailyMissionStatus(User user) {
        final int MISSION_REWARD_XP = 300; 
        final String MISSION_TEXT = "筋トレ記録を1回投稿する";
        LocalDate today = LocalDate.now();
        
        if (user.getIsRewardClaimedToday() != null && user.getIsRewardClaimedToday() && 
            (user.getLastMissionCompletionDate() == null || !today.equals(user.getLastMissionCompletionDate()))) {
             user.setIsRewardClaimedToday(false);
             userRepository.save(user);
        }
        boolean isCompleted = (user.getLastMissionCompletionDate() != null && today.equals(user.getLastMissionCompletionDate()));
        return new MissionStatusDto(
            isCompleted,
            user.getIsRewardClaimedToday() != null ? user.getIsRewardClaimedToday() : false, 
            MISSION_REWARD_XP,
            MISSION_TEXT
        );
    }
    
    @Transactional
    public boolean claimMissionReward(User user) {
        MissionStatusDto status = getDailyMissionStatus(user);
        LocalDate today = LocalDate.now();
        if (status.isMissionCompleted() && !status.isRewardClaimed()) {
            user.addXp(status.getRewardXp());
            user.setLastMissionCompletionDate(today);
            user.setIsRewardClaimedToday(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public void incrementTrainingMissionProgress(User user) {
        LocalDate today = LocalDate.now();
        long todayRecordsCount = trainingRecordRepository.countByUser_IdAndRecordDate(user.getId(), today);
        if (todayRecordsCount > 0) {
            user.setLastMissionCompletionDate(today);
            if (user.getIsRewardClaimedToday() == null) {
                user.setIsRewardClaimedToday(false);
            }
            userRepository.save(user);
        }
    }

    @Transactional
    public void markMissionCompletedByPost(User user) {
        LocalDate today = LocalDate.now();
        user.setLastMissionCompletionDate(today);
        user.setIsRewardClaimedToday(false); 
        userRepository.save(user);
    }
    public void addExp(User user, int xp) {
        user.addXp(xp);
        userRepository.save(user);
    }
    // UserService.java に追加するメソッド

    /**
     * ログイン中のユーザーオブジェクトを取得する。
     * * 【重要】
     * 実際の本番環境では、Spring Securityなどを使用して、
     * 現在認証されているユーザーのIDやユーザー名を取得するロジックを
     * ここに実装する必要があります。
     * * @return ログイン中のUserオブジェクト。認証されていない場合はnullを返す。
     */
    public User getLoggedInUser() {
        // --- ★ここにSpring Security連携ロジックを実装します★ ---
        
        // 1. Spring Securityのコンテキストから認証情報を取得
        //    例: String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. 認証情報（ユーザー名など）を使って、UserRepositoryからUserエンティティを検索

        // --- デバッグ/仮実装として、ここでは一時的に固定のユーザー名で検索します ---
        // 実際の認証システムに接続後、この行は削除または修正してください。
        String loggedInUsername = "test"; // 仮のユーザー名

        // findByUsername メソッドはすでに存在するのでそれを利用します。
        return userRepository.findByUsername(loggedInUsername).orElse(null);
        // ----------------------------------------------------------------------
    }
    
    // --- チップ関連処理 ---
    @Transactional
    public void addChips(String username, int chips) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.addChips(chips); // Userエンティティのメソッドで加算
            userRepository.save(user); // DBに保存
        });
    }

    @Transactional
    public boolean useChips(String username, int cost) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) return false;
        User user = optionalUser.get();
        boolean success = user.useChips(cost); // 消費処理
        if (success) {
            userRepository.save(user); // 消費後の残高を保存
        }
        return success;
    }

    public int getChipCount(String username) {
        return userRepository.findByUsername(username)
            .map(User::getChipCount)
            .orElse(0);
    }
    public int getUserLevel(String username) {
        return userRepository.findByUsername(username)
                .map(User::getLevel)
                .orElse(1); // ユーザーが見つからない場合はLv.1
    }
    public int getUserMaterialCount(String username, String materialType) {
        return userItemRepository.findByUser_UsernameAndItem_Type(username, materialType)
                .map(UserItem::getCount)
                .orElse(0);
    }
 // ===== 新規追加: 素材を消費する処理 =====
    @Transactional
    public boolean consumeUserMaterial(String username, String materialType, int cost) {
        Optional<UserItem> optItem = userItemRepository.findByUser_UsernameAndItem_Type(username, materialType);
        if (optItem.isEmpty()) {
            return false; // 素材が存在しない
        }
        UserItem item = optItem.get();
        if (item.getCount() < cost) {
            return false; // 足りない
        }
        item.setCount(item.getCount() - cost); // 減算
        userItemRepository.save(item);
        return true;
    }

    // ===== 新規追加: キャラを解放済みにする処理 =====
    @Transactional
    public void unlockCharacterForUser(String username, Long characterId) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.addUnlockedCharacter(characterId); // Userエンティティにこのメソッドを用意
            userRepository.save(user);
        });
    }
    public int getUserMaterialCount(String username, Long item_id) {
        System.out.println("DEBUG: getUserMaterialCount called - username=" + username + ", item_id=" + item_id);
        
        List<UserItem> items = userItemRepository.findAllByUser_UsernameAndItemId(username, item_id);
        
        System.out.println("DEBUG: Found " + items.size() + " items for username=" + username + ", item_id=" + item_id);
        
        return items.size();
    }
    public BackgroundUnlockDto checkNewBackgroundUnlocks(String username) {
        BackgroundUnlockDto dto = new BackgroundUnlockDto();
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return dto;
        }
        
        int currentLevel = user.getLevel();
        int lastCheckedLevel = user.getLastBackgroundCheckLevel();
        
     // 背景解放レベルの定義
        // ※ Backgrounds.htmlのdata-unlock-levelと一致させること
        int[][] backgroundLevels = {
            {40, 0},  // 水の世界: level 40
            {70, 1},  // 木の世界: level 70
            {100, 2}, // 光の世界: level 100
            {130, 3}  // 闇の世界: level 130
        };
        
        String[] backgroundIds = {"water", "grass", "light", "dark"};
        String[] backgroundNames = {"水の世界", "木の世界", "光の世界", "闇の世界"};
        
        // 最後にチェックしたレベルから現在のレベルまでの間に解放された背景を検出
        for (int i = 0; i < backgroundLevels.length; i++) {
            int requiredLevel = backgroundLevels[i][0];
            int index = backgroundLevels[i][1];
            
            if (requiredLevel > lastCheckedLevel && requiredLevel <= currentLevel) {
                // 新しく解放された背景
                dto.addUnlockedBackground(
                    backgroundIds[index], 
                    backgroundNames[index], 
                    requiredLevel
                );
                
                System.out.println("==========================================");
                System.out.println("DEBUG: 背景解放検出");
                System.out.println("==========================================");
                System.out.println("背景名: " + backgroundNames[index]);
                System.out.println("必要レベル: " + requiredLevel);
                System.out.println("現在レベル: " + currentLevel);
                System.out.println("==========================================");
            }
        }
        
        // チェック済みレベルを更新
        user.setLastBackgroundCheckLevel(currentLevel);
        userRepository.save(user);
        
        return dto;
    }
 // ===== 新規追加: 素材を消費する処理 =====
    @Transactional
    public boolean consumeUserMaterial1(String username, String materialType, int cost) {
        Optional<UserItem> optItem = userItemRepository.findByUser_UsernameAndItem_Type(username, materialType);
        if (optItem.isEmpty()) {
            return false; // 素材が存在しない
        }
        UserItem item = optItem.get();
        if (item.getCount() < cost) {
            return false; // 足りない
        }
        item.setCount(item.getCount() - cost); // 減算
        userItemRepository.save(item);
        return true;
    }

    // ===== 新規追加: キャラを解放済みにする処理 =====
    @Transactional
    public void unlockCharacterForUser1(String username, Long characterId) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.addUnlockedCharacter(characterId); // ★Userエンティティにこのメソッドを用意
            userRepository.save(user);
        });
    }
    @Transactional
    public boolean consumeUserMaterialByItemId(String username, Long itemId, int cost) {
        // ユーザーが持っている該当アイテムのレコードを全て取得
        List<UserItem> items = userItemRepository.findAllByUser_UsernameAndItemId(username, itemId);
        
        int totalCount = items.size(); // 1レコード=1個なのでサイズが所持数
        
        System.out.println("DEBUG: consumeUserMaterialByItemId - username=" + username + ", itemId=" + itemId + ", totalCount=" + totalCount + ", cost=" + cost);
        
        if (totalCount < cost) {
            System.out.println("DEBUG: 素材不足 - 必要: " + cost + ", 所持: " + totalCount);
            return false; // 足りない
        }
        
        // 必要数だけレコードを削除
        for (int i = 0; i < cost && i < items.size(); i++) {
            userItemRepository.delete(items.get(i));
            System.out.println("DEBUG: 素材削除 - id=" + items.get(i).getId());
        }
        
        System.out.println("DEBUG: 素材消費成功 - " + cost + "個削除しました");
        return true;
    }
    @Transactional(readOnly = true)
    public List<Long> getUnlockedCharacterIds(String username) {
        return userRepository.findByUsername(username)
            .map(user -> {
                // EAGERフェッチなので即座に取得可能
                // または、トランザクション内でLazyロードも可能
                if (user.getUnlockedCharacters() == null) {
                    System.out.println("DEBUG: unlockedCharacters is null for user: " + username);
                    return new ArrayList<Long>();
                }
                System.out.println("DEBUG: getUnlockedCharacterIds - user: " + username + ", count: " + user.getUnlockedCharacters().size());
                return new ArrayList<>(user.getUnlockedCharacters());
            })
            .orElseGet(() -> {
                System.out.println("DEBUG: User not found: " + username);
                return new ArrayList<>();
            });
    }
    /**
     * ユーザー名を更新する（クールダウン期間チェック付き）
     */
    @Transactional
    public void updateUsername(String currentUsername, String newUsername) {
        // 1. 新しい名前が既に使われていないかチェック
        if (userRepository.findByUsername(newUsername).isPresent()) {
            throw new IllegalArgumentException("そのユーザー名は既に使用されています。");
        }

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. ★クールダウン期間のチェック
        if (user.getLastUsernameChangeDate() != null) {
            long daysSinceLastChange = ChronoUnit.DAYS.between(user.getLastUsernameChangeDate(), LocalDateTime.now());
            
            if (daysSinceLastChange < USERNAME_CHANGE_COOLDOWN_DAYS) {
                long daysRemaining = USERNAME_CHANGE_COOLDOWN_DAYS - daysSinceLastChange;
                throw new IllegalArgumentException(
                    "ユーザー名は一度変更すると " + USERNAME_CHANGE_COOLDOWN_DAYS + " 日間変更できません。" +
                    "あと " + daysRemaining + " 日お待ちください。"
                );
            }
        }

        // 3. 更新処理
        user.setUsername(newUsername);
        user.setLastUsernameChangeDate(LocalDateTime.now()); // ★現在時刻を記録
        userRepository.save(user);
    }

}