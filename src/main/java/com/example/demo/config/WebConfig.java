package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "/uploads/**" というURLへのアクセスを
        // アプリ実行フォルダ直下の "uploads/" ディレクトリ内のファイルにマッピングする
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}