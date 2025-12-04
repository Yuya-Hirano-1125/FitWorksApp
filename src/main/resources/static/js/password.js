/* パスワード表示切り替え関数 */
function togglePasswordVisibility(inputId, icon) {
    const input = document.getElementById(inputId);
    
    if (input.type === "password") {
        input.type = "text";
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash'); // 斜線付きの目に変更
    } else {
        input.type = "password";
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye'); // 普通の目に戻す
    }
}