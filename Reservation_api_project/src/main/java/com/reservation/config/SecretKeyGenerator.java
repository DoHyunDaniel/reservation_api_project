package com.reservation.config;

import java.security.Key;

import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

public class SecretKeyGenerator {
    public static void main(String[] args) {
        Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String secretKey = Encoders.BASE64.encode(key.getEncoded()); // Base64 인코딩
        System.out.println("Generated SECRET_KEY: " + secretKey);
    }
}