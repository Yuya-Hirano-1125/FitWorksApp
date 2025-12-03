/* CharactersUnlock.js */

window.gameState = {
    level: 1,
    stones: { fire: 0, water: 0, grass: 0, light: 0, dark: 0 }
};

document.addEventListener('DOMContentLoaded', () => {
    updateUI();
});

function updateUI() {
    // 1. ステータスバーの更新
    const levelEl = document.getElementById('current-level');
    if(levelEl) levelEl.textContent = window.gameState.level;

    ['fire', 'water', 'grass', 'light', 'dark'].forEach(type => {
        const el = document.getElementById(`stone-${type}`);
        if(el) el.textContent = window.gameState.stones[type];
    });

    // 2. トラック（属性）ごとに処理を行う
    // これにより、炎属性の進行度が水属性に影響したりしないようにします
    const tracks = document.querySelectorAll('.pass-track');

    tracks.forEach(track => {
        const nodes = track.querySelectorAll('.pass-node');

        nodes.forEach((node, index) => {
            const card = node.querySelector('.js-card');
            if (!card) return; // カードがないNodeはスキップ

            const evolveBtn = card.querySelector('.js-evolve-btn');
            const nameEl = card.querySelector('.character-name');
            const levelText = card.querySelector('.js-req-level');
            const materialText = card.querySelector('.js-req-material');
            const lockText = card.querySelector('.lock-required-level'); // ロック画面のテキスト

            // データ取得
            const reqLevel = parseInt(card.getAttribute('data-req-level'), 10);
            const cost = parseInt(card.getAttribute('data-cost'), 10);
            const type = card.getAttribute('data-type');
            const currentStone = window.gameState.stones[type] || 0;

            // 名前保存（初回のみ）
            if (nameEl && !nameEl.getAttribute('data-original-name')) {
                nameEl.setAttribute('data-original-name', nameEl.textContent);
            }

            // --- 【ここが変更点】 LOCKED（中身が見えない）の判定ロジック ---
            let isChainUnlocked = false;

            if (index === 0) {
                // 1番目のキャラ（初期キャラ）は常にLOCKED解除
                isChainUnlocked = true;
            } else {
                // 2番目以降は「前のノード」が claimed（進化済み）を持っているかチェック
                const prevNode = nodes[index - 1];
                if (prevNode.classList.contains('claimed')) {
                    isChainUnlocked = true;
                }
            }

            // --- 状態の適用 ---

            if (!isChainUnlocked) {
                // ■ ロック状態（前の進化が終わっていない）
                card.classList.add('locked-card');
                
                // ロック理由を表示
                if(lockText) lockText.textContent = "前の進化が必要";
                
                // 名前を隠す
                if (nameEl) nameEl.textContent = "???";

                // ボタン無効化
                if(evolveBtn) {
                    evolveBtn.textContent = "LOCKED";
                    evolveBtn.classList.add('disabled');
                    evolveBtn.onclick = null;
                }

            } else {
                // ■ ロック解除（姿が見える状態）
                card.classList.remove('locked-card');

                // 名前を表示
                if (nameEl) nameEl.textContent = nameEl.getAttribute('data-original-name');

                // ここから「レベル」と「素材」の判定を行う
                const isLevelMet = window.gameState.level >= reqLevel;
                const isMaterialMet = currentStone >= cost;
                const isAlreadyEvolved = evolveBtn && (evolveBtn.textContent.includes('完了') || evolveBtn.textContent.includes('入手済み'));

                // 素材テキストの表示更新
                if (materialText) {
                    if (cost > 0) {
                        materialText.innerHTML = `<i class="fa-solid fa-cubes"></i> 素材 ${currentStone}/${cost}`;
                        if (isMaterialMet) {
                            materialText.classList.add('req-met');
                            materialText.style.color = '#2ecc71';
                            materialText.style.fontWeight = 'bold';
                        } else {
                            materialText.classList.remove('req-met');
                            materialText.style.color = '';
                            materialText.style.fontWeight = '';
                        }
                    } else {
                        materialText.innerHTML = `<i class="fa-solid fa-check"></i> 初期所持`;
                    }
                }

                // レベルテキストの表示更新
                if (levelText) {
                    if (isLevelMet) {
                        levelText.innerHTML = `<i class="fa-solid fa-crown"></i> Lv.${reqLevel} OK`;
                        levelText.classList.add('req-met');
                    } else {
                        levelText.innerHTML = `<i class="fa-solid fa-crown"></i> Lv.${reqLevel}`;
                        levelText.classList.remove('req-met');
                    }
                }

                // ボタン制御
                if (isAlreadyEvolved) {
                    // 既に進化済みの場合
                    // 何もしない（現状維持）
                } else {
                    if (!isLevelMet) {
                        // レベル不足（姿は見えているが押せない）
                        evolveBtn.textContent = `Lv.${reqLevel}が必要`;
                        evolveBtn.classList.add('disabled');
                        evolveBtn.onclick = null;
                    } else if (!isMaterialMet) {
                        // 素材不足
                        evolveBtn.textContent = "素材不足";
                        evolveBtn.classList.add('disabled');
                        evolveBtn.onclick = null;
                    } else {
                        // 進化可能！
                        evolveBtn.textContent = "進化させる！";
                        evolveBtn.classList.remove('disabled');
                        evolveBtn.style.background = ""; // デフォルト色に戻す
                        evolveBtn.onclick = () => window.executeEvolution(card, type, cost);
                    }
                }
            }
        });
    });

    // 3. プログレスバーの更新
    updateProgressBars();
}

