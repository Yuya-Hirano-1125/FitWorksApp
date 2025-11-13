package com.example.demo.controller;

import java.util.concurrent.CompletableFuture; // ★ 追加

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping; // ★ 追加
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.AICoachService;

@RestController
@RequestMapping("/api") // ★ ベースパスを統一
public class AICoachController {

    private final AICoachService aiCoachService;

    public AICoachController(AICoachService aiCoachService) {
        this.aiCoachService = aiCoachService;
    }

    /**
     * AIコーチへの問い合わせを非同期で実行し、Webスレッドをブロックしないようにする。
     * @param message ユーザーメッセージ
     * @return CompletableFuture<String> 非同期処理の結果
     */
    @PostMapping("/chat")
    // ★ 戻り値の型を String から CompletableFuture<String> に変更
    // ★ @RequestParam ではなく @RequestBody String message を受け取るのが一般的ですが、
    //   ここでは元の @RequestParam("message") に合わせています。
    public CompletableFuture<String> getAICoachResponse(@RequestParam("message") String message) {
        
        // CompletableFuture.supplyAsync を使用し、サービス呼び出しを別スレッドで実行する
        return CompletableFuture.supplyAsync(() -> {
            // この処理は @EnableAsync が有効な環境下の Executor で実行されます。
            return aiCoachService.getGeminiAdvice(message);
        });
    }
}