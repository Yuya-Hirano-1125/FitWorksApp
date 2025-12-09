/* =========================================================
   CharactersUnlock.js - ポジション制御＆レスポンシブ対応版
   ========================================================= */

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

const MATERIAL_IMAGES = {
    fire:  { r: '/img/item/R-red.png', sr: '/img/item/SR-red.png', ssr: '/img/item/SSR-red.png' },
    water: { r: '/img/item/R-blue.png', sr: '/img/item/SR-blue.png', ssr: '/img/item/SSR-blue.png' },
    grass: { r: '/img/materials/grass_r.png', sr: '/img/materials/grass_sr.png', ssr: '/img/materials/grass_ssr.png' },
    light: { r: '/img/materials/light_r.png', sr: '/img/materials/light_sr.png', ssr: '/img/materials/light_ssr.png' },
    dark:  { r: '/img/materials/dark_r.png', sr: '/img/materials/dark_sr.png', ssr: '/img/materials/dark_ssr.png' }
};

document.addEventListener('DOMContentLoaded', () => {
    updateUI();
});

function getEvolutionRecipe(targetStar) {
    if (targetStar <= 1) return []; 
    if (targetStar === 2) return []; 
    if (targetStar === 3) return [{ rank: 'r', count: 3 }];
    if (targetStar >= 4) return [
        { rank: 'r',   count: 5 },
        { rank: 'sr',  count: 3 },
        { rank: 'ssr', count: 1 }
    ];
    return [];
}

function updateUI() {
    const levelEl = document.getElementById('current-level');
    if(levelEl) levelEl.textContent = window.gameState.level;
    
    const tracks = document.querySelectorAll('.pass-track');

    tracks.forEach((track, trackIndex) => {
        const nodes = track.querySelectorAll('.pass-node');
        
        let isAttributeUnlocked = true;
        if (trackIndex > 0) {
            const prevTrack = tracks[trackIndex - 1];
            const prevNodes = prevTrack.querySelectorAll('.pass-node');
            const lastNodeOfPrev = prevNodes[prevNodes.length - 1];
            if (!lastNodeOfPrev.classList.contains('claimed')) {
                isAttributeUnlocked = false;
            }
        }

        let isPrevClaimed = isAttributeUnlocked; 

        nodes.forEach((node, index) => {
            const card = node.querySelector('.js-card');
            if (!card) return; 

            const charNameEl = card.querySelector('.character-name');
            if (charNameEl && !charNameEl.dataset.originalName) {
                charNameEl.dataset.originalName = charNameEl.textContent;
            }

            const evolveBtn = card.querySelector('.js-evolve-btn');
            const materialText = card.querySelector('.js-req-material');
            const tagEl = card.querySelector('.evolution-tag');

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

            if (isClaimed) {
                card.classList.remove('locked-card');
                if (charNameEl) charNameEl.textContent = charNameEl.dataset.originalName;
                const lockElements = card.querySelectorAll('.lock-overlay');
                lockElements.forEach(el => el.style.display = 'none');
                
                if(materialText) materialText.style.display = 'none';
                if(evolveBtn) {
                    evolveBtn.textContent = "GET済み";
                    evolveBtn.classList.add('disabled');
                    evolveBtn.onclick = null;
                }
                isPrevClaimed = true;

            } else if (isPrevClaimed) {
                card.classList.remove('locked-card');
                if (charNameEl) charNameEl.textContent = charNameEl.dataset.originalName;
                const lockElements = card.querySelectorAll('.lock-overlay');
                lockElements.forEach(el => el.style.display = 'none');

                if (materialText) {
                    materialText.style.display = 'block';
                    if (recipe.length === 0) {
                        materialText.innerHTML = `素材不要`;
                    } else {
                        const firstItem = recipe[0];
                        let iconPath = '';
                        if (MATERIAL_IMAGES[type]) iconPath = MATERIAL_IMAGES[type][firstItem.rank];
                        materialText.innerHTML = `<img src="${iconPath}" style="width:14px;vertical-align:middle;"> 素材が必要`;
                    }
                }

                if (!isLevelMet) {
                    card.classList.add('locked-card');
                    if(evolveBtn) {
                        evolveBtn.textContent = `Lv.${reqLevel}`;
                        evolveBtn.classList.add('disabled');
                    }
                } else {
                    if(evolveBtn) {
                        evolveBtn.classList.remove('disabled');
                        if (isMaterialMet) {
                            evolveBtn.textContent = "進化可能！";
                            // ★変更：カード自体を渡して位置計算に使う
                            evolveBtn.onclick = (e) => {
                                e.stopPropagation();
                                openDynamicModal(card, type, recipe);
                            };
                        } else {
                            evolveBtn.textContent = "素材不足";
                            evolveBtn.classList.add('disabled');
                        }
                    }
                }

            } else {
                card.classList.add('locked-card');
                if (charNameEl) charNameEl.textContent = "?????";
                if(evolveBtn) {
                    evolveBtn.textContent = "---";
                    evolveBtn.classList.add('disabled');
                }
                const lockOverlay = card.querySelector('.lock-overlay');
                if(lockOverlay) lockOverlay.style.display = 'flex';
            }
        });
    });
}

