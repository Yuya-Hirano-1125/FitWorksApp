package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "characters") // SQL予約語回避のため複数形にします
@Data
@NoArgsConstructor
public class Character {

    @Id
    // data.sqlでIDを手動指定(0, 10, 20...)しているため、
    // @GeneratedValueはあえて付けません。
    private Long id;

    private String name;

    @Column(name = "image_path")
    private String imagePath;

    private String attribute; // fire, water, etc.

    private String rarity;    // ★1, ★2, etc.

    @Column(name = "required_level")
    private Integer requiredLevel;

    @Column(name = "unlock_cost")
    private Integer unlockCost;
}