package com.example.demo.service;

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

    // コンストラクタでRepositoryを受け取る
    public TrainingDataService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    // 50音順ソート用のコンパレータ
    private final Comparator<ExerciseData> exerciseComparator = Comparator.comparing(e -> getReading(e.getName()));

    // 種目名から読み仮名を取得（ソート用）
    private String getReading(String name) {
        String s = name;
        if (s.startsWith("懸垂")) return "ケンスイ" + s;
        if (s.startsWith("水中")) return "スイチュウ" + s;
        if (s.startsWith("水泳")) return "スイエイ" + s;
        if (s.startsWith("踏み台")) return "フミダイ" + s;
        if (s.startsWith("縄跳び")) return "ナワトビ" + s;
        if (s.startsWith("二重跳び")) return "ニジュウトビ" + s;
        if (s.startsWith("Tバー")) return "ティーバー" + s;
        if (s.startsWith("Yレイズ")) return "ワイレイズ" + s;
        if (s.startsWith("Zプレス")) return "ゼットプレス" + s;
        if (s.startsWith("JM")) return "ジェイエム" + s;
        if (s.startsWith("HIIT")) return "ヒット" + s;
        return s;
    }

    // Entity(DBデータ) -> DTO(表示用データ) 変換メソッド
    private ExerciseData convertToDto(Exercise e) {
        return new ExerciseData(
            e.getName(),
            e.getTargetPart(),
            e.getEquipment(),
            e.getDescription(),
            e.getDifficulty()
        );
    }

    /**
     * DBからWEIGHT種目を取得し、部位ごとにグループ化して返す
     */
    public Map<String, List<ExerciseData>> getFreeWeightExercisesByPart() {
        // DBから "WEIGHT" タイプの全データを取得
        List<Exercise> entities = exerciseRepository.findByType("WEIGHT");
        
        // 部位ごとにリストにまとめるためのMap
        Map<String, List<ExerciseData>> map = new LinkedHashMap<>();
        
        // 表示順序を固定したい場合は、ここで空のリストをputしておく
        String[] order = {"胸", "背中", "脚", "肩", "腕", "腹筋"};
        for (String part : order) {
            map.put(part, new ArrayList<>());
        }

        // 取得したデータを部位ごとに振り分け
        for (Exercise e : entities) {
            String group = e.getBodyPartGroup();
            map.computeIfAbsent(group, k -> new ArrayList<>()).add(convertToDto(e));
        }

        // 各部位リスト内で50音順にソート
        for (List<ExerciseData> list : map.values()) {
            list.sort(exerciseComparator);
        }

        return map;
    }

    /**
     * DBからWEIGHT種目を取得し、部位ごとにグループ化して返す（互換用）
     */
    public Map<String, List<ExerciseData>> getFreeWeightExercises() {
        return getFreeWeightExercisesByPart();
    }

    /**
     * DBからCARDIO種目を取得して返す
     */
    public List<ExerciseData> getCardioExercises() {
        List<Exercise> entities = exerciseRepository.findByType("CARDIO");
        
        return entities.stream()
                .map(this::convertToDto)
                .sorted(exerciseComparator)
                .collect(Collectors.toList());
    }

    // MySet作成フォームなどで使用する簡易Map
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

    // 部位のリストを取得
    public List<String> getMuscleParts() {
        return new ArrayList<>(getFreeWeightExercisesByPart().keySet());
    }
    
    // 有酸素運動の簡易リスト
    public List<String> getSimpleCardioExercisesList() {
        return getCardioExercises().stream()
                .map(ExerciseData::getName)
                .collect(Collectors.toList());
    }

    // 種目名からExerciseDataを取得
    public ExerciseData getExerciseDataByName(String name) {
        Exercise e = exerciseRepository.findByName(name);
        if (e != null) {
            return convertToDto(e);
        }
        return null;
    }
    
    // 種目名から部位を逆引きするメソッド
    public String findPartByExerciseName(String name) {
        Exercise e = exerciseRepository.findByName(name);
        if (e != null) {
            if ("CARDIO".equals(e.getType())) {
                return "有酸素";
            }
            return e.getBodyPartGroup(); 
        }
        return "その他";
    }
}