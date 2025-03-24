package com.reservation.config;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * JWT 기반 인증을 위한 커스텀 AuthenticationToken
 * - Spring Security의 인증 객체로 사용됩니다.
 * - JWT 토큰이 유효할 경우, 사용자 정보를 포함한 인증 객체를 생성합니다.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final UserDetails principal;

    /**
     * 생성자
     *
     * @param principal   인증된 사용자 정보 (UserDetails)
     * @param authorities 사용자 권한 목록 (역할/권한 정보)
     */
    public JwtAuthenticationToken(UserDetails principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true); // 토큰이 유효하므로 인증된 상태로 설정
    }

    /**
     * 자격 증명 정보 반환
     * - JWT 인증에서는 별도의 비밀번호가 필요 없기 때문에 null 반환
     *
     * @return null
     */
    @Override
    public Object getCredentials() {
        return null;
    }

    /**
     * 인증된 사용자 객체 반환
     *
     * @return principal (UserDetails)
     */
    @Override
    public Object getPrincipal() {
        return principal;
    }
}
