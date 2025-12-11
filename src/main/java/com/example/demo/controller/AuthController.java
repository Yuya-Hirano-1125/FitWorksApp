package com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.AuthProvider;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.SmsService;
import com.example.demo.service.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserRepository userRepository;

    
    @GetMapping("/")
    public String showStartPage(Authentication authentication) {
        // もしすでにログインしているユーザーなら、ホーム画面(home)へ転送する
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/home";
        }
        
        // ログインしていなければ start.html を表示
        return "start"; 
    }
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String phoneNumber,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               Model model) {
        
        boolean hasError = false;

        // 1. ユーザー名チェック
        if (userService.isUsernameTaken(username)) {
            model.addAttribute("usernameError", "このユーザー名は既に使用されています。");
            hasError = true;
        }

        // 2. メールアドレスチェック
        if (userService.isEmailTaken(email)) {
            model.addAttribute("emailError", "このメールアドレスは既に登録されています。");
            hasError = true;
        }

        // 3. 電話番号チェック
        if (userService.isPhoneNumberTaken(phoneNumber)) {
            model.addAttribute("phoneError", "この電話番号は既に登録されています。");
            hasError = true;
        }

        // 4. パスワード不一致チェック (既存のロジックがあればそれに合わせる)
        if (!password.equals(confirmPassword)) {
            model.addAttribute("passwordError", "パスワードが一致しません。");
            hasError = true;
        }

        // エラーがある場合、入力値を保持したまま登録画面に戻る
        if (hasError) {
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("phoneNumber", phoneNumber);
            // パスワードはセキュリティ上、空にするのが一般的ですが、戻しても構いません
            return "auth/register"; // ファイルのパスに合わせてください
        }

        // エラーがない場合、登録処理へ
        try {
            userService.registerUser(username, email, phoneNumber, password);
            return "redirect:/login?registerSuccess";
        } catch (Exception e) {
            model.addAttribute("globalError", "登録中にエラーが発生しました。");
            return "auth/register";
        }
    }
    
    // --- ホーム画面表示 ---
    @GetMapping("/home")
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        User user = userRepository.findByUsername(userDetails.getUsername())
                                  .orElse(null);
        
        if (user == null) {
            return "redirect:/logout";
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("level", user.getLevel());
        model.addAttribute("experiencePoints", user.getXp());
        model.addAttribute("requiredXp", user.calculateRequiredXp());
        model.addAttribute("progressPercent", user.getProgressPercent());
        
        // ★追加: 称号を表示するためにモデルに格納
        model.addAttribute("userTitle", user.getDisplayTitle());

        return "misc/home";
    }

    // --- SMS認証用 ---
    @PostMapping("/api/auth/send-otp")
    @ResponseBody
    public ResponseEntity<?> sendOtp(@RequestParam String phoneNumber) {
        try {
            smsService.sendOtp(phoneNumber);
            return ResponseEntity.ok("SMS sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send SMS: " + e.getMessage());
        }
    }
 // クラス内に以下を追加してください
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        // templates/auth/forgot-password.html を表示する
        return "auth/forgot-password";
    }
    @PostMapping("/api/auth/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyOtp(@RequestParam String phoneNumber, 
                                       @RequestParam String code, 
                                       HttpServletRequest request) {
        if (smsService.verifyOtp(phoneNumber, code)) {
            User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setPhoneNumber(phoneNumber);
                    newUser.setProvider(AuthProvider.PHONE);
                    newUser.setUsername("phone_" + phoneNumber.substring(phoneNumber.length() - 4)); 
                    return userRepository.save(newUser);
                });

            CustomUserDetails userDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authToken);
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            return ResponseEntity.ok("Login successful");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid verification code");
    }
 // 1. パスワードリセット画面からの送信を受け取る (POST)
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String phoneNumber, 
                                      RedirectAttributes redirectAttributes, 
                                      Model model) {
        
        // ▼▼▼ 追加: 電話番号が登録されているかチェック ▼▼▼
        if (userRepository.findByPhoneNumber(phoneNumber).isEmpty()) {
            model.addAttribute("error", "この電話番号は登録されていません。");
            return "auth/forgot-password"; // 入力画面に戻る
        }
        try {
            smsService.sendOtp(phoneNumber);
            
            // ★修正: URLパラメータではなく、FlashAttributeにセットする
            // これによりURLには表示されず、データは次の画面まで維持されます
            redirectAttributes.addFlashAttribute("phoneNumber", phoneNumber);
            
            return "redirect:/verify-code"; // ?phoneNumber=... を削除
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "SMS送信に失敗しました: " + e.getMessage());
            return "auth/forgot-password";
        }
    }

    // ■ 修正: GET /verify-code
    @GetMapping("/verify-code")
    public String showVerifyCode(@ModelAttribute("phoneNumber") String phoneNumber, Model model) {
        // FlashAttributeで渡されたphoneNumberは自動的にModelに入りますが、
        // 明示的に受け取るか、Modelから取得します。
        
        // 注意: リロードするとFlashAttributeは消えるため、その場合の対策
        if (phoneNumber == null || phoneNumber.isEmpty()) {
             // 電話番号がない状態で直接アクセスされた場合、入力画面に戻すのが安全です
             return "redirect:/forgot-password";
        }

        model.addAttribute("phoneNumber", phoneNumber);
        return "auth/verify-code";
    }

    // 3. 認証コードを検証する (POST: verify-code.htmlからの送信)
 // 3. 認証コードを検証する (POST)
    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String phoneNumber, 
                             @RequestParam String code, 
                             RedirectAttributes redirectAttributes,
                             Model model) {
        // SMSサービスの検証メソッド（仮定）
        if (smsService.verifyOtp(phoneNumber, code)) {
            // 認証成功
            // パスワード再設定画面へ電話番号を引き継ぐ
            // redirectAttributes.addAttribute を使うとURLパラメータ(?phoneNumber=...)として付与され、
            // リダイレクト先でも消えずに残ります（GETパラメータとして扱われる）。
            redirectAttributes.addAttribute("phoneNumber", phoneNumber);
            
            return "redirect:/reset-password"; 
        } else {
            // 認証失敗
            model.addAttribute("error", "認証コードが正しくありません。");
            model.addAttribute("phoneNumber", phoneNumber);
            return "auth/verify-code";
        }
    }

    // 4. パスワード再設定画面を表示 (GET)
    // 開発確認用として、パラメータがなくても表示できるように required = false にしています
    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam(name = "phoneNumber", required = false) String phoneNumber, Model model) {
        // reset-password.html では "token" という名前で hidden input に入れているため
        // ここで token として渡してあげます。
        model.addAttribute("token", phoneNumber);
        
        return "auth/reset-password";
    }

    // 5. 新しいパスワードを保存する (POST)
    @PostMapping("/reset-password")
    public String updatePassword(@RequestParam("token") String phoneNumber,
                                 @RequestParam("password") String newPassword,
                                 Model model) {
        try {
            // ここでパスワード更新処理を行う
            // 例: userService.updatePassword(phoneNumber, newPassword);
            System.out.println("パスワード更新: " + phoneNumber + " -> " + newPassword);

            // 更新完了後はログイン画面へ
            return "redirect:/login?resetSuccess";
        } catch (Exception e) {
            model.addAttribute("error", "パスワード更新に失敗しました");
            model.addAttribute("token", phoneNumber);
            return "auth/reset-password";
        }
    }
}