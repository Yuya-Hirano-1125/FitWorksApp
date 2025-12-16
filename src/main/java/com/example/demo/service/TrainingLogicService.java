package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collections;
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
    @Autowired
    private UserService userService;


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
    
    public int calculateChipReward(String exerciseFullName) {
        int baseXp = getExperiencePoints(exerciseFullName);
        if (baseXp == XP_ADVANCED) {
            return 5;
        } else if (baseXp == XP_INTERMEDIATE) {
            return 3;
        } else if (baseXp == XP_BEGINNER) {
            return 1;
        }
        return 0;
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

    // 元のAIメニュー生成ロジック（互換性用）
    public Map<String, Object> generateAiSuggestedMenu() {
        return generateAiSuggestedMenu(15, null, "gym", "intermediate");
    }
    
    public Map<String, Object> generateAiSuggestedMenu(Integer duration, List<String> targetParts) {
        return generateAiSuggestedMenu(duration, targetParts, "gym", "intermediate");
    }

    // ★ 改修版: 時間・部位・場所・難易度に基づくAIメニュー生成ロジック
    public Map<String, Object> generateAiSuggestedMenu(Integer duration, List<String> targetParts, String location, String difficulty) {
        Map<String, Object> menu = new LinkedHashMap<>();
        List<String> programList = new ArrayList<>();
        Random random = new Random();

        if (duration == null || duration < 3) duration = 15; 
        if (location == null) location = "gym";
        if (difficulty == null) difficulty = "intermediate";

        // 部位リスト
        List<String> availableParts = new ArrayList<>();
        Map<String, List<ExerciseData>> allExercises = trainingDataService.getFreeWeightExercises();
        
        if (targetParts == null || targetParts.isEmpty()) {
            availableParts.addAll(allExercises.keySet());
        } else {
            availableParts.addAll(targetParts);
        }

        // 候補種目の選定（場所によるフィルタリング）
        List<ExerciseData> candidateExercises = new ArrayList<>();
        for (String part : availableParts) {
            List<ExerciseData> exercises = allExercises.get(part);
            if (exercises != null) {
                for (ExerciseData ex : exercises) {
                    if ("home".equals(location)) {
                        // 自宅の場合: マシン、バーベル、ケーブル、スミスなどを除外
                        // 逆に 自重、ダンベル、チューブ を優先（ここでは除外ロジックで実装）
                        String name = ex.getFullName();
                        if (!name.contains("バーベル") && !name.contains("マシン") && 
                            !name.contains("ケーブル") && !name.contains("スミス") &&
                            !name.contains("プレスダウン") && !name.contains("ラットプル")) {
                            candidateExercises.add(ex);
                        }
                    } else {
                        // ジムの場合は全て対象
                        candidateExercises.add(ex);
                    }
                }
            }
        }
        Collections.shuffle(candidateExercises);

        // 種目数の計算
        int exerciseTime = 4;
        int numExercises = Math.max(1, duration / exerciseTime);
        if (duration < 5) numExercises = 1;

        // 種目リスト作成
        List<ExerciseData> selectedExercises = new ArrayList<>();
        for (int i = 0; i < numExercises && !candidateExercises.isEmpty(); i++) {
            if (i < candidateExercises.size()) {
                selectedExercises.add(candidateExercises.get(i));
            } else {
                break;
            }
        }

        // メニュー構成
        for (int i = 0; i < selectedExercises.size(); i++) {
            ExerciseData ex = selectedExercises.get(i);
            String fullName = ex.getFullName();

            // 難易度に応じた設定
            int sets = 3;
            int reps = 10;
            String weightGuide = "";

            switch (difficulty) {
                case "beginner":
                    sets = 2;
                    reps = 12 + random.nextInt(4); // 12-15回
                    if (fullName.contains("自重")) {
                        weightGuide = "(自重)";
                    } else {
                        weightGuide = "(軽め)";
                    }
                    break;
                case "advanced":
                    sets = 4;
                    reps = 8 + random.nextInt(3); // 8-10回
                    if (fullName.contains("自重")) {
                        weightGuide = "(加重または限界まで)";
                    } else {
                        weightGuide = "(高重量)";
                    }
                    break;
                case "intermediate":
                default:
                    sets = 3;
                    reps = 10 + random.nextInt(3); // 10-12回
                    if (fullName.contains("自重")) {
                        weightGuide = "(自重)";
                    } else {
                        weightGuide = "(中重量)";
                    }
                    break;
            }
            
            // 時間が極端に短い場合はセット数を減らす
            if (duration < 10) sets = Math.max(1, sets - 1);

            programList.add((i + 1) + ". " + fullName + ": " + sets + "セット x " + reps + "回 " + weightGuide);
        }

        // 有酸素運動の提案 (時間が20分以上の場合)
        boolean addCardio = (duration >= 20 && random.nextBoolean());
        if (addCardio) {
            List<ExerciseData> cardioList = trainingDataService.getCardioExercises();
            if (!cardioList.isEmpty()) {
                ExerciseData cardio = cardioList.get(random.nextInt(cardioList.size()));
                int cardioTime = Math.min(10, duration / 4);
                programList.add("★ 仕上げ: " + cardio.getFullName() + ": " + cardioTime + "分");
            }
        }
        
        menu.put("programList", programList);
        menu.put("targetTime", duration);
        menu.put("restTime", (duration < 10) ? 30 : 60);

        return menu;
    }

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

    public String selectCareExercise(String symptom) {
        if (symptom == null) return "深呼吸";
        if (symptom.contains("目") || symptom.contains("眼")) {
            return "ホットアイケア";
        } else if (symptom.contains("肩") || symptom.contains("首")) {
            return "キャット＆カウ";
        } else if (symptom.contains("腰")) {
            return "フォームローラー(背中)";
        } else if (symptom.contains("足") || symptom.contains("脚")) {
            return "動的ストレッチ(股関節)";
        } else {
            return "ウォーキング"; 
        }
    }

    public int rewardUserWithChips(String username, String exerciseFullName) {
        int chips = calculateChipReward(exerciseFullName);
        userService.addChips(username, chips); 
        return chips;
    }
}