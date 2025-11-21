package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
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
}
