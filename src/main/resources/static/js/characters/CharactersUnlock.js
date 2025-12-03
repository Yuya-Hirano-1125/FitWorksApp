/* =========================================================
   CharactersUnlock.js - 完全修正版
   ・ポップアップ位置ズレ防止（画面中央固定）
   ・属性間ロック機能（前の属性クリア必須）
   ========================================================= */

/* 1. ゲームデータの初期化 */
window.gameState = {
    level: 1,
    stones: {
        fire:  { r: 0, sr: 0, ssr: 0 },
        water: { r: 0, sr: 0, ssr: 0 },
        grass: { r: 0, sr: 0, ssr: 0 },
        light: { r: 0, sr: 0, ssr: 0 },
        dark:  { r: 0, sr: 0, ssr: 0 }
    }
};

/* 2. 画像パスの設定 */
const MATERIAL_IMAGES = {
    fire: {
        r:   '/img/item/R-red.png',
        sr:  '/img/item/SR-red.png',
        ssr: '/img/item/SSR-red.png'
    },
    water: {
        r:   '/img/item/R-blue.png',
        sr:  '/img/item/SR-blue.png',
        ssr: '/img/item/SSR-blue.png'
    },
    grass: {
        r:   '/img/materials/grass_r.png',
        sr:  '/img/materials/grass_sr.png',
        ssr: '/img/materials/grass_ssr.png'
    },
    light: {
        r:   '/img/materials/light_r.png',
        sr:  '/img/materials/light_sr.png',
        ssr: '/img/materials/light_ssr.png'
    },
    dark: {
        r:   '/img/materials/dark_r.png',
        sr:  '/img/materials/dark_sr.png',
        ssr: '/img/materials/dark_ssr.png'
    }
};

/* 初期化処理 */
document.addEventListener('DOMContentLoaded', () => {
    updateUI();
});

/* 3. 進化レシピ判定 */
function getEvolutionRecipe(targetStar) {
    if (targetStar <= 2) return []; // ★2: 素材不要
    if (targetStar === 3) return [{ rank: 'r', count: 3 }]; // ★3: R3個
    if (targetStar >= 4) return [
        { rank: 'r',   count: 5 },
        { rank: 'sr',  count: 3 },
        { rank: 'ssr', count: 1 }
    ]; // ★4: フルセット
    return [];
}

