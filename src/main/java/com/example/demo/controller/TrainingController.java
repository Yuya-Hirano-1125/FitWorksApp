package com.example.demo.controller; 

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
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

    // ★★★ ブックマーク用リポジトリ ★★★
    @Autowired
    private ExerciseBookmarkRepository exerciseBookmarkRepository;

    // ★★★ マイセット用リポジトリ ★★★
    @Autowired
    private MySetRepository mySetRepository;

	private User getCurrentUser(Authentication authentication) { 
		if (authentication == null) return null; 
		return userService.findByUsername(authentication.getName()); 
	} 
	
	// ★★★ 経験値(XP)定数と計算ロジック ★★★
	private static final int XP_BEGINNER = 300;
	private static final int XP_INTERMEDIATE = 500;
	private static final int XP_ADVANCED = 1000;
	private static final int XP_PER_LEVEL = 5000; 

	private int getExperiencePoints(String exerciseName) {
		if (exerciseName == null || exerciseName.trim().isEmpty()) {
			return 0; 
		}
		if (exerciseName.contains("(上級)")) {
			return XP_ADVANCED;
		} else if (exerciseName.contains("(中級)")) {
			return XP_INTERMEDIATE;
		} else if (exerciseName.contains("(初級)")) {
			return XP_BEGINNER;
		}
		return 0; 
	}
	
	private int calculateTotalVolumeXp(TrainingLogForm form) {
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

	private static final Map<String, List<String>> FREE_WEIGHT_EXERCISES_BY_PART = new LinkedHashMap<>() {{
		put("胸", List.of("チェストフライ (初級)", "ベンチプレス (中級)", "ダンベルプレス (中級)", "インクラインプレス (中級)"));
		put("背中", List.of("ラットプルダウン (初級)", "シーテッドロー (初級)", "ベントオーバーロー (中級)", "デッドリフト (上級)"));
		put("脚", List.of("レッグプレス (初級)", "レッグエクステンション (初級)", "レッグカール (初級)", "スクワット (中級)"));
		put("肩", List.of("サイドレイズ (初級)", "フロントレイズ (初級)", "ショルダープレス (中級)", "オーバーヘッドプレス (中級)"));
		put("腕", List.of("アームカール (初級)", "ハンマーカール (初級)", "トライセプスエクステンション (初級)"));
		put("腹筋", List.of("クランチ (初級)", "レッグレイズ (中級)", "ロシアンツイスト (中級)"));
		put("その他", List.of("カーフレイズ (初級)", "ヒップスラスト (中級)"));
	}};
	
	private static final List<String> CARDIO_EXERCISES = List.of(
			"ウォーキング (初級)", "サイクリング (初級)", "エリプティカル (初級)", "ランニング (中級)", "水泳 (中級)", "ローイング (中級)", "トレッドミルインターバル (上級)"
	);

    // ランダム生成ロジック
    private Map<String, Object> generateAiSuggestedMenu() {
        Map<String, Object> menu = new LinkedHashMap<>();
        List<String> programList = new ArrayList<>();
        Random random = new Random();

        List<String> mainParts = List.of("胸", "背中", "脚", "肩");
        String selectedPart = mainParts.get(random.nextInt(mainParts.size()));
        
        List<String> exercises = FREE_WEIGHT_EXERCISES_BY_PART.get(selectedPart);
        if (exercises == null || exercises.isEmpty()) {
            exercises = List.of("全身サーキット (中級)");
        }
        
        List<String> availableExercises = new ArrayList<>(exercises);
        List<String> selectedExercises = new ArrayList<>();
        
        int numExercises = 3 + random.nextInt(2);
        
        for (int i = 0; i < numExercises && !availableExercises.isEmpty(); i++) {
            int index = random.nextInt(availableExercises.size());
            selectedExercises.add(availableExercises.remove(index));
        }
        
        for (int i = 0; i < selectedExercises.size(); i++) {
            String exercise = selectedExercises.get(i);
            int sets = 3 + random.nextInt(2);
            int reps = 8 + random.nextInt(5);
            int baseWeight = 30; 
            int difficultyAdjustment = getExperiencePoints(exercise) / 30;
            int weight = baseWeight + random.nextInt(50) + difficultyAdjustment; 
            
            programList.add((i + 1) + ". " + exercise + ": " + sets + "セット x " + reps + "回 (" + weight + "kg)");
        }
        
        if (random.nextInt(10) < 4) {
            String cardio = CARDIO_EXERCISES.get(random.nextInt(CARDIO_EXERCISES.size()));
            int duration = 15 + random.nextInt(16);
            programList.add((selectedExercises.size() + 1) + ". " + cardio + ": " + duration + "分");
        }

        int totalTime = 40 + random.nextInt(31);
        int restTime = 45 + random.nextInt(31);

        menu.put("programList", programList);
        menu.put("targetTime", totalTime);
        menu.put("restTime", restTime);
        
        return menu;
    }

    // AI提案テキスト解析
    private List<String> parseAiProposal(String proposalText) {
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
	
	@GetMapping("/training")
	public String showTrainingOptions(Authentication authentication, Model model) {	
		if (getCurrentUser(authentication) == null) {
			return "redirect:/login";	
		}
		
		model.addAttribute("freeWeightExercisesByPart", FREE_WEIGHT_EXERCISES_BY_PART);
		model.addAttribute("freeWeightParts", FREE_WEIGHT_EXERCISES_BY_PART.keySet());
		model.addAttribute("cardioExercises", CARDIO_EXERCISES);
		
		return "training/training";	
	}

    // ★★★ ブックマーク一覧画面 ★★★
    @GetMapping("/training/bookmarks")
    public String showBookmarkList(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<ExerciseBookmark> bookmarks = exerciseBookmarkRepository.findByUserOrderByIdDesc(currentUser);
        model.addAttribute("bookmarks", bookmarks);
        
        return "training/bookmark-list";
    }

    // ★★★ ブックマーク登録/解除 ★★★
    @PostMapping("/training/bookmark/toggle")
    public String toggleBookmark(
            @RequestParam("exerciseName") String exerciseName,
            @RequestParam("type") String type,
            @RequestParam(value = "redirectUrl", defaultValue = "/training/exercises") String redirectUrl,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

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

    // ★★★ マイセット一覧表示 ★★★
    @GetMapping("/training/mysets")
    public String showMySetList(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        List<MySet> mySets = mySetRepository.findByUserOrderByIdDesc(currentUser);
        model.addAttribute("mySets", mySets);
        return "training/myset-list";
    }

    // ★★★ マイセット作成フォーム表示 ★★★
    @GetMapping("/training/mysets/new")
    public String showMySetForm(Authentication authentication, Model model) {
        if (getCurrentUser(authentication) == null) return "redirect:/login";

        model.addAttribute("mySet", new MySet());
        model.addAttribute("freeWeightExercisesByPart", FREE_WEIGHT_EXERCISES_BY_PART);
        model.addAttribute("cardioExercises", CARDIO_EXERCISES);
        return "training/myset-form";
    }

    // ★★★ マイセット保存処理 ★★★
    @PostMapping("/training/mysets/save")
    public String saveMySet(
            @ModelAttribute MySet mySet,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";

        mySet.setUser(currentUser);
        mySetRepository.save(mySet);
        redirectAttributes.addFlashAttribute("successMessage", "マイセット「" + mySet.getName() + "」を保存しました！");
        
        return "redirect:/training/mysets";
    }

    // ★★★ マイセット削除処理 ★★★
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

	@GetMapping("/training/map")
	public String showNearbyGymsMap(Authentication authentication) {
		if (getCurrentUser(authentication) == null) {
			return "redirect:/login";	
		}
		return "training/nearby_gyms";	
	}

	@GetMapping("/training/exercises")
	public String showExerciseList(Authentication authentication, Model model) {
		User currentUser = getCurrentUser(authentication);
		if (currentUser == null) {
			return "redirect:/login";	
		}
        
        List<String> bookmarkedNames = exerciseBookmarkRepository.findByUserOrderByIdDesc(currentUser)
                .stream()
                .map(ExerciseBookmark::getExerciseName)
                .collect(Collectors.toList());
        
        model.addAttribute("bookmarkedNames", bookmarkedNames);

		return "training/exercise-list";	
	}

	@PostMapping("/training/start")
	public String startTrainingSession(
			@RequestParam("type") String type,
			@RequestParam(value = "exerciseName", required = false) String exerciseName,
            @RequestParam(value = "aiProposal", required = false) String aiProposal,
            // ★追加: マイセットID
            @RequestParam(value = "mySetId", required = false) Long mySetId,
			Authentication authentication,
			Model model) {
		
		User currentUser = getCurrentUser(authentication);
		if (currentUser == null) {
			return "redirect:/login";	
		}
		
		String title = "";
		String selectedExercise = "";

		switch (type) {
			case "ai-suggested":
				title = "AIおすすめメニューセッション";
				selectedExercise = "AIおすすめプログラム";	
				
                if (aiProposal != null && !aiProposal.trim().isEmpty()) {
                    List<String> parsedProgram = parseAiProposal(aiProposal);
                    model.addAttribute("programList", parsedProgram);
                    
                    model.addAttribute("targetTime", 45);
                    model.addAttribute("restTime", 60);
                } else {
                    Map<String, Object> aiMenu = generateAiSuggestedMenu();
                    model.addAttribute("programList", aiMenu.get("programList"));
                    model.addAttribute("targetTime", aiMenu.get("targetTime"));
                    model.addAttribute("restTime", aiMenu.get("restTime"));
                }
				break;

            // ★★★ マイセット開始ロジック ★★★
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
                        
                        // 目標時間は種目数 x 10分と仮定
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
		if (currentUser == null) {
			return "redirect:/login";
		}

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
	
	@PostMapping("/training-log/save")
	public String saveTrainingRecord(@ModelAttribute("trainingLogForm") TrainingLogForm form,	Authentication authentication,RedirectAttributes redirectAttributes) {
		
		User currentUser = getCurrentUser(authentication);
		if (currentUser == null) {
			return "redirect:/login";
		}
		
		String exerciseIdentifier = null; // XP計算用
		int savedCount = 0;

		// ▼▼▼ 記録保存ロジック：セットごとの記録に対応 ▼▼▼
		if ("WEIGHT".equals(form.getType())) {
			exerciseIdentifier = form.getExerciseName();

			// setListがある場合（セッション画面からの複数セット登録）
			if (form.getSetList() != null && !form.getSetList().isEmpty()) {
				for (TrainingLogForm.SetDetail detail : form.getSetList()) {
					// 重量または回数が入力されている場合のみ保存
					if (detail.getWeight() != null || detail.getReps() != null) {
						TrainingRecord record = new TrainingRecord();
						record.setUser(currentUser);
						record.setRecordDate(form.getRecordDate());
						record.setType("WEIGHT");
						record.setExerciseName(form.getExerciseName());
						
						// 1行＝1セットとして記録
						record.setSets(1);	
						record.setWeight(detail.getWeight());
						record.setReps(detail.getReps());
						
						trainingRecordRepository.save(record);
						savedCount++;
					}
				}
			} else {
				// 既存ロジック（単一レコード/フォームからの登録）
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
			// 有酸素運動
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
		// ▲▲▲ 記録保存ロジック終わり ▲▲▲
		
		// ★★★ XP計算とユーザー情報更新ロジック (統合) ★★★
		int earnedXP = 0;
		if (savedCount > 0 && exerciseIdentifier != null) {
			
			// 1. 難易度による基本XPを取得
			int baseDifficultyXp = getExperiencePoints(exerciseIdentifier);	
			
			// 2. 追加XP (ボリュームまたは時間) の計算
			int additionalXp = 0;
            
			if ("WEIGHT".equals(form.getType())) {
				// フリーウェイトの場合: 重量 × 回数 (総ボリューム) を追加XPとする
				additionalXp = calculateTotalVolumeXp(form);
			} else if ("CARDIO".equals(form.getType()) && form.getDurationMinutes() != null) {
				// 有酸素運動の場合: 時間 (分) を追加XPとする
				additionalXp = form.getDurationMinutes();
			}
			
			// 3. 獲得XP = 基本XP (難易度) + 追加XP (ボリューム/時間)
			earnedXP = baseDifficultyXp + additionalXp;
		}

		if (earnedXP > 0) {
			int newTotalXp = currentUser.getXp() + earnedXP;
			currentUser.setXp(newTotalXp);
			// 💡 TODO: レベルアップチェック
			userRepository.save(currentUser);	

			redirectAttributes.addFlashAttribute("successMessage",	
				form.getRecordDate().toString() + " のトレーニングを記録し、" + earnedXP + " XPを獲得しました！");
		} else {
			redirectAttributes.addFlashAttribute("successMessage", form.getRecordDate().toString() + " のトレーニングを記録しました！");
		}
		
		// デイリーミッションの進捗を更新
		missionService.updateMissionProgress(currentUser.getId(), "TRAINING_LOG");
		
		LocalDate recordedDate = form.getRecordDate();
		return "redirect:/training-log?year=" + recordedDate.getYear() + "&month=" + recordedDate.getMonthValue();
	}
}