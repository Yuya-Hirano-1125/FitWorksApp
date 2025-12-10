package com.example.demo.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.demo.entity.AuthProvider;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // プロバイダ (line, apple)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        // プロバイダ固有IDの取得
        // LINEは "sub" または "userId"、Appleは "sub" が識別子
        String providerId = oAuth2User.getAttribute("sub");
        if (providerId == null) {
            // LINEの場合、userId属性がIDになることもある
            Object userIdObj = oAuth2User.getAttribute("userId");
            if (userIdObj != null) {
                providerId = userIdObj.toString();
            } else {
                 // 最終手段としてNameを使用 (一意性が低い場合があるので注意)
                providerId = oAuth2User.getName();
            }
        }

        updateOrCreateUser(provider, providerId, oAuth2User);

        return oAuth2User;
    }

    private void updateOrCreateUser(AuthProvider provider, String providerId, OAuth2User oAuth2User) {
        Optional<User> userOptional = userRepository.findByProviderAndProviderId(provider, providerId);
        
        if (userOptional.isEmpty()) {
            User user = new User();
            user.setProvider(provider);
            user.setProviderId(providerId);
            
            // ユーザー名の設定（重複回避のためランダムサフィックスを付与）
            String name = oAuth2User.getAttribute("name");
            if (name == null) {
                name = provider.name();
            }
            user.setUsername(name + "_" + UUID.randomUUID().toString().substring(0, 8));
            
            // メールアドレスがあれば設定（Apple等は隠蔽する場合あり）
            String email = oAuth2User.getAttribute("email");
            if (email != null) {
                user.setEmail(email);
            }

            userRepository.save(user);
        } else {
            // 既存ユーザーの更新が必要ならここに記述（例：名前が変わった場合など）
            User user = userOptional.get();
            // userRepository.save(user);
        }
    }
}