package id.ac.ui.cs.advprog.palmerypayment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/webhook").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/debug/healthcheck").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/payrolls/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/notif/broadcast").hasRole("ADMIN")
                        .requestMatchers("/api/debug/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/payments/create").hasRole("ADMIN")
                        .requestMatchers("/api/payments/**").authenticated()
                        .requestMatchers("/api/notif/**").authenticated()
                        .requestMatchers("/api/wallets/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                ));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${auth.allowed-origins:http://localhost:3001}") List<String> allowedOrigins
    ) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(allowedOrigins);
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PATCH", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${auth.jwt-secret:replace-this-secret-with-at-least-32-characters}") String jwtSecret) {
        return NimbusJwtDecoder.withSecretKey(buildSigningKey(jwtSecret)).build();
    }

    @Bean
    public Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

        return jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>(scopeConverter.convert(jwt));
            String role = jwt.getClaimAsString("role");
            if (role != null && !role.isBlank()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            }
            return new JwtAuthenticationToken(jwt, authorities);
        };
    }

    private SecretKey buildSigningKey(String configuredSecret) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(configuredSecret);
            if (decodedKey.length >= 32) {
                return new SecretKeySpec(decodedKey, "HmacSHA256");
            }
        } catch (RuntimeException ignored) {
            // Fall back to raw-string key handling.
        }

        byte[] rawKey = configuredSecret.getBytes(StandardCharsets.UTF_8);
        if (rawKey.length < 32) {
            try {
                rawKey = java.security.MessageDigest.getInstance("SHA-256").digest(rawKey);
            } catch (java.security.NoSuchAlgorithmException ex) {
                throw new IllegalStateException("SHA-256 unavailable", ex);
            }
        }
        return new SecretKeySpec(rawKey, "HmacSHA256");
    }
}
