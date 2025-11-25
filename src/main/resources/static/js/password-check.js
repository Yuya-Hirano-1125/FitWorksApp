// js/password-check.js

document.addEventListener('DOMContentLoaded', () => {
    const passwordInput = document.getElementById('new-password');
    const confirmInput = document.getElementById('confirm-password'); // 確認フィールドを取得
    // const strengthText = document.getElementById('strength-text'); // 既存
    // const strengthBar = document.getElementById('strength-bar');   // 既存
    
    // ★ 削除: const passwordMatchError = document.getElementById('password-match-error'); // エラーメッセージ要素を取得
    
    // パスワード条件リストの要素を取得 (既存)
    const criteriaLength = document.getElementById('criteria-length');
    const criteriaUppercase = document.getElementById('criteria-uppercase');
    const criteriaNumber = document.getElementById('criteria-number');
    const criteriaSpecial = document.getElementById('criteria-special');
    
    // 省略されている強度表示要素を再取得
    const strengthText = document.getElementById('strength-text'); 
    const strengthBar = document.getElementById('strength-bar'); 


    // ★ 修正: 要素チェックから passwordMatchError を除外 ★
    if (passwordInput && confirmInput && strengthText && strengthBar && criteriaLength) {
        // 強度チェック
        passwordInput.addEventListener('input', updatePasswordStrength);
        
        // ★ パスワード一致チェックのイベントリスナーを追加 ★
        passwordInput.addEventListener('input', checkPasswordMatch);
        confirmInput.addEventListener('input', checkPasswordMatch);
        
        // 初回ロード時に強度をチェック
        updatePasswordStrength(); 
        checkPasswordMatch();
    }

    /**
     * パスワード入力欄と確認入力欄が一致するかチェックし、エラー表示を制御する関数
     */
    function checkPasswordMatch() {
        const password = passwordInput.value;
        const confirmPassword = confirmInput.value;
        
        // 値が一致しない場合、HTML5標準のエラーメッセージを設定し、送信をブロック
        if (confirmPassword.length > 0 && password !== confirmPassword) {
            // ★ 変更: エラーメッセージを表示する処理を削除 ★
            confirmInput.setCustomValidity("パスワードが一致しません。"); // カスタムバリデーションメッセージを設定
        } else {
            // エラーを解除
            // ★ 変更: エラーメッセージを非表示にする処理を削除 ★
            confirmInput.setCustomValidity(""); // エラーを解除
        }
    }


    /**
     * パスワードの強度を判定し、表示を更新する関数 (既存ロジック)
     */
    function updatePasswordStrength() {
        const password = passwordInput.value; 
        const results = checkPasswordStrengthLogic(password);

        // 1. 強度バーの更新
        strengthText.textContent = `強度: ${results.strength.text}`;
        strengthBar.className = `strength-bar ${results.strength.class}`;
        strengthBar.style.width = results.strength.width;
        
        // 2. 条件リストの更新
        updateCriteriaDisplay(criteriaLength, results.criteria.length, "8文字以上であること");
        updateCriteriaDisplay(criteriaUppercase, results.criteria.hasUpper, "英大文字（A–Z）を含める");
        updateCriteriaDisplay(criteriaNumber, results.criteria.hasNumber, "数字（0–9）を含める");
        updateCriteriaDisplay(criteriaSpecial, results.criteria.hasSpecial, "記号（!@#$%^& など）を含める");
        
        // 強度チェックの際も、一致チェックを呼び出すことでリアルタイム性を確保
        checkPasswordMatch(); 
    }

    /**
     * 個別の条件表示を更新するヘルパー関数 (既存ロジック)
     */
    function updateCriteriaDisplay(element, isMet, text) {
        if (isMet) {
            element.classList.add('met');
            element.classList.remove('unmet');
            element.innerHTML = `<i class="fa-solid fa-check"></i> ${text}`;
        } else {
            element.classList.add('unmet');
            element.classList.remove('met');
            element.innerHTML = `<i class="fa-solid fa-xmark"></i> ${text}`;
        }
    }

    /**
     * パスワード強度を判定するロジック (関数名を変更して分離)
     */
    function checkPasswordStrengthLogic(password) {
        // 条件達成状況の判定
        const hasLower = /[a-z]/.test(password);
        const hasUpper = /[A-Z]/.test(password);
        const hasNumber = /[0-9]/.test(password);
        const hasSpecial = /[^A-Za-z0-9]/.test(password);
        
        const isLengthValid = password.length >= 8;

        if (password.length === 0) {
            return { 
                strength: { text: '未入力', class: 'weak', width: '0%' },
                criteria: { length: false, hasUpper: false, hasNumber: false, hasSpecial: false }
            };
        }

        let score = 0;
        
        // 1. 長さのチェック
        if (password.length >= 8) score += 1;
        if (password.length >= 12) score += 1;

        // 2. 文字種類のチェック (スコア計算用)
        let charTypeCount = 0;
        if (hasLower) charTypeCount++;
        if (hasUpper) charTypeCount++;
        if (hasNumber) charTypeCount++;
        if (hasSpecial) charTypeCount++;

        if (charTypeCount >= 2) score += 1;
        if (charTypeCount >= 3) score += 1;

        // 強度テキストと幅の設定
        let text, strengthClass, width;

        if (score < 2) {
            text = '弱'; strengthClass = 'weak'; width = '33%';
        } else if (score < 4) {
            text = '中'; strengthClass = 'medium'; width = '66%';
        } else {
            text = '強'; strengthClass = 'strong'; width = '100%';
        }

        return { 
            strength: { text, class: strengthClass, width },
            criteria: { 
                length: isLengthValid, 
                hasUpper: hasUpper, 
                hasNumber: hasNumber, 
                hasSpecial: hasSpecial 
            }
        };
    }
});