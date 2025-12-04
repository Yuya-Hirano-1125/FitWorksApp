document.addEventListener('DOMContentLoaded', function() {
    
    // =========================================
    // 1. パスワード欄のコピペ・切り取り禁止 (新規追加)
    // =========================================
    // すべてのパスワード入力欄（と、それに関連する入力欄）を取得
    const passwordInputs = document.querySelectorAll('.password-wrapper input');

    passwordInputs.forEach(input => {
        // 貼り付け (Paste) 禁止
        input.addEventListener('paste', function(e) {
            e.preventDefault();
            // alert('貼り付けはできません'); // 必要であればコメントアウトを外して警告を表示
        });

        // コピー (Copy) 禁止
        input.addEventListener('copy', function(e) {
            e.preventDefault();
        });

        // 切り取り (Cut) 禁止
        input.addEventListener('cut', function(e) {
            e.preventDefault();
        });
    });


    // =========================================
    // 2. 目のアイコンの機能 (前回までの内容)
    // =========================================
    const icons = document.querySelectorAll('.toggle-password-icon');

    icons.forEach(icon => {
        const wrapper = icon.closest('.password-wrapper');
        if (!wrapper) return;
        
        const input = wrapper.querySelector('input');
        if (!input) return;

        // --- アイコンの出現制御 (文字がある時だけ表示) ---
        const updateIconVisibility = () => {
            if (input.value.length > 0) {
                icon.style.display = 'block';
            } else {
                icon.style.display = 'none';
                // 空になったら隠す状態に戻す
                if (input.type === 'text') {
                    input.type = 'password';
                    icon.classList.remove('fa-eye-slash');
                    icon.classList.add('fa-eye');
                }
            }
        };

        updateIconVisibility();
        input.addEventListener('input', updateIconVisibility);

        // --- クリックでの表示切り替え (トグル動作) ---
        icon.addEventListener('click', function(e) {
            e.preventDefault();

            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    });
});