function updateProgressBars() {
    ['fire', 'water', 'grass', 'light', 'dark'].forEach(type => {
        const track = document.getElementById(`progress-${type}`);
        // セクション取得
        const section = document.querySelector(`.attribute-heading.${type}-color`)?.closest('.pass-section');
        if (!section || !track) return;

        const allCards = section.querySelectorAll('.js-card');
        // ロックされていない（locked-cardがない）＝姿が見えているカードの割合
        // もしくは「進化済み(claimed)」の割合にするならここを変えます
        const unlockedCards = section.querySelectorAll('.js-card:not(.locked-card)');
        
        if(allCards.length > 0) {
            let progress = (unlockedCards.length / allCards.length) * 100;
            if(progress < 5 && unlockedCards.length > 0) progress = 5; 
            track.style.height = `${progress}%`;
        }
    });
}

/* ==============================================
   アクション関数
   ============================================== */
window.executeEvolution = function(card, type, cost) {
    if (!confirm(`素材を${cost}個消費して進化させますか？`)) return;
    
    // 素材消費
    window.gameState.stones[type] -= cost;
    
    // 進化完了演出
    const btn = card.querySelector('.js-evolve-btn');
    if(btn) {
        btn.textContent = "進化完了";
        btn.classList.add('disabled');
        btn.style.background = "#2ecc71"; // 緑色
        btn.onclick = null;
    }
    
    // チェックマークに変更
    const marker = card.closest('.pass-node').querySelector('.node-marker');
    if(marker) {
        marker.innerHTML = '<i class="fa-solid fa-check"></i>';
        marker.style.background = '#2ecc71';
        marker.style.color = 'white';
        marker.style.border = '2px solid white';
    }
    
    // 親のpass-nodeにclaimedクラスをつける（これが次のLOCKED解除の鍵になります）
    const node = card.closest('.pass-node');
    if(node) {
        node.classList.add('claimed');
    }

    // 画面更新（次のカードのLOCKEDを解除するため）
    updateUI();
};

/* ==============================================
   デバッグ用
   ============================================== */
window.addLevel = function(amount) {
    window.gameState.level += amount;
    updateUI();
};

window.addStone = function(type, amount) {
    window.gameState.stones[type] += amount;
    updateUI();
};

window.resetAll = function() {
    window.gameState.level = 1;
    window.gameState.stones = { fire: 0, water: 0, grass: 0, light: 0, dark: 0 };
    location.reload(); 
};