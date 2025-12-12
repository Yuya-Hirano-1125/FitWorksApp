-- R（レア）素材
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('紅玉', '/img/item/R-red.png', '素材', 'R', 1);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('蒼玉', '/img/item/R-blue.png', '素材', 'R', 2);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('翠玉', '/img/item/R-green.png', '素材', 'R', 3);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('聖玉', '/img/item/R-yellow.png', '素材', 'R', 4);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('闇玉', '/img/item/R-purple.png', '素材', 'R', 5);

-- SR（スーパーレア）素材
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('赤の聖結晶', '/img/item/SR-red.png', '素材', 'SR', 6);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('青の聖結晶', '/img/item/SR-blue.png', '素材', 'SR', 7);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('緑の聖結晶', '/img/item/SR-green.png', '素材', 'SR', 8);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('黄の聖結晶', '/img/item/SR-yellow.png', '素材', 'SR', 9);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('紫の聖結晶', '/img/item/SR-purple.png', '素材', 'SR', 10);

-- SSR（スーパースーパーレア）素材
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('赫焔鱗', '/img/item/SSR-red.png', '素材', 'SSR', 11);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('氷華の杖', '/img/item/SSR-blue.png', '素材', 'SSR', 12);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('緑晶灯', '/img/item/SSR-green.png', '素材', 'SSR', 13);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('夢紡ぎの枕', '/img/item/SSR-yellow.png', '素材', 'SSR', 14);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('月詠みの杖', '/img/item/SSR-purple.png', '素材', 'SSR', 15);

-- UR（ウルトラレア）素材
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('夢幻の鍵', '/img/item/UR-niji.png', '素材', 'UR', 16);

-- --------------------------------------------------------
-- キャラクターデータ (CharactersUnlock.htmlに基づく)
-- IDはHTML内の value="10" 等や画像ファイル名に準拠
-- --------------------------------------------------------

-- 炎属性 (Fire)
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (0, 'エンバーハート', '/img/character/0.png', 'fire', '★1', 1, 0);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (10, 'ドラコ', '/img/character/10.png', 'fire', '★2', 10, 5);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (20, 'ドラコス', '/img/character/20.png', 'fire', '★3', 20, 10);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (30, 'ドラグノイド', '/img/character/30.png', 'fire', '★4', 30, 20);

-- 水属性 (Water)
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (40, 'ルーナドロップ', '/img/character/40.png', 'water', '★1', 40, 10);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (50, 'ドリー', '/img/character/50.png', 'water', '★2', 50, 15);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (60, 'ドルフィ', '/img/character/60.png', 'water', '★3', 60, 20);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (70, 'ドルフィナス', '/img/character/70.png', 'water', '★4', 70, 25);

-- 草属性 (Grass)
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (80, 'フォリアン', '/img/character/80.png', 'grass', '★1', 80, 10);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (90, 'シル', '/img/character/90.png', 'grass', '★2', 90, 15);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (100, 'シルファ', '/img/character/100.png', 'grass', '★3', 100, 20);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (110, 'シルフィナ', '/img/character/110.png', 'grass', '★4', 110, 25);
-- ========================================================
-- 1. マスタデータ (アイテム & キャラクター)
-- ========================================================

-- ▼ アイテムデータ (itemテーブル)
-- R（レア）素材
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('紅玉', '/img/item/R-red.png', '素材', 'R', 1);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('蒼玉', '/img/item/R-blue.png', '素材', 'R', 2);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('翠玉', '/img/item/R-green.png', '素材', 'R', 3);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('聖玉', '/img/item/R-yellow.png', '素材', 'R', 4);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('闇玉', '/img/item/R-purple.png', '素材', 'R', 5);

-- SR（スーパーレア）素材
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('赤の聖結晶', '/img/item/SR-red.png', '素材', 'SR', 6);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('青の聖結晶', '/img/item/SR-blue.png', '素材', 'SR', 7);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('緑の聖結晶', '/img/item/SR-green.png', '素材', 'SR', 8);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('黄の聖結晶', '/img/item/SR-yellow.png', '素材', 'SR', 9);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('紫の聖結晶', '/img/item/SR-purple.png', '素材', 'SR', 10);

-- SSR（スーパースーパーレア）素材
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('赫焔鱗', '/img/item/SSR-red.png', '素材', 'SSR', 11);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('氷華の杖', '/img/item/SSR-blue.png', '素材', 'SSR', 12);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('緑晶灯', '/img/item/SSR-green.png', '素材', 'SSR', 13);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('夢紡ぎの枕', '/img/item/SSR-yellow.png', '素材', 'SSR', 14);
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('月詠みの杖', '/img/item/SSR-purple.png', '素材', 'SSR', 15);

