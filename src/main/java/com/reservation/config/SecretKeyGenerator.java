package com.reservation.config;

import java.security.Key;

import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

/**
 * JWT HS256 알고리즘에 사용할 Secret Key를 생성하는 유틸 클래스
 * - 실행 시, 안전한 시크릿 키를 생성하고 Base64로 인코딩하여 출력합니다.
 * - 생성된 키는 application.yml 또는 .properties의 jwt.secret에 등록하여 사용합니다.
 *
 * 🔐 사용 방법:
 * 1. main 메서드를 실행하면 콘솔에 시크릿 키가 출력됩니다.
 * 2. 출력된 키를 application.yml 등에 다음과 같이 설정합니다:
 *
 *    jwt:
 *      secret: <여기에 복사된 키>
 *      experation: 3600000  # 1시간 (밀리초 단위)
 */
public class SecretKeyGenerator {

    /**
     * JWT 시크릿 키 생성 및 출력
     * - HS256 알고리즘에 적합한 Key를 생성
     * - Base64 인코딩된 문자열을 콘솔에 출력
     */
    public static void main(String[] args) {
        Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String secretKey = Encoders.BASE64.encode(key.getEncoded());
        System.out.println("Generated SECRET_KEY: " + secretKey);
    }
}
