package com.secure_task.service;

import com.secure_task.dto.AuthResponse;
import com.secure_task.dto.LoginRequest;
import com.secure_task.dto.RegisterRequest;
import com.secure_task.entity.User;
import com.secure_task.repository.UserRepository;
import com.secure_task.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;


    // ─── REGISTER ────────────────────────────────────────

public AuthResponse register(RegisterRequest registerRequest){
    // 1. Check if email already exists
    if(userRepository.existsByEmail(registerRequest.email())){
        throw  new RuntimeException("Email already registered");

    }

    // 2. Build User entity
    User user= User.builder()
            .name(registerRequest.name())
            .email(registerRequest.email())
            .password(passwordEncoder.encode(registerRequest.password()))
            .role("ROLE_USER")
            .authProvider(User.AuthProvider.LOCAL)
            .build();

    // 3. Save to PostgreSQL Database
    userRepository.save(user);

    // 4. Generate JWT and return

    String token= jwtUtil.generateToken(user);
    return AuthResponse.of(token, user.getEmail(),
            user.getName(),user.getRole());


}

// ─── LOGIN ───────────────────────────────────────────

 public AuthResponse login(LoginRequest  loginRequest){
     // 1. Authenticate via Spring Security
     //    This calls loadUserByUsername + checks password
try {
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    loginRequest.email(),
                    loginRequest.password()
            )
    );
}catch (Exception e){
    // This will print the REAL reason in console
    System.out.println("AUTH FAILED: " + e.getClass().getName());
    System.out.println("REASON: " + e.getMessage());
    throw e;
}

     // If wrong credentials → throws BadCredentialsException automatically
     // 2. Load user and generate token

     User user=userRepository.findByEmail(loginRequest.email()).orElseThrow();
     String token= jwtUtil.generateToken(user);
     return  AuthResponse.of(token,user.getEmail(),
             user.getName(), user.getRole());

 }




}
