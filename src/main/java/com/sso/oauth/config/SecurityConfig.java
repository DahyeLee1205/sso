package com.sso.oauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**", "/login/**", "/kakao, ","/getAccessToken").permitAll() // 모두 허용
                        //.anyRequest().authenticated()
                        .anyRequest().permitAll() // 모든 요청 허용
                )
                .formLogin(login -> login.disable())       // 기본 로그인 화면 비활성화
                .httpBasic(httpBasic -> httpBasic.disable()); // HTTP Basic 인증 비활성화
/*                .oauth2Login(oauth2 ->oauth2
                .defaultSuccessUrl("/home", true)
                );*/

        return http.build();
    }

}
