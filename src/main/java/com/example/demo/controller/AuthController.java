package com.example.demo.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.service.UserService;
 
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ... (認証関連のメソッド省略) ...

    @GetMapping("/home")
    public String home(
        @AuthenticationPrincipal UserDetails userDetails,
        Model model
    ) {
        if (userDetails != null) {
            model.addAttribute("username", userDetails.getUsername());
        } else {
            model.addAttribute("username", "ゲスト");
        }
        return "home";
    }

    // 【削除済み】TrainingControllerに処理を移譲するため、AuthControllerから削除
    // @GetMapping("/training")
    // public String training() { return "training"; }
    
    // 【削除済み】GachaControllerに処理を移譲するため、AuthControllerから削除
    // @GetMapping("/gacha")
    // public String gacha() { return "gacha"; } 
    
    @GetMapping("/training-log")
    public String trainingLog(Model model) { 
        // 仮のデータを作成
        model.addAttribute("records", List.of(
            new Record("2025/11/13", "ベンチプレス", "胸", 85, 5, 3),
            new Record("2025/11/13", "AIおすすめ", "全身", 0, 40, 1),
            new Record("2025/11/12", "デッドリフト", "背中・脚", 100, 3, 3)
        ));
        return "training-log"; 
    }

    // ★ キャラクター一覧画面への遷移
    @GetMapping("/characters")
    public String characterList(Model model) {
        // 仮のキャラクターデータ
        model.addAttribute("characters", List.of(
            new Character("バルクモン", "水の種族", 15, 950, 1200, "水色のマッチョなモンスター", "active", "hi1.png"),
            new Character("ヒカリモン", "光の種族", 8, 400, 600, "光を放つ翼を持つ天使", "rest", "hikari1.png"),
            new Character("クサモン", "木の種族", 3, 120, 300, "草木の蔓を持つ優しい妖精", "rest", "kusa1.png")
        ));
        return "character-list";
    }

    @GetMapping("/settings")
    public String settings() { return "settings"; }
}

// データを保持するためのインナークラス (Recordクラス)
class Record {
    public String date;
    public String name;
    public String part;
    public int weight;
    public int reps;
    public int sets;

    public Record(String date, String name, String part, int weight, int reps, int sets) {
        this.date = date;
        this.name = name;
        this.part = part;
        this.weight = weight;
        this.reps = reps;
        this.sets = sets;
    }
    public String getDate() { return date; }
    public String getName() { return name; }
    public String getPart() { return part; }
    public int getWeight() { return weight; }
    public int getReps() { return reps; }
    public int getSets() { return sets; }
}

// キャラクター情報を保持するインナークラス
class Character {
    public String name;
    public String species;
    public int level;
    public int currentExp;
    public int requiredExp;
    public String description;
    public String status;
    public String image;

    public Character(String name, String species, int level, int currentExp, int requiredExp, String description, String status, String image) {
        this.name = name;
        this.species = species;
        this.level = level;
        this.currentExp = currentExp;
        this.requiredExp = requiredExp;
        this.description = description;
        this.status = status;
        this.image = image;
    }

    public String getName() { return name; }
    public String getSpecies() { return species; }
    public int getLevel() { return level; }
    public int getCurrentExp() { return currentExp; }
    public int getRequiredExp() { return requiredExp; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getImage() { return image; }
    public int getExpPercent() { return (int) (((double) currentExp / requiredExp) * 100); }
}
