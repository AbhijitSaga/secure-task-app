package com.secure_task.entity;


import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User  implements UserDetails {
    // ✅ Implements UserDetails so Spring Security can use this directly
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private  Long id;

@Column(nullable = false, unique = true)
private String email;

private String name;

    // 🔑 Password is NULL for OAuth2 (Google) users
private  String password;

@Column(nullable = false)
@Builder.Default
private String role="ROLE_USER";


    // ─── Auth Provider ────────────────────────────────────
    // LOCAL  = registered via username/password
    // GOOGLE = registered via Google OAuth2
 @Column(name = "auth_provider", nullable = false)
 @Enumerated(EnumType.STRING)
 @Builder.Default
 private AuthProvider  authProvider = AuthProvider.LOCAL;  // Google's unique user ID

    @Column(name = "provider_id")
    private String providerId;     // Google's unique user ID

    @Builder.Default
    private boolean enabled =true;

@Column(name = "created_at",updatable = false)
@Builder.Default
private LocalDateTime createAt=LocalDateTime.now();


// ─── UserDetails Methods ──────────────────────────────
// Spring Security calls these to check auth


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //convert "ROLE_USER" string to GrantedAuthority object
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;// We use email as username
    }

    @Override
    public boolean isAccountNonExpired() {
       // return UserDetails.super.isAccountNonExpired();
   return  true;
    }

    @Override
    public boolean isAccountNonLocked() {
       // return UserDetails.super.isAccountNonLocked();
        return  true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        //return UserDetails.super.isCredentialsNonExpired();
        return  true;
    }

    @Override
    public boolean isEnabled() {
       // return UserDetails.super.isEnabled();
        return enabled;
    }

    // ─── Auth Provider Enum ──────────────────────────────
    public enum AuthProvider {
        LOCAL, GOOGLE
    }
}
