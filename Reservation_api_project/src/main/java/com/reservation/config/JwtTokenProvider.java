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

@Component
public class JwtTokenProvider {

	private final Key key;
	private final long expiration;

	public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, @Value("${jwt.experation}") long expiration) {
		byte[] decodedKey = Base64.getDecoder().decode(secretKey);
		this.key = Keys.hmacShaKeyFor(decodedKey);
		this.expiration = expiration;
	}

	public String generateToken(Long userId, String role) {
		return Jwts.builder().setSubject(userId.toString()).claim("role", role).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	public Claims parseToken(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}

	public Long getUserIdFromToken(String token) {
		return Long.parseLong(parseToken(token).getSubject());
	}

	public String getUserRoleFromToken(String token) {
		return parseToken(token).get("role", String.class);
	}
	
	// JWT 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
