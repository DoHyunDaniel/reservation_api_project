package com.reservation.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security 보안 설정 클래스
 * - JWT 기반 인증 방식 적용
 * - 사용자 권한(ROLE_USER, ROLE_OWNER, ROLE_ADMIN)에 따른 접근 제어
 */
@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * AuthenticationManager 빈 등록
     * - Spring Security의 인증 관리 객체
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Spring Security 필터 체인 설정
     * - 경로별 권한 설정, JWT 인증 필터 등록
     * - 세션 사용하지 않고 Stateless 방식 적용
     *
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/users/auth/login", "/users/signup").permitAll()
                .requestMatchers("/stores/list").permitAll()
                .requestMatchers("/stores/register", "/stores/delete", "/stores/update").hasRole("OWNER")
                .requestMatchers("/reservation/admin/**").hasRole("ADMIN")
                .requestMatchers("/reservation/owner/**", "/reservation/confirm", "/reservation/check-in/**").hasRole("OWNER")
                .requestMatchers("/reservation/**", "/reviews/**", "/upload/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form.disable());

        return http.build();
    }

    /**
     * 패스워드 인코더 Bean 등록
     * - BCrypt 해시 알고리즘 사용
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
