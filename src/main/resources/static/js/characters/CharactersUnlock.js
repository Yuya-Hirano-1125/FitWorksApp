/* ==============================================
   キャラクター進化システム JS
   ============================================== */

// ユーザーの状態（本来はDBから取得する値）
let gameState = {
    level: 1,
    stones: {
        fire: 0,
        water: 0,
        grass: 0,
        light: 0,
        dark: 0
    }
};

// ページ読み込み時に実行
document.addEventListener('DOMContentLoaded', () => {
    updateUI();
});

/* ---------------------------------------------
   UI更新のメインロジック
   --------------------------------------------- */
function updateUI() {
    // 1. ヘッダーのステータス表示を更新
    document.getElementById('current-level').textContent = gameState.level;
    document.getElementById('stone-fire').textContent = gameState.stones.fire;
    // 必要に応じて他属性も更新
    // document.getElementById('stone-water').textContent = gameState.stones.water; ...

    // 2. すべてのカードに対して判定を行う
    const cards = document.querySelectorAll('.js-card');

    cards.forEach(card => {
        // HTMLのdata属性から条件を取得
        const reqLevel = parseInt(card.getAttribute('data-req-level'));
        const cost = parseInt(card.getAttribute('data-cost'));
        const type = card.getAttribute('data-type'); // 'fire' など

        // --- A. レベルロックの判定 ---
        if (gameState.level >= reqLevel) {
            // 条件達成！ -> ロック解除
            if (card.classList.contains('locked-card')) {
                card.classList.remove('locked-card'); // これでCSSの隠蔽が解除され、画像が出る
                
                // ロックアイコンを「Next」などに変える演出（親要素のマーカー操作）
                const nodeMarker = card.closest('.pass-node').querySelector('.node-marker');
                nodeMarker.innerHTML = '<i class="fa-solid fa-angles-down"></i>';
                nodeMarker.style.background = '#e67e22'; // アクティブ色に変更
            }

            // --- B. 進化素材の判定 (ロック解除後のみチェック) ---
            const currentStone = gameState.stones[type];
            const requirementsBox = card.querySelector('.requirements-box');
            const evolveBtn = card.querySelector('.js-evolve-btn');

            // 表示テキスト更新
            const levelText = card.querySelector('.js-req-level');
            const materialText = card.querySelector('.js-req-material');
            
            // レベル条件表示
            levelText.innerHTML = `<i class="fa-solid fa-crown"></i> Lv.${reqLevel} OK`;
            levelText.className = 'req-row met'; // 緑色にする

            // 素材条件表示
            if (currentStone >= cost) {
                // 素材も足りている場合
                materialText.innerHTML = `<i class="fa-solid fa-cubes"></i> 素材 ${currentStone}/${cost} OK`;
                materialText.className = 'req-row met'; // 緑色
                
                // ボタンを有効化
                evolveBtn.classList.remove('disabled');
                evolveBtn.textContent = "進化可能！";
                card.classList.add('ready-to-evolve'); // カードをピカピカさせるクラス追加
                
                // ボタンクリックイベントの設定（重複登録防止のためonclick使用）
                evolveBtn.onclick = () => {
                    executeEvolution(card, type, cost);
                };

            } else {
                // 素材が足りない場合
                materialText.innerHTML = `<i class="fa-solid fa-cubes"></i> 素材 ${currentStone}/${cost}`;
                materialText.className = 'req-row unmet'; // 赤色
                
                // ボタンを無効化
                evolveBtn.classList.add('disabled');
                evolveBtn.textContent = "素材不足";
                card.classList.remove('ready-to-evolve');
                evolveBtn.onclick = null;
            }

        } else {
            // 条件未達 -> ロック維持（または再ロック）
            card.classList.add('locked-card');
            // 必要ならテキスト類を初期状態に戻す処理をここに書く
        }
    });

    // 3. 進捗バーの更新 (炎属性の例)
    updateProgressBar('fire');
}

/* ---------------------------------------------
   進捗バーの計算
   --------------------------------------------- */
function updateProgressBar(type) {
    const track = document.getElementById(`progress-${type}`);
    if(!track) return;

    // その属性のカードのうち、ロック解除されている数をカウント
    const allCards = document.querySelectorAll(`.character-card.${type}`);
    const unlockedCards = document.querySelectorAll(`.character-card.${type}:not(.locked-card)`);
    
    // 割合計算 (Claimedの分として+1オフセットしたり調整してください)
    // 簡易的に「解除数 / 総数」で計算
    let progress = (unlockedCards.length / allCards.length) * 100;
    
    // 最低でも少しは見せる
    if (progress < 10) progress = 10;
    
    track.style.height = `${progress}%`;
}

/* ---------------------------------------------
   進化実行処理（デモ用）
   --------------------------------------------- */
function executeEvolution(card, type, cost) {
    if (!confirm(`素材を${cost}個消費して進化させますか？`)) return;

    // 素材消費
    gameState.stones[type] -= cost;
    
    // UI更新
    updateUI();
    
    // 進化完了の演出（CSSクラス付け替え）
    card.classList.remove('ready-to-evolve');
    const btn = card.querySelector('.js-evolve-btn');
    btn.textContent = "進化完了";
    btn.classList.add('disabled');
    btn.style.background = "#2ecc71"; // 緑色に
    
    // ここでマーカーをチェックマークに変えるなどの処理
    const nodeMarker = card.closest('.pass-node').querySelector('.node-marker');
    nodeMarker.innerHTML = '<i class="fa-solid fa-check"></i>';
    nodeMarker.style.background = '#2ecc71';
    
    alert("進化しました！");
}


/* ==============================================
   デバッグ用関数 (ボタン操作用)
   ============================================== */
function addLevel(amount) {
    gameState.level += amount;
    updateUI();
}

function addStone(type, amount) {
    gameState.stones[type] += amount;
    updateUI();
}

function resetAll() {
    gameState.level = 1;
    gameState.stones.fire = 0;
    updateUI();
}