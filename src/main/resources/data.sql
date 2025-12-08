-- 既存データがあっても重複エラーにならないように OR IGNORE を使用
INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('紅玉', '/img/item/R-red.png', 'R');
INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('蒼玉', '/img/item/R-blue.png', 'R');
INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('翠玉', '/img/item/R-green.png', 'R');
INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('聖玉', '/img/item/R-yellow.png', 'R');
INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('闇玉', '/img/item/R-purple.png', 'R');

INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('赤の聖結晶', '/img/item/SR-red.png', 'SR');
INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('青の聖結晶', '/img/item/SR-blue.png', 'SR');
INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('緑の聖結晶', '/img/item/SR-green.png', 'SR');
INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('黄の聖結晶', '/img/item/SR-yellow.png', 'SR');
INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('紫の聖結晶', '/img/item/SR-purple.png', 'SR');

INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('赫焔鱗', '/img/item/SSR-red.png', 'SSR');
INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('氷華の杖', '/img/item/SSR-blue.png', 'SSR');
INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('緑晶灯', '/img/item/SSR-green.png', 'SSR');
INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('夢紡ぎの枕', '/img/item/SSR-yellow.png', 'SSR');
INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('月詠みの杖', '/img/item/SSR-purple.png', 'SSR');

INSERT OR IGNORE INTO item (name, image_path, type) VALUES ('虹の神結晶', '/img/item/UR-niji.png', 'UR');
