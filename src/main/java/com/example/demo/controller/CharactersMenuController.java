package com.example.demo.controller;

import java.util.Arrays; // ダミーデータ用
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CharactersMenuController {
    
    // --- 依存性の注入 (DB連携に必要なServiceなどをここに宣言します) ---
    // @Autowired
    // private UserService userService; 
    // @Autowired
    // private BackgroundService backgroundService;
    // ------------------------------------------------------------------


    // ... (既存のメソッド省略) ...

    // 背景一覧画面へ遷移
    @GetMapping("/characters/menu/Backgrounds")
    public String showBackgrounds(Model model) {
        
        // 1. ユーザーの現在のレベルを取得
        // ★本来はDBから取得: int userLevel = userService.getUserLevel(currentUser);
        // ★ここでは動作確認のためダミー値を設定します
        int userLevel = 33; 
        model.addAttribute("userLevel", userLevel);

        // 2. すべての背景アイテムのリストを取得（解放レベル情報を含む）
        // ★本来はDBから取得: List<BackgroundItem> allBackgrounds = backgroundService.findAllBackgrounds();
        List<BackgroundItem> allBackgrounds = getMockBackgrounds(); // ダミーデータ
        model.addAttribute("allBackgrounds", allBackgrounds);
        
        // ★おまけ: 現在装備中の背景アイテム（カスタマイズ画面と同様に表示する場合）
        // 装備中のアイテムがない場合は null、ある場合はエンティティをセット
        // model.addAttribute("equippedBackground", backgroundService.getEquippedBackground(currentUser)); 
        model.addAttribute("equippedBackground", null); 

        return "characters/menu/Backgrounds"; 
    }
    
    // ------------------------------------------------------------------
    // ★★★★ 注意: 実際の開発ではこのモック関数は削除し、Service/Repositoryを使用してください ★★★★
    // ------------------------------------------------------------------
    
    // 【仮の背景アイテムクラスとモックデータ】
    // 実際にはEntityとして定義する必要があります
    private static class BackgroundItem {
        public Long id;
        public String name;
        public String imagePath;
        public int requiredLevel; // 解放に必要なレベル
        // コンストラクタ、ゲッター、セッターなど... 
        
        public BackgroundItem(Long id, String name, String imagePath, int requiredLevel) {
            this.id = id;
            this.name = name;
            this.imagePath = imagePath;
            this.requiredLevel = requiredLevel;
        }
    }
    
    private List<BackgroundItem> getMockBackgrounds() {
        return Arrays.asList(
            // レベル1: 常に解放
            new BackgroundItem(101L, "教室の背景", "/img/background/classroom.png", 1),
            // レベル5: Lv7で解放済み
            new BackgroundItem(102L, "夕焼けの教室", "/img/background/sunset.png", 5), 
            // レベル7: Lv7で解放済み
            new BackgroundItem(103L, "森の散歩道", "/img/background/forest.png", 7), 
            // レベル10: Lv7でロック中
            new BackgroundItem(104L, "宇宙の背景", "/img/background/space.png", 10),
            // レベル20: Lv7でロック中
            new BackgroundItem(105L, "深海", "/img/background/deepsea.png", 20)
        );
    }
}
