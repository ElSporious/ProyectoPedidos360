package com.example.adminrabbit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;

@Configuration
@Profile("local")
public class LocalSecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> {
            String usuario = token.toLowerCase();
            String rol = switch (usuario) {
                case "admin" -> "Admin";
                case "operador" -> "Operador";
                case "cliente" -> "Cliente";
                default -> throw new BadJwtException("Token local no valido: " + token);
            };
            return Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("roles", List.of(rol))
                    .claim("preferred_username", usuario + "@local.test")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
        };
    }
}
