package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.CharacterEntity;

@Repository
public interface CharacterRepository extends JpaRepository<CharacterEntity, Long> {

    /**
     * 属性ごとにキャラクターを検索
     * 例: findByAttribute("fire") → 炎属性キャラ一覧
     */
    List<CharacterEntity> findByAttribute(String attribute);

    /**
     * レアリティで検索
     */
    List<CharacterEntity> findByRarity(String rarity);

    /**
     * 必要レベル以下のキャラを検索
     */
    List<CharacterEntity> findByRequiredLevelLessThanEqual(int level);
}
