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
 // XP計算ロジック（ExerciseDataを使う形に修正済み）
    public int getExperiencePoints(ExerciseData exercise) {
        if (exercise == null || exercise.getDifficulty() == null) {
            return 0;
        }
        switch (exercise.getDifficulty()) {
            case "上級":
                return XP_ADVANCED;
            case "中級":
                return XP_INTERMEDIATE;
            case "初級":
                return XP_BEGINNER;
            default:
                return 0;
        }
    }

    public int calculateChipReward(ExerciseData exercise) {
        int baseXp = getExperiencePoints(exercise);
        if (baseXp == XP_ADVANCED) {
            return 6;
        } else if (baseXp == XP_INTERMEDIATE) {
            return 5;
        } else if (baseXp == XP_BEGINNER) {
            return 4; // 初級は2チップに修正済み
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

    // 古いメソッド（互換性）
    public Map<String, Object> generateAiSuggestedMenu(Integer duration, List<String> targetParts, String location, String difficulty) {
        return generateAiSuggestedMenu(duration, targetParts, location, difficulty, null);
    }

    // ★ 改修版: 器具フィルター対応
    public Map<String, Object> generateAiSuggestedMenu(Integer duration, List<String> targetParts, String location, String difficulty, List<String> availableEquipment) {
        Map<String, Object> menu = new LinkedHashMap<>();
        List<String> programList = new ArrayList<>();
        Random random = new Random();

        if (duration == null || duration < 3) duration = 15; 
        if (location == null) location = "gym";
        if (difficulty == null) difficulty = "intermediate";
        
        // 器具リストがnullの場合は、場所からデフォルト設定
        if (availableEquipment == null || availableEquipment.isEmpty()) {
            availableEquipment = new ArrayList<>();
            if ("home".equals(location)) {
                availableEquipment.add("bodyweight");
                availableEquipment.add("dumbbell");
            } else {
                // ジムなら全種
                availableEquipment.add("bodyweight");
                availableEquipment.add("dumbbell");
                availableEquipment.add("barbell");
                availableEquipment.add("machine");
                availableEquipment.add("cable");
                availableEquipment.add("smith");
                availableEquipment.add("pullup_bar");
            }
        }

        // 部位リスト
        List<String> availableParts = new ArrayList<>();
        Map<String, List<ExerciseData>> allExercises = trainingDataService.getFreeWeightExercises();
        
        if (targetParts == null || targetParts.isEmpty()) {
            availableParts.addAll(allExercises.keySet());
        } else {
            availableParts.addAll(targetParts);
        }

        // 候補種目の選定（器具フィルタリング）
        List<ExerciseData> candidateExercises = new ArrayList<>();
        for (String part : availableParts) {
            List<ExerciseData> exercises = allExercises.get(part);
            if (exercises != null) {
                for (ExerciseData ex : exercises) {
                    if (isPlayable(ex, availableEquipment)) {
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

            int sets = 3;
            int reps = 10;
            String weightGuide = "";

            switch (difficulty) {
                case "beginner":
                    sets = 2;
                    reps = 12 + random.nextInt(4);
                    weightGuide = fullName.contains("自重") ? "(自重)" : "(軽め)";
                    break;
                case "advanced":
                    sets = 4;
                    reps = 8 + random.nextInt(3);
                    weightGuide = fullName.contains("自重") ? "(加重または限界まで)" : "(高重量)";
                    break;
                case "intermediate":
                default:
                    sets = 3;
                    reps = 10 + random.nextInt(3);
                    weightGuide = fullName.contains("自重") ? "(自重)" : "(中重量)";
                    break;
            }
            
            if (duration < 10) sets = Math.max(1, sets - 1);

            programList.add((i + 1) + ". " + fullName + ": " + sets + "セット x " + reps + "回 " + weightGuide);
        }

        // 有酸素運動の提案
        boolean addCardio = (duration >= 20 && random.nextBoolean());
        if (addCardio) {
            List<ExerciseData> cardioList = trainingDataService.getCardioExercises();
            if (!cardioList.isEmpty()) {
                List<ExerciseData> filteredCardio = new ArrayList<>();
                for(ExerciseData c : cardioList) {
                    if(isPlayable(c, availableEquipment)) {
                        filteredCardio.add(c);
                    }
                }
                
                if (!filteredCardio.isEmpty()) {
                    ExerciseData cardio = filteredCardio.get(random.nextInt(filteredCardio.size()));
                    int cardioTime = Math.min(10, duration / 4);
                    programList.add("★ 仕上げ: " + cardio.getFullName() + ": " + cardioTime + "分");
                }
            }
        }
        
        menu.put("programList", programList);
        menu.put("targetTime", duration);
        menu.put("restTime", (duration < 10) ? 30 : 60);

        return menu;
    }
    
    // 器具判定ロジック
    private boolean isPlayable(ExerciseData ex, List<String> availableEquipment) {
        String name = ex.getName();
        String equipInfo = ex.getEquipment(); // 例: "ダンベル/バーベル"
        
        // 懸垂・ディップスの特別判定
        if (name.contains("懸垂") || name.contains("チンアップ") || name.contains("ハンギング") || name.contains("ディップス")) {
            return availableEquipment.contains("pullup_bar");
        }
        
        // 通常のマッチング
        if (equipInfo == null) return true; // 情報なしならOKとするか、NGとするか。ここではOK
        
        // データ側の器具文字列を分割してチェック
        // "ダンベル/バーベル" -> どちらかがあればOK
        String[] requiredEquips = equipInfo.split("/");
        for (String req : requiredEquips) {
            if (req.contains("自重") && availableEquipment.contains("bodyweight")) return true;
            if (req.contains("ダンベル") && availableEquipment.contains("dumbbell")) return true;
            if (req.contains("ケトルベル") && (availableEquipment.contains("dumbbell") || availableEquipment.contains("kettlebell"))) return true; // KBはダンベルで代用可
            if (req.contains("バーベル") && availableEquipment.contains("barbell")) return true;
            if (req.contains("マシン") && availableEquipment.contains("machine")) return true;
            if (req.contains("ケーブル") && availableEquipment.contains("cable")) return true;
            if (req.contains("スミス") && availableEquipment.contains("smith")) return true;
            if (req.contains("プレート") && availableEquipment.contains("barbell")) return true; // プレートはバーベルあるならあると仮定
        }
        
        return false;
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

 // ユーザーに報酬を付与するメソッド
 // ユーザーにチップだけ付与する（XPはController側でLevelServiceが処理）
    public int rewardUserWithChips(String username, ExerciseData exercise) {
        int chips = calculateChipReward(exercise);
        userService.addChips(username, chips);
        return chips;
    }
}