package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.example.demo.repository.UserItemRepository;
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

    @Value("${app.base-url}")
    private String baseUrl;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TrainingRecordRepository trainingRecordRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.trainingRecordRepository = trainingRecordRepository;
    }
    
    @Transactional
    public void registerUser(String username, String password, String email) {
        registerNewUser(username, email, null, password);
    }

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

    public User getLoggedInUser() {
        // 必要に応じてSpring Securityの実装を追加
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
    

    // ★★★ 修正箇所: Item IDを指定して正確な所持数を取得 ★★★
    public int getUserMaterialCount(String username, Long itemId) {
        List<UserItem> items = userItemRepository.findAllByUser_UsernameAndItemId(username, itemId);
        
        int totalQuantity = 0;
        if (items != null) {
            for (UserItem item : items) {
                // UserItemエンティティの getCount() は quantity を返す前提
                totalQuantity += item.getCount();
            }
        }
        return totalQuantity;
    }

    @Transactional
    public boolean consumeUserMaterial(String username, String materialType, int cost) {
        // 名前検索版消費メソッド
        Optional<UserItem> optItem = userItemRepository.findByUser_UsernameAndItem_Type(username, materialType);
        if (optItem.isEmpty()) {
            return false;
        }
        UserItem item = optItem.get();
        if (item.getCount() < cost) {
            return false;
        }
        item.setCount(item.getCount() - cost);
        userItemRepository.save(item);
        return true;
    }

    // ★★★ 修正箇所: Item IDを指定して素材を消費 ★★★
    @Transactional
    public boolean consumeUserMaterialByItemId(String username, Long itemId, int cost) {
        List<UserItem> items = userItemRepository.findAllByUser_UsernameAndItemId(username, itemId);
        
        // 現在の総所持数を計算
        int currentTotal = 0;
        for (UserItem item : items) {
            currentTotal += item.getCount();
        }
        
        if (currentTotal < cost) {
            System.out.println("DEBUG: Not enough materials (ID: " + itemId + "). Owned: " + currentTotal + ", Cost: " + cost);
            return false;
        }
        
        // 消費処理: 複数のレコードがある場合は順番に減らす
        int remainingCost = cost;
        for (UserItem item : items) {
            if (remainingCost <= 0) break;
            
            int owned = item.getCount();
            if (owned > remainingCost) {
                // 十分にある場合、減算して終了
                item.setCount(owned - remainingCost);
                userItemRepository.save(item);
                remainingCost = 0;
            } else {
                // 全部使っても足りない、またはちょうどの場合は0にして次へ
                // ※0個になったレコードを削除するかは仕様によるが、ここでは0個として残す（再取得時にレコードを使い回すため）
                // 削除したい場合は userItemRepository.delete(item);
                item.setCount(0);
                userItemRepository.save(item);
                remainingCost -= owned;
            }
        }
        
        return true;
    }

    @Transactional
    public void unlockCharacterForUser(String username, Long characterId) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.addUnlockedCharacter(characterId);
            userRepository.save(user);
        });
    }

    public BackgroundUnlockDto checkNewBackgroundUnlocks(String username) {
        BackgroundUnlockDto dto = new BackgroundUnlockDto();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return dto;
        
        int currentLevel = user.getLevel();
        int lastCheckedLevel = user.getLastBackgroundCheckLevel();
        
        int[][] backgroundLevels = {
            {40, 0}, {70, 1}, {100, 2}, {130, 3}
        };
        String[] backgroundIds = {"water", "grass", "light", "dark"};
        String[] backgroundNames = {"水の世界", "木の世界", "光の世界", "闇の世界"};
        
        for (int i = 0; i < backgroundLevels.length; i++) {
            int requiredLevel = backgroundLevels[i][0];
            int index = backgroundLevels[i][1];
            if (requiredLevel > lastCheckedLevel && requiredLevel <= currentLevel) {
                dto.addUnlockedBackground(backgroundIds[index], backgroundNames[index], requiredLevel);
            }
        }
        user.setLastBackgroundCheckLevel(currentLevel);
        userRepository.save(user);
        return dto;
    }
    
    @Transactional(readOnly = true)
    public List<Long> getUnlockedCharacterIds(String username) {
        return userRepository.findByUsername(username)
            .map(user -> new ArrayList<>(user.getUnlockedCharacters()))
            .orElseGet(ArrayList::new);
    }
}