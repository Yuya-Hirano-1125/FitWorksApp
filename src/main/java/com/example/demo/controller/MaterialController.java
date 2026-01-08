package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.dto.ItemCountDTO;
import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;

@Controller
public class MaterialController {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public MaterialController(UserRepository userRepository, ItemRepository itemRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    // ログインユーザーごとの素材一覧を表示
    @GetMapping("/characters/menu/CharactersEvolutionMaterial")
    public String showMaterials(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 未ログイン時はログインページへリダイレクト
        if (userDetails == null) {
            return "redirect:/login";
        }

        // ログインユーザーIDを取得
        Long userId = userDetails.getId();

        // 1. ユーザー情報を取得 (所持アイテムMapを使うため)
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        // 2. 全アイテムマスタを取得 (未所持のアイテムも表示するため)
        List<Item> allItems = itemRepository.findAllOrderBySortOrder();

        // 3. 表示用リストを作成 (全アイテム × ユーザーの所持数)
        List<ItemCountDTO> items = new ArrayList<>();
        
        for (Item item : allItems) {
            // Userクラスに追加したヘルパーメソッドで所持数を取得 (未所持なら0が返る)
            int count = user.getItemCount(item);
            
            ItemCountDTO dto = new ItemCountDTO();
            dto.setName(item.getName());
            dto.setImagePath(item.getImagePath());
            dto.setRarity(item.getRarity());
            dto.setCount(count);
            
            items.add(dto);
        }

        // デバッグ出力
        System.out.println("ログインユーザーID: " + userId);
        System.out.println("表示対象アイテム総数: " + items.size());

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