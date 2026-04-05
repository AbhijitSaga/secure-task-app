package com.secure_task.filter;

import com.secure_task.service.UserDetailsServiceImpl;
import com.secure_task.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// ✅ Runs ONCE per request (before every API call)
// Checks: Authorization: Bearer <token>
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl UserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
// 1. Get Authorization header
      final String authHeader=request.getHeader("Authorization");

// 2. If no header or not Bearer token → skip (let other filters handle)
if(authHeader==null || !authHeader.startsWith("Bearer ")){
    filterChain.doFilter(request,response);
    return;
}

// 3. Extract JWT token (remove "Bearer " prefix)
final String jwt=authHeader.substring(7);
final  String userEmail;

try{
    userEmail=jwtUtil.extractUsername(jwt);
}catch (Exception ex){
// Invalid token → just continue (user stays unauthenticated)
filterChain.doFilter(request,response);
return;
}


// 4. If email found AND not yet authenticated
  if(userEmail!=null && SecurityContextHolder.getContext().getAuthentication()==null){

      UserDetails userDetails=
              UserDetailsService.loadUserByUsername(userEmail);

      // 5. Validate token

      if(jwtUtil.isTokenValid(jwt,userDetails)){
          // 6. Create authentication object
          var authToken=new UsernamePasswordAuthenticationToken(
                  userDetails,
                  null,
                  userDetails.getAuthorities()
          );

          authToken.setDetails(
                  new WebAuthenticationDetailsSource().buildDetails(request)
          );

          // 7. Set authentication in SecurityContext
          // After this, @AuthenticationPrincipal works in controllers!

          SecurityContextHolder.getContext()
                          .setAuthentication(authToken);
      }


  }

        // 8. Continue to next filter
        filterChain.doFilter(request, response);

    }
}
