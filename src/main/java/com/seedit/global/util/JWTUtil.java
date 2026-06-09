package com.seedit.global.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT 발급/검증 유틸리티
 */
public class JWTUtil {

    private final SecretKey secretKey;
    private final long accessTokenexpirationMs;
    private final long refreshTokenexpirationMs;

    /**
     * @param secret
     * @param accessTokenexpirationMs
     * @param refreshTokenexpirationMs
     */
    public JWTUtil(String secret, long accessTokenexpirationMs, long refreshTokenexpirationMs){
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenexpirationMs = accessTokenexpirationMs;
        this.refreshTokenexpirationMs = refreshTokenexpirationMs;
    }

    public String generateAccessToken(String subject){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenexpirationMs);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(String subject){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenexpirationMs);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch(JwtException e){
            return false;
        }
    }

    public String getSubject(String token){ return getClaims(token).getSubject();}

    public Claims getClaims(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token){
        try{
            Date exp = getClaims(token).getExpiration();
            return exp.before(new Date());
        } catch (JwtException e){
            return true;
        }
    }

}
