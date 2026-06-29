package com.example.flood_alert.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;

import com.example.flood_alert.repository.InvalidatedTokenRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signedKey}")
    private String signedKey;

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    private NimbusJwtDecoder nimbusJwtDecoder;

    @Override
    public Jwt decode(String token) throws JwtException {

        if (nimbusJwtDecoder == null) {

            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(signedKey.getBytes(), "HS512");

            nimbusJwtDecoder =
                    NimbusJwtDecoder.withSecretKey(secretKeySpec)
                            .macAlgorithm(MacAlgorithm.HS512)
                            .build();
        }

        Jwt jwt = nimbusJwtDecoder.decode(token);

        String jti = jwt.getId();

        if (invalidatedTokenRepository.existsByJwtId(jti)) {
            throw new JwtException("Token has been invalidated");
        }

        return jwt;
    }
}