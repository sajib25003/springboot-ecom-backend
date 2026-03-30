package com.ashik.SpringEcom.config;

//import com.ashik.SpringEcom.model.User;
import com.ashik.SpringEcom.repo.UserRepo;
import com.ashik.SpringEcom.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

//import java.util.Optional;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserRepo repo;

//    @Autowired
//    private UserDetailsService userDetailsService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // UserDetailsService using email
//    @Bean
//    public UserDetailsService userDetailsService() {
//        return username -> {
//            Optional<User> user = repo.findByEmail(username);
//            return user.map(u -> org.springframework.security.core.userdetails.User
//                    .withUsername(u.getEmail())
//                    .password(u.getPassword())
//                    .roles(u.getRole().name()) // spring roles
//                    .build()
//            ).orElseThrow(() -> new RuntimeException("User not found"));
//        };
//    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http, PasswordEncoder encoder) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(customUserDetailsService).passwordEncoder(encoder);
        return authBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/register",
                                "/api/login",
                                "/api/refresh",
                                "/api/logout-user"
                        )
                        .permitAll()
                        .requestMatchers("/products").hasAnyRole("USER", "ADMIN")
                        .anyRequest()
                        .authenticated()
                )
                .addFilterBefore(jwtRequestFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                //session management
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}