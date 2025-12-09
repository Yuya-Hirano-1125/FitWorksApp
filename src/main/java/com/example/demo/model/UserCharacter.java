package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_characters")
public class UserCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;      // ユーザーID
    private Long characterId; // キャラクターID (1, 2, 3...)

    // コンストラクタ（空）
    public UserCharacter() {}

    // コンストラクタ（保存用）
    public UserCharacter(Long userId, Long characterId) {
        this.userId = userId;
        this.characterId = characterId;
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getCharacterId() { return characterId; }
}