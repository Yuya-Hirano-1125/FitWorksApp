package com.example.demo.service;

import java.time.LocalDateTime;
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

    // 複数回ガチャ
    public List<GachaItem> roll(int count, int userId) {
        List<GachaItem> results = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            results.add(drawGacha(userId));
        }

        return results;
    }

    // 単発 + DB保存
    public GachaItem drawGacha(int userId) {

        GachaItem item = getRandomItem();

        GachaResult result = new GachaResult(
            userId,
            item.getName(),
            item.getRarity(),
            LocalDateTime.now().toString()
        );

        repository.save(result);

        return item;
    }

    // 抽選
    private GachaItem getRandomItem() {

        int r = random.nextInt(100);

        if (r < 5) {
            return new GachaItem("超レア・プログラム", "SSR", "/img/ssr.png");
        } else if (r < 25) {
            return new GachaItem("レア・プログラム", "SR", "/img/sr.png");
        } else {
            return new GachaItem("ノーマル・プログラム", "R", "/img/r.png");
        }
    }

    // 出現確率
    public List<Map<String, Object>> getProbabilityList() {

        List<Map<String, Object>> list = new ArrayList<>();

        list.add(Map.of(
                "rarity", "SSR",
                "name", "超レア・プログラム",
                "rate", "5%",
                "color", "#FFD700"
        ));

        list.add(Map.of(
                "rarity", "SR",
                "name", "レア・プログラム",
                "rate", "20%",
                "color", "#C0C0C0"
        ));

        list.add(Map.of(
                "rarity", "R",
                "name", "ノーマル・プログラム",
                "rate", "75%",
                "color", "#B87333"
        ));

        return list;
    }
}
