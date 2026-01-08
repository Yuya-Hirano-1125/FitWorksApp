package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.dto.ItemCountDTO;
import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;

@Controller
public class MaterialController {

    private final UserRepository userRepository;

    // ItemRepositoryへの依存を削除し、UserRepositoryを注入
    public MaterialController(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        // 最新のユーザー情報を取得 (インベントリを確実に取得するため)
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        // ユーザーのインベントリマップを取得
        Map<Item, Integer> inventory = user.getInventory();

        // MapのエントリをDTOリストに変換
        List<ItemCountDTO> items = new ArrayList<>();
        
        for (Map.Entry<Item, Integer> entry : inventory.entrySet()) {
            Item item = entry.getKey();
            Integer count = entry.getValue();
            
            // アイテムの個数が0以下の場合はリストに含めない場合はここでチェック
            if (count <= 0) continue;

            // DTOに変換
            ItemCountDTO dto = new ItemCountDTO();
            dto.setName(item.getName());
            dto.setImagePath(item.getImagePath());
            dto.setRarity(item.getRarity());
            dto.setCount(count);
            
            // ソート順制御のために元のItemエンティティの情報を利用したい場合、DTOにフィールドを追加するか、
            // ここで一時的に保持してソートに使用します。
            // 今回はDTOにはsortOrderがない前提で、変換後にItemのsortOrderを参照できないため
            // ItemCountDTOにsortOrderフィールドがない場合は、Itemオブジェクト自体を使ってソートしてから変換するのがベターです。
            
            items.add(dto);
        }

        // ソート: 簡易的にレアリティ順や名前順などでソート (必要に応じて調整してください)
        // 元のItemにsortOrderがある場合は、MapのKeySetをsortしてからDTOにするのが理想です。
        // ここではDTO変換後に簡易ソートする例とします。
        
        // デバッグ出力
        System.out.println("ログインユーザーID: " + userId);
        System.out.println("所持アイテム種類数: " + items.size());

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