/* =================================================
   動的モーダル生成処理 (カード付近へ表示)
   ================================================= */
function openDynamicModal(card, type, recipe) {
    // 既存モーダル削除
    const oldModal = document.getElementById('js-dynamic-modal');
    if (oldModal) oldModal.remove();

    const imgSrc = card.querySelector('.character-img').src;
    let materialsHtml = '';
    let allMet = true;
    
    if (recipe.length > 0) {
        recipe.forEach(item => {
            const held = window.gameState.stones[type] ? window.gameState.stones[type][item.rank] : 0;
            const needed = item.count;
            const isEnough = (held >= needed);
            if (!isEnough) allMet = false;
            
            let imgPath = MATERIAL_IMAGES[type] ? MATERIAL_IMAGES[type][item.rank] : '';
            
            materialsHtml += `
                <div class="ff-mat-item ${isEnough ? 'ok' : 'ng'}">
                    <img src="${imgPath}" class="ff-mat-img">
                    <div class="ff-mat-count ${isEnough ? 'ok' : 'ng'}">
                        ${held}/${needed}
                    </div>
                </div>
            `;
        });
    } else {
        materialsHtml = '<div style="color:#555; padding:5px; font-weight:bold; font-size:12px;">素材は不要です♪</div>';
    }

    const modalHtml = `
    <div id="js-dynamic-modal" class="modal-overlay">
        <div class="modal-content">
            <h3 class="modal-header">進化の儀式</h3>
            <div style="margin:5px;">
                <img src="${imgSrc}" style="height:60px; object-fit:contain;">
            </div>
            <p class="modal-message">進化させますか？</p>
            <div class="ff-mat-list">
                ${materialsHtml}
            </div>
            <div class="modal-actions">
                <button id="js-evolve-execute-btn" class="evolve-btn" style="width:100px; background:linear-gradient(to bottom, #fd79a8, #e84393); border:2px solid #fff;">
                    OK
                </button>
                <button onclick="closeDynamicModal()" class="evolve-btn" style="width:80px; background:#b2bec3; border-color:#dfe6e9;">
                    Cancel
                </button>
            </div>
        </div>
    </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHtml);

    const modal = document.getElementById('js-dynamic-modal');
    
    // ■ 位置計算ロジック
    // カードの座標を取得
    const rect = card.getBoundingClientRect();
    const scrollX = window.scrollX || document.documentElement.scrollLeft;
    const scrollY = window.scrollY || document.documentElement.scrollTop;
    
    // モーダルの仮サイズ（レンダリング前なので推測値、あるいは一度表示してから取得）
    // ここでは一度透明で表示してサイズを取得するテクニックもあるが、簡易的に計算
    const modalWidth = 280; 
    const modalHeight = 250; 

    // 基本位置：カードの上側中央
    let top = rect.top + scrollY - modalHeight + 20; // 少し被せる
    let left = rect.left + scrollX + (rect.width / 2) - (modalWidth / 2);

    // 画面外チェック（左）
    if (left < 10) left = 10;
    // 画面外チェック（右）
    const bodyWidth = document.body.clientWidth;
    if (left + modalWidth > bodyWidth) {
        left = bodyWidth - modalWidth - 10;
    }
    // 画面外チェック（上）: 上にはみ出るならカードの下に出す
    if (rect.top - modalHeight < 0) {
        top = rect.bottom + scrollY + 10;
        // 吹き出しの三角を逆にするクラスをつけるとベストだが今回は省略
    }

    // スタイル適用
    modal.style.top = `${top}px`;
    modal.style.left = `${left}px`;
    
    // アニメーション開始
    void modal.offsetWidth; 
    modal.classList.add('active');

    const btn = document.getElementById('js-evolve-execute-btn');
    if (btn) {
        if (!allMet) {
            btn.classList.add('disabled');
            btn.textContent = "不足";
        } else {
            btn.onclick = function() {
                window.executeEvolution(card, type, recipe);
                closeDynamicModal();
            };
        }
    }
}

window.executeEvolution = function(card, type, recipe) {
    recipe.forEach(item => {
        window.gameState.stones[type][item.rank] -= item.count;
    });

    const node = card.closest('.pass-node');
    if (node) {
        node.classList.add('claimed');
        const marker = node.querySelector('.node-marker');
        if(marker) {
            marker.innerHTML = '<i class="fa-solid fa-heart"></i>';
            marker.style.background = '#ffeb3b';
            marker.style.color = '#ff4757';
            marker.style.borderColor = '#fff';
        }
    }
    updateUI();
};

window.closeDynamicModal = function() {
    const modal = document.getElementById('js-dynamic-modal');
    if (modal) {
        modal.classList.remove('active');
        setTimeout(() => modal.remove(), 250);
    }
};

window.addLevel = function(n) {
    window.gameState.level += n;
    updateUI();
};
window.debugAddMaterial = function(type, rank, amount) {
    if (window.gameState.stones[type]) {
        window.gameState.stones[type][rank] += amount;
        updateUI();
    }
	document.addEventListener('DOMContentLoaded', () => {
	    
	    // ==========================================
	    // デバッグ機能 (windowオブジェクトに登録してHTMLから呼べるようにする)
	    // ==========================================
	    
	    // 現在のレベル（初期値）
	    // 本来はサーバーから取得した値をセットすべきですが、
	    // デバッグ用なので一旦HTMLの表示から取得するか、初期値1とします。
	    let currentLevel = parseInt(document.getElementById('current-level').innerText) || 1;

	    // レベルを加算する関数
	    window.addLevel = function(amount) {
	        currentLevel += amount;
	        updateLevelDisplay();
	        alert(`デバッグ: レベルが ${currentLevel} になりました！`);
	    };

	    // 素材を追加する関数 (今回はコンソール表示のみ)
	    window.debugAddMaterial = function(type, rank, amount) {
	        console.log(`デバッグ: ${type}属性のランク${rank}素材を ${amount}個 追加しました（仮想）`);
	        alert(`${type}素材(+${amount})を追加しました！`);
	    };

	    // 画面表示を更新する内部関数
	    function updateLevelDisplay() {
	        const levelSpan = document.getElementById('current-level');
	        if(levelSpan) {
	            levelSpan.innerText = currentLevel;
	        }
	        
	        // レベルが変わったので、ロック状態の見た目も更新チェック
	        // (注意: 実際のロック解除はサーバー通信が必要ですが、
	        //  ここでは見た目上の「条件満たしてる感」を出すためにクラスを操作します)
	        checkRequirements();
	    }

	    // 必要レベルを満たしているかチェックして表示を変える
	    function checkRequirements() {
	        document.querySelectorAll('.js-card').forEach(card => {
	            const reqLevel = parseInt(card.dataset.reqLevel) || 0;
	            const reqLevelElement = card.querySelector('.js-req-level');
	            
	            if (reqLevelElement) {
	                if (currentLevel >= reqLevel) {
	                    reqLevelElement.style.color = '#2ecc71'; // 緑色 (OK)
	                    reqLevelElement.innerHTML = `<i class="fa-solid fa-check"></i> Lv.${reqLevel}`;
	                } else {
	                    reqLevelElement.style.color = ''; // 元の色に戻す
	                    reqLevelElement.innerHTML = `<i class="fa-solid fa-crown"></i> Lv.${reqLevel}`;
	                }
	            }
	            
	            // 進化ボタンの活性化制御
	            // (注: すでに解放済みのボタンには .js-evolve-btn がない場合もあるのでチェック)
	            const btn = card.querySelector('.js-evolve-btn');
	            if (btn && !btn.innerText.includes('GET済み')) { // GET済みでない場合のみ
	                if (currentLevel >= reqLevel) {
	                    btn.classList.remove('disabled');
	                    btn.disabled = false;
	                } else {
	                    btn.classList.add('disabled');
	                    btn.disabled = true;
	                }
	            }
	        });
	    }

	    // 初期化時に一度チェックを実行
	    checkRequirements();
	});
	}