/* 4. メイン UI更新関数 */
function updateUI() {
    // ステータスバー更新
    const levelEl = document.getElementById('current-level');
    if(levelEl) levelEl.textContent = window.gameState.level;
    
    // 素材数更新
    ['fire', 'water', 'grass', 'light', 'dark'].forEach(type => {
        // オブジェクトの中身を合計して表示、または代表値を表示（ここでは仮にRの数を出すなど）
        // ※HTML側のID構造によりますが、ここではシンプルにRの数を出す例、あるいは合計
        const el = document.getElementById(`stone-${type}`);
        if(el && window.gameState.stones[type]) {
            // 簡易的にR/SR/SSRの合計を表示（必要に応じて変更してください）
            const s = window.gameState.stones[type];
            el.textContent = s.r + s.sr + s.ssr;
        }
    });

    // --- トラック（属性）ごとの処理 ---
    const tracks = document.querySelectorAll('.pass-track');

    tracks.forEach((track, trackIndex) => {
        const nodes = track.querySelectorAll('.pass-node');
        
        // 【追加機能】前の属性をコンプリートしているか判定
        let isAttributeUnlocked = true;
        if (trackIndex > 0) {
            // ひとつ前のトラックを取得
            const prevTrack = tracks[trackIndex - 1];
            const prevNodes = prevTrack.querySelectorAll('.pass-node');
            // 前トラックの最後のキャラ
            const lastNodeOfPrev = prevNodes[prevNodes.length - 1];
            
            // 最後のキャラが「claimed（進化済み）」でなければ、今の属性はまだロック
            if (!lastNodeOfPrev.classList.contains('claimed')) {
                isAttributeUnlocked = false;
            }
        }

        // 最初のノード（卵）の挑戦権は、属性ロックが解除されているかどうかで決まる
        let isPrevClaimed = isAttributeUnlocked; 

        nodes.forEach((node, index) => {
            const card = node.querySelector('.js-card');
            if (!card) return; 

            // 要素取得
            const evolveBtn = card.querySelector('.js-evolve-btn');
            const materialText = card.querySelector('.js-req-material');
            const lockText = card.querySelector('.lock-required-level');
            const tagEl = card.querySelector('.evolution-tag');

            // データ取得
            const reqLevel = parseInt(card.dataset.reqLevel || 0);
            const type = card.dataset.type;
            
            let targetStar = 2;
            if (tagEl) targetStar = parseInt(tagEl.textContent.replace(/\D/g, '')) || 2;

            const recipe = getEvolutionRecipe(targetStar);
            const currentLevel = window.gameState.level;
            const isLevelMet = (currentLevel >= reqLevel);
            
            let isMaterialMet = true;
            recipe.forEach(item => {
                const held = window.gameState.stones[type] ? window.gameState.stones[type][item.rank] : 0;
                if (held < item.count) isMaterialMet = false;
            });

            const isClaimed = node.classList.contains('claimed');

            // --- 表示ロジック ---
            if (isClaimed) {
                // 進化済み
                card.classList.remove('locked-card');
                if(evolveBtn) {
                    evolveBtn.textContent = "進化完了";
                    evolveBtn.classList.add('disabled');
                    evolveBtn.style.background = "#2ecc71";
                    evolveBtn.onclick = null;
                }
                isPrevClaimed = true; // 次へ進める

            } else if (isPrevClaimed) {
                // 挑戦可能 (前のキャラをクリア済み ＆ 属性ロック解除済み)
                card.classList.remove('locked-card');
                if(lockText) lockText.style.display = 'none';

                // 素材テキスト
                if (materialText) {
                    if (recipe.length === 0) {
                        materialText.innerHTML = `<i class="fa-solid fa-bolt"></i> 素材不要`;
                        materialText.style.color = '#2ecc71';
                    } else {
                        // アイコン表示
                        const firstItem = recipe[0];
                        let iconPath = '';
                        if (MATERIAL_IMAGES[type]) iconPath = MATERIAL_IMAGES[type][firstItem.rank];

                        materialText.innerHTML = `<img src="${iconPath}" class="material-icon-sm"> 素材 x${recipe.length}種`;
                        
                        if(isMaterialMet) {
                            materialText.style.color = '#2ecc71';
                            materialText.style.fontWeight = 'bold';
                        } else {
                            materialText.style.color = '';
                            materialText.style.fontWeight = '';
                        }
                    }
                }

                // ボタン制御
                if (!isLevelMet) {
                    evolveBtn.textContent = `Lv.${reqLevel}で解放`;
                    evolveBtn.classList.add('disabled');
                    evolveBtn.onclick = null;
                } else {
                    if (!isMaterialMet) {
                        evolveBtn.textContent = "素材不足";
                        evolveBtn.classList.remove('disabled'); 
                        evolveBtn.style.background = "#95a5a6"; 
                    } else {
                        evolveBtn.textContent = "進化させる！";
                        evolveBtn.classList.remove('disabled');
                        evolveBtn.style.background = ""; 
                    }
                    // ★修正：モーダルを開く処理
                    evolveBtn.onclick = () => window.openEvolutionModal(card, type, targetStar, recipe);
                }
                isPrevClaimed = false; // 次はまだロック

            } else {
                // 完全ロック (前のキャラ未クリア または 前の属性未コンプ)
                card.classList.add('locked-card');
                if(lockText) {
                    lockText.style.display = 'block';
                    // もし属性ロックなら文言を変えるなどしても良いが、ここでは統一
                    lockText.textContent = "LOCKED";
                }
                if(evolveBtn) {
                    evolveBtn.textContent = "???";
                    evolveBtn.classList.add('disabled');
                    evolveBtn.onclick = null;
                }
                isPrevClaimed = false;
            }
        });
    });
}
/* =========================================================
   【背景全画面対応版】ポップアップ生成関数
   素材特大サイズ版（Extra Big Materials）
   ========================================================= */
