// js/password-check.js

document.addEventListener('DOMContentLoaded', () => {
    // ページ共通の要素（新しいパスワード入力欄）
    const passwordInput = document.getElementById('new-password');
    const strengthText = document.getElementById('strength-text');
    const strengthBar = document.getElementById('strength-bar');

    // ページ固有の要素を動的に設定
    // register.html: 'confirm-password' | change-password.html: 'confirm-new-password'
    const confirmInput = document.getElementById('confirm-password') || document.getElementById('confirm-new-password');
    
    // パスワード条件リストの要素
    const criteriaLength = document.getElementById('criteria-length');
    const criteriaUppercase = document.getElementById('criteria-uppercase');
    const criteriaNumber = document.getElementById('criteria-number');
    const criteriaSpecial = document.getElementById('criteria-special');

    if (passwordInput && strengthText && strengthBar && criteriaLength) {
        // 強度チェック
        passwordInput.addEventListener('input', updatePasswordStrength);
        
        // パスワード一致チェック（確認フィールドが存在する場合のみ）
        if (confirmInput) {
            passwordInput.addEventListener('input', checkPasswordMatch);
            confirmInput.addEventListener('input', checkPasswordMatch);
        }
        
        // 初回ロード時に強度をチェック
        updatePasswordStrength(); 
        if (confirmInput) {
            checkPasswordMatch();
        }
    }

    /**
     * パスワード入力欄と確認入力欄が一致するかチェックし、フォーム送信時の検証を行う関数
     */
    function checkPasswordMatch() {
        // confirmInputが存在しないページでは実行しない
        if (!confirmInput) return;
        
        const password = passwordInput.value;
        const confirmPassword = confirmInput.value;
        
        // 値が一致しない場合、HTML5標準のエラーメッセージを設定し、送信をブロック
        if (confirmPassword.length > 0 && password !== confirmPassword) {
            confirmInput.setCustomValidity("パスワードが一致しません。"); 
        } else {
            // エラーを解除
            confirmInput.setCustomValidity(""); 
        }
    }


    /**
     * パスワードの強度と条件リストを更新する関数
     */
    function updatePasswordStrength() {
        const password = passwordInput.value; 
        const results = checkPasswordStrengthLogic(password);

        // 1. 強度バーの更新
        strengthText.textContent = `強度: ${results.strength.text}`;
        strengthBar.className = `strength-bar ${results.strength.class}`;
        strengthBar.style.width = results.strength.width;
        
        // 2. 条件リストの更新
        if (criteriaLength) {
            updateCriteriaDisplay(criteriaLength, results.criteria.length, "8文字以上であること");
            updateCriteriaDisplay(criteriaUppercase, results.criteria.hasUpper, "英大文字（A–Z）を含める");
            updateCriteriaDisplay(criteriaNumber, results.criteria.hasNumber, "数字（0–9）を含める");
            updateCriteriaDisplay(criteriaSpecial, results.criteria.hasSpecial, "記号（!@#$%^& など）を含める");
        }
        
        // 強度チェックの際も、一致チェックを呼び出すことでリアルタイム性を確保
        if (confirmInput) {
            checkPasswordMatch(); 
        }
    }

    /**
     * 個別の条件表示を更新するヘルパー関数
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
     * パスワード強度を判定するロジック
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