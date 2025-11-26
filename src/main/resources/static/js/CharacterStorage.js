document.addEventListener("DOMContentLoaded", () => {

    let currentLevel = 1;
    const maxLevel = 999;

    const currentLevelSpan = document.getElementById("current-level");
    const cards = document.querySelectorAll(".character-card");

    // --------------------------
    // カードのロック状態を更新
    // --------------------------
    function updateCharacterUnlocks() {
        currentLevelSpan.textContent = currentLevel;

        cards.forEach(card => {
            const required = parseInt(card.dataset.unlockedLevel);
            const img = card.querySelector(".character-img");
            const needLvSpan = card.querySelector(".required-level-display");

            if (needLvSpan) needLvSpan.textContent = required;

            if (currentLevel >= required) {
                // 解放
                card.classList.remove("locked");
                card.classList.add("unlocked");

                if (img && img.dataset.src) {
                    img.src = img.dataset.src;
                }

            } else {
                // 未解放
                card.classList.add("locked");
                card.classList.remove("unlocked");

                img.src = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
            }
        });
    }

    // --------------------------
    // レベルアップ（1秒ごと）
    // --------------------------
    setInterval(() => {
        if (currentLevel < maxLevel) {
            currentLevel++;
            updateCharacterUnlocks();
        }
    }, 1000);

    // 初期表示
    updateCharacterUnlocks();

});
