package com.example.demo;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

// AppConfig.java を作成
@Configuration
@EnableAsync // (FitWorksAppApplicationにもあるが、こちらにも定義可能)
public class AppConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // ★ 核心サイズ: 同時に実行されるスレッドの数 (CPUコア数程度が目安)
        executor.setCorePoolSize(10); 
        
        // ★ 最大サイズ: 待機キューが満杯になったときに作成される最大スレッド数
        //    AI処理が重いため、デフォルトの200よりも高い50など、余裕を持たせる
        executor.setMaxPoolSize(50); 
        
        // 待機キューのサイズ: 実行待ちのリクエストを格納
        executor.setQueueCapacity(100);
        
        // スレッド名のプレフィックスを設定 (ログで識別しやすくなる)
        executor.setThreadNamePrefix("AI-Processor-");
        
        executor.initialize();
        return executor;
    }
}