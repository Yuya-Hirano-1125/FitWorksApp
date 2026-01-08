package com.example.demo.service;

// ★追加: entryメソッドを静的インポートすると記述がスッキリします
import static java.util.Map.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.ExerciseData;
import com.example.demo.entity.Exercise;
import com.example.demo.repository.ExerciseRepository;

@Service
public class TrainingDataService {

    private final ExerciseRepository exerciseRepository;

    // ★修正: Map.ofEntries を使用して11個以上のペアを定義
    private static final Map<String, String> SPECIAL_READINGS = Map.ofEntries(
        entry("懸垂", "ケンスイ"),
        entry("水中", "スイチュウ"),
        entry("水泳", "スイエイ"),
        entry("踏み台", "フミダイ"),
        entry("縄跳び", "ナワトビ"),
        entry("二重跳び", "ニジュウトビ"),
        entry("Tバー", "ティーバー"),
        entry("Yレイズ", "ワイレイズ"),
        entry("Zプレス", "ゼットプレス"),
        entry("JM", "ジェイエム"),
        entry("HIIT", "ヒット")
    );

    // 部位の表示順序
    private static final List<String> BODY_PART_ORDER = List.of("胸", "背中", "脚", "肩", "腕", "腹筋");

    public TrainingDataService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    // 50音順ソート用のコンパレータ
    private final Comparator<ExerciseData> exerciseComparator = Comparator.comparing(e -> getReading(e.getName()));

    private String getReading(String name) {
        // 特殊な接頭辞が含まれているかチェック
        return SPECIAL_READINGS.entrySet().stream()
                .filter(entry -> name.startsWith(entry.getKey()))
                .map(entry -> entry.getValue() + name.substring(entry.getKey().length()))
                .findFirst()
                .orElse(name); // 特殊ルールがなければそのままの名前を使う
    }

    private ExerciseData convertToDto(Exercise e) {
        return new ExerciseData(
            e.getName(),
            e.getTargetPart(),
            e.getEquipment(),
            e.getDescription(),
            e.getDifficulty()
        );
    }

    public Map<String, List<ExerciseData>> getFreeWeightExercisesByPart() {
        List<Exercise> entities = exerciseRepository.findByType("WEIGHT");

        // ベースとなる順序付きマップを作成
        Map<String, List<ExerciseData>> map = new LinkedHashMap<>();
        BODY_PART_ORDER.forEach(part -> map.put(part, new ArrayList<>()));

        // データを振り分け
        entities.forEach(e -> {
            String group = e.getBodyPartGroup();
            map.computeIfAbsent(group, k -> new ArrayList<>()).add(convertToDto(e));
        });

        // 各リストをソート
        map.values().forEach(list -> list.sort(exerciseComparator));

        return map;
    }

    public Map<String, List<ExerciseData>> getFreeWeightExercises() {
        return getFreeWeightExercisesByPart();
    }

    public List<ExerciseData> getCardioExercises() {
        return exerciseRepository.findByType("CARDIO").stream()
                .map(this::convertToDto)
                .sorted(exerciseComparator)
                .collect(Collectors.toList());
    }

    public Map<String, List<String>> getSimpleFreeWeightExercisesMap() {
        return getFreeWeightExercisesByPart().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(ExerciseData::getName)
                                .collect(Collectors.toList()),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public List<String> getMuscleParts() {
        return new ArrayList<>(getFreeWeightExercisesByPart().keySet());
    }
    
    public List<String> getSimpleCardioExercisesList() {
        return getCardioExercises().stream()
                .map(ExerciseData::getName)
                .collect(Collectors.toList());
    }

    public ExerciseData getExerciseDataByName(String name) {
        Exercise e = exerciseRepository.findByName(name);
        return (e != null) ? convertToDto(e) : null;
    }
    
    public String findPartByExerciseName(String name) {
        Exercise e = exerciseRepository.findByName(name);
        if (e != null) {
            return "CARDIO".equals(e.getType()) ? "有酸素" : e.getBodyPartGroup();
        }
        return "その他";
    }
}