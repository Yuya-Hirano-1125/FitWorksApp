package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.entity.UserItem;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserItemRepository;
import com.example.demo.repository.UserRepository;

@Service
public class CustomizationService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserItemRepository userItemRepository;
    
    @Autowired
    private UserRepository userRepository;

    public List<UserItem> getOwnedItems(User user) {
        return userItemRepository.findByUser(user);
    }

    /**
     * アイテムを装備します。
     */
    @Transactional
    public boolean equipItem(User user, Long itemId) {
        // 1. 所有権とアイテム存在の確認
        if (!userItemRepository.existsByUserAndItemId(user, itemId)) {
            return false;
        }

        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            return false;
        }
        Item itemToEquip = itemOpt.get();

        // 2. 装備スロットの更新
        if ("BACKGROUND".equals(itemToEquip.getType())) {
            user.setEquippedBackgroundItem(itemToEquip);
        } else if ("COSTUME".equals(itemToEquip.getType())) {
            user.setEquippedCostumeItem(itemToEquip);
        } else {
            return false;
        }

        userRepository.save(user);
        return true;
    }
    
    /**
     * アイテムの装備を解除します。
     */
    @Transactional
    public void unequipItem(User user, String itemType) {
        if ("BACKGROUND".equals(itemType.toUpperCase())) {
            user.setEquippedBackgroundItem(null);
        } else if ("COSTUME".equals(itemType.toUpperCase())) {
            user.setEquippedCostumeItem(null);
        }
        userRepository.save(user);
    }
}