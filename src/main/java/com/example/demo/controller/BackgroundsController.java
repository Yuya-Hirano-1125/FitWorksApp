package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// ✅ 修正: Userエンティティのパッケージをインポート
import com.example.demo.entity.User;
// ✅ 必須: UserServiceのパッケージをインポート (ここでは com.example.demo.service と仮定)
import com.example.demo.service.UserService; 

@Controller
@RequestMapping("/characters") 
public class BackgroundsController {

    // サービス層の注入
    @Autowired
    private UserService userService; 

    /**
     * 背景一覧画面を表示するメソッド
     * URL: /characters/backgrounds に対応
     */
    @GetMapping("/backgrounds") 
    public String showBackgrounds(Model model) {
        
        // 【重要】認証済みのユーザー情報を取得する処理に置き換える必要があります。
        // Spring Securityを使用している場合は、以下のように Principal や SecurityContext から取得します。
        // ここでは、仮のロジックとしてログインユーザーを取得するメソッドを呼び出します。
        
        // 1. ログイン中のユーザー情報をサービスから取得
        //    (このメソッドは、実際の認証ロジックに合わせて UserServiceに実装が必要です)
        User currentUser = userService.getLoggedInUser(); 
        
        int currentLevel = 100; // 初期値として1を設定
        
        if (currentUser != null) {
            // 2. Userエンティティの getLevel() メソッドを使ってレベルを取得
            //    エンティティには getLevel() があるため、そのまま利用します。
        	currentLevel = currentUser.getLevel(); 
        } else {
            // ユーザーが見つからない場合の処理（例: ログインページへリダイレクトなど）
            System.err.println("ログインユーザー情報が見つかりません。");
            // return "redirect:/login"; // 例
        }
        
        // 3. 取得したレベルをThymeleafテンプレートに渡す
        model.addAttribute("currentLevel", currentLevel);
        
        return "forward:/characters/menu/Backgrounds";
    }
}