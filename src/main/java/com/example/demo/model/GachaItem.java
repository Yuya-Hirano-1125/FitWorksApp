package com.example.demo.model;

public class GachaItem {
    private String name;
    private String rarity;
    private String imagePath;

    public GachaItem(String name, String rarity, String imagePath) {
        this.name = name;
        this.rarity = rarity;
        this.imagePath = imagePath;
    }

    public String getName() { return name; }
    public String getRarity() { return rarity; }
    public String getImagePath() { return imagePath; }
}
