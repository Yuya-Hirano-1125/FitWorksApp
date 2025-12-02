package com.example.demo.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dto.TrainingLogForm;
import com.example.demo.entity.ExerciseBookmark;
import com.example.demo.entity.MySet;
import com.example.demo.entity.TrainingRecord;
import com.example.demo.entity.User;
import com.example.demo.repository.ExerciseBookmarkRepository;
import com.example.demo.repository.MySetRepository;
import com.example.demo.repository.TrainingRecordRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MissionService;
import com.example.demo.service.TrainingDataService;
import com.example.demo.service.TrainingLogicService;
import com.example.demo.service.UserService;

@Controller
public class TrainingController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainingRecordRepository trainingRecordRepository;

    @Autowired
    private MissionService missionService;

    @Autowired
    private ExerciseBookmarkRepository exerciseBookmarkRepository;

    @Autowired
    private MySetRepository mySetRepository;

    // ★★★ 新しいServiceを注入 ★★★
    @Autowired
    private TrainingDataService trainingDataService;

    @Autowired
    private TrainingLogicService trainingLogicService;

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) return null;
        return userService.findByUsername(authentication.getName());
    }

    // メニュー選択画面
    @GetMapping("/training")
    public String showTrainingOptions(Authentication authentication, Model model) {
        if (getCurrentUser(authentication) == null) {
            return "redirect:/login";
        }

        // training.html 用の単純な文字列リストを取得
        Map<String, List<String>> simpleFreeWeightMap = trainingDataService.getSimpleFreeWeightExercisesMap();
        List<String> simpleCardioList = trainingDataService.getSimpleCardioExercisesList();

        model.addAttribute("freeWeightExercisesByPart", simpleFreeWeightMap);
        model.addAttribute("freeWeightParts", simpleFreeWeightMap.keySet());
        model.addAttribute("cardioExercises", simpleCardioList);

        return "training/training";
    }

    // 種目詳細一覧画面
    @GetMapping("/training/exercises")
    public String showExerciseList(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // 詳細データを持つオブジェクトを渡す
        model.addAttribute("freeWeightExercisesByPart", trainingDataService.getFreeWeightExercises());
        model.addAttribute("cardioExercises", trainingDataService.getCardioExercises());

        List<String> bookmarkedNames = exerciseBookmarkRepository.findByUserOrderByIdDesc(currentUser)
                .stream()
                .map(ExerciseBookmark::getExerciseName)
                .collect(Collectors.toList());

        model.addAttribute("bookmarkedNames", bookmarkedNames);

        return "training/exercise-list";
    }

    // セッション開始処理
    @PostMapping("/training/start")
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
                    return "redirect:/training";
                }
                title = ("free-weight".equals(type) ? "フリーウェイト" : "有酸素運動") + "セッション";
                break;
            default:
                return "redirect:/training";
        }
        model.addAttribute("trainingType", type);
        model.addAttribute("trainingTitle", title);
        model.addAttribute("selectedExercise", selectedExercise);
        model.addAttribute("today", LocalDate.now());
        return "training/training-session";
    }

    // ブックマーク一覧
    @GetMapping("/training/bookmarks")
    public String showBookmarkList(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";
        List<ExerciseBookmark> bookmarks = exerciseBookmarkRepository.findByUserOrderByIdDesc(currentUser);
        model.addAttribute("bookmarks", bookmarks);
        return "training/bookmark-list";
    }

    // ブックマーク追加・削除
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

    // マイセット一覧
    @GetMapping("/training/mysets")
    public String showMySetList(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";
        List<MySet> mySets = mySetRepository.findByUserOrderByIdDesc(currentUser);
        model.addAttribute("mySets", mySets);
        return "training/myset-list";
    }

    // マイセット作成フォーム
    @GetMapping("/training/mysets/new")
    public String showMySetForm(Authentication authentication, Model model) {
        if (getCurrentUser(authentication) == null) return "redirect:/login";

        model.addAttribute("mySet", new MySet());
        // フォーム用には単純リストを渡す
        model.addAttribute("freeWeightExercisesByPart", trainingDataService.getSimpleFreeWeightExercisesMap());
        model.addAttribute("cardioExercises", trainingDataService.getSimpleCardioExercisesList());
        return "training/myset-form";
    }

    // マイセット保存
    @PostMapping("/training/mysets/save")
    public String saveMySet(@ModelAttribute MySet mySet, Authentication authentication, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";
        mySet.setUser(currentUser);
        mySetRepository.save(mySet);
        redirectAttributes.addFlashAttribute("successMessage", "マイセット「" + mySet.getName() + "」を保存しました！");
        return "redirect:/training/mysets";
    }

    // マイセット削除
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

    // ログ保存処理
    @PostMapping("/training-log/save")
    public String saveTrainingRecord(@ModelAttribute("trainingLogForm") TrainingLogForm form, Authentication authentication, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        String exerciseIdentifier = null;
        int savedCount = 0;

        if ("WEIGHT".equals(form.getType())) {
            exerciseIdentifier = form.getExerciseName();
            if (form.getSetList() != null && !form.getSetList().isEmpty()) {
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
                        trainingRecordRepository.save(record);
                        savedCount++;
                    }
                }
            } else {
                TrainingRecord record = new TrainingRecord();
                record.setUser(currentUser);
                record.setRecordDate(form.getRecordDate());
                record.setType(form.getType());
                record.setExerciseName(form.getExerciseName());
                record.setSets(form.getSets());
                record.setReps(form.getReps());
                record.setWeight(form.getWeight());
                trainingRecordRepository.save(record);
                savedCount = 1;
            }
        } else if ("CARDIO".equals(form.getType())) {
            TrainingRecord record = new TrainingRecord();
            record.setUser(currentUser);
            record.setRecordDate(form.getRecordDate());
            record.setType(form.getType());
            record.setCardioType(form.getCardioType());
            record.setDurationMinutes(form.getDurationMinutes());
            record.setDistanceKm(form.getDistanceKm());
            exerciseIdentifier = form.getCardioType();
            trainingRecordRepository.save(record);
            savedCount = 1;
        }

        int earnedXP = 0;
        if (savedCount > 0 && exerciseIdentifier != null) {
            // Serviceを使ってXP計算
            int baseDifficultyXp = trainingLogicService.getExperiencePoints(exerciseIdentifier);
            int additionalXp = 0;
            if ("WEIGHT".equals(form.getType())) {
                additionalXp = trainingLogicService.calculateTotalVolumeXp(form);
            } else if ("CARDIO".equals(form.getType()) && form.getDurationMinutes() != null) {
                additionalXp = form.getDurationMinutes();
            }
            earnedXP = baseDifficultyXp + additionalXp;
        }

        if (earnedXP > 0) {
            int newTotalXp = currentUser.getXp() + earnedXP;
            currentUser.setXp(newTotalXp);
            userRepository.save(currentUser);
            redirectAttributes.addFlashAttribute("successMessage", form.getRecordDate().toString() + " のトレーニングを記録し、" + earnedXP + " XPを獲得しました！");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", form.getRecordDate().toString() + " のトレーニングを記録しました！");
        }

        missionService.updateMissionProgress(currentUser.getId(), "TRAINING_LOG");
        LocalDate recordedDate = form.getRecordDate();
        return "redirect:/training-log?year=" + recordedDate.getYear() + "&month=" + recordedDate.getMonthValue();
    }

    // マップ画面
    @GetMapping("/training/map")
    public String showNearbyGymsMap(Authentication authentication) {
        if (getCurrentUser(authentication) == null) return "redirect:/login";
        return "training/nearby_gyms";
    }

    // トレーニングログカレンダー表示
    @GetMapping("/training-log")
    public String showTrainingLog(
            Authentication authentication,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            Model model) {

        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

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

        List<TrainingRecord> records = trainingRecordRepository.findByUser_IdAndRecordDateBetween(
                currentUser.getId(), firstOfMonth, lastOfMonth);

        Map<LocalDate, Boolean> loggedDates = records.stream()
                .collect(Collectors.toMap(
                    TrainingRecord::getRecordDate,
                    r -> true,
                    (a, b) -> a
                ));

        List<LocalDate> calendarDays = new ArrayList<>();
        int paddingDays = firstOfMonth.getDayOfWeek().getValue() % 7;
        if (paddingDays == 0) paddingDays = 7;
        paddingDays = (paddingDays == 7) ? 0 : paddingDays;

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

        List<String> dayLabels = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            dayLabels.add(day.getDisplayName(TextStyle.SHORT, Locale.JAPANESE));
        }
        model.addAttribute("dayLabels", dayLabels);
        return "log/training-log";
    }

    @GetMapping("/training-log/all")
    public String showAllTrainingLog(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";
        List<TrainingRecord> allRecords = trainingRecordRepository.findByUser_IdOrderByRecordDateDesc(currentUser.getId());
        model.addAttribute("records", allRecords);
        return "log/training-log-all";
    }

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
}