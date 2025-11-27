// テーマを適用する関数
function applyTheme() {
    const savedTheme = localStorage.getItem('fitworks-theme') || 'default';
    const body = document.body;

    // 一旦クラスをリセット
    body.classList.remove('theme-universal', 'theme-high-contrast');

    // テーマクラスを追加
    if (savedTheme !== 'default') {
        body.classList.add('theme-' + savedTheme);
    }
}

// ページ読み込み時に即座に実行（画面のチラつき防止）
applyTheme();