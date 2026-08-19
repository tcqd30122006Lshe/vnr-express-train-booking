package com.trainbooking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String[] PUBLIC_POST_ENDPOINTS={
            "/h2-console/**",
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/introspect",
            "/api/v1/auth/logout",
            "/api/v1/trips/search",
            "/api/v1/bookings/**",
            "/api/v1/payments/**",
            "/api/v1/tickets/**"
    };
    private final String[] PUBLIC_GET_POINTS={
            "/",
            "/index.html",
            "/favicon.ico",
            "/*.css",
            "/*.js",
            "/style.css",
            "/app.js",
            "/h2-console/**",
            "/static/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/api/v1/stations/**",
            "/api/v1/trips/**",
            "/api/v1/payments/**",
            "/api/v1/tickets/**",
            "/api/v1/analytics/**"
    };
    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        httpSecurity.authorizeHttpRequests(request ->request.requestMatchers(HttpMethod.POST,PUBLIC_POST_ENDPOINTS).permitAll()
                .requestMatchers(HttpMethod.GET,PUBLIC_GET_POINTS).permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()

        );

        httpSecurity.oauth2ResourceServer(oauth2->oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(customJwtDecoder)
                .jwtAuthenticationConverter( jwtAuthenticationConverter()))

        );
        httpSecurity.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter=new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter=new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
