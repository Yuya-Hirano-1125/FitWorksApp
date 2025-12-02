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

    @Autowired
    private ExerciseBookmarkRepository exerciseBookmarkRepository;

    @Autowired
    private MySetRepository mySetRepository;

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) return null;
        return userService.findByUsername(authentication.getName());
    }

    // ★★★ データ構造定義: 種目詳細クラス ★★★
    public static class ExerciseData {
        private String name;
        private String difficulty; // "初級", "中級", "上級"
        private String howTo;
        private String points;

        public ExerciseData(String name, String difficulty, String howTo, String points) {
            this.name = name;
            this.difficulty = difficulty;
            this.howTo = howTo;
            this.points = points;
        }

        public String getName() { return name; }
        public String getDifficulty() { return difficulty; }
        public String getHowTo() { return howTo; }
        public String getPoints() { return points; }

        // 表示用にフルネームを返す (例: "ベンチプレス (中級)")
        // これがDB保存時やフォーム送信時のID代わりになります
        public String getFullName() {
            return name + " (" + difficulty + ")";
        }
    }

    // ★★★ 種目データの構築 (部位別マップ) ★★★
    private static final Map<String, List<ExerciseData>> EXERCISE_DATA_MAP = new LinkedHashMap<>();
    private static final List<ExerciseData> CARDIO_DATA_LIST = new ArrayList<>();

    static {
        // --- 胸 ---
        List<ExerciseData> chest = new ArrayList<>();
        chest.add(new ExerciseData("プッシュアップ", "初級", 
            "手幅を肩幅よりやや広めにとり、頭からかかとまで一直線にする。胸が床につくまで下ろし、押し上げる。", 
            "腰を反らさない。肩甲骨を寄せる意識で行う。"));
        chest.add(new ExerciseData("膝つきプッシュアップ", "初級", 
            "膝を床についた状態でプッシュアップを行う。負荷が軽いのでフォーム習得に最適。", 
            "お尻が突き出ないように、膝から頭までを一直線に保つ。"));
        chest.add(new ExerciseData("チェストプレスマシン", "初級", 
            "マシンに座り、グリップを胸の高さに合わせて握る。前方に押し出し、ゆっくり戻す。", 
            "背中をシートにしっかりつけ、肩が上がらないように注意する。"));
        chest.add(new ExerciseData("チェストフライ", "初級", 
            "マシンまたはダンベルを使用。腕を大きく開き、胸の前で閉じる動作を行う。", 
            "肘の角度を固定し、胸の収縮を感じながら行う。"));
        chest.add(new ExerciseData("ベンチプレス", "中級", 
            "ベンチに仰向けになり、バーベルを肩幅より広めに握る。胸のトップまで下ろし、押し上げる。", 
            "肩甲骨を寄せて胸を張る（ブリッジ）。お尻を浮かせない。"));
        chest.add(new ExerciseData("ダンベルプレス", "中級", 
            "両手にダンベルを持ち、ベンチに仰向けになる。胸の横から真上に押し上げる。", 
            "バーベルより可動域を広く取れる。ダンベル同士を当てないように制御する。"));
        chest.add(new ExerciseData("ダンベルフライ", "中級", 
            "仰向けになり、肘を軽く曲げた状態で腕を左右に開いていく。胸のストレッチを感じたら戻す。", 
            "腕だけで戻さず、胸の筋肉で抱きかかえるように動作する。"));
        chest.add(new ExerciseData("インクラインベンチプレス", "中級", 
            "ベンチの角度を30〜45度に設定し、斜め上方向にプレスする。大胸筋上部を狙う。", 
            "脇を開きすぎない。バーを鎖骨のあたりに下ろす。"));
        chest.add(new ExerciseData("ディップス", "上級", 
            "平行棒を握り、体を持ち上げる。前傾姿勢を取りながら肘を曲げて体を沈め、押し戻す。", 
            "前傾姿勢を維持することで胸に効く（直立だと腕に効く）。深く下ろしすぎると肩を痛めるので注意。"));
        chest.add(new ExerciseData("ケーブルクロスオーバー", "中級", 
            "ケーブルマシンのハンドルを握り、胸の前で合わせるように腕を閉じる。", 
            "肘の角度を固定し、胸の収縮を意識する。フィニッシュで手首を交差させるとより収縮する。"));
        EXERCISE_DATA_MAP.put("胸", chest);

        // --- 背中 ---
        List<ExerciseData> back = new ArrayList<>();
        back.add(new ExerciseData("ラットプルダウン", "初級", 
            "バーを肩幅より広めに握り、鎖骨に向かって引き下ろす。", 
            "腕ではなく背中で引く。胸を張り、肩甲骨を下げる意識で。"));
        back.add(new ExerciseData("シーテッドロー", "初級", 
            "マシンに座り、ハンドルを腹部に向かって引く。", 
            "上体を倒しすぎない。引くときに肩甲骨を寄せる。"));
        back.add(new ExerciseData("バックエクステンション", "初級", 
            "器具にうつ伏せになり、上体を起こす。脊柱起立筋を鍛える。", 
            "反動を使わず、ゆっくり行う。反りすぎに注意。"));
        back.add(new ExerciseData("ワンハンドローイング", "中級", 
            "片手と片膝をベンチにつき、反対の手でダンベルを引き上げる。", 
            "背中は平らに保つ。ダンベルを腰の方へ引くイメージで。"));
        back.add(new ExerciseData("ベントオーバーロー", "中級", 
            "立った状態で前傾姿勢をとり、バーベルを腹部へ引き上げる。", 
            "腰を丸めない（重要）。ニーグリップをしっかり効かせる。"));
        back.add(new ExerciseData("チンアップ(懸垂)", "中級", 
            "バーにぶら下がり、顎がバーを超えるまで体を引き上げる。", 
            "反動を使わない。下ろすときも力を抜かずにゆっくり下ろす。"));
        back.add(new ExerciseData("デッドリフト", "上級", 
            "床に置いたバーベルを、背筋を伸ばしたまま直立するまで引き上げる。", 
            "「腰で引く」のではなく「足で地面を押す」イメージ。背中が丸まると怪我の原因になる。"));
        back.add(new ExerciseData("Tバーロー", "中級", 
            "Tバーマシンのハンドルを握り、前傾姿勢で胸に向かって引き上げる。", 
            "背中の厚みを作るのに効果的。上体の角度を固定する。"));
        EXERCISE_DATA_MAP.put("背中", back);

        // --- 脚 ---
        List<ExerciseData> legs = new ArrayList<>();
        legs.add(new ExerciseData("エアスクワット", "初級", 
            "自重で行うスクワット。足を肩幅に開き、太ももが平行になるまで腰を下ろす。", 
            "膝がつま先より前に出過ぎないように。背筋を伸ばす。"));
        legs.add(new ExerciseData("レッグプレス", "初級", 
            "マシンに座り、フットプレートを足で押す。", 
            "膝を伸ばしきらない（ロックしない）。お尻が浮かないようにする。"));
        legs.add(new ExerciseData("レッグエクステンション", "初級", 
            "マシンに座り、パッドを足首に当てて膝を伸ばす。大腿四頭筋に集中。", 
            "蹴り上げるのではなく、筋肉の収縮で持ち上げる。"));
        legs.add(new ExerciseData("レッグカール", "初級", 
            "マシンにうつ伏せになり、かかとをお尻に近づけるように膝を曲げる。", 
            "反動を使わず、ハムストリングスの収縮を感じる。"));
        legs.add(new ExerciseData("スクワット", "中級", 
            "バーベルを担いで行うスクワット。下半身全体の筋力強化。", 
            "腹圧をしっかりかける。目線は少し前へ。深くしゃがむほど効果が高いが柔軟性に合わせて。"));
        legs.add(new ExerciseData("ランジ", "中級", 
            "ダンベルを持ち、片足を大きく前に踏み出して腰を落とす。交互に行う。", 
            "上半身をまっすぐに保つ。前の足のかかとで踏ん張って戻る。"));
        legs.add(new ExerciseData("ブルガリアンスクワット", "上級", 
            "片足を後ろのベンチに乗せ、片足だけでスクワットを行う。", 
            "非常に負荷が高い。膝が内側に入らないように注意。バランスをとるため体幹も使う。"));
        legs.add(new ExerciseData("ルーマニアンデッドリフト", "中級", 
            "膝を軽く曲げた状態で固定し、股関節を支点に上体を倒してハムストリングスを伸ばす。", 
            "バーベルは常に体に沿わせる。背中を丸めない。ももの裏側のストレッチを感じる。"));
        legs.add(new ExerciseData("カーフレイズ", "初級", 
            "つま先立ちになり、ふくらはぎを収縮させて戻す。", 
            "可動域を大きくとる。段差を使うとより効果的。"));
        legs.add(new ExerciseData("ヒップスラスト", "中級", 
            "ベンチに肩甲骨を乗せ、バーベルを腰に乗せてお尻を持ち上げる。", 
            "お尻（大臀筋）を強く収縮させる。腰を反りすぎない。"));
        EXERCISE_DATA_MAP.put("脚", legs);

        // --- 肩 ---
        List<ExerciseData> shoulders = new ArrayList<>();
        shoulders.add(new ExerciseData("サイドレイズ", "初級", 
            "ダンベルを持ち、肘を軽く曲げて横に開くように持ち上げる。", 
            "小指側から上げるイメージで。肩より高く上げない。反動を使わない。"));
        shoulders.add(new ExerciseData("ショルダープレス", "中級", 
            "ダンベルを耳の横で構え、真上に押し上げる。", 
            "腰を反らさない。肘を伸ばしきったところで一瞬止める。"));
        shoulders.add(new ExerciseData("フロントレイズ", "初級", 
            "ダンベルを体の前に持ち、肩の高さまで持ち上げる。", 
            "三角筋前部を意識。体を揺らさない。"));
        shoulders.add(new ExerciseData("オーバーヘッドプレス", "中級", 
            "立ってバーベルを持ち、頭上へ押し上げる。体幹の強さも必要。", 
            "顎を引いてバーの軌道を確保する。腹圧を高めて体を安定させる。"));
        shoulders.add(new ExerciseData("リアデルトフライ", "初級", 
            "前傾姿勢になり、ダンベルを横に開く。肩の後ろ側を鍛える。", 
            "肩甲骨を寄せすぎないようにすると肩の後ろに効きやすい。"));
        shoulders.add(new ExerciseData("アーノルドプレス", "中級", 
            "手のひらが自分に向くようにダンベルを持ち、回転させながら押し上げる。", 
            "可動域が広く、肩全体を刺激できる。スムーズな回転を意識。"));
        EXERCISE_DATA_MAP.put("肩", shoulders);

        // --- 腕 ---
        List<ExerciseData> arms = new ArrayList<>();
        arms.add(new ExerciseData("ダンベルカール", "初級", 
            "ダンベルを持ち、肘を固定して曲げる。", 
            "肘の位置を動かさない。下ろすときもゆっくりと。"));
        arms.add(new ExerciseData("ハンマーカール", "初級", 
            "手のひらを内側に向けたままカールを行う。", 
            "上腕二頭筋の外側と前腕に効く。手首を固定する。"));
        arms.add(new ExerciseData("トライセプスエクステンション", "初級", 
            "ダンベルを頭の後ろに構え、肘を伸ばして持ち上げる。", 
            "肘を開きすぎない。二の腕（上腕三頭筋）の収縮を感じる。"));
        arms.add(new ExerciseData("アームカール", "初級", 
            "バーベルまたはダンベルで肘を曲げる基本的な種目。", 
            "反動を使わず、筋肉の収縮を感じる。"));
        arms.add(new ExerciseData("バーベルカール", "中級", 
            "バーベルを用いて高重量でカールを行う。", 
            "体の反動（チーティング）を使わないように壁に背をつけて行うのも良い。"));
        arms.add(new ExerciseData("スカルクラッシャー", "中級", 
            "ベンチに仰向けになり、バーベルを額のあたりへ下ろして伸ばす。", 
            "上腕三頭筋に強い負荷がかかる。肘の位置を固定する。"));
        EXERCISE_DATA_MAP.put("腕", arms);

        // --- 腹筋 ---
        List<ExerciseData> abs = new ArrayList<>();
        abs.add(new ExerciseData("クランチ", "初級", 
            "仰向けで膝を立て、おへそを覗き込むように上体を丸める。", 
            "腰を床から離さない。息を吐きながら収縮させる。"));
        abs.add(new ExerciseData("レッグレイズ", "中級", 
            "仰向けで足を揃え、床と垂直になるまで上げ下げする。", 
            "腰が浮かないように手をお尻の下に敷くと良い。下ろすときに耐える。"));
        abs.add(new ExerciseData("ロシアンツイスト", "中級", 
            "上体を起こして座り、重りを持って左右に体をねじる。", 
            "腹斜筋に効く。足は浮かせたまま行うと強度アップ。"));
        abs.add(new ExerciseData("プランク", "初級", 
            "肘とつま先で体を支え、板のように真っ直ぐキープする。", 
            "お尻が上がったり下がったりしない。腹筋に常に力を入れる。"));
        abs.add(new ExerciseData("アブローラー(膝つき)", "中級", 
            "ローラーを握り、膝をついて前方に転がし、戻る。", 
            "腰を反らすと痛めるので、猫背気味をキープする。限界まで行ったら戻る。"));
        abs.add(new ExerciseData("ハンギングレッグレイズ", "上級", 
            "バーにぶら下がり、足を足の付け根から持ち上げる。", 
            "腹筋下部に強烈に効く。体が揺れないように制御する。"));
        EXERCISE_DATA_MAP.put("腹筋", abs);

        // --- その他 ---
        List<ExerciseData> others = new ArrayList<>();
        others.add(new ExerciseData("バーピー", "中級", 
            "スクワット、腕立て伏せ、ジャンプを連続で行う全身運動。", 
            "リズミカルに行う。心拍数を上げるのに最適。"));
        others.add(new ExerciseData("ケトルベルスイング", "中級", 
            "ケトルベルを股下から前方へ振り上げる。", 
            "腕の力ではなく、股関節の伸展（お尻の力）で振り上げる。"));
        EXERCISE_DATA_MAP.put("その他", others);

        // --- 有酸素 ---
        CARDIO_DATA_LIST.add(new ExerciseData("ウォーキング", "初級", 
            "背筋を伸ばし、大股でリズミカルに歩く。", 
            "腕を大きく振る。呼吸を止めない。最低20分以上続けると脂肪燃焼効果が高い。"));
        CARDIO_DATA_LIST.add(new ExerciseData("サイクリング", "初級", 
            "エアロバイクなどを一定のペースで漕ぐ。", 
            "膝への負担が少ない。背中を丸めすぎない。負荷を軽くしすぎず、適度な抵抗で。"));
        CARDIO_DATA_LIST.add(new ExerciseData("エリプティカル", "初級", 
            "クロストレーナーを使用。手足を同時に動かす。", 
            "関節への負担が少ない。大きく動かすことで全身運動になる。"));
        CARDIO_DATA_LIST.add(new ExerciseData("ランニング", "中級", 
            "会話ができる程度のペース（ニコニコペース）で走る。", 
            "着地の衝撃を和らげるシューズを選ぶ。無理のない距離から始める。"));
        CARDIO_DATA_LIST.add(new ExerciseData("水泳", "中級", 
            "全身を使う有酸素運動。クロールや平泳ぎなど。", 
            "水の抵抗で筋力もつく。関節への負担がほぼゼロ。"));
        CARDIO_DATA_LIST.add(new ExerciseData("ローイング", "中級", 
            "ボート漕ぎマシンを使用。全身の筋肉を使う。", 
            "脚で押してから腕で引くリズムを大切に。背中を丸めない。"));
        CARDIO_DATA_LIST.add(new ExerciseData("HIIT(タバタ式)", "上級", 
            "20秒全力運動＋10秒休憩を8セット繰り返す高強度インターバル。", 
            "短時間で極めて高い脂肪燃焼効果があるが、心拍数が急上昇するため体調に注意。"));
        CARDIO_DATA_LIST.add(new ExerciseData("トレッドミルインターバル", "上級", 
            "ダッシュと歩きを交互に繰り返す。", 
            "心肺機能を限界まで高める。傾斜をつけるとさらに効果的。"));
    }

    // XP計算ロジック
    private static final int XP_BEGINNER = 300;
    private static final int XP_INTERMEDIATE = 500;
    private static final int XP_ADVANCED = 1000;

    private int getExperiencePoints(String exerciseFullName) {
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

    // ランダムメニュー生成 (詳細データ対応版)
    private Map<String, Object> generateAiSuggestedMenu() {
        Map<String, Object> menu = new LinkedHashMap<>();
        List<String> programList = new ArrayList<>();
        Random random = new Random();

        List<String> mainParts = new ArrayList<>(EXERCISE_DATA_MAP.keySet()); // 部位リスト
        String selectedPart = mainParts.get(random.nextInt(mainParts.size()));

        List<ExerciseData> exercises = EXERCISE_DATA_MAP.get(selectedPart);
        if (exercises == null || exercises.isEmpty()) {
            // フォールバック
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
            ExerciseData cardio = CARDIO_DATA_LIST.get(random.nextInt(CARDIO_DATA_LIST.size()));
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

    // ★★★ 画面表示用メソッド ★★★

    @GetMapping("/training")
    public String showTrainingOptions(Authentication authentication, Model model) {
        if (getCurrentUser(authentication) == null) {
            return "redirect:/login";
        }

        // training.html は単純な文字列リスト（名前+難易度）を期待しているため、変換して渡す
        Map<String, List<String>> simpleFreeWeightMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<ExerciseData>> entry : EXERCISE_DATA_MAP.entrySet()) {
            List<String> simpleList = entry.getValue().stream()
                .map(ExerciseData::getFullName)
                .collect(Collectors.toList());
            simpleFreeWeightMap.put(entry.getKey(), simpleList);
        }

        List<String> simpleCardioList = CARDIO_DATA_LIST.stream()
                .map(ExerciseData::getFullName)
                .collect(Collectors.toList());

        model.addAttribute("freeWeightExercisesByPart", simpleFreeWeightMap);
        model.addAttribute("freeWeightParts", simpleFreeWeightMap.keySet());
        model.addAttribute("cardioExercises", simpleCardioList);

        return "training/training";
    }

    @GetMapping("/training/bookmarks")
    public String showBookmarkList(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";
        List<ExerciseBookmark> bookmarks = exerciseBookmarkRepository.findByUserOrderByIdDesc(currentUser);
        model.addAttribute("bookmarks", bookmarks);
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

    @GetMapping("/training/mysets")
    public String showMySetList(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";
        List<MySet> mySets = mySetRepository.findByUserOrderByIdDesc(currentUser);
        model.addAttribute("mySets", mySets);
        return "training/myset-list";
    }

    @GetMapping("/training/mysets/new")
    public String showMySetForm(Authentication authentication, Model model) {
        if (getCurrentUser(authentication) == null) return "redirect:/login";

        // MySet作成画面も文字列リストを使用
        Map<String, List<String>> simpleFreeWeightMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<ExerciseData>> entry : EXERCISE_DATA_MAP.entrySet()) {
            List<String> simpleList = entry.getValue().stream()
                .map(ExerciseData::getFullName)
                .collect(Collectors.toList());
            simpleFreeWeightMap.put(entry.getKey(), simpleList);
        }
        List<String> simpleCardioList = CARDIO_DATA_LIST.stream()
                .map(ExerciseData::getFullName)
                .collect(Collectors.toList());

        model.addAttribute("mySet", new MySet());
        model.addAttribute("freeWeightExercisesByPart", simpleFreeWeightMap);
        model.addAttribute("cardioExercises", simpleCardioList);
        return "training/myset-form";
    }

    @PostMapping("/training/mysets/save")
    public String saveMySet(@ModelAttribute MySet mySet, Authentication authentication, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) return "redirect:/login";
        mySet.setUser(currentUser);
        mySetRepository.save(mySet);
        redirectAttributes.addFlashAttribute("successMessage", "マイセット「" + mySet.getName() + "」を保存しました！");
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

    @GetMapping("/training/map")
    public String showNearbyGymsMap(Authentication authentication) {
        if (getCurrentUser(authentication) == null) return "redirect:/login";
        return "training/nearby_gyms";
    }

    @GetMapping("/training/exercises")
    public String showExerciseList(Authentication authentication, Model model) {
        User currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // ★★★ 詳細リスト画面にはリッチな ExerciseData オブジェクトのMap/List を渡す ★★★
        model.addAttribute("freeWeightExercisesByPart", EXERCISE_DATA_MAP);
        model.addAttribute("cardioExercises", CARDIO_DATA_LIST);

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
            int baseDifficultyXp = getExperiencePoints(exerciseIdentifier);
            int additionalXp = 0;
            if ("WEIGHT".equals(form.getType())) {
                additionalXp = calculateTotalVolumeXp(form);
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
}