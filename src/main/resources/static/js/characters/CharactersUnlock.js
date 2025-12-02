<script>
        // グローバル変数としてゲーム状態を定義
        window.gameState = {
            level: 1,
            stones: {
                fire: 0,
                water: 0,
                grass: 0,
                light: 0,
                dark: 0
            }
        };

        document.addEventListener('DOMContentLoaded', () => {
            updateUI();
        });

        /* --- UI更新のメインロジック --- */
        function updateUI() {
            // 1. ヘッダーのステータス更新
            const levelEl = document.getElementById('current-level');
            if(levelEl) levelEl.textContent = window.gameState.level;

            ['fire', 'water', 'grass', 'light', 'dark'].forEach(type => {
                const el = document.getElementById(`stone-${type}`);
                if(el) el.textContent = window.gameState.stones[type];
            });

            // 2. カードごとの判定
            const cards = document.querySelectorAll('.js-card');
            cards.forEach(card => {
                const reqLevel = parseInt(card.getAttribute('data-req-level'), 10);
                const cost = parseInt(card.getAttribute('data-cost'), 10);
                const type = card.getAttribute('data-type'); 

                // 現在の素材数などを取得
                const currentStone = window.gameState.stones[type] || 0;
                const evolveBtn = card.querySelector('.js-evolve-btn');
                const levelText = card.querySelector('.js-req-level');
                const materialText = card.querySelector('.js-req-material');

                // ★修正点: ロック状態に関わらず、まずはテキスト情報を最新に更新する
                
                // (1) レベル条件の表示更新
                if(levelText) {
                    if (window.gameState.level >= reqLevel) {
                        levelText.innerHTML = `<i class="fa-solid fa-crown"></i> Lv.${reqLevel} OK`;
                        levelText.className = 'req-row met';
                    } else {
                        levelText.innerHTML = `<i class="fa-solid fa-crown"></i> Lv.${reqLevel}`;
                        levelText.className = 'req-row unmet'; // 赤文字
                    }
                }

                // (2) 素材条件の表示更新
                if(materialText) {
                    if (currentStone >= cost) {
                        materialText.innerHTML = `<i class="fa-solid fa-cubes"></i> 素材 ${currentStone}/${cost} OK`;
                        materialText.className = 'req-row met'; // 緑文字
                    } else {
                        materialText.innerHTML = `<i class="fa-solid fa-cubes"></i> 素材 ${currentStone}/${cost}`;
                        materialText.className = 'req-row unmet'; // 赤文字
                    }
                }

                // --- ここからロック解除＆ボタン制御判定 ---

                // A. レベルロック判定
                if (window.gameState.level >= reqLevel) {
                    // レベル条件クリア -> ロック解除処理
                    if (card.classList.contains('locked-card')) {
                        card.classList.remove('locked-card');
                        const marker = card.closest('.pass-node').querySelector('.node-marker');
                        if(marker) {
                            marker.innerHTML = '<i class="fa-solid fa-angles-down"></i>';
                            marker.style.background = '#e67e22';
                        }
                    }

                    // B. 進化ボタンの制御 (素材が足りているか)
                    if (currentStone >= cost) {
                        // 進化可能！
                        if(evolveBtn) {
                            evolveBtn.classList.remove('disabled');
                            evolveBtn.textContent = "進化可能！";
                            evolveBtn.onclick = null; 
                            evolveBtn.onclick = () => window.executeEvolution(card, type, cost);
                        }
                        card.classList.add('ready-to-evolve');
                    } else {
                        // 素材不足
                        if(evolveBtn) {
                            evolveBtn.classList.add('disabled');
                            evolveBtn.textContent = "素材不足";
                            evolveBtn.onclick = null;
                        }
                        card.classList.remove('ready-to-evolve');
                    }

                } else {
                    // レベル不足 -> ロック継続
                    card.classList.add('locked-card');
                    // ボタンなどは無効化のまま
                    if(evolveBtn) {
                        evolveBtn.classList.add('disabled');
                        evolveBtn.textContent = "Lv不足"; // ボタンの文言も変えておくと親切
                    }
                }
            });

            // 3. 進捗バー更新
            ['fire', 'water', 'grass', 'light', 'dark'].forEach(type => {
                updateProgressBar(type);
            });
        }

        /* --- 進捗バー --- */
        function updateProgressBar(type) {
            const track = document.getElementById(`progress-${type}`);
            if(!track) return;
            const allCards = document.querySelectorAll(`.character-card.${type}`);
            const unlockedCards = document.querySelectorAll(`.character-card.${type}:not(.locked-card)`);
            if(allCards.length > 0) {
                let progress = (unlockedCards.length / allCards.length) * 100;
                if(progress < 10) progress = 10; 
                track.style.height = `${progress}%`;
            }
        }

        /* --- グローバル関数 (操作用) --- */
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
            
            // リセット処理
            const cards = document.querySelectorAll('.js-card');
            cards.forEach(card => {
                card.classList.add('locked-card');
                card.classList.remove('ready-to-evolve');
                const marker = card.closest('.pass-node').querySelector('.node-marker');
                if(marker) {
                    marker.innerHTML = '<i class="fa-solid fa-lock"></i>';
                    marker.style.background = '#bdc3c7';
                }
                const btn = card.querySelector('.js-evolve-btn');
                if(btn) {
                    btn.classList.add('disabled');
                    btn.textContent = "進化させる！";
                    btn.style.background = "";
                }
            });
            updateUI();
        };

        window.executeEvolution = function(card, type, cost) {
            if (!confirm(`素材を${cost}個消費して進化させますか？`)) return;
            window.gameState.stones[type] -= cost;
            updateUI();
            
            // 進化完了演出
            card.classList.remove('ready-to-evolve');
            const btn = card.querySelector('.js-evolve-btn');
            if(btn) {
                btn.textContent = "進化完了";
                btn.classList.add('disabled');
                btn.style.background = "#2ecc71";
                btn.onclick = null;
            }
            const marker = card.closest('.pass-node').querySelector('.node-marker');
            if(marker) {
                marker.innerHTML = '<i class="fa-solid fa-check"></i>';
                marker.style.background = '#2ecc71';
            }
            alert("進化しました！");
        };
    </script>