package com.example.demo.security;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.example.demo.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // パスワードエンコーダーを定義
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // 認証プロバイダーを定義
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    /**
     * CSRFトークンをレスポンスヘッダーに含めるカスタムフィルターを定義します。
     * これにより、React (JavaScript) がトークンを読み取ることが可能になります。
     */
    private Filter csrfCookieFilter() {
        return (request, response, chain) -> {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            
            // ServletResponseをHttpServletResponseにキャスト
            HttpServletResponse httpResponse = (HttpServletResponse) response; 

            if (csrfToken != null) {
                // クライアント側で読み取り可能なヘッダーとしてトークンを設定
                // ReactはこのヘッダーまたはCookieからトークンを取得します
                httpResponse.setHeader(csrfToken.getHeaderName(), csrfToken.getToken());
            }
            try {
                chain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                e.printStackTrace();
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. CORSを有効化
            .cors(Customizer.withDefaults())
            
            // 2. CSRF設定: Cookieでトークンを公開
            .csrf((csrf) -> csrf
                // JSからアクセスできるように HttpOnly=false でCookieにトークンを設定
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            // 3. CSRFトークンを全てのレスポンスヘッダーに含めるフィルターを追加
            .addFilterAfter(csrfCookieFilter(), BasicAuthenticationFilter.class)

            // 4. ページごとのアクセス制御
            .authorizeHttpRequests(auth -> auth
                // Reactのエントリポイント、静的ファイル、認証画面を認証なしで許可
                .requestMatchers(
                    "/", 
                    "/home", 
                    "/ai-coach", 
                    "/ai-coach/**", 
                    "/settings",
                    "/change-password",
                    "/forgot-password", 
                    "/register", 
                    "/login", 
                    "/css/**", 
                    "/js/**", 
                    "/images/**",
                    "/{path:[^\\.]*}" // 静的ファイルでないその他のルートパス
                ).permitAll()
                // APIエンドポイント (/api/**) は認証が必要
                .requestMatchers("/api/**").authenticated() 
                .anyRequest().authenticated()
            )
            
            // 5. フォームログイン
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            
            // 6. ログアウト設定
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN") // CSRF Cookieも削除
                .permitAll()
            )
            
            // 7. 認証プロバイダ登録
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}