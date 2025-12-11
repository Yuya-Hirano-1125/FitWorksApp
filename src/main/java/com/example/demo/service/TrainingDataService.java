package com.example.demo.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.ExerciseData;

@Service
public class TrainingDataService {

    private static final Map<String, List<ExerciseData>> EXERCISE_DATA_MAP = new LinkedHashMap<>();
    private static final List<ExerciseData> CARDIO_DATA_LIST = new ArrayList<>();

    // コンストラクタまたは静的ブロックでデータを初期化
    static {
        initializeExercises();
    }

    private static void initializeExercises() {
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

        // --- リカバリー・ケア ---
        List<ExerciseData> care = new ArrayList<>();
        care.add(new ExerciseData("フォームローラー(背中)", "全レベル",
            "フォームローラーを背中の下に置き、上下に転がる。",
            "背骨の矯正と起立筋の緊張緩和に効果的。呼吸を止めない。"));
        care.add(new ExerciseData("眼球運動(8の字)", "全レベル",
            "顔を動かさず、目だけで空中に大きな8の字を描く。右回り・左回りを行う。",
            "デジタルデバイスによる眼精疲労の回復。眼輪筋をほぐす。"));
        care.add(new ExerciseData("キャット＆カウ", "初級",
            "四つん這いになり、息を吐きながら背中を丸め、吸いながら反らす。",
            "自律神経を整え、背骨の柔軟性を高める。"));
        care.add(new ExerciseData("動的ストレッチ(股関節)", "初級",
            "壁に手を突き、片足を振り子のように前後左右に振る。",
            "トレーニング前の怪我予防。股関節の可動域を広げる。"));
        care.add(new ExerciseData("ホットアイケア", "全レベル",
            "温めたタオルを目元に乗せて5分間リラックスする。",
            "血流を促進し、副交感神経を優位にする。睡眠の質向上。"));
        EXERCISE_DATA_MAP.put("リカバリー・ケア", care);

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
        CARDIO_DATA_LIST.add(new ExerciseData("タバタ式バーピー", "上級",
            "20秒全力バーピー＋10秒休憩を8セット。",
            "短時間でVO2Max（最大酸素摂取量）を極限まで高める。"));
        CARDIO_DATA_LIST.add(new ExerciseData("LSD (Long Slow Distance)", "中級",
            "会話ができる程度のペースで60分以上走り続ける。",
            "毛細血管を増やし、基礎的な持久力と回復力を高める。"));
    }

    public Map<String, List<ExerciseData>> getFreeWeightExercises() {
        return EXERCISE_DATA_MAP;
    }

    public List<ExerciseData> getCardioExercises() {
        return CARDIO_DATA_LIST;
    }

    // 文字列リストを返す（Controller互換用）
    public List<String> getSimpleCardioExercisesList() {
        return CARDIO_DATA_LIST.stream()
                .map(ExerciseData::getFullName)
                .collect(Collectors.toList());
    }
    
    /**
     * 部位ごとの種目名リストを返す（Map<String, List<String>>）
     */
    public Map<String, List<String>> getFreeWeightExercisesByPart() {
        Map<String, List<String>> simpleMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<ExerciseData>> entry : EXERCISE_DATA_MAP.entrySet()) {
            List<String> simpleList = entry.getValue().stream()
                .map(ExerciseData::getFullName)
                .collect(Collectors.toList());
            simpleMap.put(entry.getKey(), simpleList);
        }
        return simpleMap;
    }

    // 後方互換性のため
    public Map<String, List<String>> getSimpleFreeWeightExercisesMap() {
        return getFreeWeightExercisesByPart();
    }
    
    public List<ExerciseData> getRecoveryExercises() {
        return EXERCISE_DATA_MAP.getOrDefault("リカバリー・ケア", new ArrayList<>());
    }

    public List<String> getMuscleParts() {
        return new ArrayList<>(EXERCISE_DATA_MAP.keySet());
    }

    public ExerciseData getExerciseDataByName(String name) {
        // フリーウェイトから検索
        for (List<ExerciseData> list : EXERCISE_DATA_MAP.values()) {
            for (ExerciseData ex : list) {
                if (ex.getFullName().equals(name)) {
                    return ex;
                }
            }
        }
        // 有酸素から検索
        for (ExerciseData ex : CARDIO_DATA_LIST) {
            if (ex.getFullName().equals(name)) {
                return ex;
            }
        }
        return null;
    }

    // ★追加: 種目名から「部位名」を取得するメソッド
    public String findPartByExerciseName(String exerciseName) {
        for (Map.Entry<String, List<ExerciseData>> entry : EXERCISE_DATA_MAP.entrySet()) {
            for (ExerciseData ex : entry.getValue()) {
                if (ex.getFullName().equals(exerciseName)) {
                    return entry.getKey();
                }
            }
        }
        // 有酸素の場合は「有酸素」として返す
        for (ExerciseData ex : CARDIO_DATA_LIST) {
            if (ex.getFullName().equals(exerciseName)) {
                return "有酸素";
            }
        }
        return "その他";
    }
}