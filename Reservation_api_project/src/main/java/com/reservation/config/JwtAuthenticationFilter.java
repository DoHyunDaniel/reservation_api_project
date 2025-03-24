package com.reservation.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * - 모든 요청마다 한 번 실행되어 JWT 토큰을 검증하고
 *   사용자 정보를 Spring Security의 Context에 등록합니다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 필터 생성자
     *
     * @param jwtTokenProvider JWT 유틸 클래스 (토큰 파싱 및 검증)
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 필터의 핵심 로직
     * - Authorization 헤더에서 토큰을 추출한 뒤 유효성을 검사하고,
     *   인증된 사용자 정보를 SecurityContext에 저장합니다.
     *
     * @param request  HTTP 요청
     * @param response HTTP 응답
     * @param chain    필터 체인
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 1. 요청에서 Authorization 헤더로부터 JWT 토큰 추출
        String token = getTokenFromRequest(request);

        // 2. 토큰이 존재하고 유효하면 사용자 정보 추출
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Claims claims = jwtTokenProvider.parseToken(token);

            // JWT claims에서 사용자 ID 및 역할(role) 추출
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            // request scope에 사용자 ID 저장 (컨트롤러에서 활용 가능)
            request.setAttribute("userId", Long.parseLong(userId));

            // Spring Security에서 사용할 UserDetails 생성
            UserDetails userDetails = User.builder()
                    .username(userId)
                    .password("") // 비밀번호는 사용되지 않음
                    .roles(role)
                    .build();

            // 인증 객체 생성 및 SecurityContext에 등록
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                    userDetails, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 다음 필터로 요청 전달
        chain.doFilter(request, response);
    }

    /**
     * 요청 헤더에서 "Authorization" 값을 추출하여 JWT 토큰을 반환
     * - "Bearer " 접두사를 제거하여 실제 토큰만 반환
     *
     * @param request HTTP 요청
     * @return JWT 토큰 문자열 (없을 경우 null)
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 토큰만 추출
        }
        return null;
    }
}
