package com.manifest.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 */
public class JwtUtil {

    public static final String SECRET_KEY = "manifest_reader_secret_2026";
    private static final long EXPIRE_MS = 7 * 24 * 60 * 60 * 1000L; // 7天

    public static String createToken(String userId, String companyCode) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("companyCode", companyCode)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_MS))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public static String getUserId(String token) {
        return parseToken(token).getSubject();
    }

    public static String getCompanyCode(String token) {
        return (String) parseToken(token).get("companyCode");
    }
}
