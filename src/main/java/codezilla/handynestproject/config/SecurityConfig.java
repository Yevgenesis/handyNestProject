package codezilla.handynestproject.config;

import codezilla.handynestproject.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // to work with @PreAuthorize
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter authenticationFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
//                        .requestMatchers("/users/**",
//                                "/attachments/**",
//                                "/categories/**",
//                                "feedbacks/**",
//                                "/performers/**",
//                                "/tasks/**",
//                                "/v3/api-docs/**",
//                                "/swagger-ui/**")
                        .requestMatchers("/categories/**").permitAll()
                        .anyRequest().authenticated()) // ToDo .authenticated() для прода, а .permitAll() для дев
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Bean
//    //for encode password
//    public PasswordEncoder passwordEncoder() {
////        return NoOpPasswordEncoder.getInstance();
//         return new BCryptPasswordEncoder();
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}