package com.example.demo.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.entity.CharacterEntity;

@Service
public class CharacterService {

    // キャラごとに進化素材・進化条件をセット
    public void applyEvolutionData(CharacterEntity chara) {
        switch (chara.getName()) {
            case "ドラコ":
                // R素材
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("紅玉", 3),
                    Map.entry("蒼玉", 3),
                    Map.entry("翠玉", 3),
                    Map.entry("聖玉", 3),
                    Map.entry("闇玉", 3)
                ));
                // 進化条件
                chara.setEvolutionConditions(Map.ofEntries(
                    Map.entry("必要レベル", "10"),
                    Map.entry("素材ランク", "R素材")
                ));
                break;

            case "ドラコス":
                // R + SR素材
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("紅玉", 5),
                    Map.entry("蒼玉", 5),
                    Map.entry("翠玉", 5),
                    Map.entry("聖玉", 5),
                    Map.entry("闇玉", 5),
                    Map.entry("赤の聖結晶", 3),
                    Map.entry("青の聖結晶", 3),
                    Map.entry("緑の聖結晶", 3),
                    Map.entry("黄の聖結晶", 3),
                    Map.entry("闇の聖結晶", 3)
                ));
                chara.setEvolutionConditions(Map.ofEntries(
                    Map.entry("必要レベル", "20"),
                    Map.entry("素材ランク", "R素材 + SR素材")
                ));
                break;

            case "ドラグノイド":
                // R + SR + SSR素材
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("紅玉", 7),
                    Map.entry("蒼玉", 7),
                    Map.entry("翠玉", 7),
                    Map.entry("聖玉", 7),
                    Map.entry("闇玉", 7),
                    Map.entry("赤の聖結晶", 5),
                    Map.entry("青の聖結晶", 5),
                    Map.entry("緑の聖結晶", 5),
                    Map.entry("黄の聖結晶", 5),
                    Map.entry("闇の聖結晶", 5),
                    Map.entry("赫焔鱗", 1) // 火属性限定
                ));
                chara.setEvolutionConditions(Map.ofEntries(
                    Map.entry("必要レベル", "30"),
                    Map.entry("素材ランク", "R素材 + SR素材 + SSR素材")
                ));
                break;

            default:
                chara.setEvolutionMaterials(Map.of());
                chara.setEvolutionConditions(Map.of());
        }
    }
}
