-- 既存データがあっても重複エラーにならないように OR IGNORE を使用
INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('紅玉', '/img/item/R-red.png', '素材', 'R');
INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('蒼玉', '/img/item/R-blue.png', '素材', 'R');
INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('翠玉', '/img/item/R-green.png', '素材', 'R');
INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('聖玉', '/img/item/R-yellow.png', '素材', 'R');
INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('闇玉', '/img/item/R-purple.png', '素材', 'R');

INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('赤の聖結晶', '/img/item/SR-red.png', '素材', 'SR');
INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('青の聖結晶', '/img/item/SR-blue.png', '素材', 'SR');
INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('緑の聖結晶', '/img/item/SR-green.png', '素材', 'SR');
INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('黄の聖結晶', '/img/item/SR-yellow.png', '素材', 'SR');
INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('紫の聖結晶', '/img/item/SR-purple.png', '素材', 'SR');

INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('赫焔鱗', '/img/item/SSR-red.png', '素材', 'SSR');
INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('氷華の杖', '/img/item/SSR-blue.png', '武器', 'SSR');
INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('緑晶灯', '/img/item/SSR-green.png', '装飾', 'SSR');
INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('夢紡ぎの枕', '/img/item/SSR-yellow.png', '装飾', 'SSR');
INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('月詠みの杖', '/img/item/SSR-purple.png', '武器', 'SSR');

INSERT OR IGNORE INTO item (name, image_path, type, rarity) VALUES ('虹の神結晶', '/img/item/UR-niji.png', '素材', 'UR');
