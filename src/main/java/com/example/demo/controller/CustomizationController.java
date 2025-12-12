package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.User;
import com.example.demo.entity.UserItem;
import com.example.demo.service.CustomizationService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/gacha/customize")
public class CustomizationController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CustomizationService customizationService;

    /** キャラクターカスタマイズ画面表示 */
    @GetMapping
    public String showCustomization(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        
        // 装備中のアイテム
        model.addAttribute("equippedBackground", user.getEquippedBackgroundItem());
        model.addAttribute("equippedCostume", user.getEquippedCostumeItem());

        // 所有アイテム一覧
        List<UserItem> ownedItems = customizationService.getOwnedItems(user);
        model.addAttribute("ownedItems", ownedItems);

        return "forward:/gacha/customization"; // Thymeleafテンプレート名
    }

    /** アイテムを装備する */
    @PostMapping("/equip/{itemId}")
    public String equipItem(@PathVariable("itemId") Long itemId, 
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        boolean success = customizationService.equipItem(user, itemId);
        
        if (success) {
            redirectAttributes.addFlashAttribute("message", "アイテムを装備しました！");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "アイテムの装備に失敗しました。");
        }

        return "redirect:/gacha/customize";
    }

    /** アイテムの装備を解除する */
    @PostMapping("/unequip/{itemType}")
    public String unequipItem(@PathVariable("itemType") String itemType, 
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        customizationService.unequipItem(user, itemType);

        redirectAttributes.addFlashAttribute("message", itemType.toUpperCase() + "の装備を解除しました。");

        return "redirect:/gacha/customize";
    }
}