-- UR（ウルトラレア）素材
INSERT OR IGNORE INTO item (name, image_path, type, rarity, sort_order) VALUES ('夢幻の鍵', '/img/item/UR-niji.png', '素材', 'UR', 16);


-- ▼ キャラクターデータ (charactersテーブル)
-- 炎属性 (Fire)
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (0, 'エンバーハート', '/img/character/0.png', 'fire', '★1', 1, 0);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (10, 'ドラコ', '/img/character/10.png', 'fire', '★2', 10, 5);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (20, 'ドラコス', '/img/character/20.png', 'fire', '★3', 20, 10);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (30, 'ドラグノイド', '/img/character/30.png', 'fire', '★4', 30, 20);

-- 水属性 (Water)
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (40, 'ルーナドロップ', '/img/character/40.png', 'water', '★1', 40, 10);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (50, 'ドリー', '/img/character/50.png', 'water', '★2', 50, 15);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (60, 'ドルフィ', '/img/character/60.png', 'water', '★3', 60, 20);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (70, 'ドルフィナス', '/img/character/70.png', 'water', '★4', 70, 25);

-- 草属性 (Grass)
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (80, 'フォリアン', '/img/character/80.png', 'grass', '★1', 80, 10);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (90, 'シル', '/img/character/90.png', 'grass', '★2', 90, 15);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (100, 'シルファ', '/img/character/100.png', 'grass', '★3', 100, 20);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (110, 'シルフィナ', '/img/character/110.png', 'grass', '★4', 110, 25);

-- 光属性 (Light)
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (120, 'ハローネスト', '/img/character/120.png', 'light', '★1', 120, 10);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (130, 'メリー', '/img/character/130.png', 'light', '★2', 130, 15);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (140, 'メリル', '/img/character/140.png', 'light', '★3', 140, 20);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (150, 'メリノア', '/img/character/150.png', 'light', '★4', 150, 25);

-- 闇属性 (Dark)
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (160, 'ネビュリス', '/img/character/160.png', 'dark', '★1', 160, 10);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (170, 'ロービ', '/img/character/170.png', 'dark', '★2', 170, 15);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (180, 'ローバス', '/img/character/180.png', 'dark', '★3', 180, 20);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (190, 'ロービアス', '/img/character/190.png', 'dark', '★4', 190, 25);

-- シークレット (Secret/Dark)
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (250, 'シークレット', '/img/placeholder_final.png', 'dark', '???', 250, 50);


-- ========================================================
-- 2. ユーザーデータ (usersテーブル) ※これを先に作る必要があります
-- ========================================================
-- テスト用ユーザーID: 1 を作成 (Lv.100)
INSERT OR IGNORE INTO users (id, username, password, email, level, xp) 
VALUES (1, 'テスト勇者', 'password', 'test@example.com', 100, 0);


-- ========================================================
-- 3. 所持データ (user_items, user_characters) ※ユーザー作成後に実行
-- ========================================================

-- ▼ 所持アイテム (user_itemsテーブル)
-- ユーザーID:1 に素材を持たせる
INSERT OR IGNORE INTO user_items (user_id, item_id, quantity, acquired_at) VALUES (1, 1, 50, CURRENT_TIMESTAMP); -- 紅玉
INSERT OR IGNORE INTO user_items (user_id, item_id, quantity, acquired_at) VALUES (1, 2, 50, CURRENT_TIMESTAMP); -- 蒼玉
INSERT OR IGNORE INTO user_items (user_id, item_id, quantity, acquired_at) VALUES (1, 3, 50, CURRENT_TIMESTAMP); -- 翠玉
INSERT OR IGNORE INTO user_items (user_id, item_id, quantity, acquired_at) VALUES (1, 4, 50, CURRENT_TIMESTAMP); -- 聖玉
INSERT OR IGNORE INTO user_items (user_id, item_id, quantity, acquired_at) VALUES (1, 5, 50, CURRENT_TIMESTAMP); -- 闇玉

