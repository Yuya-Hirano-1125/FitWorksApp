package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.dto.ItemCountDTO;
import com.example.demo.repository.ItemRepository;

@Controller
@RequestMapping("/materials")
public class MaterialController {

    private final ItemRepository itemRepository;

    public MaterialController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    // ユーザーごとの素材一覧を表示
    @GetMapping("/{userId}")
    public String showMaterials(@PathVariable Long userId, Model model) {
        // Repositoryからユーザーの所持数を取得
        List<ItemCountDTO> items = itemRepository.findItemCountsByUserId(userId);

        // デバッグ出力：取得件数と中身を確認
        System.out.println("取得したアイテム数: " + items.size());
        for (ItemCountDTO item : items) {
            System.out.println("name=" + item.getName() 
                + ", imagePath=" + item.getImagePath() 
                + ", count=" + item.getCount());
        }

        model.addAttribute("items", items);

        // 必要数を渡す（例: Mapで管理）
        /*
        model.addAttribute("itemNeedMap", Map.of(
            "紅玉", 5,
            "蒼玉", 5,
            "翠玉", 5,
            "聖玉", 5,
            "闇玉", 5,
            "赤の聖結晶", 3,
            "青の聖結晶", 3,
            "緑の聖結晶", 3,
            "黄の聖結晶", 3,
            "紫の聖結晶", 3,
            "赫焔鱗", 2,
            "氷華の杖", 2,
            "緑晶灯", 2,
            "夢紡ぎの枕", 2,
            "月詠みの杖", 2,
            "虹の神結晶", 1
        ));
        */

        // Thymeleafテンプレートを返す
        // ファイルは src/main/resources/templates/characters/menu/CharactersEvolutionMaterial.html にあるので、
        // return のパスも "characters/menu/CharactersEvolutionMaterial" に修正
        return "characters/menu/CharactersEvolutionMaterial";
    }
}
