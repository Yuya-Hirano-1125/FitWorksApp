package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_characters")
@Data
@NoArgsConstructor // デフォルトコンストラクタ(JPA用)
public class UserCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ユーザー情報との紐づけ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ★重要: ここが1つだけであることを確認してください
    // 以前のコードにあった "private Long character_id;" は削除されています。
    @Column(name = "character_id", nullable = false)
    private Long characterId;

    // 取得日時
    @Column(name = "obtained_at", nullable = false, updatable = false)
    private LocalDateTime obtainedAt;

    @PrePersist
    protected void onCreate() {
        this.obtainedAt = LocalDateTime.now();
    }
    
    // Serviceで使用するコンストラクタ
    public UserCharacter(User user, Long characterId) {
        this.user = user;
        this.characterId = characterId;
    }
}