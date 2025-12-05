package com.example.demo.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.TrainingLogForm;
import com.example.demo.entity.ExerciseBookmark;
import com.example.demo.entity.MySet;
import com.example.demo.entity.User;
import com.example.demo.repository.BodyWeightRecordRepository;
import com.example.demo.repository.ExerciseBookmarkRepository;
import com.example.demo.repository.MySetRepository;
import com.example.demo.repository.TrainingRecordRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MissionService;
import com.example.demo.service.TrainingDataService;
import com.example.demo.service.TrainingLogicService;
import com.example.demo.service.UserService;

import lombok.Data;

@Controller
@RequestMapping("/training")   // ★追加: クラスレベルで /training を付与
public class TrainingController {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private TrainingRecordRepository trainingRecordRepository;
    @Autowired private MissionService missionService;
    @Autowired private ExerciseBookmarkRepository exerciseBookmarkRepository;
    @Autowired private MySetRepository mySetRepository;
    @Autowired private TrainingDataService trainingDataService;
    @Autowired private TrainingLogicService trainingLogicService;
    @Autowired private BodyWeightRecordRepository bodyWeightRecordRepository;

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) return null;
        return userService.findByUsername(authentication.getName());
    }
    
    @Data
    public static class BatchTrainingLogForm {
        private List<TrainingLogForm> logs;
    }

    /**
     * ミッション1: ランニング → /training/training に遷移
     */
    @GetMapping("/training")
    public String showTrainingOptions(Authentication authentication, Model model) {
        if (getCurrentUser(authentication) == null) return "redirect:/login";
        Map<String, List<String>> simpleFreeWeightMap = trainingDataService.getSimpleFreeWeightExercisesMap();
        List<String> simpleCardioList = trainingDataService.getSimpleCardioExercisesList();
        model.addAttribute("freeWeightExercisesByPart", simpleFreeWeightMap);
        model.addAttribute("freeWeightParts", simpleFreeWeightMap.keySet());
        model.addAttribute("cardioExercises", simpleCardioList);
        return "training/training"; // templates/training/training.html
    }

    @GetMapping("/exercises")
    public String showExerciseList(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";
        model.addAttribute("freeWeightExercisesByPart", trainingDataService.getFreeWeightExercises());
        model.addAttribute("cardioExercises", trainingDataService.getCardioExercises());
        List<String> bookmarkedNames = exerciseBookmarkRepository.findByUserOrderByIdDesc(currentUser)
                .stream().map(ExerciseBookmark::getExerciseName).collect(Collectors.toList());
        model.addAttribute("bookmarkedNames", bookmarkedNames);
        return "training/exercise-list";
    }

    @PostMapping("/start")
    public String startTrainingSession(
            @RequestParam("type") String type,
            @RequestParam(value = "exerciseName", required = false) String exerciseName,
            @RequestParam(value = "aiProposal", required = false) String aiProposal,
            @RequestParam(value = "mySetId", required = false) Long mySetId,
            Authentication authentication,
            Model model) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        String title = "";
        String selectedExercise = "";

        switch (type) {
            case "ai-suggested":
                title = "AIおすすめメニューセッション";
                selectedExercise = "AIおすすめプログラム";
                if (aiProposal != null && !aiProposal.trim().isEmpty()) {
                    List<String> parsedProgram = trainingLogicService.parseAiProposal(aiProposal);
                    model.addAttribute("programList", parsedProgram);
                    model.addAttribute("targetTime", 45);
                    model.addAttribute("restTime", 60);
                } else {
                    Map<String, Object> aiMenu = trainingLogicService.generateAiSuggestedMenu();
                    model.addAttribute("programList", aiMenu.get("programList"));
                    model.addAttribute("targetTime", aiMenu.get("targetTime"));
                    model.addAttribute("restTime", aiMenu.get("restTime"));
                }
                break;
            case "myset":
                if (mySetId != null) {
                    MySet mySet = mySetRepository.findByIdAndUser(mySetId, currentUser);
                    if (mySet != null) {
                        title = "マイセット: " + mySet.getName();
                        selectedExercise = mySet.getName();
                        List<String> exercises = new ArrayList<>();
                        for (int i = 0; i < mySet.getExerciseNames().size(); i++) {
                            exercises.add((i + 1) + ". " + mySet.getExerciseNames().get(i));
                        }
                        model.addAttribute("programList", exercises);
                        model.addAttribute("mySetExercises", mySet.getExerciseNames());
                        model.addAttribute("cardioList", trainingDataService.getSimpleCardioExercisesList());
                        model.addAttribute("targetTime", exercises.size() * 10);
                        model.addAttribute("restTime", 60);
                    }
                }
                break;
            case "free-weight":
            case "cardio":
                if (exerciseName != null && !exerciseName.trim().isEmpty()) {
                    selectedExercise = exerciseName.trim();
                } else {
                    return "redirect:/training/training";
                }
                title = ("free-weight".equals(type) ? "フリーウェイト" : "有酸素運動") + "セッション";
                break;
            default:
                return "redirect:/training/training";
        }
        model.addAttribute("trainingType", type);
        model.addAttribute("trainingTitle", title);
        model.addAttribute("selectedExercise", selectedExercise);
        model.addAttribute("today", LocalDate.now());
        return "training/training-session";
    }

    // 以下の bookmark, myset, saveTrainingRecord, saveBatchTrainingLog, log 関連メソッドは元のまま
}
