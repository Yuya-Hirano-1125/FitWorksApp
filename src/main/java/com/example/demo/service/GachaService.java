package com.example.demo.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Item;
import com.example.demo.entity.User;
// import com.example.demo.entity.UserItem; // 削除
import com.example.demo.model.GachaItem;
import com.example.demo.model.GachaResult;
import com.example.demo.repository.GachaResultRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserRepository; // 追加
// import com.example.demo.repository.UserItemRepository; // 削除

@Service
public class GachaService {

    @Autowired
    private GachaResultRepository gachaResultRepo;

    @Autowired
    private ItemRepository itemRepo;

    // 削除: UserItemRepository
    // @Autowired
    // private UserItemRepository userItemRepo;

    // 追加: ユーザー情報の更新に必要
    @Autowired
    private UserRepository userRepository;

    private final Random random = new Random();

    // ----------- 複数回ガチャ -----------
    public List<GachaItem> roll(int count, Long userId) {
        List<GachaItem> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            results.add(drawGacha(userId));
        }
        return results;
    }

    // ----------- ガチャ1回分の処理 -----------
    private GachaItem drawGacha(Long userId) {
        // 1. 確率テーブルの作成
        List<ProbabilityItem> table = createProbabilityTable();

        // 2. 抽選
        double totalRate = 0;
        for (ProbabilityItem p : table) {
            totalRate += p.rate;
        }

        double r = random.nextDouble() * totalRate; // 0.0 ～ 100.0 (合計値)
        double current = 0;
        ProbabilityItem selected = null;

        for (ProbabilityItem p : table) {
            current += p.rate;
            if (r < current) {
                selected = p;
                break;
            }
        }

        // 万が一選択されなかった場合（計算誤差対策）は最後の商品を選択
        if (selected == null) {
            selected = table.get(table.size() - 1);
        }

        // 3. 抽選結果のオブジェクト作成
        GachaItem result = new GachaItem(selected.name, selected.rarity, selected.image);

        // 4. ★修正: ユーザーの所持アイテムに追加 (DB保存)
        // Itemマスタからエンティティを取得
        Item itemEntity = itemRepo.findByName(selected.name);
        
        if (itemEntity != null) {
            // ユーザーを取得
            User user = userRepository.findById(userId).orElse(null);
            
            if (user != null) {
                // ★修正: Userクラスのメソッドを使ってインベントリに追加
                user.addItem(itemEntity, 1);
                
                // 変更を保存
                userRepository.save(user);
            }
        }

        // 5. ガチャ履歴テーブルにも保存
        GachaResult history = new GachaResult();
        history.setUserId(userId);
        history.setItemName(selected.name);
        history.setRarity(selected.rarity);
        
        // 日本時間で保存
        ZonedDateTime nowJst = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));
        history.setDrawDateTime(nowJst.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        gachaResultRepo.save(history);

        return result;
    }

    // ----------- 確率テーブル定義 (ハードコード) -----------
    // ※ 実際にはDBから取得するなど柔軟に設定可能です
    private List<ProbabilityItem> createProbabilityTable() {
        // 合計100%になるように設定
        // UR: 2.35%, SSR: 17.65%, SR: 32.95%, R: 47.05%
        
        List<ProbabilityItem> table = new ArrayList<>();

        // --- UR (2.35%) ---
        // table.add(new ProbabilityItem("夢幻の鍵", "UR", "/img/item/UR-niji-touka.png", 1.175));
        table.add(new ProbabilityItem("虹の結晶", "UR", "/img/item/UR-niji.png", 2.35));

        // --- SSR (17.65% -> 5種等分: 3.53%) ---
        table.add(new ProbabilityItem("赤の秘石", "SSR", "/img/item/SSR-red.png", 3.53));
        table.add(new ProbabilityItem("青の秘石", "SSR", "/img/item/SSR-blue.png", 3.53));
        table.add(new ProbabilityItem("緑の秘石", "SSR", "/img/item/SSR-green.png", 3.53));
        table.add(new ProbabilityItem("黄の秘石", "SSR", "/img/item/SSR-yellow.png", 3.53));
        table.add(new ProbabilityItem("紫の秘石", "SSR", "/img/item/SSR-purple.png", 3.53));

        // --- SR (32.95% -> 5種等分: 6.59%) ---
        table.add(new ProbabilityItem("赤の大結晶", "SR", "/img/item/SR-red.png", 6.59));
        table.add(new ProbabilityItem("青の大結晶", "SR", "/img/item/SR-blue.png", 6.59));
        table.add(new ProbabilityItem("緑の大結晶", "SR", "/img/item/SR-green.png", 6.59));
        table.add(new ProbabilityItem("黄の大結晶", "SR", "/img/item/SR-yellow.png", 6.59));
        table.add(new ProbabilityItem("紫の大結晶", "SR", "/img/item/SR-purple.png", 6.59));

        // --- R (47.05% -> 5種等分: 9.41%) ---
        table.add(new ProbabilityItem("紅玉", "R", "/img/item/R-red.png", 9.41));
        table.add(new ProbabilityItem("蒼玉", "R", "/img/item/R-blue.png", 9.41));
        table.add(new ProbabilityItem("翠玉", "R", "/img/item/R-green.png", 9.41));
        table.add(new ProbabilityItem("聖玉", "R", "/img/item/R-yellow.png", 9.41));
        table.add(new ProbabilityItem("闇玉", "R", "/img/item/R-purple.png", 9.41));

        return table;
    }

    // ----------- 内部クラス: 確率定義用DTO -----------
    private static class ProbabilityItem {
        String name;
        String rarity;
        String image;
        double rate; // パーセント (例: 1.5 = 1.5%)

        public ProbabilityItem(String name, String rarity, String image, double rate) {
            this.name = name;
            this.rarity = rarity;
            this.image = image;
            this.rate = rate;
        }
    }

    // ----------- 提供割合表示用 -----------
    public List<Map<String, Object>> getProbabilityList() {

        return List.of(
            Map.of("rarity", "UR", "name", "夢幻の鍵", "rate", "2.35%", "color", "#FF66FF"),
            Map.of("rarity", "SSR", "name", "SSR 5種", "rate", "17.65%", "color", "#FFD700"),
            Map.of("rarity", "SR", "name", "SR 5種", "rate", "32.95%", "color", "#C0C0C0"),
            Map.of("rarity", "R", "name", "R 5種", "rate", "47.05%", "color", "#a26ac9")
        );
    }
}