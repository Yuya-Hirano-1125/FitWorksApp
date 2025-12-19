package com.example.demo.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.entity.CharacterEntity;

@Service
public class CharacterService {

    /**
     * キャラクターに進化素材と進化条件を設定
     * data.sqlの定義順に基づき、素材のキーはアイテムID(String)を使用
     * 1~5:R, 6~10:SR, 11~15:SSR, 16:UR
     */
    public void applyEvolutionData(CharacterEntity chara) {
        if (chara == null || chara.getName() == null) {
            return;
        }

        switch (chara.getName()) {
            // ===== 炎属性 =====
            case "エンバーハート":
                chara.setEvolutionMaterials(Map.of());
                chara.setEvolutionConditions(Map.of("必要レベル", "1", "備考", "初期解放済み"));
                break;

            case "ドラコ":
                chara.setEvolutionMaterials(Map.of(
                    "1", 3, // 紅玉
                    "2", 3, // 蒼玉
                    "3", 3, // 翠玉
                    "4", 3, // 聖玉
                    "5", 3  // 闇玉
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "10", "素材ランク", "R素材×5種"));
                break;

            case "ドラコス":
                // ★修正箇所: 重複していたキー"5"を"10"(紫の聖結晶)に修正しました
                chara.setEvolutionMaterials(Map.of(
                    "1", 5, "2", 5, "3", 5, "4", 5, "5", 5,
                    "6", 3, "7", 3, "8", 3, "9", 3, "10", 3
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "20", "素材ランク", "R素材 + SR素材", "必要キャラ解放", "ドラコ"));
                break;

            case "ドラグノイド":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("1", 7), Map.entry("2", 7), Map.entry("3", 7), Map.entry("4", 7), Map.entry("5", 7),
                    Map.entry("6", 5), Map.entry("7", 5), Map.entry("8", 5), Map.entry("9", 5), Map.entry("10", 5),
                    Map.entry("11", 1) // 赫焔鱗
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "30", "素材ランク", "R素材 + SR素材 + SSR素材", "属性限定", "火属性のみ", "必要キャラ解放", "ドラコス"));
                break;

            // ===== 水属性 =====
            case "ルーナドロップ":
                chara.setEvolutionMaterials(Map.of("1", 3, "2", 3, "3", 3, "4", 3, "5", 3));
                chara.setEvolutionConditions(Map.of("必要レベル", "40", "素材ランク", "R素材×5種", "必要キャラ解放", "ドラグノイド"));
                break;

