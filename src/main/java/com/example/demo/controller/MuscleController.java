package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.User;
import com.example.demo.entity.UserMuscle;
import com.example.demo.service.MuscleService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/muscles")
public class MuscleController {

    private final MuscleService muscleService;
    private final UserService userService;

    public MuscleController(MuscleService muscleService, UserService userService) {
        this.muscleService = muscleService;
        this.userService = userService;
    }

    @GetMapping
    public String index(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        List<UserMuscle> muscles = muscleService.getUserMuscles(user);
        model.addAttribute("muscles", muscles);
        return "muscles/muscle-status";
    }

    @PostMapping("/rename")
    public String renameMuscle(@RequestParam("id") Long id, 
                               @RequestParam("newName") String newName, 
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(principal.getName());
        muscleService.updateMuscleName(id, user, newName);
        redirectAttributes.addFlashAttribute("successMessage", "名前を変更しました！");
        return "redirect:/muscles";
    }
}