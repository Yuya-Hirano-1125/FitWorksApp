package com.example.demo.entity;

public enum AppTitle {
    
    // --- 1. 初心者・チュートリアル (3個) ---
    BEGINNER("駆け出し", "FitWorksへようこそ！旅の始まりです。", "COMMON"),
    FIRST_STEP("最初の一歩", "初めてトレーニングを記録した証。", "COMMON"),
    FASHIONISTA("おしゃれ番長", "初めて着せ替えを行った。", "COMMON"),

    // --- 2. 継続・ログイン (4個) ---
    THREE_DAYS_BOZU("三日坊主バスター", "3日連続でログインした。", "RARE"),
    WEEKLY_WARRIOR("週間戦士", "7日連続でログインした。", "SR"),
    HABIT_MASTER("習慣の達人", "30日連続でログインした。", "SSR"),
    PERSISTENCE("不屈の精神", "久しぶりにログインした（おかえり！）。", "COMMON"),

    // --- 3. レベル到達 (5個) ---
    ROOKIE_TRAINER("見習いトレーナー", "レベル5に到達した。", "COMMON"),
    INTERMEDIATE("中級トレーナー", "レベル10に到達した。", "RARE"),
    VETERAN("ベテラン", "レベル30に到達した。", "SR"),
    ELITE("エリート", "レベル50に到達した。", "SSR"),
    MUSCLE_GOD("筋肉の神", "レベル100に到達した。", "UR"),

    // --- 4. 部位別マスタリー (6個) ---
    CHEST_LOVER("大胸筋マニア", "胸のトレーニングを累計10回記録した。", "RARE"),
    BACK_DEMON("鬼の背中", "背中のトレーニングを累計10回記録した。", "RARE"),
    LEG_DAY_SURVIVOR("脚トレの生還者", "脚のトレーニングを累計10回記録した。", "RARE"),
    SHOULDER_KING("メロン肩", "肩のトレーニングを累計10回記録した。", "RARE"),
    ARM_WRESTLER("豪腕", "腕のトレーニングを累計10回記録した。", "RARE"),
    ABS_OF_STEEL("鋼の腹筋", "腹筋のトレーニングを累計10回記録した。", "RARE"),

    // --- 5. トレーニング時間・習慣 (4個) ---
    MORNING_ROUTINE("朝活勢", "朝（04:00-10:00）にトレーニングを記録した。", "COMMON"),
    NIGHT_OWL("深夜の住人", "深夜（22:00-04:00）にトレーニングを記録した。", "COMMON"),
    WEEKEND_HERO("週末のヒーロー", "土日にトレーニングを記録した。", "COMMON"),
    LUNCH_TIME_GYM("昼休みジム", "お昼（11:00-14:00）にトレーニングを記録した。", "COMMON"),

    // --- 6. 実績・回数 (4個) ---
    DILIGENT("努力家", "トレーニング記録数が合計50回を超えた。", "SR"),
    IRON_MAN("鉄人", "トレーニング記録数が合計100回を超えた。", "SSR"),
    LIMIT_BREAKER("限界突破", "1日で大量のXPを獲得した。", "SR"),
    MULTI_TASKER("マルチタスカー", "1日に3種類以上の部位を鍛えた。", "SR"),

    // --- 7. ソーシャル・その他 (4個) ---
    FRIENDLY("フレンドリー", "フレンドが1人以上できた。", "COMMON"),
    POPULAR("人気者", "フレンドが10人以上できた。", "SR"),
    RICH("大富豪", "ガチャを累計50回引いた。", "SSR"),
    AI_BEST_FRIEND("AIの親友", "AIコーチと10回以上会話した。", "RARE");

    private final String displayName;
    private final String description;
    private final String rarity; // COMMON, RARE, SR, SSR, UR

    AppTitle(String displayName, String description, String rarity) {
        this.displayName = displayName;
        this.description = description;
        this.rarity = rarity;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getRarity() { return rarity; }
}