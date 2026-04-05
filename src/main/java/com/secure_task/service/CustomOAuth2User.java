package com.secure_task.service;

import com.secure_task.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

// Wraps our User entity so it works as BOTH UserDetails and OAuth2User
public class CustomOAuth2User implements OAuth2User {
    private final User user;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    /*@Override
    public Collection getAuthorities() {
        return user.getAuthorities();
    }*/
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }
     @Override
    public String getName() {
        return user.getEmail();
    }
    public String getEmail() {
        return user.getEmail();
    }


}
