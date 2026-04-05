package com.secure_task.config;

import com.secure_task.filter.JwtAuthFilter;
import com.secure_task.handler.OAuth2SuccessHandler;
import com.secure_task.service.CustomOAuth2UserService;
import com.secure_task.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    // ─── THE MAIN SECURITY FILTER CHAIN ────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity){
        httpSecurity
        // 1. DISABLE CSRF (not needed for stateless JWT APIs)
        .csrf(AbstractHttpConfigurer::disable)

        // 2. CORS configuration
                .cors(cors-> cors.configurationSource(corsConfigurationSource()))

        // 3. URL AUTHORIZATION RULES
                .authorizeHttpRequests(
                        auth->auth
                                //public endpoints
                                .requestMatchers(
                                        "/api/auth/**",          // login, register
                                        "/oauth2/**",            // OAuth2 redirect
                                        "/login/oauth2/**",      // OAuth2 callback
                                        "/actuator/health"       // health check

                                ).permitAll()

                                // admin only
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                // Everything else requires authentication
                                .anyRequest().authenticated()
                )

    // 4. STATELESS SESSION (JWT = no server-side session)
                .sessionManagement(
                        session-> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                )

     // 5. LOCAL AUTH (username/password) provider
                .authenticationProvider(authenticationProvider())

    // 6. JWT FILTER — runs before default UsernamePasswordFilter
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class)

    // 7. OAUTH2 LOGIN configuration
                .oauth2Login(oauth2-> oauth2
                        .userInfoEndpoint(endpoint-> endpoint
                                .userService(oAuth2UserService)  // custom user service
                        )
                        .successHandler(oAuth2SuccessHandler)  // generates JWT
                        .failureUrl("/login?error=oauth_failed") // ✅ Custom failure URL
                        // OR
                        .failureHandler((request, response, exception) -> {
                            System.out.println("OAuth2 Error: " + exception.getMessage());
                            response.sendRedirect("/login");  // ✅ ?error nahi aayega
                        })
                );







return httpSecurity.build();
    }



    // ─── PASSWORD ENCODER ──────────────────────────────
    // BCrypt is the industry standard for password hashing
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // ─── AUTHENTICATION PROVIDER ───────────────────────
    // Connects UserDetailsService + PasswordEncoder
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        //provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }


    // ─── CORS CONFIGURATION ────────────────────────────
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration corsConfiguration=new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000"));
       corsConfiguration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
       corsConfiguration.setAllowedHeaders(List.of("*"));
       corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource=new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);
   return urlBasedCorsConfigurationSource;
    }

    @Bean

    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return new ProviderManager(authenticationProvider());
    }

}
