package com.diginexa.bitacora.config;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.util.Base64;

@Getter
@Configuration
public class JwtConfig {

    @Value("${jwt.secret:mySuperSecretKeyForJWTGenerationThatIsAtLeast64CharactersLong123456789012345}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration:86400}")
    private Long refreshExpiration;

    @Bean
    public SecretKey secretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}