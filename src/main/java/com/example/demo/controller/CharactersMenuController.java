package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/characters") // URLのルートは /characters
public class CharactersMenuController {

    /**
     * home.htmlのリンク @{/characters/menu} に対応
     * @return テンプレートファイルのパス (src/main/resources/templates/以下)
     */
    @GetMapping("characters/menu/CharactersMenu") 
    public String showCharactersMenu() {
        // ★ 修正箇所: サブディレクトリのパスを追記
        return "characters/menu/CharactersMenu"; 
    }
}