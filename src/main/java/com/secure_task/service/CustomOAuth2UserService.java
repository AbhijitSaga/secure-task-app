package com.secure_task.service;

import com.secure_task.entity.User;
import com.secure_task.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    // ✅ Called by Spring after Google sends user info


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1. Fetch Google user info (name, email, picture etc.)
OAuth2User oAuth2User=super.loadUser(userRequest);

        // 2. Extract attributes from Google
        String email=oAuth2User.getAttribute("email");
        String name=oAuth2User.getAttribute("name");
        String providerId=oAuth2User.getAttribute("sub");  // Google's unique ID
        // 3. Find or create user in our DB

        User user=userRepository.findByEmail(email).orElseGet(
                ()-> {
                    // First login via Google → create account automatically!
                  return userRepository.save(
                          User.builder()
                                  .email(email)
                                  .name(name)
                                  .password(null)
                                  .role("ROLE_USER")
                                  .authProvider(User.AuthProvider.GOOGLE)
                                  .providerId(providerId)
                                  .build()
                  );
                }
        );


        // 4. Update name if changed in Google account
     if(!name.equals(user.getName())){
         user.setName(name);
         userRepository.save(user);
     }

        // 5. Return our User (which implements OAuth2User via UserDetails)
        // We wrap it so Spring Security treats it as both OAuth2User + UserDetails
        return new  CustomOAuth2User(user, oAuth2User.getAttributes());

    }
}
