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

    private final Map<String, List<ExerciseData>> exerciseMap = new LinkedHashMap<>();
    private final List<ExerciseData> cardioList = new ArrayList<>();

    public TrainingDataService() {
        initializeData();
    }

    private void initializeData() {
        // ==========================================
        // 胸 (Chest)
        // ==========================================
        List<ExerciseData> chest = new ArrayList<>();
        // バーベル
        chest.add(new ExerciseData("ベンチプレス", "大胸筋", "バーベル", "ベンチに仰向けになり、バーベルを胸の位置まで下ろし、垂直に押し上げる胸トレの王道種目。", "中級"));
        chest.add(new ExerciseData("インクライン・ベンチプレス", "大胸筋上部", "バーベル", "ベンチの背もたれを30-45度に起こして行うベンチプレス。鎖骨付近を狙ってバーを下ろす。", "中級"));
        chest.add(new ExerciseData("デクライン・ベンチプレス", "大胸筋下部", "バーベル", "頭側が低くなるように傾斜をつけて行うプレス。大胸筋下部のラインを作るのに有効。", "中級"));
        chest.add(new ExerciseData("ワイドグリップ・ベンチプレス", "大胸筋外側", "バーベル", "手幅を通常より広くして行う。可動域は狭くなるが大胸筋へのストレッチが強くかかる。", "中級"));
        chest.add(new ExerciseData("ナローグリップ・ベンチプレス", "大胸筋内側・三頭", "バーベル", "手幅を肩幅より狭くして行う。上腕三頭筋と大胸筋内側への刺激が強まる。", "中級"));
        chest.add(new ExerciseData("リバースグリップ・ベンチプレス", "大胸筋上部", "バーベル", "逆手でバーを持って行うプレス。大胸筋上部への刺激が増すが、セーフティバーの使用を推奨。", "上級"));
        chest.add(new ExerciseData("ランドマインプレス", "大胸筋上部", "バーベル", "バーベルの片側を固定し、反対側を片手または両手で斜め上に押し出す。肩と胸上部に効く。", "中級"));
        chest.add(new ExerciseData("フロアプレス", "大胸筋", "バーベル", "床に寝て行うベンチプレス。可動域が制限されるため、三頭筋への負荷が高まり、肩への負担が減る。", "中級"));
        chest.add(new ExerciseData("ギロチンプレス", "大胸筋上部", "バーベル", "首の近く（頸動脈付近）にバーを下ろす危険だが効果的な種目。必ず軽い重量で慎重に行うこと。", "上級"));
        // ダンベル
        chest.add(new ExerciseData("ダンベルプレス", "大胸筋", "ダンベル", "可動域を広くとれる基本種目。トップで胸を寄せる意識を持つと収縮感が高まる。", "中級"));
        chest.add(new ExerciseData("インクライン・ダンベルプレス", "大胸筋上部", "ダンベル", "傾斜をつけたベンチで行うダンベルプレス。大胸筋上部の厚みを作るのに最適。", "中級"));
        chest.add(new ExerciseData("デクライン・ダンベルプレス", "大胸筋下部", "ダンベル", "頭側を低くして行うプレス。胸の下部の輪郭を強調する。", "中級"));
        chest.add(new ExerciseData("ダンベルフライ", "大胸筋", "ダンベル", "肘を少し曲げ、円を描くようにダンベルを開閉する。ストレッチ種目の代表格。", "中級"));
        chest.add(new ExerciseData("インクライン・ダンベルフライ", "大胸筋上部", "ダンベル", "上部狙いのフライ。ストレッチポジションでしっかり胸を張る。", "中級"));
        chest.add(new ExerciseData("デクライン・ダンベルフライ", "大胸筋下部", "ダンベル", "下部狙いのフライ。", "中級"));
        chest.add(new ExerciseData("ダンベル・プルオーバー", "大胸筋・広背筋", "ダンベル", "ベンチに仰向けになり、ダンベルを頭の後ろへ下ろしてから胸の上へ戻す。胸郭を広げる効果も。", "中級"));
        chest.add(new ExerciseData("スクイーズプレス", "大胸筋内側", "ダンベル", "ダンベル同士を押し付け合いながらプレス動作を行う。常に胸の内側に力が入る。", "中級"));
        chest.add(new ExerciseData("アラウンド・ザ・ワールド", "大胸筋", "ダンベル", "ベンチに寝て、掌を上に向け、円を描くようにダンベルを頭上から腰横まで移動させる。", "中級"));
        // マシン
        chest.add(new ExerciseData("チェストプレス(マシン)", "大胸筋", "マシン", "軌道が固定されており安全に高重量を扱える。初心者から上級者までおすすめ。", "初級"));
        chest.add(new ExerciseData("インクライン・チェストプレス", "大胸筋上部", "マシン", "上部狙いのマシンプレス。", "初級"));
        chest.add(new ExerciseData("デクライン・チェストプレス", "大胸筋下部", "マシン", "下部狙いのマシンプレス。", "初級"));
        chest.add(new ExerciseData("ペックデックフライ", "大胸筋内側", "マシン", "座った状態で腕を閉じる動作を行う。胸の収縮を感じやすい種目。", "初級"));
        chest.add(new ExerciseData("リアデルト/ペックフライ", "大胸筋", "マシン", "ペックフライ機能を使用。肘をパッドに当てて閉じるタイプが多い。", "初級"));
        chest.add(new ExerciseData("スミス・ベンチプレス", "大胸筋", "スミスマシン", "軌道が固定されたバーベルプレス。バランスを崩す心配がなく胸への意識に集中できる。", "初級"));
        chest.add(new ExerciseData("スミス・インクラインプレス", "大胸筋上部", "スミスマシン", "スミスマシンを用いた上部狙いのプレス。", "初級"));
        chest.add(new ExerciseData("ハンマーストレングス・プレス", "大胸筋", "マシン", "片腕ずつ独立して動くマシン。左右差の解消や収縮感に優れる。", "中級"));
        // ケーブル・自重
        chest.add(new ExerciseData("ケーブルクロスオーバー(ハイ)", "大胸筋下部", "ケーブル", "高い位置から斜め下へ引き寄せる。下部の輪郭形成に有効。", "中級"));
        chest.add(new ExerciseData("ケーブルクロスオーバー(ミドル)", "大胸筋中部", "ケーブル", "真横から胸の前へ引き寄せる。パンプアップに最適。", "中級"));
        chest.add(new ExerciseData("ケーブルクロスオーバー(ロー)", "大胸筋上部", "ケーブル", "低い位置から斜め上へ引き上げる。上部の盛り上がりを作る。", "中級"));
        chest.add(new ExerciseData("プッシュアップ(腕立て伏せ)", "大胸筋", "自重", "基本の自重種目。体幹を真っ直ぐ保ち、胸を床に近づける。", "初級"));
        chest.add(new ExerciseData("インクライン・プッシュアップ", "大胸筋下部", "自重", "手が高い位置（ベンチ等）にある腕立て伏せ。負荷が軽く初心者向け。", "初級"));
        chest.add(new ExerciseData("デクライン・プッシュアップ", "大胸筋上部", "自重", "足が高い位置にある腕立て伏せ。負荷が高く上部に効く。", "中級"));
        chest.add(new ExerciseData("ディップス(大胸筋狙い)", "大胸筋下部", "自重/加重", "「上半身のスクワット」と呼ばれる。前傾姿勢で行うことで胸下部に強烈な刺激が入る。", "中級"));
        chest.add(new ExerciseData("スベンドプレス", "大胸筋内側", "プレート", "プレートを両手で挟み込み、胸の前で押し出す。収縮感を高める仕上げ種目。", "初級"));
        exerciseMap.put("胸", chest);

        // ==========================================
        // 背中 (Back)
        // ==========================================
        List<ExerciseData> back = new ArrayList<>();
        back.add(new ExerciseData("デッドリフト", "背中全体", "バーベル", "床からバーベルを引き上げる全身運動。背中の厚みと全体の筋力アップに最強の種目。", "上級"));
        back.add(new ExerciseData("ベントオーバーロウ", "広背筋・僧帽筋", "バーベル", "前傾姿勢でバーベルを腹部へ引き上げる。背中の厚みを作る基本種目。", "中級"));
        back.add(new ExerciseData("イェイツロウ", "広背筋下部", "バーベル", "逆手で行うベントオーバーロウ。上体をやや起こし気味に行う。", "中級"));
        back.add(new ExerciseData("ワンハンド・ダンベルロウ", "広背筋", "ダンベル", "ベンチに片手片膝をつき、片手でダンベルを引き上げる。可動域を広く取れる。", "中級"));
        back.add(new ExerciseData("懸垂(プルアップ)", "広背筋", "自重/加重", "順手で行う懸垂。背中の広がりを作るのに最も効果的な自重種目。", "中級"));
        back.add(new ExerciseData("チンアップ(逆手懸垂)", "広背筋・二頭筋", "自重/加重", "逆手で行う懸垂。二頭筋の関与が増えるが、広背筋下部にも効きやすい。", "中級"));
        back.add(new ExerciseData("ラットプルダウン(フロント)", "広背筋", "マシン", "マシンでバーを鎖骨付近に引き下ろす。背中の広がりを作る基本種目。", "初級"));
        back.add(new ExerciseData("ラットプルダウン(ビハインド)", "広背筋・大円筋", "マシン", "首の後ろにバーを下ろす。背中の上部や細かい筋肉に効くが、肩の柔軟性が必要。", "中級"));
        back.add(new ExerciseData("ラットプルダウン(Vバー)", "広背筋中部", "マシン", "パラレルグリップで行う。背中の厚みや下部への刺激が入りやすい。", "初級"));
        back.add(new ExerciseData("シーテッド・ケーブルロウ", "僧帽筋・広背筋", "ケーブル", "座った状態でケーブルを腹部へ引き寄せる。肩甲骨を寄せる意識で厚みを作る。", "初級"));
        back.add(new ExerciseData("Tバーロウ", "広背筋・僧帽筋", "バーベル", "専用マシンまたはランドマインを使用し、足の間にあるバーを引き上げる。", "中級"));
        back.add(new ExerciseData("ランドマインロウ", "広背筋", "バーベル", "バーの片側を固定して片手で行うロウイング。軌道が安定し収縮させやすい。", "中級"));
        back.add(new ExerciseData("ラックプル", "脊柱起立筋", "バーベル", "膝の高さ程度から行うデッドリフト。背中の筋肉に負荷を集中できる。", "上級"));
        back.add(new ExerciseData("グッドモーニング", "脊柱起立筋・ハム", "バーベル", "バーベルを担ぎ、お辞儀をするように上体を倒す。脊柱起立筋とハムストリングスを強化。", "中級"));
        back.add(new ExerciseData("シールロウ", "広背筋中部", "バーベル/ダンベル", "ベンチにうつ伏せになり、床からバーを引き上げる。腰への負担がなく背中に集中できる。", "中級"));
        back.add(new ExerciseData("メドウズロウ", "広背筋", "バーベル", "ランドマインのバーの先端を横から握り、肘を外に開くように引く。背中の上部・外側に効く。", "上級"));
        back.add(new ExerciseData("シュラッグ(バーベル)", "僧帽筋上部", "バーベル", "肩をすくめる動作で僧帽筋上部を鍛える。首周りの厚みを作る。", "中級"));
        back.add(new ExerciseData("シュラッグ(ダンベル)", "僧帽筋上部", "ダンベル", "ダンベルで行うシュラッグ。バーベルより可動域を広く取れる。", "中級"));
        back.add(new ExerciseData("ストレートアーム・プルダウン", "広背筋", "ケーブル", "肘を伸ばしたままケーブルを太ももまで下ろす。広背筋をアイソレートして鍛えられる。", "初級"));
        back.add(new ExerciseData("フェイスプル", "僧帽筋・リアデルト", "ケーブル", "ケーブルを顔の高さへ引き寄せる。肩の後ろや僧帽筋中部、ローテーターカフに有効。", "初級"));
        back.add(new ExerciseData("バックエクステンション", "脊柱起立筋", "自重/マシン", "背筋台で上体を起こす。脊柱起立筋の基本種目。", "初級"));
        back.add(new ExerciseData("インバーテッドロウ", "広背筋", "自重", "「斜め懸垂」。鉄棒やスミスマシンのバーにぶら下がり、体を斜めにして引き上げる。", "初級"));
        back.add(new ExerciseData("プルオーバー(マシン)", "広背筋", "マシン", "ノーチラスマシンなどで肘パッドを押し下げる。広背筋全体に強いストレッチと収縮を与える。", "初級"));
        back.add(new ExerciseData("ハイロウ(マシン)", "広背筋上部", "マシン", "斜め上から引き下ろす軌道を持つマシン。広背筋と大円筋を狙いやすい。", "初級"));
        back.add(new ExerciseData("スーパーマン", "脊柱起立筋", "自重", "床にうつ伏せになり、手足を同時に持ち上げる。自宅でできる背筋運動。", "初級"));
        exerciseMap.put("背中", back);

        // ==========================================
        // 脚 (Legs)
        // ==========================================
        List<ExerciseData> legs = new ArrayList<>();
        legs.add(new ExerciseData("バーベル・スクワット", "大腿四頭筋・全体", "バーベル", "「キング・オブ・エクササイズ」。下半身全体を強烈に鍛える。", "上級"));
        legs.add(new ExerciseData("フロントスクワット", "大腿四頭筋", "バーベル", "体の前でバーを保持して行うスクワット。上体が起きるため大腿四頭筋への負荷が高い。", "上級"));
        legs.add(new ExerciseData("ハックスクワット", "大腿四頭筋", "マシン", "背もたれに寄りかかって行うスクワット。腰への負担を減らし四頭筋を攻めることができる。", "中級"));
        legs.add(new ExerciseData("レッグプレス", "大腿四頭筋・臀部", "マシン", "高重量を扱えるマシン種目。足の位置で効く部位を変化させられる。", "初級"));
        legs.add(new ExerciseData("レッグエクステンション", "大腿四頭筋", "マシン", "座って膝を伸ばす単関節種目。大腿四頭筋をアイソレートして鍛える。", "初級"));
        legs.add(new ExerciseData("レッグカール(ライイング)", "ハムストリングス", "マシン", "うつ伏せで膝を曲げる。ハムストリングスの基本種目。", "初級"));
        legs.add(new ExerciseData("レッグカール(シーテッド)", "ハムストリングス", "マシン", "座って膝を曲げる。ストレッチポジションでの負荷がかかりやすい。", "初級"));
        legs.add(new ExerciseData("ルーマニアンデッドリフト", "ハム・臀部", "バーベル/ダンベル", "膝をあまり曲げず、お尻を突き出しながらバーを下ろす。もも裏とお尻のストレッチ種目。", "中級"));
        legs.add(new ExerciseData("ランジ", "大腿四頭筋・臀部", "ダンベル/自重", "片足を前に踏み込む。バランス感覚と下半身の筋力を養う。", "中級"));
        legs.add(new ExerciseData("ウォーキングランジ", "下半身全体", "自重/ダンベル", "前に進みながら行うランジ。心拍数も上がり脂肪燃焼効果も高い。", "中級"));
        legs.add(new ExerciseData("ブルガリアンスクワット", "大腿四頭筋・臀部", "ダンベル/自重", "片足をベンチに乗せて行うスクワット。お尻と前ももに強烈に効く。", "中級"));
        legs.add(new ExerciseData("ゴブレットスクワット", "大腿四頭筋", "ダンベル/KB", "胸の前でダンベル等を抱えて行うスクワット。フォーム習得やアップに最適。", "初級"));
        legs.add(new ExerciseData("スモウスクワット", "内転筋・臀部", "ダンベル/バーベル", "足を大きく広げて行うスクワット。内ももとお尻に効く。", "中級"));
        legs.add(new ExerciseData("ヒップスラスト", "大臀筋", "バーベル/マシン", "ベンチに背中を乗せ、腰にバーベルを乗せてお尻を持ち上げる。最強のお尻トレ。", "中級"));
        legs.add(new ExerciseData("カーフレイズ", "下腿三頭筋", "マシン/自重", "つま先立ちを繰り返す。ふくらはぎを鍛える。", "初級"));
        legs.add(new ExerciseData("アダクション", "内転筋", "マシン", "足を閉じる動作で内ももを鍛える。", "初級"));
        legs.add(new ExerciseData("アブダクション", "中臀筋", "マシン", "足を開く動作でお尻の横（中臀筋）を鍛える。", "初級"));
        legs.add(new ExerciseData("ピストルスクワット", "下半身全体", "自重", "片足で行うスクワット。高い筋力とバランス能力が必要な高難易度自重トレ。", "上級"));
        legs.add(new ExerciseData("シシースクワット", "大腿四頭筋", "自重", "上体を後ろに倒しながら膝を前に出す特殊なスクワット。四頭筋のストレッチに特化。", "中級"));
        legs.add(new ExerciseData("ステップアップ", "大腿四頭筋・臀部", "台/ダンベル", "台に片足で登る動作。お尻と太ももの引き締めに。", "初級"));
        legs.add(new ExerciseData("グルートブリッジ", "大臀筋", "自重", "仰向けになり膝を立ててお尻を持ち上げる。ヒップスラストの簡易版。", "初級"));
        legs.add(new ExerciseData("ケーブル・プルスルー", "ハム・臀部", "ケーブル", "股の間からケーブルを引き出す。お尻の引き締めに効果的。", "初級"));
        legs.add(new ExerciseData("ケトルベル・スイング", "ハム・臀部", "ケトルベル", "股関節の爆発的な伸展を使う全身運動。有酸素効果も高い。", "中級"));
        legs.add(new ExerciseData("シングルレッグ・デッドリフト", "ハム・体幹", "ダンベル/KB", "片足立ちで行うデッドリフト。バランス力ともも裏の強化。", "中級"));
        legs.add(new ExerciseData("ゼルチャースクワット", "大腿四頭筋・体幹", "バーベル", "肘でバーベルを抱えて行うスクワット。体幹への負荷が非常に高い。", "上級"));
        exerciseMap.put("脚", legs);

        // ==========================================
        // 肩 (Shoulders)
        // ==========================================
        List<ExerciseData> shoulders = new ArrayList<>();
        shoulders.add(new ExerciseData("ミリタリープレス", "三角筋全体", "バーベル", "立ってバーベルを頭上に押し上げる。肩の筋力と体幹を鍛える基本種目。", "中級"));
        shoulders.add(new ExerciseData("ショルダープレス(ダンベル)", "三角筋前・中部", "ダンベル", "ダンベルを頭上に押し上げる。可動域が広く、左右のバランスを整えられる。", "中級"));
        shoulders.add(new ExerciseData("アーノルドプレス", "三角筋前・中部", "ダンベル", "手首を回転させながら行うプレス。三角筋前部から中部まで広く刺激が入る。", "中級"));
        shoulders.add(new ExerciseData("サイドレイズ", "三角筋中部", "ダンベル", "腕を横に開く。肩幅（メロン肩）を作るための最重要種目。", "初級"));
        shoulders.add(new ExerciseData("ケーブル・サイドレイズ", "三角筋中部", "ケーブル", "ケーブルで行うサイドレイズ。動作中常に負荷が抜けにくい。", "中級"));
        shoulders.add(new ExerciseData("フロントレイズ", "三角筋前部", "ダンベル/バーベル", "腕を前に上げる。三角筋前部を個別に鍛える。", "初級"));
        shoulders.add(new ExerciseData("リアデルトフライ", "三角筋後部", "ダンベル", "前傾姿勢で腕を横に開く。肩の後ろ側を鍛え、立体感を出す。", "初級"));
        shoulders.add(new ExerciseData("フェイスプル", "三角筋後部", "ケーブル", "ケーブルを顔に向かって引く。リアデルトとローテーターカフに有効。", "初級"));
        shoulders.add(new ExerciseData("アップライトロウ", "三角筋中部・僧帽筋", "バーベル", "バーベルを顎の下まで引き上げる。肩と僧帽筋を同時に鍛える。", "中級"));
        shoulders.add(new ExerciseData("ショルダープレス(マシン)", "三角筋前・中部", "マシン", "安全に高重量を扱えるプレス系種目。", "初級"));
        shoulders.add(new ExerciseData("リアデルトフライ(マシン)", "三角筋後部", "マシン", "ペックデックマシンの逆向き使用。簡単にリアデルトに効かせられる。", "初級"));
        shoulders.add(new ExerciseData("ハンドスタンドプッシュアップ", "三角筋全体", "自重", "逆立ち腕立て伏せ。自重で行う最強の肩トレ。", "上級"));
        shoulders.add(new ExerciseData("パイクプッシュアップ", "三角筋前部", "自重", "腰を高く上げ「くの字」になって行う腕立て伏せ。肩に体重を乗せる。", "中級"));
        shoulders.add(new ExerciseData("プッシュプレス", "三角筋・瞬発力", "バーベル", "下半身の反動を使ってバーベルを挙げる。瞬発力向上と高重量トレーニングに。", "上級"));
        shoulders.add(new ExerciseData("ビハインドネックプレス", "三角筋中部", "バーベル", "首の後ろからバーを挙げる。三角筋中部に効くが、肩関節への負担に注意。", "上級"));
        shoulders.add(new ExerciseData("ランドマインプレス(片手)", "三角筋前部", "バーベル", "片手で行う斜め上へのプレス。肩への負担が比較的少なく安全。", "中級"));
        shoulders.add(new ExerciseData("Zプレス", "三角筋・体幹", "バーベル/ダンベル", "床に座って行うプレス。下半身の踏ん張りが使えないため体幹と肩の純粋な筋力が必要。", "中級"));
        shoulders.add(new ExerciseData("ボトムアッププレス", "三角筋・安定性", "ケトルベル", "ケトルベルを逆さにして行うプレス。握力と肩の安定性を極限まで高める。", "上級"));
        shoulders.add(new ExerciseData("インクライン・サイドレイズ", "三角筋中部", "ダンベル", "インクラインベンチに横向きに寝て行うレイズ。初動の負荷を高める。", "中級"));
        shoulders.add(new ExerciseData("Yレイズ", "三角筋中部・僧帽筋", "ダンベル", "Yの字に腕を上げる。僧帽筋下部と三角筋中部に効く。", "初級"));
        shoulders.add(new ExerciseData("シュラッグ", "僧帽筋", "ダンベル/バーベル", "肩をすくめる動作。首回りの迫力を出す。", "初級"));
        shoulders.add(new ExerciseData("キューバンプレス", "ローテーターカフ", "ダンベル", "アップライトロウとプレスを組み合わせた動き。肩のインナーマッスル強化。", "中級"));
        shoulders.add(new ExerciseData("ヘイロー(Halo)", "肩周り柔軟性", "ケトルベル/プレート", "頭の周りで重りを回す。肩甲骨の可動域向上とウォーミングアップに最適。", "初級"));
        exerciseMap.put("肩", shoulders);

        // ==========================================
        // 腕 (Arms)
        // ==========================================
        List<ExerciseData> arms = new ArrayList<>();
        // 二頭
        arms.add(new ExerciseData("バーベルカール", "上腕二頭筋", "バーベル", "高重量を扱える二頭筋の基本種目。", "中級"));
        arms.add(new ExerciseData("ダンベルカール", "上腕二頭筋", "ダンベル", "左右独立して行うカール。手首を捻る動作を入れると収縮感が増す。", "中級"));
        arms.add(new ExerciseData("インクライン・ダンベルカール", "上腕二頭筋(長頭)", "ダンベル", "傾斜ベンチに寝て行う。ストレッチ時に強い負荷がかかる。", "中級"));
        arms.add(new ExerciseData("プリチャーカール", "上腕二頭筋(短頭)", "EZバー/ダンベル", "肘を台に固定して行う。反動を使わずに二頭筋を孤立させる。", "初級"));
        arms.add(new ExerciseData("ハンマーカール", "上腕筋", "ダンベル", "縦持ちで行うカール。上腕筋と前腕を鍛え、腕の厚みを出す。", "中級"));
        arms.add(new ExerciseData("ケーブルカール", "上腕二頭筋", "ケーブル", "動作中常に負荷がかかり続ける。仕上げに最適。", "初級"));
        arms.add(new ExerciseData("コンセントレーションカール", "上腕二頭筋", "ダンベル", "座って膝に肘を当てて行う。収縮を意識しやすい。", "初級"));
        arms.add(new ExerciseData("スパイダーカール", "上腕二頭筋(短頭)", "ダンベル/EZバー", "インクラインベンチにうつ伏せになって行う。収縮ポジションでの負荷が強い。", "中級"));
        arms.add(new ExerciseData("ドラッグカール", "上腕二頭筋(長頭)", "バーベル", "バーを体に沿わせるように引き上げる。長頭に強い刺激が入る。", "中級"));
        arms.add(new ExerciseData("ゾットマンカール", "上腕二頭筋・前腕", "ダンベル", "上げる時は普通に、下ろす時は逆手で。二頭と前腕を同時に鍛える。", "中級"));
        arms.add(new ExerciseData("リバースカール", "前腕・上腕筋", "バーベル", "逆手で行うカール。前腕と上腕筋をターゲットにする。", "中級"));
        arms.add(new ExerciseData("21カール", "上腕二頭筋", "バーベル", "可動域を下半分・上半分・全体に分けて7回ずつ行う追い込み法。", "中級"));
        // 三頭
        arms.add(new ExerciseData("ナローベンチプレス", "上腕三頭筋", "バーベル", "手幅を狭くしたベンチプレス。三頭筋全体に高重量で負荷をかけられる。", "中級"));
        arms.add(new ExerciseData("スカルクラッシャー", "上腕三頭筋", "EZバー/ダンベル", "仰向けになり、額に向かってバーを下ろす。三頭筋の基本種目。", "中級"));
        arms.add(new ExerciseData("プレスダウン", "上腕三頭筋", "ケーブル", "ケーブルを押し下げる。バーやロープなどアタッチメントで刺激が変わる。", "初級"));
        arms.add(new ExerciseData("フレンチプレス", "上腕三頭筋(長頭)", "ダンベル/EZバー", "頭の後ろで重量を上げ下げする。長頭（二の腕の内側）によく効く。", "中級"));
        arms.add(new ExerciseData("キックバック", "上腕三頭筋", "ダンベル", "前傾姿勢で肘を伸ばす。収縮感が強く、軽い重量でも効かせられる。", "初級"));
        arms.add(new ExerciseData("ディップス(三頭筋狙い)", "上腕三頭筋", "自重/加重", "体を立てて行うディップス。三頭筋への負荷が集中する。", "中級"));
        arms.add(new ExerciseData("ベンチディップス", "上腕三頭筋", "自重", "ベンチに手をついて行うディップス。自宅でも行いやすい。", "初級"));
        arms.add(new ExerciseData("オーバーヘッドエクステンション", "上腕三頭筋(長頭)", "ケーブル/ダンベル", "頭上からケーブルを引く。ストレッチ刺激が強い。", "初級"));
        arms.add(new ExerciseData("テイトプレス", "上腕三頭筋", "ダンベル", "ベンチに寝て、ダンベルを胸の内側に向かって下ろす独特な種目。", "中級"));
        arms.add(new ExerciseData("JMプレス", "上腕三頭筋", "バーベル/スミス", "プレスとエクステンションの中間のような動き。高重量で三頭を破壊する。", "上級"));
        arms.add(new ExerciseData("ダイヤモンド・プッシュアップ", "上腕三頭筋", "自重", "手でダイヤ型を作って行う腕立て伏せ。三頭筋に強く効く。", "初級"));
        // 前腕
        arms.add(new ExerciseData("リストカール", "前腕屈筋群", "ダンベル/バーベル", "手首を巻き込む動き。前腕の内側を太くする。", "初級"));
        arms.add(new ExerciseData("リバース・リストカール", "前腕伸筋群", "ダンベル/バーベル", "手の甲側に手首を返す動き。前腕の外側を鍛える。", "初級"));
        arms.add(new ExerciseData("ファーマーズウォーク", "前腕・握力", "ダンベル/KB", "重いダンベルを持って歩く。握力と体幹、全身の連動性を鍛える。", "初級"));
        arms.add(new ExerciseData("プレートピンチ", "握力", "プレート", "プレートをつまんで持つ。ピンチ力を鍛える。", "初級"));
        exerciseMap.put("腕", arms);

        // ==========================================
        // 腹筋 (Abs)
        // ==========================================
        List<ExerciseData> abs = new ArrayList<>();
        abs.add(new ExerciseData("クランチ", "腹直筋上部", "自重", "膝を立てて寝転がり、肩甲骨が浮くまで上体を丸める基本種目。", "初級"));
        abs.add(new ExerciseData("シットアップ", "腹直筋", "自重", "足を押さえて上体を完全に起こす。腹筋全体と股関節屈筋群を使う。", "初級"));
        abs.add(new ExerciseData("プランク", "体幹", "自重", "肘とつま先で体を支えて静止する。体幹の安定性を高める。", "初級"));
        abs.add(new ExerciseData("サイドプランク", "腹斜筋", "自重", "横向きで体を支えるプランク。脇腹（腹斜筋）を鍛える。", "初級"));
        abs.add(new ExerciseData("マウンテンクライマー", "腹直筋・有酸素", "自重", "腕立て伏せの姿勢で足を交互に引き寄せる。脂肪燃焼効果も高い。", "初級"));
        abs.add(new ExerciseData("リバースクランチ", "腹直筋下部", "自重", "膝を胸に引き寄せ、お尻を浮かせる。骨盤の後傾動作を意識する。", "初級"));
        abs.add(new ExerciseData("アブドミナルクランチ(マシン)", "腹直筋", "マシン", "マシンで腹筋を行う。初心者でも効かせやすい。", "初級"));
        abs.add(new ExerciseData("ロータリートルソー", "腹斜筋", "マシン", "座って体を捻るマシン。脇腹を集中的に鍛える。", "初級"));
        abs.add(new ExerciseData("デッドバグ", "体幹", "自重", "仰向けで手足を対角に動かす。腰痛予防やドローインの練習に最適。", "初級"));
        abs.add(new ExerciseData("バードドッグ", "体幹", "自重", "四つん這いで手足を対角に伸ばす。背面の体幹安定性を高める。", "初級"));
        abs.add(new ExerciseData("ヒールタッチ", "腹斜筋", "自重", "仰向けで膝を立て、体を横に曲げてかかとに触れる。地味だが脇腹に効く。", "初級"));
        abs.add(new ExerciseData("レッグレイズ", "腹直筋下部", "自重", "仰向けで足を上下させる。下っ腹に効く。腰が浮かないように注意。", "中級"));
        abs.add(new ExerciseData("アブローラー(膝コロ)", "腹直筋全体", "器具", "膝をついてローラーを転がす。腹筋に対し非常に強い負荷がかかる。", "中級"));
        abs.add(new ExerciseData("ロシアンツイスト", "腹斜筋", "自重/加重", "上体を起こして座り、左右に体を捻る。くびれ作りに有効。", "中級"));
        abs.add(new ExerciseData("ケーブル・クランチ", "腹直筋", "ケーブル", "ケーブルを抱えて上体を丸める。加重しやすく筋肥大に向く。", "中級"));
        abs.add(new ExerciseData("ウッドチョッパー", "腹斜筋", "ケーブル", "木こりのようにケーブルを斜めに引く。回旋動作を鍛える。", "中級"));
        abs.add(new ExerciseData("Vシット", "腹直筋", "自重", "手足を同時に上げてV字を作る。瞬発的な腹筋力が必要。", "中級"));
        abs.add(new ExerciseData("バイシクルクランチ", "腹斜筋", "自重", "自転車を漕ぐように足を動かしながら対角の肘と膝を近づける。", "中級"));
        abs.add(new ExerciseData("パロフプレス", "対回旋筋(体幹)", "ケーブル/バンド", "横からの負荷に耐えながら手を前に伸ばす。捻じれを防ぐ「アンチローテーション」種目。", "中級"));
        abs.add(new ExerciseData("ランドマインツイスト", "腹斜筋", "バーベル", "バーベルの端を持って左右に振る。腹斜筋と全身の連動性を鍛える。", "中級"));
        abs.add(new ExerciseData("ハンギング・レッグレイズ", "腹直筋下部", "自重", "バーにぶら下がって足を持ち上げる。強度の高い下部種目。", "上級"));
        abs.add(new ExerciseData("アブローラー(立ちコロ)", "腹直筋全体", "器具", "立って行うアブローラー。最高強度の腹筋種目の一つ。", "上級"));
        abs.add(new ExerciseData("ドラゴンフラッグ", "腹直筋全体", "自重", "ブルース・リーが行っていた超高強度種目。背中だけを接点に体を一直線にする。", "上級"));
        abs.add(new ExerciseData("トゥ・トゥ・バー(Toes to Bar)", "腹直筋全体", "自重", "ぶら下がった状態からつま先をバーにタッチさせる。クロスフィットで人気。", "上級"));
        abs.add(new ExerciseData("Lシット", "体幹・屈筋", "自重", "体を支えて足を前に伸ばしL字で静止する。体操選手のような体幹が必要。", "上級"));
        abs.add(new ExerciseData("フラッグ(人間鯉のぼり)", "体幹全体", "自重", "柱を掴んで体を横に浮かす超高難易度パフォーマンス種目。", "上級"));
        exerciseMap.put("腹筋", abs);

        // ==========================================
        // 有酸素運動 (Cardio)
        // ==========================================
        cardioList.add(new ExerciseData("ランニング(屋外)", "全身", "自重", "屋外を走る。景色が変わるため気分転換にも良い。", "初級"));
        cardioList.add(new ExerciseData("ランニング(トレッドミル)", "全身", "マシン", "ジムのランニングマシン。ペース管理がしやすく天候に左右されない。", "初級"));
        cardioList.add(new ExerciseData("ジョギング", "全身", "自重", "会話ができる程度のペースで走る。脂肪燃焼やリカバリーに。", "初級"));
        cardioList.add(new ExerciseData("水中ウォーキング", "全身", "プール", "水の中を歩く。水圧によるマッサージ効果や適度な抵抗がある。", "初級"));
        cardioList.add(new ExerciseData("水泳(平泳ぎ)", "全身", "プール", "比較的長く泳ぎ続けやすい泳法。股関節の柔軟性も使う。", "初級"));
        cardioList.add(new ExerciseData("ウォーキング(傾斜あり)", "全身", "マシン", "トレッドミルで傾斜をつけて歩く。膝への負担を抑えつつ消費カロリーを稼げる。", "初級"));
        cardioList.add(new ExerciseData("ウォーキング(屋外)", "全身", "自重", "最も手軽な有酸素運動。通勤や散歩で日常的に取り入れたい。", "初級"));
        cardioList.add(new ExerciseData("エアロバイク", "下半身", "マシン", "自転車漕ぎマシン。膝や腰への負担が少なく長時間行いやすい。", "初級"));
        cardioList.add(new ExerciseData("リカンベントバイク", "下半身", "マシン", "背もたれ付きのバイク。腰に不安がある人でも安心。", "初級"));
        cardioList.add(new ExerciseData("クロストレーナー", "全身", "マシン", "手足を使って楕円軌道で動く。関節への衝撃が少なく全身運動になる。", "初級"));
        cardioList.add(new ExerciseData("シャドーボクシング", "全身", "自重", "鏡に向かってパンチを打つ。器具なしで場所を選ばずできる。", "初級"));
        cardioList.add(new ExerciseData("踏み台昇降", "下半身", "台", "台を昇り降りする。自宅でテレビを見ながらできる有酸素運動。", "初級"));
        cardioList.add(new ExerciseData("ジャンピングジャック", "全身", "自重", "手足を開閉しながらジャンプする。ウォーミングアップによく使われる。", "初級"));
        cardioList.add(new ExerciseData("スキーエルゴ(SkiErg)", "全身", "マシン", "スキーのストックを突く動作を行う。上半身メインの珍しい有酸素マシン。", "中級"));
        cardioList.add(new ExerciseData("縄跳び", "全身", "器具", "シンプルだが強度の高いジャンプ運動。ふくらはぎの強化と心肺機能向上に。", "中級"));
        cardioList.add(new ExerciseData("バーピー", "全身", "自重", "腕立て伏せからジャンプを繰り返す全身運動。１種目で全身を追い込める。", "中級"));
        cardioList.add(new ExerciseData("水泳(クロール)", "全身", "プール", "水の抵抗を利用した全身運動。関節への負担が少なく消費カロリーが高い。", "中級"));
        cardioList.add(new ExerciseData("ボクササイズ(サンドバッグ)", "全身", "器具", "パンチやキックを打ち込む。ストレス発散と脂肪燃焼に最適。", "中級"));
        cardioList.add(new ExerciseData("ステアクライマー", "下半身", "マシン", "無限に階段を登るマシン。お尻や脚の引き締めに効果絶大。", "中級"));
        cardioList.add(new ExerciseData("ローイングエルゴメーター", "全身", "マシン", "ボート漕ぎ運動。背中や全身の筋肉を使い、非常に高いカロリー消費を誇る。", "中級"));
        cardioList.add(new ExerciseData("スケーティングジャンプ", "下半身", "自重", "スケート選手のように左右にジャンプする。横方向の動きとお尻の強化に。", "中級"));
        cardioList.add(new ExerciseData("ケトルベル・スイング(有酸素)", "全身", "ケトルベル", "軽量のKBで高回数行うことで、有酸素運動としての効果を発揮する。", "中級"));
        cardioList.add(new ExerciseData("ターキッシュゲットアップ", "全身", "ケトルベル", "仰向けからKBを持ったまま立ち上がる。全身の連動性と体幹を鍛える機能的動作。", "上級"));
        cardioList.add(new ExerciseData("ダッシュ/スプリント", "全身", "自重", "全速力で走る。心肺機能と瞬発力を高める無酸素運動に近い有酸素。", "上級"));
        cardioList.add(new ExerciseData("スピンバイク(HIIT)", "下半身", "マシン", "高強度のバイク運動。短時間で追い込みたい時に最適。", "上級"));
        cardioList.add(new ExerciseData("アサルトバイク", "全身", "マシン", "手でハンドルを押し引きしながら漕ぐバイク。地獄のようなキツさで有名。", "上級"));
        cardioList.add(new ExerciseData("HIIT(バーピー等)", "全身", "自重", "高強度インターバルトレーニング。短時間で脂肪燃焼と持久力アップを狙う。", "上級"));
        cardioList.add(new ExerciseData("二重跳び", "全身", "器具", "縄跳びの強度を高めたバージョン。瞬発力が必要。", "上級"));
        cardioList.add(new ExerciseData("タバタ式トレーニング", "全身", "自重", "20秒全力・10秒休憩を8セット繰り返す。究極の時短トレーニング。", "上級"));
        cardioList.add(new ExerciseData("バトルロープ", "全身", "器具", "太いロープを波打たせる。上半身の筋持久力と心肺機能を猛烈に鍛える。", "上級"));
        cardioList.add(new ExerciseData("スレッドプッシュ(ソリ押し)", "全身", "器具", "重りを載せたソリを押して走る。下半身のパワーと心肺機能を極限まで高める。", "上級"));
    }

    // --- Getter Methods ---

    // 全てのフリーウェイト/マシン種目を部位ごとのMapで取得
    public Map<String, List<ExerciseData>> getFreeWeightExercises() {
        return exerciseMap;
    }

 // ドロップダウン表示用 (ExerciseDataのMap)
    public Map<String, List<ExerciseData>> getFreeWeightExercisesByPart() {
        return exerciseMap;  // 変換せずそのまま返す
    }
    
 // MySet作成フォームなどで使用する簡易Map (名前だけ)
    public Map<String, List<String>> getSimpleFreeWeightExercisesMap() {
        return exerciseMap.entrySet().stream()
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
        return new ArrayList<>(exerciseMap.keySet());
    }

    // 有酸素運動の全リストを取得 (ExerciseData型)
    public List<ExerciseData> getCardioExercises() {
        return cardioList;
    }
    
    // 有酸素運動の簡易リスト (String型)
    public List<String> getSimpleCardioExercisesList() {
        return cardioList.stream()
                .map(ExerciseData::getName)
                .collect(Collectors.toList());
    }

    // 種目名からExerciseDataを取得する便利メソッド
    public ExerciseData getExerciseDataByName(String name) {
        // フリーウェイトから検索
        for (List<ExerciseData> list : exerciseMap.values()) {
            for (ExerciseData ex : list) {
                if (ex.getName().equals(name) || ex.getFullName().equals(name)) {
                    return ex;
                }
            }
        }
        // 有酸素から検索
        for (ExerciseData ex : cardioList) {
            if (ex.getName().equals(name) || ex.getFullName().equals(name)) {
                return ex;
            }
        }
        return null;
    }
    
    // 種目名から部位を逆引きするメソッド
    public String findPartByExerciseName(String name) {
        for (Map.Entry<String, List<ExerciseData>> entry : exerciseMap.entrySet()) {
            for (ExerciseData ex : entry.getValue()) {
                if (ex.getName().equals(name) || ex.getFullName().equals(name)) {
                    return entry.getKey();
                }
            }
        }
        // 有酸素判定
        for (ExerciseData ex : cardioList) {
            if (ex.getName().equals(name) || ex.getFullName().equals(name)) {
                return "有酸素";
            }
        }
        return "その他";
    }
}