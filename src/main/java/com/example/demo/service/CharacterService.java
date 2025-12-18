package com.example.demo.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.entity.CharacterEntity;

@Service
public class CharacterService {

    /**
     * キャラクターに進化素材と進化条件を設定
     */
    public void applyEvolutionData(CharacterEntity chara) {
        if (chara == null || chara.getName() == null) {
            return;
        }

        switch (chara.getName()) {
            // ===== 炎属性 =====
            case "エンバーハート":
                // 初期キャラは素材不要
                chara.setEvolutionMaterials(Map.of());
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "1",
                    "備考", "初期解放済み"
                ));
                break;

            case "ドラコ":
                chara.setEvolutionMaterials(Map.of(
                    "紅玉", 3,
                    "蒼玉", 3,
                    "翠玉", 3,
                    "聖玉", 3,
                    "闇玉", 3
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "10",
                    "素材ランク", "R素材×5種"
                ));
                break;

            case "ドラコス":
                chara.setEvolutionMaterials(Map.of(
                    "紅玉", 5,
                    "蒼玉", 5,
                    "翠玉", 5,
                    "聖玉", 5,
                    "闇玉", 5,
                    "赤の聖結晶", 3,
                    "青の聖結晶", 3,
                    "緑の聖結晶", 3,
                    "黄の聖結晶", 3,
                    "闇の聖結晶", 3
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "20",
                    "素材ランク", "R素材 + SR素材",
                    "必要キャラ解放", "ドラコ"
                ));
                break;

