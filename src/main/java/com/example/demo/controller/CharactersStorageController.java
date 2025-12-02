package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/characters")
public class CharactersStorageController {

	//キャラクター解放画面へ遷移
    @GetMapping("/storage")
    public String showCharacterStorage() {
        return "characters/Storage"; 
    }
    
    //★１炎タイプの卵
    @GetMapping("/dracoegg") 
    public String DracoEggInfo() {
        return "characters/dracoegg"; 
    }
    
    //★2炎タイプの1進化目
    @GetMapping("/draco") 
    public String DracoInfo() {
        return "characters/draco"; 
    }
    
    //★3炎タイプの2進化目
    @GetMapping("/dracos") 
    public String DracosInfo() {
        return "characters/dracos"; 
    }
    
  //★4炎タイプの最終進化
    @GetMapping("/dragonoid") 
    public String DragonoidInfo() {
        return "characters/dragonoid"; 
    }
  //★１水タイプの卵
    @GetMapping("/dollyegg") 
    public String DollyEggInfo() {
        return "characters/dollyegg"; 
    }
  //★2水タイプの1進化目
    @GetMapping("/dolly") 
    public String DollyInfo() {
        return "characters/dolly"; 
    }
  //★3水タイプの2進化目
    @GetMapping("/dolphy") 
    public String DolphyInfo() {
        return "characters/dolphy"; 
    }
  //★4水タイプの最終進化
    @GetMapping("/dolphinas") 
    public String DolphinasInfo() {
        return "characters/dolphinas"; 
    }
  //★1草タイプの卵
    @GetMapping("/shiruegg") 
    public String ShiruEggInfo() {
        return "characters/shiruegg"; 
    }
  //★2草タイプの1進化目
    @GetMapping("/shiru") 
    public String ShiruInfo() {
        return "characters/shiru"; 
    }
  //★3草タイプの2進化目
    @GetMapping("/shirufa") 
    public String ShirufaInfo() {
        return "characters/shirufa"; 
    }
  //★4草タイプの最終進化
    @GetMapping("/shirufina") 
    public String ShirufinaInfo() {
        return "characters/shirufina"; 
    }
   //★1光タイプの卵
    @GetMapping("/merryegg") 
    public String MerryEggInfo() {
        return "characters/merryegg"; 
    }
  //★2光タイプの1進化目
    @GetMapping("/merry") 
    public String MerryInfo() {
        return "characters/merry"; 
    }
  //★3光タイプの2進化目
    @GetMapping("/meriru") 
    public String MeriruInfo() {
        return "characters/Meriru"; 
    }
  //★4光タイプの最終進化
    @GetMapping("/merinoa") 
    public String MerinoaInfo() {
        return "characters/merinoa"; 
    }
  //★1闇タイプの卵
    @GetMapping("/robiegg") 
    public String RobiEggInfo() {
        return "characters/robiegg"; 
    }
  //★2闇タイプの1進化目
    @GetMapping("/robi") 
    public String RobiInfo() {
        return "characters/robi"; 
    }
  //★3闇タイプの2進化目
    @GetMapping("/robus") 
    public String RobusInfo() {
        return "characters/robus"; 
    }
  //★4闇タイプの最終進化
    @GetMapping("/robius") 
    public String RobiusInfo() {
        return "characters/robius"; 
    }
}