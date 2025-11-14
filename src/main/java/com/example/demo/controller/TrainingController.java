package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TrainingController {

    /**
     * トレーニング開始時の各オプションを処理し、入力画面に遷移します。
     * @param type 選択されたトレーニングタイプ (ai-suggested, free-weight, cardio)
     */
    @GetMapping("/training/start")
    public String startTraining(@RequestParam("type") String type, Model model) {
        
        model.addAttribute("trainingType", type);

        switch (type) {
            case "ai-suggested":
                // AIメニューは、専用の画面でセッションを開始する（ここではダミー）
                model.addAttribute("title", "AIおすすめメニュー開始");
                model.addAttribute("programName", "腹筋をバキバキにするプログラム");
                return "training-session";
                
            case "free-weight":
                // フリーウェイトは、重量と回数の入力フォームがある画面に遷移
                model.addAttribute("title", "フリーウェイト (自由記録)");
                return "training-form-weight";
                
            case "cardio":
                // 有酸素運動は、時間と距離の入力フォームがある画面に遷移
                model.addAttribute("title", "有酸素運動 (時間記録)");
                return "training-form-cardio";

            default:
                // 不正なタイプの場合はホームに戻すか、エラー画面へ
                return "redirect:/training"; 
        }
    }

    // TODO: @PostMapping("/training/save") で記録をDBに保存するメソッドを後で追加する
}