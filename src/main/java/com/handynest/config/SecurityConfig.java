package com.handynest.config;

import com.handynest.auth.security.JwtAuthenticationFilter;
import com.handynest.common.api.ApiConstants;
import com.handynest.common.web.JsonAccessDeniedHandler;
import com.handynest.common.web.JsonAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter authenticationFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.POST,
                                ApiConstants.API_V1 + "/auth/register",
                                ApiConstants.API_V1 + "/auth/login",
                                ApiConstants.API_V1 + "/auth/refresh").permitAll()
                        .requestMatchers(ApiConstants.API_V1 + "/categories/**").permitAll()
                        .requestMatchers(ApiConstants.API_V1 + "/geo/**").permitAll()
                        .requestMatchers(HttpMethod.GET, ApiConstants.API_V1 + "/performers").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                ApiConstants.API_V1 + "/tasks",
                                ApiConstants.API_V1 + "/tasks/*").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                ApiConstants.API_V1 + "/tasks/*/feedbacks",
                                ApiConstants.API_V1 + "/users/*/feedbacks",
                                ApiConstants.API_V1 + "/performers/*/feedbacks").permitAll()
                        .requestMatchers(HttpMethod.POST, ApiConstants.API_V1 + "/payments/webhooks/**").permitAll()
                        .requestMatchers(new RegexRequestMatcher(
                                "^" + ApiConstants.API_V1 + "/performers/(?!me(?:/|$))[^/]+$",
                                "GET"
                        )).permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui-custom.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()) //  .authenticated() for production, and .permitAll() for develop
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth, PasswordEncoder passwordEncoder) throws Exception {
//        auth.inMemoryAuthentication()
//                .withUser("user")
//                .password(passwordEncoder.encode("password"))
//                .roles("ADMIN");
//    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
