package com.three.ott_suggestion.global.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";
    // 토큰 만료시간
    private final long ACCESS_TOKEN_TIME = 60 * 60 * 1000L; // 60분
    private final long REFRESH_TOKEN_TIME = 7 * 24 * 60 * 60 * 1000L; // 7일

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // 토큰 생성
    public String createAccessToken(String username) {
        Date date = new Date();

        return BEARER_PREFIX +
            Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(date.getTime() + ACCESS_TOKEN_TIME))
                .setIssuedAt(date)
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    public String createRefreshToken(String username) {
        Date date = new Date();

        return BEARER_PREFIX +
            Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(date.getTime() + REFRESH_TOKEN_TIME))
                .setIssuedAt(date)
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    // header 에서 JWT 가져오기
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 토큰 검증
    public int validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return 0;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.");
            return 1;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return 2;
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public Claims getUserInfoFromExpriedToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
