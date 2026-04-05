package com.secure_task.handler;

import com.secure_task.entity.User;
import com.secure_task.repository.UserRepository;
import com.secure_task.service.CustomOAuth2User;
import com.secure_task.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
// ✅ After Google login succeeds → generate JWT → redirect to frontend
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. Get the authenticated OAuth2 user
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getEmail();

        // 2. Load from DB
        User user = userRepository.findByEmail(email).orElseThrow();

        // 3. Generate JWT
        String token = jwtUtil.generateToken(user);

        // 4. Redirect to frontend with token in URL
        //    Frontend extracts token and stores in localStorage
        String redirectUrl = "http://localhost:3000/oauth2/callback?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }


}

