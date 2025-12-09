package com.example.demo.security;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

/**
 * FitWorks 用の UserDetailsService 実装
 * CustomUserDetails にラップして返す
 */
@Service("fitWorksUserDetailsService") // ← Bean名を明示的に指定
public class FitWorksUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public FitWorksUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // DBからユーザーを検索
        Optional<User> optionalUser = userRepository.findByUsername(username);

        User user = optionalUser.orElseThrow(
            () -> new UsernameNotFoundException("ユーザーが見つかりません: " + username)
        );

        // CustomUserDetails に包んで返す
        return new CustomUserDetails(user);
    }
}