window.openEvolutionModal = function(card, type, targetStar, recipe) {
    const existing = document.getElementById('js-dynamic-modal');
    if (existing) existing.remove();

    // 1. クリック位置の計算
    const rect = card.getBoundingClientRect();
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
    const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft;
    const cardCenterX = rect.left + scrollLeft + (rect.width / 2);
    const cardCenterY = rect.top + scrollTop + (rect.height / 2);
    const docHeight = Math.max(
        document.body.scrollHeight, document.documentElement.scrollHeight,
        document.body.offsetHeight, document.documentElement.offsetHeight,
        document.body.clientHeight, document.documentElement.clientHeight
    );

    // 2. データ準備
    const charName = card.querySelector('.character-name').textContent;
    const charImgSrc = card.querySelector('.character-img').src;
    
    let materialsHtml = '';
    let allMet = true;

    if (recipe.length === 0) {
        materialsHtml = '<p style="width:100%;text-align:center;color:#888;margin:0;font-size:16px;">必要な素材はありません</p>';
    } else {
        recipe.forEach(item => {
            const held = window.gameState.stones[type] ? window.gameState.stones[type][item.rank] : 0;
            const needed = item.count;
            const isEnough = (held >= needed);
            if (!isEnough) allMet = false;

            let imgPath = '';
            if (MATERIAL_IMAGES[type] && MATERIAL_IMAGES[type][item.rank]) {
                imgPath = MATERIAL_IMAGES[type][item.rank];
            }
            const statusColor = isEnough ? '#2ecc71' : '#e74c3c';
            const bgColor = isEnough ? '#f0fff4' : '#fff0f0';

            // ★変更点：素材画像を特大サイズに変更
            // 枠幅: 120px, 画像: 80px, 文字: 20px
            materialsHtml += `
                <div style="display:flex; flex-direction:column; align-items:center; background:${bgColor}; padding:10px; border-radius:15px; width:120px; border:3px solid ${statusColor}; margin:10px;">
                    <img src="${imgPath}" style="width:80px; height:80px; object-fit:contain; margin-bottom:8px; filter:drop-shadow(0 2px 4px rgba(0,0,0,0.2));">
                    <div style="font-size:20px; font-weight:900; color:${statusColor}">
                        ${held}/${needed}
                    </div>
                </div>
            `;
        });
    }

    // 3. HTML生成
    const modalHtml = `
        <div id="js-dynamic-modal">
            <div onclick="closeDynamicModal()" style="
                position: absolute; top: 0; left: 0; width: 100%; height: ${docHeight}px;
                background: rgba(0,0,0,0.6); z-index: 99998; cursor: pointer;
            "></div>

            <div class="js-modal-content" style="
                position: absolute;
                top: ${cardCenterY}px;
                left: ${cardCenterX}px;
                transform: translate(-50%, -50%) scale(0.8);
                z-index: 99999;
                width: 90%;
                max-width: 600px; /* 素材が大きくなったので全体の幅も少し拡大 */
                background: white;
                border-radius: 20px;
                padding: 30px;
                text-align: center;
                box-shadow: 0 15px 40px rgba(0,0,0,0.5);
                opacity: 0;
                transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            ">
                <button onclick="closeDynamicModal()" style="
                    position: absolute; top: 15px; right: 15px;
                    background: #f1f1f1; border: none; width: 40px; height: 40px;
                    border-radius: 50%; cursor: pointer; font-size: 24px; color: #555;
                    display:flex; align-items:center; justify-content:center;
                ">×</button>
                
                <h3 style="margin: 0 0 25px 0; font-size: 26px; color: #333; font-weight:800;">${charName}</h3>
                
                <div style="display:flex; flex-direction:column; align-items:center; gap:25px; margin-bottom:30px;">
                    
                    <div style="display:flex; align-items:center; justify-content:center; flex-wrap:wrap; gap:25px; width:100%;">
                        
                        <div style="width: 150px; height: 150px; border-radius: 20px; overflow: hidden; border: 5px solid #eee; position: relative; flex-shrink:0; box-shadow:0 8px 20px rgba(0,0,0,0.15);">
                            <img src="${charImgSrc}" style="width: 100%; height: 100%; object-fit: cover;">
                            <div style="position: absolute; top: 0; left: 0; background: gold; color: white; padding: 5px 12px; font-weight: bold; font-size: 16px; border-bottom-right-radius: 15px;">★${targetStar}</div>
                        </div>
                        
                        <div style="font-size: 40px; color: #ddd;">▶</div>

                         <div style="display: flex; justify-content: center; flex-wrap: wrap; gap:5px; flex-grow:1;">
                            ${materialsHtml}
                        </div>
                    </div>
                </div>

                <button id="js-evolve-execute-btn" style="
                    width: 100%; padding: 18px; border: none; border-radius: 50px;
                    background: linear-gradient(135deg, #3498db, #8e44ad);
                    color: white; font-weight: bold; font-size: 22px; cursor: pointer;
                    box-shadow: 0 8px 20px rgba(142, 68, 173, 0.4);
                    opacity: ${allMet ? '1' : '0.6'};
                    cursor: ${allMet ? 'pointer' : 'not-allowed'};
                    letter-spacing: 2px;
                " ${allMet ? '' : 'disabled'}>
                    ${allMet ? '進化する！' : '素材不足'}
                </button>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHtml);

    setTimeout(() => {
        const content = document.querySelector('.js-modal-content');
        if(content) {
            content.style.opacity = '1';
            content.style.transform = 'translate(-50%, -50%) scale(1)';
        }
    }, 10);

    const btn = document.getElementById('js-evolve-execute-btn');
    if (btn && !btn.disabled) {
        btn.onclick = function() {
            window.executeEvolution(card, type, recipe);
            closeDynamicModal();
        };
    }
};

/* 閉じる処理 */
window.closeDynamicModal = function() {
    const modal = document.getElementById('js-dynamic-modal');
    if (modal) {
        const content = modal.querySelector('.js-modal-content');
        if(content) {
            content.style.opacity = '0';
            content.style.transform = 'translate(-50%, -50%) scale(0.8)';
        }
        setTimeout(() => modal.remove(), 200);
    }
};

/* 6. 進化実行 */
window.executeEvolution = function(card, type, recipe) {
    recipe.forEach(item => {
        window.gameState.stones[type][item.rank] -= item.count;
    });
    
    const node = card.closest('.pass-node');
    if(node) {
        node.classList.add('claimed');
        const marker = node.querySelector('.node-marker');
        if(marker) {
            marker.innerHTML = '<i class="fa-solid fa-check"></i>';
        }
    }
    // UI更新（次のキャラのロック解除判定など）
    updateUI();
};

/* 7. デバッグ用（ボタン操作） */
window.addLevel = function(amount) {
    window.gameState.level += amount;
    updateUI();
};
// 素材一括増加
window.addStone = function(type, amount) {
    window.gameState.stones[type].r += amount;
    window.gameState.stones[type].sr += amount;
    window.gameState.stones[type].ssr += 1; 
    updateUI();
};
window.resetAll = function() {
    window.gameState.level = 1;
    ['fire','water','grass','light','dark'].forEach(t => {
        window.gameState.stones[t] = { r:0, sr:0, ssr:0 };
    });
    document.querySelectorAll('.pass-node').forEach(n => n.classList.remove('claimed'));
    updateUI();
};