            case "ドラグノイド":
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
                        Map.entry("紫の聖結晶", 5),
                        Map.entry("赫焔鱗", 1)
                    ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "30",
                    "素材ランク", "R素材 + SR素材 + SSR素材",
                    "属性限定", "火属性のみ",
                    "必要キャラ解放", "ドラコス"
                ));
                break;

            // ===== 水属性 =====
            case "ルーナドロップ":
                chara.setEvolutionMaterials(Map.of(
                    "紅玉", 3,
                    "蒼玉", 3,
                    "翠玉", 3,
                    "聖玉", 3,
                    "闇玉", 3
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "40",
                    "素材ランク", "R素材×5種",
                    "必要キャラ解放", "ドラグノイド"
                ));
                break;

            case "ドリー":
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
                    Map.entry("紫の聖結晶", 3)
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "50",
                    "素材ランク", "R素材 + SR素材",
                    "必要キャラ解放", "ルーナドロップ"
                ));
                break;

            case "ドルフィ":
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
                    Map.entry("紫の聖結晶", 5),
                    Map.entry("氷華の杖", 1)
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "60",
                    "素材ランク", "R素材 + SR素材 + SSR素材",
                    "属性限定", "水属性のみ",
                    "必要キャラ解放", "ドリー"
                ));
                break;

            case "ドルフィナス":
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
                    Map.entry("紫の聖結晶", 5),
                    Map.entry("氷華の杖", 2)
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "70",
                    "素材ランク", "R素材 + SR素材 + SSR素材",
                    "属性限定", "水属性のみ",
                    "必要キャラ解放", "ドルフィ"
                ));
                break;

            // ===== 草属性 =====
            case "フォリアン":
                chara.setEvolutionMaterials(Map.of(
                    "紅玉", 3,
                    "蒼玉", 3,
                    "翠玉", 3,
                    "聖玉", 3,
                    "闇玉", 3
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "80",
                    "素材ランク", "R素材×5種",
                    "必要キャラ解放", "ドルフィナス"
                ));
                break;

            case "シル":
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
                    Map.entry("紫の聖結晶", 3)
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "90",
                    "素材ランク", "R素材 + SR素材",
                    "必要キャラ解放", "フォリアン"
                ));
                break;

            case "シルファ":
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
                    Map.entry("紫の聖結晶", 5),
                    Map.entry("緑晶灯", 1)
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "100",
                    "素材ランク", "R素材 + SR素材 + SSR素材",
                    "属性限定", "草属性のみ",
                    "必要キャラ解放", "シル"
                ));
                break;

            case "シルフィナ":
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
                    Map.entry("紫の聖結晶", 5),
                    Map.entry("緑晶灯", 2)
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "110",
                    "素材ランク", "R素材 + SR素材 + SSR素材",
                    "属性限定", "草属性のみ",
                    "必要キャラ解放", "シルファ"
                ));
                break;

            // ===== 光属性 =====
            case "ハローネスト":
                chara.setEvolutionMaterials(Map.of(
                    "紅玉", 3,
                    "蒼玉", 3,
                    "翠玉", 3,
                    "聖玉", 3,
                    "闇玉", 3
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "120",
                    "素材ランク", "R素材×5種",
                    "必要キャラ解放", "シルフィナ"
                ));
                break;

            case "メリー":
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
                    Map.entry("紫の聖結晶", 3)
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "130",
                    "素材ランク", "R素材 + SR素材",
                    "必要キャラ解放", "ハローネスト"
                ));
                break;

            case "メリル":
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
                    Map.entry("紫の聖結晶", 5),
                    Map.entry("夢紡ぎの枕", 1)
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "140",
                    "素材ランク", "R素材 + SR素材 + SSR素材",
                    "属性限定", "光属性のみ",
                    "必要キャラ解放", "メリー"
                ));
                break;

            case "メリノア":
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
                    Map.entry("紫の聖結晶", 5),
                    Map.entry("夢紡ぎの枕", 2)
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "150",
                    "素材ランク", "R素材 + SR素材 + SSR素材",
                    "属性限定", "光属性のみ",
                    "必要キャラ解放", "メリル"
                ));
                break;

            // ===== 闇属性 =====
            case "ネビュリス":
                chara.setEvolutionMaterials(Map.of(
                    "紅玉", 3,
                    "蒼玉", 3,
                    "翠玉", 3,
                    "聖玉", 3,
                    "闇玉", 3
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "160",
                    "素材ランク", "R素材×5種",
                    "必要キャラ解放", "メリノア"
                ));
                break;

            case "ロービ":
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
                    Map.entry("紫の聖結晶", 3)
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "170",
                    "素材ランク", "R素材 + SR素材",
                    "必要キャラ解放", "ネビュリス"
                ));
                break;

            case "ローバス":
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
                    Map.entry("紫の聖結晶", 5),
                    Map.entry("月詠みの杖", 1)
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "180",
                    "素材ランク", "R素材 + SR素材 + SSR素材",
                    "属性限定", "闇属性のみ",
                    "必要キャラ解放", "ロービ"
                ));
                break;

            case "ロービアス":
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
                    Map.entry("紫の聖結晶", 5),
                    Map.entry("月詠みの杖", 2)
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "190",
                    "素材ランク", "R素材 + SR素材 + SSR素材",
                    "属性限定", "闇属性のみ",
                    "必要キャラ解放", "ローバス"
                ));
                break;

            // ===== シークレット属性 =====
            case "シークレット":
                chara.setEvolutionMaterials(Map.of(
                    "紅玉", 10,
                    "蒼玉", 10,
                    "翠玉", 10,
                    "聖玉", 10,
                    "闇玉", 10,
                    "赤の聖結晶", 5,
                    "青の聖結晶", 5,
                    "緑の聖結晶", 5,
                    "黄の聖結晶", 5,
                    "闇の聖結晶", 5
                ));
                chara.setEvolutionConditions(Map.of(
                    "必要レベル", "250",
                    "素材ランク", "全ランク素材",
                    "特殊条件", "全属性の最終進化を完了"
                ));
                break;

            default:
                // デフォルトは空の設定
                chara.setEvolutionMaterials(Map.of());
                chara.setEvolutionConditions(Map.of());
                break;
        }
    }
}