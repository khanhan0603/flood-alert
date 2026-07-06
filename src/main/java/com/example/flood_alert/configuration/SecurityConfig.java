package com.example.flood_alert.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${jwt.signedKey}")
    private String signedKey;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                            "/health",
                            "/auth/**",
                            "/user/register",
                            "/sos-request/**",
                            "/area/**",
                            "/weather-data/**",
                            "/iot-device/register-device",
                            "/iot-device/read-sensor",
                            "/sos-request/*/anonymous",
                            "/sos-request/my-active-anonymous",
                            "/sos-request/*/anonymous/cancel",
                            "/sms/**",
                            "/notification/**",
                            "/hotline/emergency-contact",
                             "/predict/run"
                        ).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 ->
                    oauth2.jwt(jwt ->
                            jwt.decoder(customJwtDecoder)))
                .exceptionHandling(ex->ex
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "https://tranthitrucly.io.vn",
                "http://tranthitrucly.io.vn",
                "http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    RestTemplate restTemplate() {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(30000);
        factory.setReadTimeout(7200000);

        return new RestTemplate(factory);
    }
}