-- ▼ 所持キャラクター (user_charactersテーブル)
-- ユーザーID:1 が初期キャラ(エンバーハートなど)を持っている状態にする
-- これがないと「解放済みID」が取得できず画面エラーになります
INSERT OR IGNORE INTO user_characters (user_id, character_id, obtained_at) VALUES (1, 0, CURRENT_TIMESTAMP);   -- エンバーハート(ID:0)
INSERT OR IGNORE INTO user_characters (user_id, character_id, obtained_at) VALUES (1, 40, CURRENT_TIMESTAMP);  -- ルーナドロップ(ID:40)
INSERT OR IGNORE INTO user_characters (user_id, character_id, obtained_at) VALUES (1, 80, CURRENT_TIMESTAMP);  -- フォリアン(ID:80)
INSERT OR IGNORE INTO user_characters (user_id, character_id, obtained_at) VALUES (1, 120, CURRENT_TIMESTAMP); -- ハローネスト(ID:120)
INSERT OR IGNORE INTO user_characters (user_id, character_id, obtained_at) VALUES (1, 160, CURRENT_TIMESTAMP); -- ネビュリス(ID:160)
-- 光属性 (Light)
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (120, 'ハローネスト', '/img/character/120.png', 'light', '★1', 120, 10);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (130, 'メリー', '/img/character/130.png', 'light', '★2', 130, 15);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (140, 'メリル', '/img/character/140.png', 'light', '★3', 140, 20);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (150, 'メリノア', '/img/character/150.png', 'light', '★4', 150, 25);

-- 闇属性 (Dark)
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (160, 'ネビュリス', '/img/character/160.png', 'dark', '★1', 160, 10);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (170, 'ロービ', '/img/character/170.png', 'dark', '★2', 170, 15);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (180, 'ローバス', '/img/character/180.png', 'dark', '★3', 180, 20);
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (190, 'ロービアス', '/img/character/190.png', 'dark', '★4', 190, 25);

-- シークレット (Secret/Dark)
INSERT OR IGNORE INTO characters (id, name, image_path, attribute, rarity, required_level, unlock_cost) VALUES (250, 'シークレット', '/img/placeholder_final.png', 'dark', '???', 250, 50);

-- ユーザーテーブル（レベル管理）
CREATE TABLE IF NOT EXISTS user_data (
    user_id INTEGER PRIMARY KEY,
    name TEXT,
    level INTEGER DEFAULT 1,
    money INTEGER DEFAULT 0
);

-- ユーザー所持アイテム（素材管理）
CREATE TABLE IF NOT EXISTS user_item (
    user_id INTEGER,
    item_id INTEGER,
    quantity INTEGER DEFAULT 0,
    PRIMARY KEY (user_id, item_id),
    FOREIGN KEY (user_id) REFERENCES user_data(user_id),
    FOREIGN KEY (item_id) REFERENCES item(rowid)
);

-- ユーザー所持キャラクター（Storage用）
CREATE TABLE IF NOT EXISTS user_character (
    user_id INTEGER,
    character_id INTEGER,
    obtained_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, character_id),
    FOREIGN KEY (user_id) REFERENCES user_data(user_id),
    FOREIGN KEY (character_id) REFERENCES character(id)
);

-- --------------------------------------------------------
-- テストプレイ用データ (ユーザーID: 1)
-- --------------------------------------------------------

-- 1. ユーザーを作成 (レベル100にしておく)
-- ※usersテーブルのカラム名は実際のEntityに合わせて調整してください
INSERT OR IGNORE INTO users (id, username, password, email, level, xp) 
VALUES (1, '勇者', 'password', 'test@example.com', 100, 0);

-- 2. アイテムを持たせる (紅玉: ID=1 を 50個)
-- ※炎属性キャラの解放には ID:1 のアイテムが必要です
INSERT OR IGNORE INTO user_items (user_id, item_id, quantity, acquired_at) 
VALUES (1, 1, 50, CURRENT_TIMESTAMP);

-- ※他の属性用も必要なら追加 (蒼玉:2, 翠玉:3, 聖玉:4, 闇玉:5)
INSERT OR IGNORE INTO user_items (user_id, item_id, quantity, acquired_at) VALUES (1, 2, 50, CURRENT_TIMESTAMP);
INSERT OR IGNORE INTO user_items (user_id, item_id, quantity, acquired_at) VALUES (1, 3, 50, CURRENT_TIMESTAMP);
INSERT OR IGNORE INTO user_items (user_id, item_id, quantity, acquired_at) VALUES (1, 4, 50, CURRENT_TIMESTAMP);
INSERT OR IGNORE INTO user_items (user_id, item_id, quantity, acquired_at) VALUES (1, 5, 50, CURRENT_TIMESTAMP);