document.addEventListener('DOMContentLoaded', () => {
    // スクロール対象の要素を取得 
    const scrollContainer = document.querySelector('.character-list-wrapper');
    const scrollToStartButton = document.getElementById('scroll-to-start-button');
    
    let currentLevel = 1;
    const currentLevelSpan = document.getElementById('current-level');
    const cards = document.querySelectorAll('.character-card');

    function updateCardLockStatus() {
        cards.forEach(card => {
            const requiredLevel = parseInt(card.dataset.unlockedLevel);
            
            if (currentLevel >= requiredLevel) {
                card.classList.remove('locked');
                card.classList.add('unlocked');
            } else {
                card.classList.add('locked');
                card.classList.remove('unlocked');
            }
        });
    }

    // --- ボタンの機能 (PC: 横スクロール, スマホ: 縦スクロール) ---
    scrollToStartButton.addEventListener('click', () => {
        if (window.innerWidth > 768) {
            // PC/Tablet: 横スクロール
            scrollContainer.scrollTo({
                left: 0,
                behavior: 'smooth'
            });
        } else {
            // Mobile: 縦スクロール
            scrollContainer.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
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
    
    setInterval(levelUpDemo, 5000); 

    updateCardLockStatus();
});