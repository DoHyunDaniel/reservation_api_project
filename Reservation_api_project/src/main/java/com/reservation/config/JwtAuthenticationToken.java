package com.reservation.config;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final UserDetails principal;

    public JwtAuthenticationToken(UserDetails principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true); // 인증된 상태로 설정
    }

    @Override
    public Object getCredentials() {
        return null; // JWT는 비밀번호가 필요하지 않음
    }

    @Override
    public Object getPrincipal() {
        return principal; // 인증된 사용자 정보 반환
    }
}
