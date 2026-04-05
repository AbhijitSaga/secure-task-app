package com.secure_task.service;

import com.secure_task.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
private final UserRepository userRepository;

    // ✅ Spring Security calls this automatically during login
    // We use email as the "username"
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
           return userRepository.findByEmail(username) .orElseThrow(()->
                   new UsernameNotFoundException( "User not found with email: " + username)
                   );

        // Our User entity implements UserDetails, so we return it directly!

    }
}