            case "ドリー":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("1", 5), Map.entry("2", 5), Map.entry("3", 5), Map.entry("4", 5), Map.entry("5", 5),
                    Map.entry("6", 3), Map.entry("7", 3), Map.entry("8", 3), Map.entry("9", 3), Map.entry("10", 3)
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "50", "素材ランク", "R素材 + SR素材", "必要キャラ解放", "ルーナドロップ"));
                break;

            case "ドルフィ":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("1", 7), Map.entry("2", 7), Map.entry("3", 7), Map.entry("4", 7), Map.entry("5", 7),
                    Map.entry("6", 5), Map.entry("7", 5), Map.entry("8", 5), Map.entry("9", 5), Map.entry("10", 5),
                    Map.entry("12", 1) // 氷華の杖
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "60", "素材ランク", "R素材 + SR素材 + SSR素材", "属性限定", "水属性のみ", "必要キャラ解放", "ドリー"));
                break;

            case "ドルフィナス":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("1", 7), Map.entry("2", 7), Map.entry("3", 7), Map.entry("4", 7), Map.entry("5", 7),
                    Map.entry("6", 5), Map.entry("7", 5), Map.entry("8", 5), Map.entry("9", 5), Map.entry("10", 5),
                    Map.entry("12", 2) // 氷華の杖
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "70", "素材ランク", "R素材 + SR素材 + SSR素材", "属性限定", "水属性のみ", "必要キャラ解放", "ドルフィ"));
                break;

            // ===== 草属性 =====
            case "フォリアン":
                chara.setEvolutionMaterials(Map.of("1", 3, "2", 3, "3", 3, "4", 3, "5", 3));
                chara.setEvolutionConditions(Map.of("必要レベル", "80", "素材ランク", "R素材×5種", "必要キャラ解放", "ドルフィナス"));
                break;

            case "シル":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("1", 5), Map.entry("2", 5), Map.entry("3", 5), Map.entry("4", 5), Map.entry("5", 5),
                    Map.entry("6", 3), Map.entry("7", 3), Map.entry("8", 3), Map.entry("9", 3), Map.entry("10", 3)
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "90", "素材ランク", "R素材 + SR素材", "必要キャラ解放", "フォリアン"));
                break;

            case "シルファ":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("1", 7), Map.entry("2", 7), Map.entry("3", 7), Map.entry("4", 7), Map.entry("5", 7),
                    Map.entry("6", 5), Map.entry("7", 5), Map.entry("8", 5), Map.entry("9", 5), Map.entry("10", 5),
                    Map.entry("13", 1) // 緑晶灯
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "100", "素材ランク", "R素材 + SR素材 + SSR素材", "属性限定", "草属性のみ", "必要キャラ解放", "シル"));
                break;

            case "シルフィナ":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("1", 7), Map.entry("2", 7), Map.entry("3", 7), Map.entry("4", 7), Map.entry("5", 7),
                    Map.entry("6", 5), Map.entry("7", 5), Map.entry("8", 5), Map.entry("9", 5), Map.entry("10", 5),
                    Map.entry("13", 2) // 緑晶灯
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "110", "素材ランク", "R素材 + SR素材 + SSR素材", "属性限定", "草属性のみ", "必要キャラ解放", "シルファ"));
                break;

            // ===== 光属性 =====
            case "ハローネスト":
                chara.setEvolutionMaterials(Map.of("1", 3, "2", 3, "3", 3, "4", 3, "5", 3));
                chara.setEvolutionConditions(Map.of("必要レベル", "120", "素材ランク", "R素材×5種", "必要キャラ解放", "シルフィナ"));
                break;

            case "メリー":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("1", 5), Map.entry("2", 5), Map.entry("3", 5), Map.entry("4", 5), Map.entry("5", 5),
                    Map.entry("6", 3), Map.entry("7", 3), Map.entry("8", 3), Map.entry("9", 3), Map.entry("10", 3)
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "130", "素材ランク", "R素材 + SR素材", "必要キャラ解放", "ハローネスト"));
                break;

            case "メリル":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("1", 7), Map.entry("2", 7), Map.entry("3", 7), Map.entry("4", 7), Map.entry("5", 7),
                    Map.entry("6", 5), Map.entry("7", 5), Map.entry("8", 5), Map.entry("9", 5), Map.entry("10", 5),
                    Map.entry("14", 1) // 夢紡ぎの枕
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "140", "素材ランク", "R素材 + SR素材 + SSR素材", "属性限定", "光属性のみ", "必要キャラ解放", "メリー"));
                break;

            case "メリノア":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("1", 7), Map.entry("2", 7), Map.entry("3", 7), Map.entry("4", 7), Map.entry("5", 7),
                    Map.entry("6", 5), Map.entry("7", 5), Map.entry("8", 5), Map.entry("9", 5), Map.entry("10", 5),
                    Map.entry("14", 2) // 夢紡ぎの枕
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "150", "素材ランク", "R素材 + SR素材 + SSR素材", "属性限定", "光属性のみ", "必要キャラ解放", "メリル"));
                break;

            // ===== 闇属性 =====
            case "ネビュリス":
                chara.setEvolutionMaterials(Map.of("1", 3, "2", 3, "3", 3, "4", 3, "5", 3));
                chara.setEvolutionConditions(Map.of("必要レベル", "160", "素材ランク", "R素材×5種", "必要キャラ解放", "メリノア"));
                break;

            case "ロービ":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("1", 5), Map.entry("2", 5), Map.entry("3", 5), Map.entry("4", 5), Map.entry("5", 5),
                    Map.entry("6", 3), Map.entry("7", 3), Map.entry("8", 3), Map.entry("9", 3), Map.entry("10", 3)
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "170", "素材ランク", "R素材 + SR素材", "必要キャラ解放", "ネビュリス"));
                break;

            case "ローバス":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("1", 7), Map.entry("2", 7), Map.entry("3", 7), Map.entry("4", 7), Map.entry("5", 7),
                    Map.entry("6", 5), Map.entry("7", 5), Map.entry("8", 5), Map.entry("9", 5), Map.entry("10", 5),
                    Map.entry("15", 1) // 月詠みの杖
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "180", "素材ランク", "R素材 + SR素材 + SSR素材", "属性限定", "闇属性のみ", "必要キャラ解放", "ロービ"));
                break;

            case "ロービアス":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("1", 7), Map.entry("2", 7), Map.entry("3", 7), Map.entry("4", 7), Map.entry("5", 7),
                    Map.entry("6", 5), Map.entry("7", 5), Map.entry("8", 5), Map.entry("9", 5), Map.entry("10", 5),
                    Map.entry("15", 2) // 月詠みの杖
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "190", "素材ランク", "R素材 + SR素材 + SSR素材", "属性限定", "闇属性のみ", "必要キャラ解放", "ローバス"));
                break;

            // ===== シークレット属性 =====
            case "シークレット":
                chara.setEvolutionMaterials(Map.ofEntries(
                    Map.entry("1", 10), Map.entry("2", 10), Map.entry("3", 10), Map.entry("4", 10), Map.entry("5", 10),
                    Map.entry("6", 5), Map.entry("7", 5), Map.entry("8", 5), Map.entry("9", 5), Map.entry("10", 5)
                ));
                chara.setEvolutionConditions(Map.of("必要レベル", "250", "素材ランク", "全ランク素材", "特殊条件", "全属性の最終進化を完了"));
                break;

            default:
                chara.setEvolutionMaterials(Map.of());
                chara.setEvolutionConditions(Map.of());
                break;
        }
    }
}