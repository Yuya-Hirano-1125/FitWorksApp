package com.example.demo.service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    // SMSログイン用のOTP保存場所
    private final ConcurrentHashMap<String, String> otpStorage = new ConcurrentHashMap<>();

    // --- 既存のパスワードリセット用メソッド (UserServiceから呼ばれる) ---
    public void sendVerificationCode(String toPhoneNumber, String code) {
        String messageBody = "【FitWorks】認証コード: " + code + "\nパスワード再設定のために画面に入力してください。";
        sendSms(toPhoneNumber, messageBody);
    }

    // --- 新規追加: SMSログイン用メソッド (AuthControllerから呼ばれる) ---
    public void sendOtp(String phoneNumber) {
        // 6桁のコード生成
        String otp = String.format("%06d", new Random().nextInt(999999));
        // 保存
        otpStorage.put(phoneNumber, otp);
        
        String messageBody = "【FitWorks】ログイン認証コード: " + otp;
        sendSms(phoneNumber, messageBody);
    }

    // --- 新規追加: SMSログイン用コード検証 ---
    public boolean verifyOtp(String phoneNumber, String code) {
        String storedOtp = otpStorage.get(phoneNumber);
        if (storedOtp != null && storedOtp.equals(code)) {
            otpStorage.remove(phoneNumber); // 使い回し防止
            return true;
        }
        return false;
    }

    // 共通送信処理
    private void sendSms(String toPhoneNumber, String messageBody) {
        // 設定値チェック（空ならログ出力のみ）
        if (accountSid == null || accountSid.isEmpty() || authToken == null || authToken.isEmpty()) {
            System.out.println("=== SMS送信シミュレーション ===");
            System.out.println("To: " + toPhoneNumber);
            System.out.println("Body: " + messageBody);
            System.out.println("=============================");
            return;
        }

        try {
            // 初期化（何度も呼んでも大丈夫ですが、通常はConfigクラスで行うのがベター）
            Twilio.init(accountSid, authToken);
            Message.creator(
                new PhoneNumber(toPhoneNumber),
                new PhoneNumber(fromPhoneNumber),
                messageBody
            ).create();
            System.out.println("SMS Sent to " + toPhoneNumber);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("SMS Send Error: " + e.getMessage());
        }
    }
}