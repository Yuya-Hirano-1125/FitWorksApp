package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

    // ---- 複数回ガチャ ----
    public List<GachaItem> roll(int count, int userId) {

        List<GachaItem> results = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            results.add(drawGacha(userId)); // DB保存付き
        }

        return results;
    }

    // ---- 単発ガチャ + DB保存 ----
    public GachaItem drawGacha(int userId) {

        GachaItem item = getRandomItem();

        // DB 保存
        GachaResult result = new GachaResult(
            userId,
            item.getName(),
            item.getRarity(),
            LocalDateTime.now().toString()
        );

        repository.save(result);

        return item;
    }

    // ---- ガチャ抽選処理 ----
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

    // ---- 確率リスト表示 ----
    public List<Map<String, Object>> getProbabilityList() {

        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> ssr = new HashMap<>();
        ssr.put("rarity", "SSR");
        ssr.put("name", "超レア・プログラム");
        ssr.put("rate", "5%");
        ssr.put("color", "#FFD700");
        list.add(ssr);

        Map<String, Object> sr = new HashMap<>();
        sr.put("rarity", "SR");
        sr.put("name", "レア・プログラム");
        sr.put("rate", "20%");
        sr.put("color", "#C0C0C0");
        list.add(sr);

        Map<String, Object> rMap = new HashMap<>();
        rMap.put("rarity", "R");
        rMap.put("name", "ノーマル・プログラム");
        rMap.put("rate", "75%");
        rMap.put("color", "#B87333");
        list.add(rMap);

        return list;
    }
}
