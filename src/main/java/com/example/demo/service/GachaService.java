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
import com.example.demo.entity.UserItem;
import com.example.demo.model.GachaItem;
import com.example.demo.model.GachaResult;
import com.example.demo.repository.GachaResultRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserItemRepository;

@Service
public class GachaService {

    @Autowired
    private GachaResultRepository gachaResultRepo;

    @Autowired
    private ItemRepository itemRepo;

    @Autowired
    private UserItemRepository userItemRepo;

    private final Random random = new Random();

    // ----------- 複数回ガチャ -----------
    public List<GachaItem> roll(int count, Long userId) {
        List<GachaItem> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            results.add(drawGacha(userId));
        }
        return results;
    }

    // ----------- 単発ガチャ（履歴 + 所持 同時保存）-----------
    public GachaItem drawGacha(Long userId) {

        // ① ガチャ抽選してアイテム決定
        GachaItem gachaItem = getRandomItem();

        // ② SQLiteへ履歴を保存
        ZonedDateTime nowJst = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));
        String formattedDate = nowJst.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        GachaResult result = new GachaResult(
                userId,
                gachaItem.getName(),
                gachaItem.getRarity(),
                formattedDate
        );
        gachaResultRepo.save(result);

        // ③ Item エンティティを取得
        Item item = itemRepo.findByName(gachaItem.getName());
        if (item == null) {
            System.err.println("★ Item テーブルに一致するアイテムがありません：" + gachaItem.getName());
            return gachaItem; // 所持アイテム追加はスキップ
        }

        // ④ UserItem（所持アイテム）に保存
        UserItem userItem = new UserItem();
        User user = new User();
        user.setId(userId);

        userItem.setUser(user);
        userItem.setItem(item);

        userItemRepo.save(userItem);

        return gachaItem;
    }

    // ----------- ランダム抽選（確率テーブル方式）-----------
    private GachaItem getRandomItem() {

        List<ProbabilityItem> table = List.of(
            new ProbabilityItem("夢幻の鍵", "UR", "/img/item/UR-niji.png", 2.35),

            new ProbabilityItem("赫焔鱗", "SSR", "/img/item/SSR-red.png", 3.53),
            new ProbabilityItem("氷華の杖", "SSR", "/img/item/SSR-blue.png", 3.53),
            new ProbabilityItem("緑晶灯", "SSR", "/img/item/SSR-green.png", 3.53),
            new ProbabilityItem("夢紡ぎの枕", "SSR", "/img/item/SSR-yellow.png", 3.53),
            new ProbabilityItem("月詠みの杖", "SSR", "/img/item/SSR-purple.png", 3.53),

            new ProbabilityItem("赤の聖結晶", "SR", "/img/item/SR-red.png", 6.59),
            new ProbabilityItem("青の聖結晶", "SR", "/img/item/SR-blue.png", 6.59),
            new ProbabilityItem("緑の聖結晶", "SR", "/img/item/SR-green.png", 6.59),
            new ProbabilityItem("黄の聖結晶", "SR", "/img/item/SR-yellow.png", 6.59),
            new ProbabilityItem("紫の聖結晶", "SR", "/img/item/SR-purple.png", 6.59),

            new ProbabilityItem("紅玉", "R", "/img/item/R-red.png", 8.94),
            new ProbabilityItem("蒼玉", "R", "/img/item/R-blue.png", 8.94),
            new ProbabilityItem("翠玉", "R", "/img/item/R-green.png", 8.94),
            new ProbabilityItem("聖玉", "R", "/img/item/R-yellow.png", 8.94),
            new ProbabilityItem("闇玉", "R", "/img/item/R-purple.png", 8.94)
        );

        double r = random.nextDouble() * 100;
        double cumulative = 0;

        for (ProbabilityItem p : table) {
            cumulative += p.rate;
            if (r < cumulative) {
                return new GachaItem(p.name, p.rarity, p.image);
            }
        }

        ProbabilityItem last = table.get(table.size() - 1);
        return new GachaItem(last.name, last.rarity, last.image);
    }

    // ----------- 提供割合表示用 -----------
    public List<Map<String, Object>> getProbabilityList() {

        return List.of(
            Map.of("rarity", "UR", "name", "夢幻の鍵", "rate", "2.35%", "color", "#FF66FF"),
            Map.of("rarity", "SSR", "name", "SSR 5種", "rate", "17.65%", "color", "#FFD700"),
            Map.of("rarity", "SR", "name", "SR 5種", "rate", "32.95%", "color", "#C0C0C0"),
            Map.of("rarity", "R", "name", "R 5種", "rate", "44.70%", "color", "#B87333")
        );
    }

    // ----------- 内部クラス：確率付きアイテム -----------
    static class ProbabilityItem {
        String name;
        String rarity;
        String image;
        double rate;

        ProbabilityItem(String name, String rarity, String image, double rate) {
            this.name = name;
            this.rarity = rarity;
            this.image = image;
            this.rate = rate;
        }
    }
}
