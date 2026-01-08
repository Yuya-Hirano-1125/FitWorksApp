package com.example.demo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

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

    // --- 定数定義 ---
    private static final int XP_BEGINNER = 300;
    private static final int XP_INTERMEDIATE = 600;
    private static final int XP_ADVANCED = 1000;

    private static final int COIN_ADVANCED = 50;
    private static final int COIN_INTERMEDIATE = 40;
    private static final int COIN_BEGINNER = 30;

    private static final List<String> DEFAULT_HOME_EQUIPMENT = List.of("bodyweight", "dumbbell");
    private static final List<String> DEFAULT_GYM_EQUIPMENT = List.of("bodyweight", "dumbbell", "barbell", "machine", "cable", "smith", "pullup_bar");

    private final Random random = new Random();

    // --- XP / コイン計算ロジック ---

    public int getExperiencePoints(ExerciseData exercise) {
        if (exercise == null || exercise.getDifficulty() == null) {
            return 0;
        }
        return switch (exercise.getDifficulty()) {
            case "上級" -> XP_ADVANCED;
            case "中級" -> XP_INTERMEDIATE;
            case "初級" -> XP_BEGINNER;
            default -> 0;
        };
    }

    public int calculateChipReward(ExerciseData exercise) {
        int baseXp = getExperiencePoints(exercise);
        if (baseXp == XP_ADVANCED) return COIN_ADVANCED;
        if (baseXp == XP_INTERMEDIATE) return COIN_INTERMEDIATE;
        if (baseXp == XP_BEGINNER) return COIN_BEGINNER;
        return 0;
    }

    public int calculateTotalVolumeXp(TrainingLogForm form) {
        if (form.getSetList() == null || form.getSetList().isEmpty()) {
            return calculateVolume(form.getWeight(), form.getReps(), form.getSets());
        }

        double totalVolume = form.getSetList().stream()
                .mapToDouble(d -> calculateVolume(d.getWeight(), d.getReps(), 1))
                .sum();
        return (int) Math.round(totalVolume);
    }

    private int calculateVolume(Double weight, Integer reps, Integer sets) {
        if (weight != null && reps != null && weight > 0 && reps > 0) {
            int s = (sets != null && sets > 0) ? sets : 1;
            return (int) Math.round(weight * reps * s);
        }
        return 0;
    }

    // --- AIメニュー生成ロジック ---

    public Map<String, Object> generateAiSuggestedMenu(Integer duration, List<String> targetParts, String location, String difficulty) {
        return generateAiSuggestedMenu(duration, targetParts, location, difficulty, null);
    }

    public Map<String, Object> generateAiSuggestedMenu(Integer duration, List<String> targetParts, String location, String difficulty, List<String> availableEquipment) {
        // 1. パラメータの正規化
        int validDuration = (duration == null || duration < 3) ? 15 : duration;
        String validLocation = (location == null) ? "gym" : location;
        String validDifficulty = (difficulty == null) ? "intermediate" : difficulty;
        List<String> equipment = resolveEquipment(availableEquipment, validLocation);

        // 2. 候補となる種目を抽出・フィルタリング
        List<ExerciseData> candidateExercises = getFilteredExercises(targetParts, equipment);
        
        // 3. メニュー構成（選定）
        List<String> programList = buildProgramList(candidateExercises, validDuration, validDifficulty);

        // 4. 有酸素運動の追加判定
        addCardioIfNecessary(programList, validDuration, equipment);

        // 5. 結果の構築
        Map<String, Object> menu = new LinkedHashMap<>();
        menu.put("programList", programList);
        menu.put("targetTime", validDuration);
        menu.put("restTime", (validDuration < 10) ? 30 : 60);

        return menu;
    }

    // 装備リストの解決
    private List<String> resolveEquipment(List<String> userEquipment, String location) {
        if (userEquipment != null && !userEquipment.isEmpty()) {
            return userEquipment;
        }
        return "home".equals(location) ? DEFAULT_HOME_EQUIPMENT : DEFAULT_GYM_EQUIPMENT;
    }

    // 種目のフィルタリング
    private List<ExerciseData> getFilteredExercises(List<String> targetParts, List<String> equipment) {
        Map<String, List<ExerciseData>> allExercises = trainingDataService.getFreeWeightExercises();
        List<String> partsToSearch = (targetParts == null || targetParts.isEmpty()) 
                                     ? new ArrayList<>(allExercises.keySet()) 
                                     : targetParts;

        List<ExerciseData> candidates = new ArrayList<>();
        for (String part : partsToSearch) {
            List<ExerciseData> exercises = allExercises.get(part);
            if (exercises != null) {
                exercises.stream()
                    .filter(ex -> isPlayable(ex, equipment))
                    .forEach(candidates::add);
            }
        }
        Collections.shuffle(candidates);
        return candidates;
    }

    // プログラムリストの構築（フォーマット含む）
    private List<String> buildProgramList(List<ExerciseData> candidates, int duration, String difficulty) {
        List<String> programList = new ArrayList<>();
        int exerciseTime = 4; // 1種目あたり4分換算
        int numExercises = Math.max(1, duration / exerciseTime);
        if (duration < 5) numExercises = 1;

        int limit = Math.min(numExercises, candidates.size());
        for (int i = 0; i < limit; i++) {
            ExerciseData ex = candidates.get(i);
            programList.add((i + 1) + ". " + formatExerciseLine(ex, difficulty, duration));
        }
        return programList;
    }

    // 1行分のメニューテキスト生成
    private String formatExerciseLine(ExerciseData ex, String difficulty, int duration) {
        String fullName = ex.getFullName();
        int sets = 3;
        int reps;
        String weightGuide;

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

        return String.format("%s: %dセット x %d回 %s", fullName, sets, reps, weightGuide);
    }

    // 有酸素運動の追加
    private void addCardioIfNecessary(List<String> programList, int duration, List<String> equipment) {
        if (duration >= 20 && random.nextBoolean()) {
            List<ExerciseData> cardioList = trainingDataService.getCardioExercises().stream()
                    .filter(c -> isPlayable(c, equipment))
                    .collect(Collectors.toList());

            if (!cardioList.isEmpty()) {
                ExerciseData cardio = cardioList.get(random.nextInt(cardioList.size()));
                int cardioTime = Math.min(10, duration / 4);
                programList.add("★ 仕上げ: " + cardio.getFullName() + ": " + cardioTime + "分");
            }
        }
    }

    // 器具判定ロジック（リファクタリング版）
    private boolean isPlayable(ExerciseData ex, List<String> availableEquipment) {
        String name = ex.getName();
        String equipInfo = ex.getEquipment(); // 例: "ダンベル/バーベル"

        // 特定種目のための特別ルール（Setを使って高速化）
        Set<String> pullUpKeywords = Set.of("懸垂", "チンアップ", "ハンギング", "ディップス");
        if (pullUpKeywords.stream().anyMatch(name::contains)) {
            return availableEquipment.contains("pullup_bar");
        }

        if (equipInfo == null) return true;

        // DBの文字列表記と、availableEquipmentのキーのマッピング
        // これも定数化したほうが良いですが、ここではロジック内に留めます
        String[] requiredEquips = equipInfo.split("/");
        for (String req : requiredEquips) {
            if (checkEquipmentMatch(req, availableEquipment)) {
                return true; // どれか一つでも満たせばOK
            }
        }
        return false;
    }

    // 個別の器具マッチング
    private boolean checkEquipmentMatch(String req, List<String> available) {
        if (req.contains("自重") && available.contains("bodyweight")) return true;
        if (req.contains("ダンベル") && available.contains("dumbbell")) return true;
        // KBはダンベルで代用可とするロジック
        if (req.contains("ケトルベル") && (available.contains("dumbbell") || available.contains("kettlebell"))) return true;
        if (req.contains("バーベル") && available.contains("barbell")) return true;
        if (req.contains("マシン") && available.contains("machine")) return true;
        if (req.contains("ケーブル") && available.contains("cable")) return true;
        if (req.contains("スミス") && available.contains("smith")) return true;
        // プレートはバーベルがあるならあるとみなす
        if (req.contains("プレート") && available.contains("barbell")) return true;
        return false;
    }
    
    // --- その他ユーティリティ ---

    public List<String> parseAiProposal(String proposalText) {
        if (proposalText == null || proposalText.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(proposalText.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> line.matches(".*\\d+.*") || 
                                Set.of("セット", "回", "分", "・").stream().anyMatch(line::contains) ||
                                line.matches("^[0-9]+\\..*"))
                .map(line -> line.replaceAll("<[^>]*>", "")) // HTMLタグ除去
                .collect(Collectors.toList());
    }

    public String selectCareExercise(String symptom) {
        if (symptom == null) return "深呼吸";
        // Mapを使って分岐を整理
        Map<String, String> careMap = Map.of(
            "目", "ホットアイケア", "眼", "ホットアイケア",
            "肩", "キャット＆カウ", "首", "キャット＆カウ",
            "腰", "フォームローラー(背中)",
            "足", "動的ストレッチ(股関節)", "脚", "動的ストレッチ(股関節)"
        );
        
        return careMap.entrySet().stream()
                .filter(e -> symptom.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("ウォーキング");
    }

    public int rewardUserWithChips(String username, ExerciseData exercise) {
        int chips = calculateChipReward(exercise);
        userService.addChips(username, chips);
        return chips;
    }
}