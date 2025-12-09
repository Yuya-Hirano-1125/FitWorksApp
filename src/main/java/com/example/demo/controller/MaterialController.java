package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.dto.ItemCountDTO;
import com.example.demo.repository.ItemRepository;
import com.example.demo.security.CustomUserDetails; // ログインユーザー情報を保持するクラス

@Controller
public class MaterialController {

    private final ItemRepository itemRepository;

    public MaterialController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    // ログインユーザーごとの素材一覧を表示
    @GetMapping("/characters/menu/CharactersEvolutionMaterial")
    public String showMaterials(Model model, @AuthenticationPrincipal CustomUserDetails user) {
        // 未ログイン時はログインページへリダイレクト
        if (user == null) {
            return "redirect:/login";
        }

        // ログインユーザーのIDを取得
        Long userId = user.getId();

        // Repositoryからユーザーの所持数を登録順で取得
        List<ItemCountDTO> items = itemRepository.findItemCountsByUserId(userId);

        // デバッグ出力
        System.out.println("ログインユーザーID: " + userId);
        System.out.println("取得したアイテム数: " + items.size());
        for (ItemCountDTO item : items) {
            System.out.println("name=" + item.getName()
                + ", imagePath=" + item.getImagePath()
                + ", count=" + item.getCount()
                + ", rarity=" + item.getRarity());
        }

        // レアリティごとにフィルタリングして渡す
        model.addAttribute("itemsR", items.stream()
                .filter(i -> "R".equals(i.getRarity()))
                .collect(Collectors.toList()));
        model.addAttribute("itemsSR", items.stream()
                .filter(i -> "SR".equals(i.getRarity()))
                .collect(Collectors.toList()));
        model.addAttribute("itemsSSR", items.stream()
                .filter(i -> "SSR".equals(i.getRarity()))
                .collect(Collectors.toList()));
        model.addAttribute("itemsUR", items.stream()
                .filter(i -> "UR".equals(i.getRarity()))
                .collect(Collectors.toList()));

        // Thymeleafテンプレートを返す
        return "characters/menu/CharactersEvolutionMaterial";
    }
}
