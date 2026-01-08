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
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (0, 'エンバーハート', 'fire', '★1', 1, 0, '/img/character/0.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (10, 'ドラコ', 'fire', '★2', 10, 5, '/img/character/10.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (20, 'ドラコス', 'fire', '★3', 20, 10, '/img/character/20.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (30, 'ドラグノイド', 'fire', '★4', 30, 20, '/img/character/30.png');

-- 水属性 (Water)
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (40, 'ルーナドロップ', 'water', '★1', 40, 10, '/img/character/40.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (50, 'ドリー', 'water', '★2', 50, 15, '/img/character/50.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (60, 'ドルフィ', 'water', '★3', 60, 20, '/img/character/60.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (70, 'ドルフィナス', 'water', '★4', 70, 25, '/img/character/70.png');

-- 草属性 (Grass)
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (80, 'フォリアン', 'grass', '★1', 80, 10, '/img/character/80.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (90, 'シル', 'grass', '★2', 90, 15, '/img/character/90.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (100, 'シルファ', 'grass', '★3', 100, 20, '/img/character/100.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (110, 'シルフィナ', 'grass', '★4', 110, 25, '/img/character/110.png');

-- 光属性 (Light)
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (120, 'ハローネスト', 'light', '★1', 120, 10, '/img/character/120.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (130, 'メリー', 'light', '★2', 130, 15, '/img/character/130.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (140, 'メリル', 'light', '★3', 140, 20, '/img/character/140.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (150, 'メリノア', 'light', '★4', 150, 25, '/img/character/150.png');

-- 闇属性 (Dark)
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (160, 'ネビュリス', 'dark', '★1', 160, 10, '/img/character/160.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (170, 'ロービ', 'dark', '★2', 170, 15, '/img/character/170.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (180, 'ローバス', 'dark', '★3', 180, 20, '/img/character/180.png');
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (190, 'ロービアス', 'dark', '★4', 190, 25, '/img/character/190.png');

-- シークレット (Secret)
INSERT OR IGNORE INTO characters (id, name, attribute, rarity, required_level, unlock_cost, image_path)
VALUES (250, 'シークレット', 'secret', '★5', 250, 50, '/img/AIcoach.png');


-- ▼ 背景アイテムデータ (background_items)
-- カテゴリ: nature (レベル解放系)
INSERT INTO background_items (background_id, category, bgname, bg_code, bgimgurl, user_level, has_material, required_material_id, required_material_name)
VALUES ('fire', 'nature', '炎の世界', 'fire-original', '/img/background/fire-original.png', 1, false, NULL, NULL);

INSERT INTO background_items (background_id, category, bgname, bg_code, bgimgurl, user_level, has_material, required_material_id, required_material_name)
VALUES ('water', 'nature', '水の世界', 'water-original', '/img/background/water-original.png', 40, false, NULL, NULL);

INSERT INTO background_items (background_id, category, bgname, bg_code, bgimgurl, user_level, has_material, required_material_id, required_material_name)
VALUES ('grass', 'nature', '木の世界', 'grass-original', '/img/background/grass-original.png', 70, false, NULL, NULL);

INSERT INTO background_items (background_id, category, bgname, bg_code, bgimgurl, user_level, has_material, required_material_id, required_material_name)
VALUES ('light', 'nature', '光の世界', 'light-original', '/img/background/light-original.png', 100, false, NULL, NULL);

INSERT INTO background_items (background_id, category, bgname, bg_code, bgimgurl, user_level, has_material, required_material_id, required_material_name)
VALUES ('dark', 'nature', '闇の世界', 'dark-original', '/img/background/dark-original.png', 130, false, NULL, NULL);

-- カテゴリ: special (アイテム解放系)
INSERT INTO background_items (background_id, category, bgname, bg_code, bgimgurl, user_level, has_material, required_material_id, required_material_name)
VALUES ('classroom', 'special', '教室', 'classroom', '/img/background/classroom.png', 0, true, 16, '夢幻の鍵');

INSERT INTO background_items (background_id, category, bgname, bg_code, bgimgurl, user_level, has_material, required_material_id, required_material_name)
VALUES ('gaming', 'special', 'ゲーミングルーム', 'gaming-room', '/img/background/gaming-room.png', 0, true, 16, '夢幻の鍵');

INSERT INTO background_items (background_id, category, bgname, bg_code, bgimgurl, user_level, has_material, required_material_id, required_material_name)
VALUES ('server', 'special', 'サーバールーム', 'server-room', '/img/background/server-room.png', 0, true, 16, '夢幻の鍵');

INSERT INTO background_items (background_id, category, bgname, bg_code, bgimgurl, user_level, has_material, required_material_id, required_material_name)
VALUES ('town', 'special', '町並み', 'town-road', '/img/background/town-road.png', 0, true, 16, '夢幻の鍵');

INSERT INTO background_items (background_id, category, bgname, bg_code, bgimgurl, user_level, has_material, required_material_id, required_material_name)
VALUES ('snow', 'special', '雪道', 'snow-road', '/img/background/snow-road.png', 0, true, 16, '夢幻の鍵');

INSERT INTO background_items (background_id, category, bgname, bg_code, bgimgurl, user_level, has_material, required_material_id, required_material_name)
VALUES ('forest', 'special', '深い森', 'deep-forest', '/img/background/deep-forest.png', 0, true, 16, '夢幻の鍵');
