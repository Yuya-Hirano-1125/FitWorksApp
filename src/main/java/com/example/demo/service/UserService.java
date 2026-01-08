package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.TrainingRecordRepository;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final TrainingRecordRepository trainingRecordRepository; 
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JavaMailSender mailSender;
    
    // 削除: UserItemRepository
    // @Autowired
    // private UserItemRepository userItemRepository;

    // 追加: アイテム検索用
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SmsService smsService;
    
    private static final int USERNAME_CHANGE_COOLDOWN_DAYS = 7;

    @Value("${app.base-url}")
    private String baseUrl;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TrainingRecordRepository trainingRecordRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.trainingRecordRepository = trainingRecordRepository;
    }
    
    // --- ユーザー登録関連 ---
    @Transactional
    public void registerUser(String username, String password, String email, LocalDate birthDate) {
        registerNewUser(username, email, password, birthDate);
    }
    
    @Transactional
    public void registerNewUser(String username, String email, String rawPassword, LocalDate birthDate) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("そのユーザー名は既に使用されています。");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("そのメールアドレスは既に使用されています。");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setBirthDate(birthDate); 
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setLevel(1);
        newUser.setExperiencePoints(0);
        newUser.setTheme("default");

        userRepository.save(newUser);
        sendRegistrationEmail(newUser.getEmail(), newUser.getUsername());
    }

    // --- パスワードリセット関連 (認証コード方式) ---
    @Transactional
    public boolean sendAuthCodeByEmail(String email, LocalDate birthDate) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getBirthDate() != null && user.getBirthDate().equals(birthDate)) {
                String code = String.format("%06d", new Random().nextInt(999999));
                user.setResetPasswordToken(code);
                user.setTokenExpiration(LocalDateTime.now().plusMinutes(10));
                userRepository.save(user);
                sendAuthCodeEmail(user.getEmail(), code);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public String verifyAuthCode(String email, String code) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (code != null && code.equals(user.getResetPasswordToken()) &&
                user.getTokenExpiration() != null && 
                user.getTokenExpiration().isAfter(LocalDateTime.now())) {
                
                String resetToken = UUID.randomUUID().toString();
                user.setResetPasswordToken(resetToken);
                user.setTokenExpiration(LocalDateTime.now().plusMinutes(30)); 
                userRepository.save(user);
                return resetToken; 
            }
        }
        return null; 
    }
    
    @Transactional
    public boolean updatePasswordByToken(String token, String newPassword) {
        Optional<User> optionalUser = userRepository.findByResetPasswordToken(token);
        
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getTokenExpiration() != null && user.getTokenExpiration().isAfter(LocalDateTime.now())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetPasswordToken(null);
                user.setTokenExpiration(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    // --- メール送信ヘルパーメソッド ---
    private void sendAuthCodeEmail(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@fitworks.com");
            message.setTo(toEmail);
            message.setSubject("【FitWorks】パスワード再設定認証コード");
            message.setText("パスワード再設定のための認証コードをお知らせします。\n\n" +
                            "認証コード: " + code + "\n\n" +
                            "このコードの有効期限は10分間です。");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("メール送信失敗: " + e.getMessage());
        }
    }
    
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

    // --- 既存のパスワードリセット（旧方式） ---
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

    @Transactional
    public boolean processForgotPasswordBySms(String phoneNumber) {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String code = String.format("%06d", new Random().nextInt(999999));
            user.setResetPasswordToken(code);
            user.setTokenExpiration(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);
            smsService.sendVerificationCode(user.getPhoneNumber(), code);
            return true;
        } else {
            return false;
        }
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

    // --- 基本CRUD ---
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

    // --- 設定変更関連 ---
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
    
    @Transactional
    public void updateUsername(String currentUsername, String newUsername) {
        if (userRepository.findByUsername(newUsername).isPresent()) {
            throw new IllegalArgumentException("そのユーザー名は既に使用されています。");
        }

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

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

        user.setUsername(newUsername);
        user.setLastUsernameChangeDate(LocalDateTime.now());
        userRepository.save(user);
    }
    
    public void updateEmail(Long userId, String currentPassword, String newEmail) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("ユーザーが見つかりません"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new Exception("現在のパスワードが正しくありません");
        }

        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new Exception("このメールアドレスは既に使用されています");
        }

        user.setEmail(newEmail);
        userRepository.save(user);
    }
    
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    // --- フレンド機能 ---
    public List<User> searchUsers(String currentUsername, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(keyword);
        return users.stream()
                .filter(u -> !u.getUsername().equals(currentUsername))
                .collect(Collectors.toList());
    }

    @Transactional
    public int sendFriendRequest(String currentUsername, Long targetUserId) {
        Optional<User> senderOpt = userRepository.findByUsername(currentUsername);
        Optional<User> receiverOpt = userRepository.findById(targetUserId);

        if (senderOpt.isPresent() && receiverOpt.isPresent()) {
            User sender = senderOpt.get();
            User receiver = receiverOpt.get();
            
            if (sender.getId().equals(receiver.getId())) return 0; 
            if (sender.getFriends().contains(receiver)) return 0; 
            
            if (sender.getReceivedFriendRequests().contains(receiver)) {
                sender.addFriend(receiver);
                receiver.addFriend(sender);
                
                sender.removeReceivedFriendRequest(receiver);
                receiver.removeReceivedFriendRequest(sender);
                
                userRepository.save(sender);
                userRepository.save(receiver);
                
                return 2; 
            }

            if (receiver.getReceivedFriendRequests().contains(sender)) return 0; 

            receiver.addReceivedFriendRequest(sender);
            userRepository.save(receiver);
            return 1; 
        }
        return 0; 
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

    @Transactional
    public void removeFriend(String currentUsername, Long friendId) {
        Optional<User> userOpt = userRepository.findByUsername(currentUsername);
        Optional<User> friendOpt = userRepository.findById(friendId);

        if (userOpt.isPresent() && friendOpt.isPresent()) {
            User user = userOpt.get();
            User friend = friendOpt.get();

            if (user.getFriends().contains(friend)) {
                user.getFriends().remove(friend);
                friend.getFriends().remove(user); 
                
                userRepository.save(user);
                userRepository.save(friend);
            }
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

    // --- XP・ミッション・その他 ---
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

    public User getLoggedInUser() {
        String loggedInUsername = "test"; 
        return userRepository.findByUsername(loggedInUsername).orElse(null);
    }
    
    @Transactional
    public void addChips(String username, int chips) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.addChips(chips);
            userRepository.save(user);
        });
    }

    @Transactional
    public boolean useChips(String username, int cost) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) return false;
        User user = optionalUser.get();
        boolean success = user.useChips(cost); 
        if (success) {
            userRepository.save(user); 
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
                .orElse(1); 
    }
    
    // ★修正: UserItemRepositoryを使わず、Userエンティティのinventoryから取得
    public int getUserMaterialCount(String username, String materialType) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getInventory() == null) {
            return 0;
        }
        // マップを走査して、指定されたtypeのアイテムの合計数を返す
        return user.getInventory().entrySet().stream()
                .filter(entry -> {
                    Item item = entry.getKey();
                    return item != null && materialType.equals(item.getType());
                })
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    // ★修正: UserItemRepositoryを使わず、UserエンティティのuseItemを使用
    @Transactional
    public boolean consumeUserMaterial(String username, String materialType, int cost) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;

        // 指定されたタイプのアイテムを探す
        Optional<Item> targetItem = user.getInventory().keySet().stream()
                .filter(item -> materialType.equals(item.getType()))
                // 複数ある場合は、とりあえず最初の1つを対象とする（必要ならロジック調整）
                // または、合計数チェック後に減算するロジックが必要だが、
                // 今回は「タイプ=ユニーク」または「どれか一つ消費」と仮定して簡易実装
                .findFirst();

        if (targetItem.isPresent()) {
            Item item = targetItem.get();
            // Userクラスのヘルパーメソッドを使用
            boolean success = user.useItem(item, cost);
            if (success) {
                userRepository.save(user); // 変更を保存
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void unlockCharacterForUser(String username, Long characterId) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.addUnlockedCharacter(characterId); 
            userRepository.save(user);
        });
    }
    
    // ★修正: UserItemRepositoryを使わず、Userエンティティから取得
    public int getUserMaterialCount(String username, Long itemId) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return 0;
        
        // アイテムIDで検索 (ItemRepositoryを使うか、マップを走査)
        return user.getInventory().entrySet().stream()
                .filter(entry -> entry.getKey().getId().equals(itemId))
                .mapToInt(Map.Entry::getValue)
                .findFirst()
                .orElse(0);
    }
    
    public BackgroundUnlockDto checkNewBackgroundUnlocks(String username) {
        BackgroundUnlockDto dto = new BackgroundUnlockDto();
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return dto;
        }
        
        int currentLevel = user.getLevel();
        int lastCheckedLevel = user.getLastBackgroundCheckLevel();
        
        int[][] backgroundLevels = {
            {40, 0}, 
            {70, 1}, 
            {100, 2}, 
            {130, 3} 
        };
        
        String[] backgroundIds = {"water", "grass", "light", "dark"};
        String[] backgroundNames = {"水の世界", "木の世界", "光の世界", "闇の世界"};
        
        for (int i = 0; i < backgroundLevels.length; i++) {
            int requiredLevel = backgroundLevels[i][0];
            int index = backgroundLevels[i][1];
            
            if (requiredLevel > lastCheckedLevel && requiredLevel <= currentLevel) {
                dto.addUnlockedBackground(
                    backgroundIds[index], 
                    backgroundNames[index], 
                    requiredLevel
                );
            }
        }
        
        user.setLastBackgroundCheckLevel(currentLevel);
        userRepository.save(user);
        
        return dto;
    }

    // ★修正: UserItemRepositoryを使わず、UserエンティティのuseItemを使用
    @Transactional
    public boolean consumeUserMaterialByItemId(String username, Long itemId, int cost) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;

        // 対象のItemをリポジトリから取得 (Itemエンティティが必要)
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) return false;

        // Userクラスのメソッドで消費
        boolean success = user.useItem(item, cost);
        if (success) {
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
    @Transactional(readOnly = true)
    public List<Long> getUnlockedCharacterIds(String username) {
        return userRepository.findByUsername(username)
            .map(user -> {
                if (user.getUnlockedCharacters() == null) {
                    return new ArrayList<Long>();
                }
                return new ArrayList<>(user.getUnlockedCharacters());
            })
            .orElseGet(ArrayList::new);
    }
}