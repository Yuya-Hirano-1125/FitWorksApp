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

import com.example.demo.model.GachaItem;
import com.example.demo.model.GachaResult;
import com.example.demo.repository.GachaResultRepository;

@Service
public class GachaService {

    @Autowired
    private GachaResultRepository repository;

    private final Random random = new Random();

    // ----------- 複数回ガチャ -----------
    public List<GachaItem> roll(int count, Long userId) {
        List<GachaItem> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            results.add(drawGacha(userId));
        }
        return results;
    }

    // ----------- 単発ガチャ + DB保存 -----------
    public GachaItem drawGacha(Long userId) {

        GachaItem item = getRandomItem();

        ZonedDateTime nowJst = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));
        String formattedDate = nowJst.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        GachaResult result = new GachaResult(
                userId,
                item.getName(),
                item.getRarity(),
                formattedDate
        );

        repository.save(result);
        return item;
    }

    // ----------- ランダム抽選（確率テーブル方式）-----------
    private GachaItem getRandomItem() {

        // ★ 画像パス定義
        String urImg = "/img/item/UR-niji.png";

        String[] ssrImgs = {
            "/img/item/SSR-red.png",
            "/img/item/SSR-blue.png",
            "/img/item/SSR-green.png",
            "/img/item/SSR-yellow.png",
            "/img/item/SSR-purple.png"
        };

        String[] srImgs = {
            "/img/item/SR-red.png",
            "/img/item/SR-blue.png",
            "/img/item/SR-green.png",
            "/img/item/SR-yellow.png",
            "/img/item/SR-purple.png"
        };

        String[] rImgs = {
            "/img/item/R-red.png",
            "/img/item/R-blue.png",
            "/img/item/R-green.png",
            "/img/item/R-yellow.png",
            "/img/item/R-purple.png"
        };

        // ★ 確率テーブル
        List<ProbabilityItem> table = List.of(
            new ProbabilityItem("夢幻の鍵", "UR", urImg, 2.35),

            new ProbabilityItem("赫焔鱗", "SSR", ssrImgs[random.nextInt(ssrImgs.length)], 3.53),
            new ProbabilityItem("氷華の杖", "SSR", ssrImgs[random.nextInt(ssrImgs.length)], 3.53),
            new ProbabilityItem("緑晶灯", "SSR", ssrImgs[random.nextInt(ssrImgs.length)], 3.53),
            new ProbabilityItem("夢紡ぎの枕", "SSR", ssrImgs[random.nextInt(ssrImgs.length)], 3.53),
            new ProbabilityItem("月詠みの杖", "SSR", ssrImgs[random.nextInt(ssrImgs.length)], 3.53),

            new ProbabilityItem("赤の聖結晶", "SR", srImgs[random.nextInt(srImgs.length)], 6.59),
            new ProbabilityItem("青の聖結晶", "SR", srImgs[random.nextInt(srImgs.length)], 6.59),
            new ProbabilityItem("緑の聖結晶", "SR", srImgs[random.nextInt(srImgs.length)], 6.59),
            new ProbabilityItem("黄の聖結晶", "SR", srImgs[random.nextInt(srImgs.length)], 6.59),
            new ProbabilityItem("紫の聖結晶", "SR", srImgs[random.nextInt(srImgs.length)], 6.59),

            new ProbabilityItem("紅玉", "R", rImgs[random.nextInt(rImgs.length)], 8.94),
            new ProbabilityItem("蒼玉", "R", rImgs[random.nextInt(rImgs.length)], 8.94),
            new ProbabilityItem("翠玉", "R", rImgs[random.nextInt(rImgs.length)], 8.94),
            new ProbabilityItem("聖玉", "R", rImgs[random.nextInt(rImgs.length)], 8.94),
            new ProbabilityItem("闇玉", "R", rImgs[random.nextInt(rImgs.length)], 8.94)
        );

        double r = random.nextDouble() * 100;
        double cumulative = 0;

        for (ProbabilityItem p : table) {
            cumulative += p.rate;
            if (r < cumulative) {
                return new GachaItem(p.name, p.rarity, p.image);
            }
        }

        // 念のため最後のアイテム
        ProbabilityItem last = table.get(table.size() - 1);
        return new GachaItem(last.name, last.rarity, last.image);
    }

    // ----------- 提供割合表示用 -----------
    public List<Map<String, Object>> getProbabilityList() {

        return List.of(
            Map.of("rarity", "UR", "name", "夢幻の鍵", "rate", "2.35%", "color", "#FF66FF"),
            Map.of("rarity", "SSR", "name", "SSR武具5種", "rate", "17.65%", "color", "#FFD700"),
            Map.of("rarity", "SR", "name", "聖結晶5種", "rate", "32.95%", "color", "#C0C0C0"),
            Map.of("rarity", "R", "name", "属性玉5種", "rate", "44.70%", "color", "#B87333")
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
