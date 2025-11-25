package com.namdang.memos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namdang.memos.dto.responses.ApiResponse;
import com.namdang.memos.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    CustomJwtDecoder customJwtDecoder;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // whitelist endpoint (no authenticate need)
        String[] baseEndpoints = {"/auth/oidc/ory","/auth/login", "/auth/register", "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"};
        String[] publicEndpoints = Arrays.stream(baseEndpoints).map(endpoint -> endpoint).toArray(String[]::new);
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(publicEndpoints).permitAll()
                                .anyRequest().authenticated()
                );
        httpSecurity.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    ApiResponse<?> body = ApiResponse.builder()
                            .code(ErrorCode.INVALID_ACCOUNT.getCode())
                            .message(ErrorCode.INVALID_ACCOUNT.getMessage())
                            .build();
                    new ObjectMapper().writeValue(response.getOutputStream(), body);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    System.out.println("Here");
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json");
                    ApiResponse<?> body = ApiResponse.builder()
                            .code(ErrorCode.FORBIDDEN_ACTION.getCode())
                            .message(ErrorCode.FORBIDDEN_ACTION.getMessage())
                            .build();
                    new ObjectMapper().writeValue(response.getOutputStream(), body);
                })
        );
        //Tells Spring Security “when you see a Bearer token, hand it to this JwtDecoder
        // → give me an Authentication or fail.”
        // default flow
        // BearerTokenAuthenticationFilter is automated create when having oauth2ResourceServer
        /*
         *  AuthenticationFilter --> AuthenticationManager --> AuthenticationProvider
         * --> DaoAuthenticationProvider uses UserDetailsService and PasswordEncoder
         * */
        // JWT flow
        /*
        * BearerTokenAuthenticationFilter --> (if the header have Bearer) AuthenticationManager --> JwtAuthenticationProvider
        * --> uses NimbusJwtDecoder and JWS verification
        * JWT Parsing: Your token is decoded and verified
            Claims Extraction: Spring extracts the "scope" claim containing "ADMIN"
            Authority Conversion: Each scope value gets prefixed with "SCOPE_"
            Authentication Object Creation: A 'JwtAuthenticationToken' is created with:
            Name: JWT subject (admin@gmail.com)
            Authorities: Collection of SimpleGrantedAuthority objects
            * JwtAuthenticationToken is stored in SecurityContextHolder
            * Controller Access: Your controller retrieves the authentication object
         * */
        // After decode success by Nimbus --> have JWT --> continue flow and going to converter
        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(customJwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return httpSecurity.build();
    }

    // BearerTokenAuthenticationFilter → JwtAuthenticationProvider →
    // NimbusJwtDecoder → JwtAuthenticationConverter → SecurityContextHolder
    // JwtAuthenticationConverter serves as a crucial bridge between JWT token claims and Spring Security's authorization system.
    // it takes raw JWT payload and converts it into format that Spring Security expects
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // This converter examines your JWT's claims and extracts authorities based on specific patterns.
        // By default, it looks for claims named "scope" or "scp" and treats them as space-separated lists of authorities.
        // change the prefix from "SCOPE_" to "ROLE_",
        //    {
        //        "sub": "alice@example.com",
        //        "scope": "ROLE_ADMIN READ_USER",
        //        "iss": "https://issuer.example",
        //        "exp": 1767139200,
        //        "jti": "c1f1d7b8-..."
        //    }
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

}
