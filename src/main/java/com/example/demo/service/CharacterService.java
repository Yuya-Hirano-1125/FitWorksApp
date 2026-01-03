package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.entity.CharacterEntity;

@Service
public class CharacterService {

    /**
     * キャラクターに進化素材と進化条件を設定するメソッド
     * フロントエンド(JS)との連携のため、マップのキーには「アイテムID(String)」を使用します。
     * * ID内訳:
     * 1~5: R素材 (紅, 蒼, 翠, 聖, 闇)
     * 6~10: SR素材 (赤, 青, 緑, 黄, 紫 の聖結晶)
     * 11~15: SSR素材 (赫焔鱗, 氷華の杖, 緑晶燈, 夢紡ぎの枕, 月詠みの杖)
     * 16: UR素材 (夢幻の鍵)
     */
    public void applyEvolutionData(CharacterEntity chara) {
        if (chara == null || chara.getName() == null) {
            return;
        }

        Map<String, Integer> materials = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();

        switch (chara.getName()) {
            // ==========================================
            // 炎属性 (Fire)
            // ==========================================
            case "エンバーハート":
                conditions.put("必要レベル", "1");
                conditions.put("備考", "初期解放済み");
                break;

            case "ドラコ": // Lv10解放
                materials.put("1", 3); // 紅玉
                materials.put("2", 3); // 蒼玉
                materials.put("3", 3); // 翠玉
                materials.put("4", 3); // 聖玉
                materials.put("5", 3); // 闇玉
                conditions.put("必要レベル", "10");
                break;

            case "ドラコス": // Lv20解放
                materials.put("1", 5);
                materials.put("2", 5);
                materials.put("3", 5);
                materials.put("4", 5);
                materials.put("5", 5);
                materials.put("6", 3); // 赤の聖結晶
                materials.put("7", 3); // 青の聖結晶
                materials.put("8", 3); // 緑の聖結晶
                materials.put("9", 3); // 黄の聖結晶
                materials.put("10", 3); // 紫の聖結晶
                conditions.put("必要レベル", "20");
                conditions.put("前提キャラ", "ドラコ");
                break;

            case "ドラグノイド": // Lv30解放
                materials.put("1", 7);
                materials.put("2", 7);
                materials.put("3", 7);
                materials.put("4", 7);
                materials.put("5", 7);
                materials.put("6", 5);
                materials.put("7", 5);
                materials.put("8", 5);
                materials.put("9", 5);
                materials.put("10", 5);
                materials.put("11", 1); // 赫焔鱗 (SSR)
                conditions.put("必要レベル", "30");
                conditions.put("前提キャラ", "ドラコス");
                break;

            // ==========================================
            // 水属性 (Water)
            // ==========================================
            case "ルーナドロップ": // Lv40解放 (第1段階)
                materials.put("1", 3);
                materials.put("2", 3);
                materials.put("3", 3);
                materials.put("4", 3);
                materials.put("5", 3);
                conditions.put("必要レベル", "40");
                break;

            case "ドリー": // Lv50解放
                materials.put("1", 5);
                materials.put("2", 5);
                materials.put("3", 5);
                materials.put("4", 5);
                materials.put("5", 5);
                materials.put("6", 3);
                materials.put("7", 3);
                materials.put("8", 3);
                materials.put("9", 3);
                materials.put("10", 3);
                conditions.put("必要レベル", "50");
                conditions.put("前提キャラ", "ルーナドロップ");
                break;

            case "ドルフィ": // Lv60解放
                materials.put("1", 7);
                materials.put("2", 7);
                materials.put("3", 7);
                materials.put("4", 7);
                materials.put("5", 7);
                materials.put("6", 5);
                materials.put("7", 5);
                materials.put("8", 5);
                materials.put("9", 5);
                materials.put("10", 5);
                materials.put("12", 1); // 氷華の杖 (SSR)
                conditions.put("必要レベル", "60");
                conditions.put("前提キャラ", "ドリー");
                break;

            case "ドルフィナス": // Lv70解放
                materials.put("1", 7);
                materials.put("2", 7);
                materials.put("3", 7);
                materials.put("4", 7);
                materials.put("5", 7);
                materials.put("6", 5);
                materials.put("7", 5);
                materials.put("8", 5);
                materials.put("9", 5);
                materials.put("10", 5);
                materials.put("12", 2); // 氷華の杖 x2
                conditions.put("必要レベル", "70");
                conditions.put("前提キャラ", "ドルフィ");
                break;

            // ==========================================
            // 草属性 (Grass)
            // ==========================================
            case "フォリアン": // Lv80解放
                materials.put("1", 3);
                materials.put("2", 3);
                materials.put("3", 3);
                materials.put("4", 3);
                materials.put("5", 3);
                conditions.put("必要レベル", "80");
                break;

            case "シル": // Lv90解放
                materials.put("1", 5);
                materials.put("2", 5);
                materials.put("3", 5);
                materials.put("4", 5);
                materials.put("5", 5);
                materials.put("6", 3);
                materials.put("7", 3);
                materials.put("8", 3);
                materials.put("9", 3);
                materials.put("10", 3);
                conditions.put("必要レベル", "90");
                conditions.put("前提キャラ", "フォリアン");
                break;

            case "シルファ": // Lv100解放
                materials.put("1", 7);
                materials.put("2", 7);
                materials.put("3", 7);
                materials.put("4", 7);
                materials.put("5", 7);
                materials.put("6", 5);
                materials.put("7", 5);
                materials.put("8", 5);
                materials.put("9", 5);
                materials.put("10", 5);
                materials.put("13", 1); // 緑晶燈 (SSR)
                conditions.put("必要レベル", "100");
                conditions.put("前提キャラ", "シル");
                break;

            case "シルフィナ": // Lv110解放
                materials.put("1", 7);
                materials.put("2", 7);
                materials.put("3", 7);
                materials.put("4", 7);
                materials.put("5", 7);
                materials.put("6", 5);
                materials.put("7", 5);
                materials.put("8", 5);
                materials.put("9", 5);
                materials.put("10", 5);
                materials.put("13", 2); // 緑晶燈 x2
                conditions.put("必要レベル", "110");
                conditions.put("前提キャラ", "シルファ");
                break;

            // ==========================================
            // 光属性 (Light)
            // ==========================================
            case "ハローネスト": // Lv120解放
                materials.put("1", 3);
                materials.put("2", 3);
                materials.put("3", 3);
                materials.put("4", 3);
                materials.put("5", 3);
                conditions.put("必要レベル", "120");
                break;

            case "メリー": // Lv130解放
                materials.put("1", 5);
                materials.put("2", 5);
                materials.put("3", 5);
                materials.put("4", 5);
                materials.put("5", 5);
                materials.put("6", 3);
                materials.put("7", 3);
                materials.put("8", 3);
                materials.put("9", 3);
                materials.put("10", 3);
                conditions.put("必要レベル", "130");
                conditions.put("前提キャラ", "ハローネスト");
                break;

            case "メリル": // Lv140解放
                materials.put("1", 7);
                materials.put("2", 7);
                materials.put("3", 7);
                materials.put("4", 7);
                materials.put("5", 7);
                materials.put("6", 5);
                materials.put("7", 5);
                materials.put("8", 5);
                materials.put("9", 5);
                materials.put("10", 5);
                materials.put("14", 1); // 夢紡ぎの枕 (SSR)
                conditions.put("必要レベル", "140");
                conditions.put("前提キャラ", "メリー");
                break;

            case "メリノア": // Lv150解放
                materials.put("1", 7);
                materials.put("2", 7);
                materials.put("3", 7);
                materials.put("4", 7);
                materials.put("5", 7);
                materials.put("6", 5);
                materials.put("7", 5);
                materials.put("8", 5);
                materials.put("9", 5);
                materials.put("10", 5);
                materials.put("14", 2); // 夢紡ぎの枕 x2
                conditions.put("必要レベル", "150");
                conditions.put("前提キャラ", "メリル");
                break;

            // ==========================================
            // 闇属性 (Dark)
            // ==========================================
            case "ネビュリス": // Lv160解放
                materials.put("1", 3);
                materials.put("2", 3);
                materials.put("3", 3);
                materials.put("4", 3);
                materials.put("5", 3);
                conditions.put("必要レベル", "160");
                break;

            case "ロービ": // Lv170解放
                materials.put("1", 5);
                materials.put("2", 5);
                materials.put("3", 5);
                materials.put("4", 5);
                materials.put("5", 5);
                materials.put("6", 3);
                materials.put("7", 3);
                materials.put("8", 3);
                materials.put("9", 3);
                materials.put("10", 3);
                conditions.put("必要レベル", "170");
                conditions.put("前提キャラ", "ネビュリス");
                break;

            case "ローバス": // Lv180解放
                materials.put("1", 7);
                materials.put("2", 7);
                materials.put("3", 7);
                materials.put("4", 7);
                materials.put("5", 7);
                materials.put("6", 5);
                materials.put("7", 5);
                materials.put("8", 5);
                materials.put("9", 5);
                materials.put("10", 5);
                materials.put("15", 1); // 月詠みの杖 (SSR)
                conditions.put("必要レベル", "180");
                conditions.put("前提キャラ", "ロービ");
                break;

            case "ロービアス": // Lv190解放
                materials.put("1", 7);
                materials.put("2", 7);
                materials.put("3", 7);
                materials.put("4", 7);
                materials.put("5", 7);
                materials.put("6", 5);
                materials.put("7", 5);
                materials.put("8", 5);
                materials.put("9", 5);
                materials.put("10", 5);
                materials.put("15", 2); // 月詠みの杖 x2
                conditions.put("必要レベル", "190");
                conditions.put("前提キャラ", "ローバス");
                break;

            // ==========================================
            // シークレット (Secret)
            // ==========================================
            case "シークレット": // Lv250解放 (エンドコンテンツ)
                // 大量の素材が必要
                materials.put("1", 10);
                materials.put("2", 10);
                materials.put("3", 10);
                materials.put("4", 10);
                materials.put("5", 10);
                materials.put("6", 5);
                materials.put("7", 5);
                materials.put("8", 5);
                materials.put("9", 5);
                materials.put("10", 5);
                materials.put("16", 1); // 夢幻の鍵 (UR)
                conditions.put("必要レベル", "250");
                conditions.put("備考", "全素材・全属性の極み");
                break;

            default:
                break;
        }

        chara.setEvolutionMaterials(materials);
        chara.setEvolutionConditions(conditions);
    }

    /**
     * 素材名からアイテムIDを取得するヘルパーメソッド
     * コントローラー側で名前からIDを引く場合などに使用
     */
    public Long getMaterialItemId(String materialName) {
        if (materialName == null) return 1L;
        
        switch (materialName) {
            case "紅玉": return 1L;
            case "蒼玉": return 2L;
            case "翠玉": return 3L;
            case "聖玉": return 4L;
            case "闇玉": return 5L;
            case "赤の聖結晶": return 6L;
            case "青の聖結晶": return 7L;
            case "緑の聖結晶": return 8L;
            case "黄の聖結晶": return 9L;
            case "紫の聖結晶": return 10L;
            case "赫焔鱗": return 11L;
            case "氷華の杖": return 12L;
            case "緑晶灯": 
            case "緑晶燈": return 13L; // 表記揺れ対応
            case "夢紡ぎの枕": return 14L;
            case "月詠みの杖": return 15L;
            case "夢幻の鍵": 
            case "虹玉": return 16L; // 表記揺れ対応
            default: return 1L;
        }
    }
}