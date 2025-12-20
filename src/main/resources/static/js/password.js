document.addEventListener('DOMContentLoaded', function() {
    
    // =========================================
    // 1. パスワード欄のコピペ・切り取り禁止
    // =========================================
    const passwordInputs = document.querySelectorAll('.password-wrapper input');

    passwordInputs.forEach(input => {
        // 貼り付け (Paste) 禁止
        input.addEventListener('paste', function(e) {
            e.preventDefault();
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
    // 2. 目のアイコンの表示/非表示切り替え機能
    // =========================================
    const icons = document.querySelectorAll('.toggle-password-icon');

    icons.forEach(icon => {
        const wrapper = icon.closest('.password-wrapper');
        if (!wrapper) return;
        
        const input = wrapper.querySelector('input');
        if (!input) return;

        // クリックでの表示切り替え (トグル動作)
        icon.addEventListener('click', function(e) {
            e.preventDefault(); // フォーム送信などを防ぐ

            if (input.type === 'password') {
                // パスワードを表示
                input.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash'); // 斜線付きの目に変更
            } else {
                // パスワードを非表示
                input.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye'); // 普通の目に変更
            }
        });
    });
});