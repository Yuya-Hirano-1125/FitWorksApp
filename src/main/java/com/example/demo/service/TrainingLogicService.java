package com.example.demo.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ExerciseData;
import com.example.demo.dto.TrainingLogForm;

@Service
public class TrainingLogicService {

    @Autowired
    private TrainingDataService trainingDataService;

    // 定数
    private static final int XP_BEGINNER = 300;
    private static final int XP_INTERMEDIATE = 500;
    private static final int XP_ADVANCED = 1000;

    // XP計算ロジック
    public int getExperiencePoints(String exerciseFullName) {
        if (exerciseFullName == null || exerciseFullName.trim().isEmpty()) {
            return 0;
        }
        if (exerciseFullName.contains("(上級)")) {
            return XP_ADVANCED;
        } else if (exerciseFullName.contains("(中級)")) {
            return XP_INTERMEDIATE;
        } else if (exerciseFullName.contains("(初級)")) {
            return XP_BEGINNER;
        }
        return XP_BEGINNER;
    }

    public int calculateTotalVolumeXp(TrainingLogForm form) {
        if (form.getSetList() == null || form.getSetList().isEmpty()) {
            Double singleWeight = form.getWeight();
            Integer singleReps = form.getReps();
            Integer sets = form.getSets();

            if (singleWeight != null && singleReps != null && singleWeight > 0 && singleReps > 0 && sets != null && sets > 0) {
                return (int) Math.round(singleWeight * singleReps * sets);
            }
            return 0;
        }

        double totalVolume = 0;
        for (TrainingLogForm.SetDetail detail : form.getSetList()) {
            Double weight = detail.getWeight();
            Integer reps = detail.getReps();
            if (weight != null && reps != null && weight > 0 && reps > 0) {
                totalVolume += weight * reps;
            }
        }
        return (int) Math.round(totalVolume);
    }

    // AIメニュー生成ロジック
    public Map<String, Object> generateAiSuggestedMenu() {
        Map<String, Object> menu = new LinkedHashMap<>();
        List<String> programList = new ArrayList<>();
        Random random = new Random();

        Map<String, List<ExerciseData>> dataMap = trainingDataService.getFreeWeightExercises();
        List<String> mainParts = new ArrayList<>(dataMap.keySet()); // 部位リスト
        String selectedPart = mainParts.get(random.nextInt(mainParts.size()));

        List<ExerciseData> exercises = dataMap.get(selectedPart);
        if (exercises == null || exercises.isEmpty()) {
            programList.add("1. スクワット (中級): 3セット x 10回");
            menu.put("programList", programList);
            return menu;
        }

        List<ExerciseData> availableExercises = new ArrayList<>(exercises);
        List<ExerciseData> selectedExercises = new ArrayList<>();

        int numExercises = 3 + random.nextInt(2); // 3-4種目

        for (int i = 0; i < numExercises && !availableExercises.isEmpty(); i++) {
            int index = random.nextInt(availableExercises.size());
            selectedExercises.add(availableExercises.remove(index));
        }

        for (int i = 0; i < selectedExercises.size(); i++) {
            ExerciseData ex = selectedExercises.get(i);
            String fullName = ex.getFullName();

            int sets = 3 + random.nextInt(2);
            int reps = 8 + random.nextInt(5);
            int baseWeight = 30;
            int difficultyAdjustment = getExperiencePoints(fullName) / 30;
            int weight = baseWeight + random.nextInt(50) + difficultyAdjustment;

            programList.add((i + 1) + ". " + fullName + ": " + sets + "セット x " + reps + "回 (" + weight + "kg)");
        }

        if (random.nextInt(10) < 4) { // 40%で有酸素追加
            List<ExerciseData> cardioList = trainingDataService.getCardioExercises();
            ExerciseData cardio = cardioList.get(random.nextInt(cardioList.size()));
            int duration = 15 + random.nextInt(16);
            programList.add((selectedExercises.size() + 1) + ". " + cardio.getFullName() + ": " + duration + "分");
        }

        int totalTime = 40 + random.nextInt(31);
        int restTime = 45 + random.nextInt(31);

        menu.put("programList", programList);
        menu.put("targetTime", totalTime);
        menu.put("restTime", restTime);

        return menu;
    }

    // AI提案テキスト解析
    public List<String> parseAiProposal(String proposalText) {
        List<String> programList = new ArrayList<>();
        if (proposalText == null || proposalText.trim().isEmpty()) {
            return programList;
        }
        String[] lines = proposalText.split("\n");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty() &&
                (trimmedLine.matches(".*\\d+.*") ||
                 trimmedLine.contains("セット") ||
                 trimmedLine.contains("回") ||
                 trimmedLine.contains("分") ||
                 trimmedLine.contains("・") ||
                 trimmedLine.matches("^[0-9]+\\..*")
                )) {
                String cleanLine = trimmedLine.replaceAll("<[^>]*>", "");
                programList.add(cleanLine);
            }
        }
        if (programList.isEmpty()) {
            programList.add("AI提案内容: " + proposalText);
        }
        return programList;
    }

    // ★追加: 症状に合わせてケア種目を選択するメソッド
    public String selectCareExercise(String symptom) {
        if (symptom == null) return "深呼吸";
        
        if (symptom.contains("目") || symptom.contains("眼")) {
            return "ホットアイケア";
        } else if (symptom.contains("肩") || symptom.contains("首")) {
            return "キャット＆カウ"; // 背骨周りとして推奨
        } else if (symptom.contains("腰")) {
            return "フォームローラー(背中)";
        } else if (symptom.contains("足") || symptom.contains("脚")) {
            return "動的ストレッチ(股関節)";
        } else {
            // デフォルト
            return "ウォーキング"; 
        }
    }
}