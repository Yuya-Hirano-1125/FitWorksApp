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

@Entity
@Table(name = "user_items")
public class UserItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ユーザー（userテーブルと紐付け）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // アイテム（itemsテーブルと紐付け）
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // 所持数
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    // 取得日時
    @Column(name = "acquired_at", nullable = false, updatable = false)
    private LocalDateTime acquiredAt;

    // レコード作成時に自動で現在時刻をセット
    @PrePersist
    protected void onCreate() {
        this.acquiredAt = LocalDateTime.now();
    }

    // --- Getter / Setter ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    // ★ UserService から呼べるように count を quantity にマッピング
    public int getCount() {
        return quantity != null ? quantity : 0;
    }

    public void setCount(int count) {
        this.quantity = count;
    }

    public LocalDateTime getAcquiredAt() {
        return acquiredAt;
    }

    public void setAcquiredAt(LocalDateTime acquiredAt) {
        this.acquiredAt = acquiredAt;
    }
}
