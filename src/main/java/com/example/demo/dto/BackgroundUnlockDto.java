package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;

public class BackgroundUnlockDto {
    private boolean hasNewUnlocks;
    private List<UnlockedBackground> unlockedBackgrounds;
    
    public BackgroundUnlockDto() {
        this.hasNewUnlocks = false;
        this.unlockedBackgrounds = new ArrayList<>();
    }
    
    public boolean isHasNewUnlocks() {
        return hasNewUnlocks;
    }
    
    public void setHasNewUnlocks(boolean hasNewUnlocks) {
        this.hasNewUnlocks = hasNewUnlocks;
    }
    
    public List<UnlockedBackground> getUnlockedBackgrounds() {
        return unlockedBackgrounds;
    }
    
    public void setUnlockedBackgrounds(List<UnlockedBackground> unlockedBackgrounds) {
        this.unlockedBackgrounds = unlockedBackgrounds;
    }
    
    public void addUnlockedBackground(String id, String name, int requiredLevel) {
        this.unlockedBackgrounds.add(new UnlockedBackground(id, name, requiredLevel));
        this.hasNewUnlocks = true;
    }
    
    public static class UnlockedBackground {
        private String id;
        private String name;
        private int requiredLevel;
        
        public UnlockedBackground(String id, String name, int requiredLevel) {
            this.id = id;
            this.name = name;
            this.requiredLevel = requiredLevel;
        }
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public int getRequiredLevel() {
            return requiredLevel;
        }
    }
}