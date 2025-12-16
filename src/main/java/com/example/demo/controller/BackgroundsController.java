package com.example.demo.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Modelをインポート
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam; // 今回は不要
// import org.springframework.web.servlet.mvc.support.RedirectAttributes; // 今回は不要

import com.example.demo.service.UserService; // UserServiceのパッケージをインポート

@Controller
@RequestMapping("/characters") 
public class BackgroundsController {

    @Autowired
    private UserService userService; 

    /**
     * 背景一覧画面を表示するメソッド
     * URL: /characters/backgrounds に対応
     */
    @GetMapping("/backgrounds") 
    public String showBackgrounds(Model model, 
                                  // @RequestParam String materialType, // HTMLから渡されていないため削除
                                  // RedirectAttributes redirectAttributes, // 今回は不要
                                  Principal principal) {
        
        // ログインユーザーが認証されていない場合はエラーを返すか、ログインページにリダイレクトすべき
        if (principal == null) {
             System.err.println("認証情報がありません。");
             return "redirect:/login"; // ログインページにリダイレクト
        }

        // 1. ログイン中のユーザー名を取得
        String username = principal.getName();
        
        // 2. ユーザーレベルをサービスから取得
        // (以前の CharacterUnlockController のコードと同じロジックを使用)
        int userLevel = userService.getUserLevel(username); // DBからレベル取得
        
     // ★★★ これを追記して、コンソールに出た値を確認してください ★★★
        System.out.println("DEBUG: 取得されたレベル = " + userLevel); 
        // ★★★
        
        
        // 3. 【重要】取得したレベルをモデルに追加する
        //    HTML側が期待する変数名は ${userLevel} です。
        model.addAttribute("userLevel", userLevel);
        
        // ----------------------------------------------------------------------
        // 💡 補足: JavaScriptで使うための currentLevel もここで統一して渡しておく
        // model.addAttribute("currentLevel", userLevel);
        // ----------------------------------------------------------------------

        // 4. ビューを返す
        //    Thymeleafテンプレートのパス (例: /src/main/resources/templates/characters/Backgrounds.html)
        return "characters/menu/Backgrounds";
        
        // 注意: 以前のコードの 'forward:/characters/menu/Backgrounds' は、
        // HTMLのテンプレートパスと一致していない可能性や、内部リダイレクトでモデルが消える可能性があるため、
        // テンプレート名 'characters/Backgrounds' (または適切なパス) を直接返すように修正しました。
    }
}