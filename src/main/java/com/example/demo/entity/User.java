package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int xp = 0;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL;

    private String providerId;

    private Integer level = 123;

    private LocalDate lastMissionCompletionDate;
    private Boolean isRewardClaimedToday = false;

    // ★追加: 現在装備している称号
    @Enumerated(EnumType.STRING)
    private AppTitle equippedTitle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipped_background_item_id")
    private Item equippedBackgroundItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipped_costume_item_id")
    private Item equippedCostumeItem;

    // --- 通知設定 ---
    private Boolean notificationTrainingReminder = true;
    private Boolean notificationAiSuggestion = true;
    private Boolean notificationProgressReport = false;
    
    // 生活リズムに合わせた通知時間 (デフォルトは12:00)
    private LocalTime lifestyleReminderTime = LocalTime.of(12, 0);

    private String theme = "default";

    private String resetPasswordToken;
    private LocalDateTime tokenExpiration;
    
    // --- フレンド機能 ---
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_friends",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "friend_requests",
        joinColumns = @JoinColumn(name = "receiver_id"),
        inverseJoinColumns = @JoinColumn(name = "sender_id")
    )
    private Set<User> receivedFriendRequests = new HashSet<>();

 // --- チップ機能 ---
    @Column(name = "chip")
    private Integer chipCount;

    public User() {
        if (this.level == null) this.level = 1;
        if (this.isRewardClaimedToday == null) this.isRewardClaimedToday = false;
        if (this.notificationTrainingReminder == null) this.notificationTrainingReminder = true;
        if (this.notificationAiSuggestion == null) this.notificationAiSuggestion = true;
        if (this.notificationProgressReport == null) this.notificationProgressReport = false;
        if (this.theme == null) this.theme = "default";
        if (this.lifestyleReminderTime == null) this.lifestyleReminderTime = LocalTime.of(12, 0);
        if (this.provider == null) this.provider = AuthProvider.LOCAL;
    }

    // --- Getter / Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public AuthProvider getProvider() { return provider; }
    public void setProvider(AuthProvider provider) { this.provider = provider; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public Integer getLevel() { return level != null ? level : 1; }
    public void setLevel(Integer level) { this.level = level; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public LocalDate getLastMissionCompletionDate() { return lastMissionCompletionDate; }
    public void setLastMissionCompletionDate(LocalDate lastMissionCompletionDate) { this.lastMissionCompletionDate = lastMissionCompletionDate; }

    public Boolean getIsRewardClaimedToday() { return isRewardClaimedToday; }
    public void setIsRewardClaimedToday(Boolean isRewardClaimedToday) { this.isRewardClaimedToday = isRewardClaimedToday; }

    public AppTitle getEquippedTitle() { return equippedTitle; }
    public void setEquippedTitle(AppTitle equippedTitle) { this.equippedTitle = equippedTitle; }

    public String getDisplayTitle() {
        return equippedTitle != null ? equippedTitle.getDisplayName() : "なし";
    }

    public Item getEquippedBackgroundItem() { return equippedBackgroundItem; }
    public void setEquippedBackgroundItem(Item equippedBackgroundItem) { this.equippedBackgroundItem = equippedBackgroundItem; }

    public Item getEquippedCostumeItem() { return equippedCostumeItem; }
    public void setEquippedCostumeItem(Item equippedCostumeItem) { this.equippedCostumeItem = equippedCostumeItem; }

    public Boolean isNotificationTrainingReminder() { return notificationTrainingReminder != null ? notificationTrainingReminder : true; }
    public void setNotificationTrainingReminder(Boolean notificationTrainingReminder) { this.notificationTrainingReminder = notificationTrainingReminder; }

    public Boolean isNotificationAiSuggestion() { return notificationAiSuggestion != null ? notificationAiSuggestion : true; }
    public void setNotificationAiSuggestion(Boolean notificationAiSuggestion) { this.notificationAiSuggestion = notificationAiSuggestion; }

    public Boolean isNotificationProgressReport() { return notificationProgressReport != null ? notificationProgressReport : false; }
    public void setNotificationProgressReport(Boolean notificationProgressReport) { this.notificationProgressReport = notificationProgressReport; }

    public LocalTime getLifestyleReminderTime() { return lifestyleReminderTime != null ? lifestyleReminderTime : LocalTime.of(12, 0); }
    public void setLifestyleReminderTime(LocalTime lifestyleReminderTime) { this.lifestyleReminderTime = lifestyleReminderTime; }

    public String getTheme() { return theme != null ? theme : "default"; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getResetPasswordToken() { return resetPasswordToken; }
    public void setResetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; }

    public LocalDateTime getTokenExpiration() { return tokenExpiration; }
    public void setTokenExpiration(LocalDateTime tokenExpiration) { this.tokenExpiration = tokenExpiration; }

    public Set<User> getFriends() { return friends; }
    public void setFriends(Set<User> friends) { this.friends = friends; }
    public void addFriend(User friend) { this.friends.add(friend); }
    public void removeFriend(User friend) { this.friends.remove(friend); }

    public Set<User> getReceivedFriendRequests() { return receivedFriendRequests; }
    public void setReceivedFriendRequests(Set<User> receivedFriendRequests) { this.receivedFriendRequests = receivedFriendRequests; }
    public void addReceivedFriendRequest(User sender) { this.receivedFriendRequests.add(sender); }
    public void removeReceivedFriendRequest(User sender) { this.receivedFriendRequests.remove(sender); }

    // --- XP関連 ---
    public int calculateRequiredXp() {
        int currentLevel = getLevel();
        return 1000 + (currentLevel - 1) * 200;
    }

    public void addXp(int earnedXp) {
        this.xp += earnedXp;
        while (true) {
            int requiredXp = calculateRequiredXp();
            if (this.xp >= requiredXp) {
                this.xp -= requiredXp;
                this.level++;
            } else {
                break;
            }
        }
    }

    
    public int getExperiencePoints() { return getXp(); }

    public int getProgressPercent() {
        int requiredXp = calculateRequiredXp();
        return requiredXp == 0 ? 0 : (int) (((double) xp / requiredXp) * 100);
    }

    public void setExperiencePoints(int i) { this.xp = i; }

 // --- チップ関連 ---
    public Integer getChipCount() { 
        return chipCount != null ? chipCount : 0; 
    }

    public void setChipCount(Integer chipCount) { 
        this.chipCount = chipCount; 
    }
 // チップを加算
    public void addChips(int chips) {
        if (chips > 0) {
            if (chipCount == null) chipCount = 0;
            chipCount += chips;
        }
    }

    // チップを消費
    public boolean useChips(int chips) {
        if (chips > 0 && chipCount != null && chipCount >= chips) {
            chipCount -= chips;
            return true;
        }
        return false;
    }
}