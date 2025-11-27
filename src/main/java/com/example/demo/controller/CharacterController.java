package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/characters")
public class CharacterController {

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
}