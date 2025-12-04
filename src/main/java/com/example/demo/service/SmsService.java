package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    public void sendVerificationCode(String toPhoneNumber, String code) {
        String messageBody = "【FitWorks】認証コード: " + code + "\nパスワード再設定のために画面に入力してください。";

        // 設定値が空の場合はコンソール出力のみ（シミュレーション）
        if (accountSid == null || accountSid.isEmpty() || authToken == null || authToken.isEmpty()) {
            System.out.println("========================================");
            System.out.println("【SMS送信シミュレーション】");
            System.out.println("宛先: " + toPhoneNumber);
            System.out.println("内容: " + messageBody);
            System.out.println("========================================");
            return;
        }

        try {
            Twilio.init(accountSid, authToken);
            Message.creator(
                new PhoneNumber(toPhoneNumber),
                new PhoneNumber(fromPhoneNumber),
                messageBody
            ).create();
            System.out.println("SMSを送信しました: " + toPhoneNumber);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("SMS送信エラー: " + e.getMessage());
        }
    }
}