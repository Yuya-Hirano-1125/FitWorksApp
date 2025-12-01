package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.demo.model.GachaItem;

@Service
public class GachaService {

    private final Random random = new Random();

    public List<GachaItem> roll(int count) {

        List<GachaItem> results = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            results.add(pickOne());
        }

        return results;
    }

    private GachaItem pickOne() {
        int r = random.nextInt(100);

        if (r < 5) {
            return new GachaItem("超レア・プログラム", "SSR", "/img/ssr.png");
        } else if (r < 25) {
            return new GachaItem("レア・プログラム", "SR", "/img/sr.png");
        } else {
            return new GachaItem("ノーマル・プログラム", "R", "/img/r.png");
        }
    }
    
    // 排出内容と確率のリストを返すメソッドを追加
    public List<Map<String, Object>> getProbabilityList() {
        List<Map<String, Object>> list = new ArrayList<>();

        // SSR (5%)
        Map<String, Object> ssr = new HashMap<>();
        ssr.put("rarity", "SSR");
        ssr.put("name", "超レア・プログラム");
        ssr.put("rate", "5%");
        ssr.put("color", "#FFD700"); // ゴールド
        list.add(ssr);

        // SR (20%)
        Map<String, Object> sr = new HashMap<>();
        sr.put("rarity", "SR");
        sr.put("name", "レア・プログラム");
        sr.put("rate", "20%");
        sr.put("color", "#C0C0C0"); // シルバー
        list.add(sr);

        // R (75%)
        Map<String, Object> r = new HashMap<>();
        r.put("rarity", "R");
        r.put("name", "ノーマル・プログラム");
        r.put("rate", "75%");
        r.put("color", "#B87333"); // ブロンズ
        list.add(r);

        return list;
    }
}