package com.reservation.config;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * JWT 토큰을 생성, 파싱, 검증하는 유틸리티 클래스
 * - 사용자 인증 및 인가 처리를 위한 JWT 처리 전담
 */
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long expiration;

    /**
     * 생성자: 시크릿 키를 디코딩하고 서명용 Key 객체로 변환
     *
     * @param secretKey  base64로 인코딩된 시크릿 키
     * @param expiration JWT 유효 시간 (밀리초)
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.experation}") long expiration) {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(decodedKey);
        this.expiration = expiration;
    }

    /**
     * JWT 토큰 생성 메소드
     *
     * @param userId    사용자 ID
     * @param role      사용자 권한 (예: "USER", "OWNER", "ADMIN")
     * @param isPartner 파트너 여부 (토큰에 직접 사용되지 않음, 확장 가능성)
     * @return 생성된 JWT 토큰 문자열
     */
    public String generateToken(Long userId, String role, boolean isPartner) {
        return Jwts.builder()
                .setSubject(userId.toString())                // 사용자 ID를 subject로 설정
                .claim("role", role)                         // 역할 정보 저장
                .setIssuedAt(new Date())                     // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256)     // 서명 알고리즘 및 키 설정
                .compact();
    }

    /**
     * JWT 토큰에서 Claims(정보) 추출
     *
     * @param token JWT 문자열
     * @return 파싱된 Claims 객체 (subject, role 등 포함)
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * JWT에서 사용자 ID(subject)를 추출
     *
     * @param token JWT 문자열
     * @return 사용자 ID (Long)
     */
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    /**
     * JWT에서 사용자 역할(Role)을 추출
     *
     * @param token JWT 문자열
     * @return 사용자 권한 문자열 (예: "USER", "OWNER")
     */
    public String getUserRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * JWT 유효성 검증
     * - 서명 위조, 만료 여부 등을 확인
     *
     * @param token JWT 문자열
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
