








package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam; 

@Controller
public class TrainingController {

    // ★ 修正点: /training のメイン画面ルーティングをAuthControllerから引き継ぐ
    @GetMapping("/training")
    public String showTrainingOptions(Model model) {
        // training.html は特別なモデルデータなしでレンダリングされます
        return "training";
    }

    /**
     * トレーニング開始時の各オプションを処理し、入力画面に遷移します。
     * @param type 選択されたトレーニングタイプ (ai-suggested, free-weight, cardio)
     */
    @GetMapping("/training/start")
    public String startTraining(@RequestParam("type") String type, Model model) {
        
        // 仮の種目データ（選択肢用）をモデルに追加
        if (type.equals("free-weight")) {
             model.addAttribute("freeWeightExercises", List.of("ベンチプレス", "スクワット", "デッドリフト"));
        } else if (type.equals("cardio")) {
             model.addAttribute("cardioExercises", List.of("ランニング", "サイクリング", "水泳"));
        }
        
        model.addAttribute("trainingType", type);
        model.addAttribute("trainingTitle", "トレーニング記録");

        switch (type) {
            case "ai-suggested":
                model.addAttribute("selectedExercise", "AIおすすめメニュー");
                model.addAttribute("programName", "腹筋をバキバキにするプログラム");
                return "training-session";
                
            case "free-weight":
                model.addAttribute("selectedExercise", "フリーウェイト");
                return "training-form-weight";
                
            case "cardio":
                model.addAttribute("selectedExercise", "有酸素運動");
                return "training-form-cardio";

            default:
                return "redirect:/training"; 
        }
    }

    // TODO: @PostMapping("/training/save") で記録をDBに保存するメソッドを後で追加する
}