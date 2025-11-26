document.addEventListener('DOMContentLoaded', () => {
    const scrollContainer = document.querySelector('.character-list-wrapper');
    const scrollToStartButton = document.getElementById('scroll-to-start-button');
    
    // currentLevel の初期値は 1 です
    let currentLevel = 1;
    const currentLevelSpan = document.getElementById('current-level');
    const cards = document.querySelectorAll('.character-card');

    // 1. 初期化：ロックオーバーレイ内のレベル表示の数字のみを更新
    cards.forEach(card => {
        const requiredLevelString = card.dataset.unlockedLevel || '1'; 
        const requiredLevel = parseInt(requiredLevelString, 10);
        
        const lockRequiredLevelDisplay = card.querySelector('.required-level-display'); 
        if (lockRequiredLevelDisplay) {
            lockRequiredLevelDisplay.textContent = requiredLevel;
        }
    });

    // 2. カードのロック状態を更新するメイン関数
    function updateCardLockStatus() {
        cards.forEach(card => {
            const requiredLevel = parseInt(card.dataset.unlockedLevel || '1', 10);
            const characterImg = card.querySelector('.character-img');
            
            if (currentLevel >= requiredLevel) {
                // **解放時 (UNLOCKED)**
                // locked クラスを削除し、unlocked クラスを付与
                card.classList.remove('locked'); 
                card.classList.add('unlocked'); 

                // 画像をデータ属性からロード（img.srcがプレースホルダーの場合のみ更新）
                if (characterImg && characterImg.dataset.src && characterImg.src.startsWith('data:image')) {
                    characterImg.src = characterImg.dataset.src;
                }
                
            } else {
                // **未解放時 (LOCKED)**
                // locked クラスを付与し、unlocked クラスを削除
                card.classList.add('locked'); 
                card.classList.remove('unlocked');
                
                // 画像をプレースホルダーに戻す
                if (characterImg) {
                    characterImg.src = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7"; 
                }
            }
        });
    }

    // --- ボタンの機能 (PC: 横スクロール, スマホ: 縦スクロール) ---
    scrollToStartButton.addEventListener('click', () => {
        if (window.innerWidth > 768) {
            scrollContainer.scrollTo({ left: 0, behavior: 'smooth' });
        } else {
            scrollContainer.scrollTo({ top: 0, behavior: 'smooth' });
        }
    });

    // --- レベルアップ機能 (デモ用) ---
    function levelUpDemo() {
        if (currentLevel < 100) {
            currentLevel += 1;
        } else {
            currentLevel = 1; 
        }
        currentLevelSpan.textContent = currentLevel;
        updateCardLockStatus();
    }
    
    // 5秒ごとにレベルアップ
    setInterval(levelUpDemo, 5000); 

    // 3. 初期ロード時にカードの状態を更新 (currentLevel=1のカードが解放されます)
    updateCardLockStatus();
});