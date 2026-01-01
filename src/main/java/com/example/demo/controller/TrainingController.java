package com.example.demo.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.DailyChartData;
import com.example.demo.dto.ExerciseData;
import com.example.demo.dto.TrainingLogForm;
import com.example.demo.entity.BodyWeightRecord;
import com.example.demo.entity.ExerciseBookmark;
import com.example.demo.entity.MySet;
import com.example.demo.entity.TrainingRecord;
import com.example.demo.entity.User;
import com.example.demo.repository.BodyWeightRecordRepository;
import com.example.demo.repository.ExerciseBookmarkRepository;
import com.example.demo.repository.MySetRepository;
import com.example.demo.repository.TrainingRecordRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AICoachService;
import com.example.demo.service.LevelService;
import com.example.demo.service.MissionService;
import com.example.demo.service.MuscleService;
import com.example.demo.service.TrainingDataService;
import com.example.demo.service.TrainingLogicService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class TrainingController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final TrainingRecordRepository trainingRecordRepository;
    private final MissionService missionService;
    private final ExerciseBookmarkRepository exerciseBookmarkRepository;
    private final MySetRepository mySetRepository;
    private final TrainingDataService trainingDataService;
    private final TrainingLogicService trainingLogicService;
    private final BodyWeightRecordRepository bodyWeightRecordRepository;
    private final AICoachService aiCoachService;
    private final MuscleService muscleService;

    @Autowired
    private LevelService levelService;

    @Autowired
    public TrainingController(UserService userService,
                              UserRepository userRepository,
                              TrainingRecordRepository trainingRecordRepository,
                              MissionService missionService,
                              ExerciseBookmarkRepository exerciseBookmarkRepository,
                              MySetRepository mySetRepository,
                              TrainingDataService trainingDataService,
                              TrainingLogicService trainingLogicService,
                              BodyWeightRecordRepository bodyWeightRecordRepository,
                              AICoachService aiCoachService,
                              MuscleService muscleService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.trainingRecordRepository = trainingRecordRepository;
        this.missionService = missionService;
        this.exerciseBookmarkRepository = exerciseBookmarkRepository;
        this.mySetRepository = mySetRepository;
        this.trainingDataService = trainingDataService;
        this.trainingLogicService = trainingLogicService;
        this.bodyWeightRecordRepository = bodyWeightRecordRepository;
        this.aiCoachService = aiCoachService;
        this.muscleService = muscleService;
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) return null;
        return userService.findByUsername(authentication.getName());
    }
    
    public static class BatchTrainingLogForm {
        private List<TrainingLogForm> logs;
        public List<TrainingLogForm> getLogs() { return logs; }
        public void setLogs(List<TrainingLogForm> logs) { this.logs = logs; }
    }

    @GetMapping("/supplement-guide")
    public String showSupplementGuide(Authentication authentication) {
        if (getCurrentUser(authentication) == null) return "redirect:/login";
        return "misc/supplement-guide";
    }
    
    @GetMapping("/training/care")
    public String showCareMenu(Authentication authentication) {
        if (getCurrentUser(authentication) == null) return "redirect:/login";
        return "training/care-menu";
    }

    @PostMapping("/training/care/advice")
    public String getCareAdvice(
            @RequestParam("symptom") String symptom,
            Authentication authentication,
            Model model) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        String aiAdvice = null;
        ExerciseData suggestedExercise = null;

        try {
            String recommendedExerciseName = trainingLogicService.selectCareExercise(symptom);
            aiAdvice = aiCoachService.generateCareAdvice(currentUser, symptom, recommendedExerciseName);
            if (recommendedExerciseName != null) {
                suggestedExercise = trainingDataService.getExerciseDataByName(recommendedExerciseName);
            }
        } catch (Exception e) {
            System.err.println("AI Care Advice Error: " + e.getMessage());
            aiAdvice = "現在AIサービスが混み合っています。ゆっくり深呼吸してリラックスしましょう。";
        }

        model.addAttribute("symptom", symptom);
        model.addAttribute("aiAdvice", aiAdvice);
        model.addAttribute("suggestedExercise", suggestedExercise); 
        
        TrainingLogForm logForm = new TrainingLogForm();
        logForm.setRecordDate(LocalDate.now());
        logForm.setType("CARE"); 
        if(suggestedExercise != null) {
            logForm.setExerciseName(suggestedExercise.getFullName());
            logForm.setSets(1);
            logForm.setReps(10); 
            logForm.setWeight(0.0);
        } else {
            logForm.setExerciseName(symptom + "ケア");
            logForm.setSets(1);
            logForm.setReps(1);
            logForm.setWeight(0.0);
        }
        model.addAttribute("trainingLogForm", logForm);
        return "training/care-result";
    }

    @GetMapping("/training")
    public String showTrainingOptions(
            Authentication authentication,
            @RequestParam(value = "preselectType", required = false) String preselectType,
            @RequestParam(value = "preselectExercise", required = false) String preselectExercise,
            Model model) {
        
        if (getCurrentUser(authentication) == null) return "redirect:/login";
        
        model.addAttribute("freeWeightParts", trainingDataService.getMuscleParts());
        model.addAttribute("freeWeightExercisesByPart", trainingDataService.getFreeWeightExercisesByPart());
        model.addAttribute("cardioExercises", trainingDataService.getCardioExercises());

        // 自動選択用の情報をモデルにセット
        if (preselectType != null && preselectExercise != null) {
            model.addAttribute("preselectType", preselectType);
            model.addAttribute("preselectExercise", preselectExercise);

            // フリーウェイトの場合、種目名から部位を特定して渡す
            if ("free-weight".equals(preselectType)) {
                String part = trainingDataService.findPartByExerciseName(preselectExercise);
                model.addAttribute("preselectPart", part);
            }
        }

        return "training/training";
    }

    @GetMapping("/training-list")
    public String showTrainingList(Authentication authentication, Model model) {
        if (getCurrentUser(authentication) == null) return "redirect:/login";
        model.addAttribute("freeWeightParts", trainingDataService.getMuscleParts());
        model.addAttribute("freeWeightExercisesByPart", trainingDataService.getFreeWeightExercisesByPart());
        return "training/training-list";
    }

    @GetMapping("/training/exercises")
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

    @PostMapping("/training/start")
    public String startTrainingSession(
            @RequestParam("type") String type,
            @RequestParam(value = "exerciseName", required = false) List<String> exerciseNames, 
            @RequestParam(value = "aiProposal", required = false) String aiProposal,
            @RequestParam(value = "mySetId", required = false) Long mySetId,
            @RequestParam(value = "duration", required = false, defaultValue = "15") Integer duration,
            @RequestParam(value = "parts", required = false) List<String> parts,
            @RequestParam(value = "location", required = false, defaultValue = "gym") String location,
            @RequestParam(value = "difficulty", required = false, defaultValue = "intermediate") String difficulty,
            @RequestParam(value = "equipment", required = false) List<String> equipment,
            Authentication authentication,
            Model model) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        String title = "";
        String selectedExercise = "";
        List<String> finalProgramList = new ArrayList<>();

        switch (type) {
            case "ai-suggested":
                String locLabel = "gym".equals(location) ? "ジム" : "自宅";
                String diffLabel = "beginner".equals(difficulty) ? "(初級)" : "advanced".equals(difficulty) ? "(上級)" : "";
                
                title = "AIメニュー: " + locLabel + " " + duration + "分 " + diffLabel;
                selectedExercise = "AIおすすめプログラム";
                
                if (aiProposal != null && !aiProposal.trim().isEmpty()) {
                    finalProgramList = trainingLogicService.parseAiProposal(aiProposal);
                    model.addAttribute("targetTime", 45);
                    model.addAttribute("restTime", 60);
                } else {
                    Map<String, Object> aiMenu = trainingLogicService.generateAiSuggestedMenu(duration, parts, location, difficulty, equipment);
                    finalProgramList = (List<String>) aiMenu.get("programList");
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
                        for (int i = 0; i < mySet.getExerciseNames().size(); i++) {
                            finalProgramList.add((i + 1) + ". " + mySet.getExerciseNames().get(i));
                        }
                        model.addAttribute("mySetExercises", mySet.getExerciseNames());
                        model.addAttribute("cardioList", trainingDataService.getSimpleCardioExercisesList());
                        model.addAttribute("targetTime", finalProgramList.size() * 10);
                        model.addAttribute("restTime", 60);
                    }
                }
                break;
            case "free-weight":
            case "cardio":
                if (exerciseNames != null && !exerciseNames.isEmpty()) {
                    selectedExercise = exerciseNames.get(0);
                    if (exerciseNames.size() > 1) {
                         for (int i = 0; i < exerciseNames.size(); i++) {
                            finalProgramList.add((i + 1) + ". " + exerciseNames.get(i));
                        }
                        title = "カスタムメニュー (" + exerciseNames.size() + "種目)";
                    } else {
                        title = ("free-weight".equals(type) ? "フリーウェイト" : "有酸素運動") + "セッション";
                    }
                } else {
                    return "redirect:/training-list";
                }
                break;
            default:
                return "redirect:/training";
        }

        model.addAttribute("trainingType", type);
        model.addAttribute("trainingTitle", title);
        model.addAttribute("selectedExercise", selectedExercise);
        model.addAttribute("programList", finalProgramList);
        model.addAttribute("today", LocalDate.now());
        return "training/training-session";
    }

    @GetMapping("/training/bookmarks")
    public String showBookmarkList(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";
        
        List<ExerciseBookmark> bookmarks = exerciseBookmarkRepository.findByUserOrderByIdDesc(currentUser);
        
        List<Map<String, Object>> displayList = new ArrayList<>();
        
        for (ExerciseBookmark bm : bookmarks) {
            ExerciseData data = trainingDataService.getExerciseDataByName(bm.getExerciseName());
            
            if (data == null) {
                data = new ExerciseData(
                    bm.getExerciseName(),
                    "不明",
                    "不明",
                    "詳細データが見つかりませんでした。",
                    "不明"
                );
            }

            Map<String, Object> item = new java.util.HashMap<>();
            item.put("bookmark", bm);
            item.put("data", data);
            displayList.add(item);
        }
        
        model.addAttribute("bookmarkList", displayList);
        return "training/bookmark-list";
    }

    @PostMapping("/training/bookmark/toggle")
    public String toggleBookmark(
            @RequestParam("exerciseName") String exerciseName,
            @RequestParam("type") String type,
            @RequestParam(value = "redirectUrl", defaultValue = "/training/exercises") String redirectUrl,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";
        Optional<ExerciseBookmark> existing = exerciseBookmarkRepository.findByUserAndExerciseName(currentUser, exerciseName);
        if (existing.isPresent()) {
            exerciseBookmarkRepository.delete(existing.get());
            redirectAttributes.addFlashAttribute("message", "「" + exerciseName + "」のブックマークを解除しました。");
        } else {
            ExerciseBookmark bookmark = new ExerciseBookmark(currentUser, exerciseName, type);
            exerciseBookmarkRepository.save(bookmark);
            redirectAttributes.addFlashAttribute("successMessage", "「" + exerciseName + "」をブックマークしました！");
        }
        return "redirect:" + redirectUrl;
    }
    
    @PostMapping("/training/api/bookmark/toggle")
    @ResponseBody
    public Map<String, Object> toggleBookmarkApi(
            @RequestParam("exerciseName") String exerciseName,
            @RequestParam("type") String type,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        Map<String, Object> response = new HashMap<>();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "ログインしてください。");
            return response;
        }

        ExerciseBookmark existing = exerciseBookmarkRepository.findByUserAndExerciseName(currentUser, exerciseName).orElse(null);
        
        boolean isBookmarked;

        if (existing != null) {
            exerciseBookmarkRepository.delete(existing);
            isBookmarked = false;
            response.put("message", exerciseName + " のブックマークを解除しました。");
        } else {
            ExerciseBookmark newBookmark = new ExerciseBookmark();
            newBookmark.setUser(currentUser);
            newBookmark.setExerciseName(exerciseName);
            newBookmark.setType(type);
            exerciseBookmarkRepository.save(newBookmark);
            isBookmarked = true;
            response.put("message", exerciseName + " をブックマークしました！");
        }

        response.put("success", true);
        response.put("isBookmarked", isBookmarked);
        return response;
    }

    @GetMapping("/training/mysets")
    public String showMySetList(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";
        
        List<MySet> mySets = mySetRepository.findByUserOrderByIdDesc(currentUser);
        model.addAttribute("mySets", mySets);

        Map<String, String> exerciseToPartMap = new HashMap<>();

        Map<String, List<ExerciseData>> freeWeightMap = trainingDataService.getFreeWeightExercisesByPart();
        
        for (Map.Entry<String, List<ExerciseData>> entry : freeWeightMap.entrySet()) {
            String part = entry.getKey();
            for (ExerciseData exercise : entry.getValue()) {
                exerciseToPartMap.put(exercise.getName(), part);
            }
        }

        List<ExerciseData> cardioList = trainingDataService.getCardioExercises();
        for (ExerciseData cardio : cardioList) {
            exerciseToPartMap.put(cardio.getName(), "有酸素運動");
        }

        model.addAttribute("exerciseCategoryMap", exerciseToPartMap);

        return "training/myset-list";
    }

    @GetMapping("/training/mysets/new")
    public String showCreateMySetForm(Model model, Authentication authentication) {
        if (getCurrentUser(authentication) == null) return "redirect:/login";

        Map<String, List<ExerciseData>> allExercisesMap = new LinkedHashMap<>();
        allExercisesMap.putAll(trainingDataService.getFreeWeightExercisesByPart());

        List<ExerciseData> cardioList = trainingDataService.getCardioExercises();
        allExercisesMap.put("有酸素運動", cardioList);
        
        List<String> allParts = new ArrayList<>(trainingDataService.getMuscleParts());
        if (!allParts.contains("有酸素運動")) {
            allParts.add("有酸素運動");
        }

        model.addAttribute("exercisesByPart", allExercisesMap);
        model.addAttribute("parts", allParts);
        
        return "training/myset-form";
    }

    @PostMapping("/training/mysets/create")
    public String createMySet(
            @RequestParam("name") String name,
            @RequestParam(value = "exerciseNames", required = false) List<String> exerciseNames,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        if (exerciseNames == null || exerciseNames.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "種目を少なくとも1つ追加してください。");
            return "redirect:/training/mysets/new";
        }

        MySet mySet = new MySet();
        mySet.setName(name);
        mySet.setExerciseNames(exerciseNames);
        mySet.setUser(currentUser);
        
        mySetRepository.save(mySet);

        redirectAttributes.addFlashAttribute("successMessage", "マイセット「" + name + "」を作成しました！");
        return "redirect:/training/mysets";
    }

    @PostMapping("/training/mysets/delete/{id}")
    public String deleteMySet(@PathVariable("id") Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";
        
        MySet target = mySetRepository.findByIdAndUser(id, currentUser);
        if (target != null) {
            mySetRepository.delete(target);
            redirectAttributes.addFlashAttribute("message", "マイセットを削除しました。");
        }
        return "redirect:/training/mysets";
    }
    
    /**
     * 新しいトレーニング記録モーダルからの保存処理（統合版）
     */
    @PostMapping("/training/log/save")
    public String saveTrainingLog(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("type") String type,
            @RequestParam("exerciseName") String exerciseName,
            // リストで受け取る
            @RequestParam(value = "weights", required = false) List<Double> weights,
            @RequestParam(value = "repsList", required = false) List<Integer> repsList,
            // ★復活: 簡易フォームのセット数を受け取るために必要です
            @RequestParam(value = "sets", required = false, defaultValue = "1") Integer sets,
            @RequestParam(value = "duration", required = false) Integer duration,
            @RequestParam(value = "distance", required = false) Double distance,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        try {
            int savedCount = 0;
            double totalVolume = 0; // XP計算用の合計負荷

            // ■ ウェイトトレーニングの場合
            if ("WEIGHT".equals(type) && weights != null && !weights.isEmpty()) {
                
                // ★簡易フォーム対応ロジック
                // 「重量リストが1つ」かつ「セット数が2以上」の場合は、その内容を指定セット数分繰り返す
                int loopCount = weights.size();
                boolean isSimpleMode = (weights.size() == 1 && sets != null && sets > 1);
                
                if (isSimpleMode) {
                    loopCount = sets;
                }

                for (int i = 0; i < loopCount; i++) {
                    // SimpleModeなら常に0番目（入力された1つの値）を使う。そうでなければi番目を使う。
                    Double w = weights.get(isSimpleMode ? 0 : i);
                    
                    // 回数の取得（安全策）
                    Integer r = 0;
                    if (isSimpleMode) {
                        r = (repsList != null && !repsList.isEmpty()) ? repsList.get(0) : 0;
                    } else {
                        r = (repsList != null && i < repsList.size()) ? repsList.get(i) : 0;
                    }

                    if (w != null) {
                        TrainingRecord record = new TrainingRecord();
                        record.setUser(currentUser);
                        record.setRecordDate(date);
                        record.setExerciseName(exerciseName);
                        record.setType("WEIGHT");
                        record.setWeight(w);
                        record.setReps(r);
                        record.setSets(i + 1); // セット番号として保存
                        
                        trainingRecordRepository.save(record);
                        
                        savedCount++;
                        totalVolume += (w * r);
                    }
                }
            } 
            // ■ 有酸素運動の場合
            else if ("CARDIO".equals(type)) {
                TrainingRecord record = new TrainingRecord();
                record.setUser(currentUser);
                record.setRecordDate(date);
                record.setExerciseName(exerciseName);
                record.setType("CARDIO");
                record.setCardioType(exerciseName);
                record.setDurationMinutes(duration);
                record.setDistanceKm(distance);
                
                trainingRecordRepository.save(record);
                savedCount = 1;
            }

            // --- ゲーミフィケーション処理 (XP, チップ) ---
            if (savedCount > 0) {
                ExerciseData exerciseData = trainingDataService.getExerciseDataByName(exerciseName);
                int earnedXP = 0;
                int muscleXp = 0;
                String targetPart = "その他";

                if (exerciseData != null) {
                    int baseDifficultyXp = trainingLogicService.getExperiencePoints(exerciseData);
                    int additionalXp = 0;

                    if ("WEIGHT".equals(type)) {
                        additionalXp = (int) (totalVolume / 100);
                        targetPart = trainingDataService.findPartByExerciseName(exerciseName);
                    } else if ("CARDIO".equals(type) && duration != null) {
                        additionalXp = duration;
                        targetPart = "有酸素";
                    }

                    earnedXP = baseDifficultyXp + additionalXp;
                    muscleXp = Math.max(10, earnedXP / 2);
                } else {
                    earnedXP = 10;
                    muscleXp = 5;
                }

                // レベルアップ・経験値付与
                levelService.addXpAndCheckLevelUp(currentUser, earnedXP);
                if (targetPart != null) {
                    String levelUpMsg = muscleService.addExperience(currentUser, targetPart, muscleXp);
                    if (levelUpMsg != null) redirectAttributes.addFlashAttribute("muscleLevelUpMessage", levelUpMsg);
                }
                userRepository.save(currentUser);

                // チップ付与
                int earnedChips = (exerciseData != null) ? trainingLogicService.calculateChipReward(exerciseData) : 0;
                if (earnedChips > 0) userService.addChips(currentUser.getUsername(), earnedChips);

                // メッセージ作成
                String successMsg = date.toString() + " 「" + exerciseName + "」を" + savedCount + "セット記録しました！";
                if (earnedChips > 0) successMsg += " (" + earnedXP + " XP, " + earnedChips + " チップ)";
                else successMsg += " (" + earnedXP + " XP)";
                
                redirectAttributes.addFlashAttribute("successMessage", successMsg);
                
                // ミッション更新 & AIアドバイス
                missionService.updateMissionProgress(currentUser.getId(), "TRAINING_LOG");
                try {
                    String advice = aiCoachService.generateTrainingAdvice(currentUser, exerciseName);
                    redirectAttributes.addFlashAttribute("aiAdvice", advice);
                } catch (Exception e) {}
            } else {
                redirectAttributes.addFlashAttribute("error", "記録するデータが入力されていません。");
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "記録の保存に失敗しました。");
        }

        return "redirect:/training-log?year=" + date.getYear() + "&month=" + date.getMonthValue();
    }
    
    @PostMapping("/training/log/body-weight/save")
    public String saveBodyWeightLog(
            @RequestParam("recordDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("weight") Double weight,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        try {
            // 既存の記録があるか確認（日付重複を防ぐ）
            // ※リポジトリに findByUserAndDate がない場合の汎用的な実装
            List<BodyWeightRecord> records = bodyWeightRecordRepository.findByUserOrderByDateAsc(currentUser);
            BodyWeightRecord record = records.stream()
                    .filter(r -> r.getDate().isEqual(date))
                    .findFirst()
                    .orElse(new BodyWeightRecord());

            if (record.getId() == null) {
                record.setUser(currentUser);
                record.setDate(date);
            }
            record.setWeight(weight);

            bodyWeightRecordRepository.save(record);
            redirectAttributes.addFlashAttribute("successMessage", date + " の体重(" + weight + "kg)を保存しました。");

            // ★追加: ミッション「WEIGHT_LOG」の進捗を更新
            missionService.updateMissionProgress(currentUser.getId(), "WEIGHT_LOG");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "体重の保存に失敗しました。");
        }

        // カレンダー画面に戻る（該当の年月を表示）
        return "redirect:/training-log/form/body-weight?year=" + date.getYear() + "&month=" + date.getMonthValue();
    }
    
    @PostMapping("/training-log/save-batch")
    public String saveBatchTrainingLog(@ModelAttribute BatchTrainingLogForm batchForm, 
                                       @RequestParam("recordDate") LocalDate recordDate,
                                       Authentication authentication, 
                                       RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        int totalXp = 0;
        int totalSaved = 0;
        StringBuilder batchSummary = new StringBuilder(); 

        if (batchForm.getLogs() != null) {
            for (TrainingLogForm form : batchForm.getLogs()) {
                if (form.getRecordDate() == null) {
                    form.setRecordDate(recordDate);
                }

                String targetPart = "その他";
                int muscleXp = 0;
                int itemXp = 0;
                ExerciseData exerciseData = trainingDataService.getExerciseDataByName(form.getExerciseName());
                if ("CARDIO".equals(form.getType())) {
                    if (form.getDurationMinutes() != null && form.getDurationMinutes() > 0) {
                        TrainingRecord record = new TrainingRecord();
                        record.setUser(currentUser);
                        record.setRecordDate(form.getRecordDate());
                        record.setType("CARDIO");
                        record.setCardioType(form.getExerciseName()); 
                        record.setDurationMinutes(form.getDurationMinutes());
                        record.setDistanceKm(form.getDistanceKm());
                        record.setMemo(form.getMemo());
                        
                        trainingRecordRepository.save(record);
                        totalSaved++;
                        
                        if (batchSummary.length() > 0) batchSummary.append(", ");
                        batchSummary.append(form.getExerciseName());
                        
                        int baseXp = trainingLogicService.getExperiencePoints(exerciseData);
                        itemXp = (baseXp + form.getDurationMinutes());
                        targetPart = "有酸素";
                    }
                } else {
                    if (form.getSetList() != null && !form.getSetList().isEmpty()) {
                        int savedCount = 0;
                        for (TrainingLogForm.SetDetail detail : form.getSetList()) {
                            if (detail.getWeight() != null || detail.getReps() != null) {
                                TrainingRecord record = new TrainingRecord();
                                record.setUser(currentUser);
                                record.setRecordDate(form.getRecordDate());
                                record.setType("WEIGHT");
                                record.setExerciseName(form.getExerciseName());
                                record.setSets(1);
                                record.setWeight(detail.getWeight());
                                record.setReps(detail.getReps());
                                record.setMemo(form.getMemo());
                                
                                trainingRecordRepository.save(record);
                                savedCount++;
                                totalSaved++;
                            }
                        }
                        if (savedCount > 0) {
                            if (batchSummary.length() > 0) batchSummary.append(", ");
                            batchSummary.append(form.getExerciseName());

                            int baseXp = trainingLogicService.getExperiencePoints(exerciseData);
                            int volXp = trainingLogicService.calculateTotalVolumeXp(form);
                            itemXp = (baseXp + volXp);
                            targetPart = trainingDataService.findPartByExerciseName(form.getExerciseName());
                        }
                    }
                }
                
                totalXp += itemXp;
                
                muscleXp = Math.max(10, itemXp / 2);
                if(totalSaved > 0) {
                    muscleService.addExperience(currentUser, targetPart, muscleXp);
                }
            }
        }
        
        if (totalSaved > 0) {
            levelService.addXpAndCheckLevelUp(currentUser, totalXp);
            userRepository.save(currentUser);
            missionService.updateMissionProgress(currentUser.getId(), "TRAINING_LOG");

            redirectAttributes.addFlashAttribute("successMessage", "マイセットの一括記録を完了し、合計 " + totalXp + " XPを獲得しました！");

            try {
                String advice = aiCoachService.generateTrainingAdvice(currentUser, batchSummary.toString() + " などのメニュー");
                redirectAttributes.addFlashAttribute("aiAdvice", advice);
            } catch (Exception e) {
                System.out.println("AI Advice Error: " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("message", "記録するデータがありませんでした。");
        }

        return "redirect:/training-log?year=" + recordDate.getYear() + "&month=" + recordDate.getMonthValue();
    }
    
    @GetMapping("/training/map")
    public String showNearbyGymsMap(Authentication authentication) {
        if (getCurrentUser(authentication) == null) return "redirect:/login";
        return "training/nearby_gyms";
    }

    @GetMapping("/training-log")
    public String showTrainingLog(Authentication authentication,
                                  @RequestParam(value = "year", required = false) Integer year,
                                  @RequestParam(value = "month", required = false) Integer month,
                                  Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        LocalDate today = LocalDate.now();
        YearMonth targetYearMonth;
        if (year != null && month != null) {
            try {
                targetYearMonth = YearMonth.of(year, month);
            } catch (Exception e) {
                targetYearMonth = YearMonth.from(today);
            }
        } else {
            targetYearMonth = YearMonth.from(today);
        }

        LocalDate firstOfMonth = targetYearMonth.atDay(1);
        LocalDate lastOfMonth = targetYearMonth.atEndOfMonth();

        List<TrainingRecord> records =
                trainingRecordRepository.findByUser_IdAndRecordDateBetween(currentUser.getId(), firstOfMonth, lastOfMonth);
        Map<LocalDate, Boolean> loggedDates =
                records.stream().collect(Collectors.toMap(TrainingRecord::getRecordDate, r -> true, (a, b) -> a));

        List<BodyWeightRecord> weightRecords = bodyWeightRecordRepository.findByUserOrderByDateAsc(currentUser);
        for (BodyWeightRecord wr : weightRecords) {
            if (wr.getDate() != null && !wr.getDate().isBefore(firstOfMonth) && !wr.getDate().isAfter(lastOfMonth)) {
                loggedDates.put(wr.getDate(), true);
            }
        }

        List<LocalDate> calendarDays = new ArrayList<>();
        int paddingDays = firstOfMonth.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < paddingDays; i++) {
            calendarDays.add(null);
        }
        for (int i = 1; i <= targetYearMonth.lengthOfMonth(); i++) {
            calendarDays.add(targetYearMonth.atDay(i));
        }

        model.addAttribute("currentDate", today);
        model.addAttribute("currentYearMonth", targetYearMonth);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("loggedDates", loggedDates);
        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("prevYear", targetYearMonth.minusMonths(1).getYear());
        model.addAttribute("prevMonth", targetYearMonth.minusMonths(1).getMonthValue());
        model.addAttribute("nextYear", targetYearMonth.plusMonths(1).getYear());
        model.addAttribute("nextMonth", targetYearMonth.plusMonths(1).getMonthValue());

        List<String> dayLabels = Arrays.asList("日", "月", "火", "水", "木", "金", "土");
        model.addAttribute("dayLabels", dayLabels);

        return "log/training-log";
    }

    @GetMapping("/training-log/all")
    public String showAllTrainingLog(Authentication authentication, 
                                     @RequestParam(value = "period", defaultValue = "day") String period, 
                                     @RequestParam(value = "part", required = false) String part,
                                     Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";
        
        model.addAttribute("period", period);
        model.addAttribute("allParts", trainingDataService.getMuscleParts());
        model.addAttribute("selectedPart", part);

        List<TrainingRecord> allRecords = trainingRecordRepository.findByUser_IdOrderByRecordDateDesc(currentUser.getId());
        
        // 部位フィルターが指定されている場合のフィルタリング
        if (part != null && !part.isEmpty()) {
            allRecords = allRecords.stream().filter(record -> {
                if ("CARDIO".equalsIgnoreCase(record.getType())) {
                    return "有酸素".equals(part);
                } else if ("WEIGHT".equalsIgnoreCase(record.getType()) || "CARE".equalsIgnoreCase(record.getType())) {
                    String recordPart = trainingDataService.findPartByExerciseName(record.getExerciseName());
                    return part.equals(recordPart);
                }
                return false;
            }).collect(Collectors.toList());
        }

        List<TrainingRecord> recordsForChart = new ArrayList<>(allRecords);
        recordsForChart.sort((r1, r2) -> r1.getRecordDate().compareTo(r2.getRecordDate()));
        
        List<BodyWeightRecord> weightRecords = new ArrayList<>();
        if (bodyWeightRecordRepository != null) { weightRecords = bodyWeightRecordRepository.findByUserOrderByDateAsc(currentUser); }
        
        Map<String, int[]> durationMap = new TreeMap<>();
        Map<String, List<Double>> weightMap = new TreeMap<>();
        
        for (TrainingRecord record : recordsForChart) {
            if (record.getRecordDate() == null) continue;
            String key = getKey(record.getRecordDate(), period);
            durationMap.putIfAbsent(key, new int[]{0, 0});
            int[] durations = durationMap.get(key);
            int duration = 0;
            if (record.getDurationMinutes() != null) { duration = record.getDurationMinutes(); } else { int sets = record.getSets() != null ? record.getSets() : 1; duration = sets * 3; }
            if ("CARDIO".equalsIgnoreCase(record.getType())) { durations[0] += duration; } else { durations[1] += duration; }
        }
        
        for (BodyWeightRecord wr : weightRecords) {
            if (wr.getDate() == null) continue;
            String key = getKey(wr.getDate(), period);
            weightMap.computeIfAbsent(key, k -> new ArrayList<>()).add(wr.getWeight());
        }
        
        List<String> allKeys = new ArrayList<>();
        allKeys.addAll(durationMap.keySet());
        allKeys.addAll(weightMap.keySet());
        allKeys = allKeys.stream().distinct().sorted().collect(Collectors.toList());
        
        List<DailyChartData> chartDataList = new ArrayList<>();
        for (String key : allKeys) {
            int[] durations = durationMap.getOrDefault(key, new int[]{0, 0});
            List<Double> weights = weightMap.get(key);
            Double avgWeight = null;
            if (weights != null && !weights.isEmpty()) { double avg = weights.stream().mapToDouble(Double::doubleValue).average().orElse(0.0); avgWeight = Math.round(avg * 10.0) / 10.0; }
            String displayDate = getLabel(key, period);
            chartDataList.add(new DailyChartData(displayDate, durations[0], durations[1], avgWeight));
        }
        
        try { ObjectMapper mapper = new ObjectMapper(); String jsonChartData = mapper.writeValueAsString(chartDataList); model.addAttribute("chartDataJson", jsonChartData); } catch (Exception e) { e.printStackTrace(); model.addAttribute("chartDataJson", "[]"); }
        
        model.addAttribute("records", allRecords);
        return "log/training-log-all";
    }
    
    private String getKey(LocalDate date, String period) { if ("week".equals(period)) { LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)); return startOfWeek.toString(); } else if ("month".equals(period)) { return date.format(DateTimeFormatter.ofPattern("yyyy-MM")); } else { return date.toString(); } }
    private String getLabel(String key, String period) { if ("week".equals(period)) { LocalDate date = LocalDate.parse(key); return date.format(DateTimeFormatter.ofPattern("MM/dd")) + "~"; } else if ("month".equals(period)) { return key.replace("-", "/"); } else { LocalDate date = LocalDate.parse(key); return date.format(DateTimeFormatter.ofPattern("MM/dd")); } }

    @GetMapping("/training-log/form/weight")
    public String showWeightLogForm(@RequestParam("date") LocalDate date, Model model) {
        TrainingLogForm form = new TrainingLogForm(); 
        form.setRecordDate(date); 
        form.setType("WEIGHT"); 
        model.addAttribute("trainingLogForm", form); 
        return "log/training-log-form-weight";
    }
    @GetMapping("/training-log/form/cardio")
    public String showCardioLogForm(@RequestParam("date") LocalDate date, Model model) {
        TrainingLogForm form = new TrainingLogForm(); 
        form.setRecordDate(date); 
        form.setType("CARDIO"); 
        model.addAttribute("trainingLogForm", form); 
        return "log/training-log-form-cardio";
    }
    
    @GetMapping("/training-log/form/body-weight")
    public String showBodyWeightLogForm(
            @RequestParam(value = "date", required = false) LocalDate date,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            Authentication authentication,
            Model model) {
        
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        // 表示する年月を決定
        LocalDate baseDate = (date != null) ? date : LocalDate.now();
        YearMonth targetYearMonth;
        if (year != null && month != null) {
            targetYearMonth = YearMonth.of(year, month);
        } else {
            targetYearMonth = YearMonth.from(baseDate);
        }

        LocalDate firstOfMonth = targetYearMonth.atDay(1);
        LocalDate lastOfMonth = targetYearMonth.atEndOfMonth();

        // 既存の体重記録を取得
        List<BodyWeightRecord> allRecords = bodyWeightRecordRepository.findByUserOrderByDateAsc(currentUser);
        Map<LocalDate, BodyWeightRecord> weightMap = allRecords.stream()
                .filter(r -> r.getDate() != null && !r.getDate().isBefore(firstOfMonth) && !r.getDate().isAfter(lastOfMonth))
                .collect(Collectors.toMap(BodyWeightRecord::getDate, r -> r, (a, b) -> b));

        // カレンダーの日付リスト作成
        List<LocalDate> calendarDays = new ArrayList<>();
        int paddingDays = firstOfMonth.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < paddingDays; i++) { calendarDays.add(null); }
        for (int i = 1; i <= targetYearMonth.lengthOfMonth(); i++) { calendarDays.add(targetYearMonth.atDay(i)); }

        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("selectedDate", date); // 指定があればモーダルを開くトリガーに
        model.addAttribute("currentYearMonth", targetYearMonth);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("weightMap", weightMap);
        model.addAttribute("username", currentUser.getUsername());
        
        model.addAttribute("prevYear", targetYearMonth.minusMonths(1).getYear());
        model.addAttribute("prevMonth", targetYearMonth.minusMonths(1).getMonthValue());
        model.addAttribute("nextYear", targetYearMonth.plusMonths(1).getYear());
        model.addAttribute("nextMonth", targetYearMonth.plusMonths(1).getMonthValue());

        model.addAttribute("dayLabels", Arrays.asList("日", "月", "火", "水", "木", "金", "土"));
        
        // フォーム初期化
        TrainingLogForm form = new TrainingLogForm();
        form.setType("BODY_WEIGHT");
        if (date != null) {
            form.setRecordDate(date);
            if (weightMap.containsKey(date)) {
                form.setWeight(weightMap.get(date).getWeight());
            }
        } else {
            form.setRecordDate(LocalDate.now());
        }
        model.addAttribute("trainingLogForm", form);

        return "log/training-log-form-body-weight";
    }
}