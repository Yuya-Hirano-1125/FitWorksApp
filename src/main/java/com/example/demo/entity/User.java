package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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

import lombok.Data;

@Entity
@Table(name = "users")
@Data // ★Lombok: Getter, Setter, toString, equals, hashCodeを自動生成
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int xp = 0;
    // ★追加: 最後にユーザー名を変更した日時
    private LocalDateTime lastUsernameChangeDate;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phoneNumber;
    
    // ★追加: 生年月日
    private LocalDate birthDate;
    
    // ★追加: 選択中のキャラクターID (nullの場合はレベル連動などのデフォルト挙動)
    @Column(name = "selected_character_id")
    private Long selectedCharacterId;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL;

    private String providerId;

    private Integer level;

    private LocalDate lastMissionCompletionDate;
    private Boolean isRewardClaimedToday = false;

    @Enumerated(EnumType.STRING)
    private AppTitle equippedTitle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipped_background_item_id")
    private Item equippedBackgroundItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipped_costume_item_id")
    private Item equippedCostumeItem;

    private Boolean notificationTrainingReminder = true;
    private Boolean notificationAiSuggestion = true;
    private Boolean notificationProgressReport = false;
    
    private LocalTime lifestyleReminderTime = LocalTime.of(12, 0);

    private String theme = "default";

    private String resetPasswordToken;
    private LocalDateTime tokenExpiration;
    
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

    @Column(name = "chip")
    private Integer chipCount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_unlocked_characters", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "character_id")
    private Set<Long> unlockedCharacters = new HashSet<>();

    // ★★★ 解放済み背景管理 ★★★
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_unlocked_backgrounds", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "background_id")
    private Set<String> unlockedBackgrounds = new HashSet<>();

    // ★★★ 選択中の背景 ★★★
    @Column(name = "selected_background")
    private String selectedBackground;

    // ★★★ 背景解放チェック済みレベル ★★★
    @Column(name = "last_background_check_level")
    private Integer lastBackgroundCheckLevel = 1;

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

    // --- ロジックを含むメソッド（Lombokで代替できないため維持） ---

    public String getDisplayTitle() {
        return equippedTitle != null ? equippedTitle.getDisplayName() : "なし";
    }

    // Nullチェック付きGetter群（DBからの読込時にNullの場合のデフォルト値を保証するため維持）
    public Integer getLevel() { return level != null ? level : 1; }
    
    public Boolean isNotificationTrainingReminder() { return notificationTrainingReminder != null ? notificationTrainingReminder : true; }
    public Boolean isNotificationAiSuggestion() { return notificationAiSuggestion != null ? notificationAiSuggestion : true; }
    public Boolean isNotificationProgressReport() { return notificationProgressReport != null ? notificationProgressReport : false; }
    
    public LocalTime getLifestyleReminderTime() { return lifestyleReminderTime != null ? lifestyleReminderTime : LocalTime.of(12, 0); }
    
    public String getTheme() { return theme != null ? theme : "default"; }

    // コレクション操作ヘルパー
    public void addFriend(User friend) { this.friends.add(friend); }
    public void removeFriend(User friend) { this.friends.remove(friend); }

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

    // エイリアスメソッド（維持）
    public int getExperiencePoints() { return getXp(); }
    public void setExperiencePoints(int xp) { this.xp = xp; }

    public int getProgressPercent() {
        int requiredXp = calculateRequiredXp();
        return requiredXp == 0 ? 0 : (int) (((double) xp / requiredXp) * 100);
    }

    // --- チップ関連 ---
    // Nullチェックが含まれるため維持
    public Integer getChipCount() { return chipCount != null ? chipCount : 0; }
    
    public void addChips(int chips) {
        if (chips > 0) {
            if (chipCount == null) chipCount = 0;
            chipCount += chips;
        }
    }

    public boolean useChips(int chips) {
        if (chips > 0 && chipCount != null && chipCount >= chips) {
            chipCount -= chips;
            return true;
        }
        return false;
    }
    
    // --- 解放済みキャラクター管理 ---
    public void addUnlockedCharacter(Long characterId) {
        if (unlockedCharacters == null) unlockedCharacters = new HashSet<>();
        unlockedCharacters.add(characterId);
    }
    public boolean hasUnlockedCharacter(Long characterId) {
        return unlockedCharacters != null && unlockedCharacters.contains(characterId);
    }
    
    // Nullチェック付きGetter/Setter（維持）
    public Set<Long> getUnlockedCharacters() {
        if (unlockedCharacters == null) unlockedCharacters = new HashSet<>();
        return unlockedCharacters;
    }
    public void setUnlockedCharacters(Set<Long> unlockedCharacters) {
        this.unlockedCharacters = unlockedCharacters != null ? unlockedCharacters : new HashSet<>();
    }

    // ★★★ 解放済み背景管理メソッド ★★★
    public Set<String> getUnlockedBackgrounds() {
        if (unlockedBackgrounds == null) unlockedBackgrounds = new HashSet<>();
        return unlockedBackgrounds;
    }
    // ロジック付きSetter（維持）
    public void setUnlockedBackgrounds(Set<String> unlockedBackgrounds) {
        this.unlockedBackgrounds = (unlockedBackgrounds != null) ? unlockedBackgrounds : new HashSet<>();
    }
    public void addUnlockedBackground(String backgroundId) {
        if (unlockedBackgrounds == null) unlockedBackgrounds = new HashSet<>();
        if (backgroundId != null && !backgroundId.isBlank()) unlockedBackgrounds.add(backgroundId);
    }
    public boolean hasUnlockedBackground(String backgroundId) {
        return backgroundId != null && unlockedBackgrounds != null && unlockedBackgrounds.contains(backgroundId);
    }

    // ★★★ 背景解放チェック済みレベル ★★★
    // Nullチェック付きGetter（維持）
    public Integer getLastBackgroundCheckLevel() { return lastBackgroundCheckLevel != null ? lastBackgroundCheckLevel : 1; }
    // ロジック付きSetter（維持）
    public void setLastBackgroundCheckLevel(Integer lastBackgroundCheckLevel) { this.lastBackgroundCheckLevel = (lastBackgroundCheckLevel != null) ? lastBackgroundCheckLevel : 1; }
    
}