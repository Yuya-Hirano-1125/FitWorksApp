// Tailwindの設定（CSSファイルで定義したフォントを適用）
        tailwind.config = {
            theme: {
                extend: {
                    fontFamily: {
                        sans: ['Poppins', 'Noto Sans JP', 'sans-serif'],
                    },
                }
            }
        }
document.addEventListener('DOMContentLoaded', () => {
    const backgroundList = document.getElementById('background-list');

    /**
     * UIの状態を更新し、選択されたカードをハイライトし、他のカードを「選択」ボタンに戻します。
     * @param {HTMLElement} selectedCard - 新たに選択された背景カードのDOM要素。
     */
    const updateSelection = (selectedCard) => {
        const allCards = backgroundList.querySelectorAll('.background-card');
        
        allCards.forEach(card => {
            const statusContainer = card.querySelector('.card-status-area');
            
            // 全てのカードから'selected'クラスを削除
            card.classList.remove('selected');
            
            // ステータス表示を更新
            if (statusContainer) {
                let content = '';
                if (card === selectedCard) {
                    // 選択されたカードに'selected'クラスを追加し、「適用中」と表示
                    card.classList.add('selected');
                    content = '<p class="status-text">適用中</p>';
                } else {
                    // 未選択のカードには「選択」ボタンを表示
                    content = '<button data-action="select" class="select-button">選択</button>';
                }
                statusContainer.innerHTML = content;
            }
        });
    };

    // リストコンテナ全体へのイベントリスナー
    backgroundList.addEventListener('click', (event) => {
        const target = event.target;
        
        // クリックされた要素が「選択」ボタンかどうかを確認
        if (target.dataset.action === 'select') {
            const card = target.closest('.background-card');
            
            if (card) {
                // 背景選択をシミュレートし、UIを更新
                updateSelection(card);

                console.log(`Background selected: ${card.dataset.bg}`);
            }
        }
    });
    
    // ページの初期設定
    const initialSelectedCard = document.getElementById('card-classroom');
    if (initialSelectedCard) {
        // 初期選択状態を反映させる
        updateSelection(initialSelectedCard);
    }
});