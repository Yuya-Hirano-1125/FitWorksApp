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
            currentLevel += 1;
            updateCharacterUnlocks();
        }
    }, 1000);

	// ... (既存のコード)

		// --------------------------
		// ロックされたカードのリンクを無効化
		// --------------------------
		const characterLinks = document.querySelectorAll(".character-link");
		
		// ★★★ この行を追加！lock-message 要素を取得します。 ★★★
		const lockMessage = document.getElementById("lock-message"); 

		characterLinks.forEach(link => {
		    link.addEventListener("click", (event) => {
		        const card = link.querySelector(".character-card");
		        
		        // カードが 'locked' クラスを持っているかチェック
		        if (card && card.classList.contains("locked")) {
		            // ロック状態であれば、デフォルトのリンク遷移をキャンセル
		            event.preventDefault();
		            
				// ★ メッセージ表示の処理 (既存のコードはここから)
				            
				            // 既に表示されていないことを確認して表示
				            if (lockMessage) { // lockMessage が上記で定義されたため、動作します
				                // 以前のタイマーをクリアして、複数回クリックされても正しく動作するようにする
				                clearTimeout(lockMessage.timeoutId);
				                
				                // 'hidden' クラスを削除してメッセージを表示
				                lockMessage.classList.remove("hidden"); 

				                // 3秒後にメッセージを非表示にするタイマーを設定
				                lockMessage.timeoutId = setTimeout(() => {
				                    lockMessage.classList.add("hidden");
				                }, 3000); // 3000ミリ秒 = 3秒
				            }

				            // コンソール出力はデバッグ用として残しておく
				            console.log("このキャラクターはロックされています。レベルが足りません。");
				        }
				    });
				});

				// 初期表示
				updateCharacterUnlocks();

			});