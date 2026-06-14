package com.handynest.auth.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  private final UserDetailsService userService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String jwt = header.substring("Bearer ".length());
    String username;
    try {
      username = jwtService.extractUserName(jwt);
    } catch (JwtException | IllegalArgumentException exception) {
      SecurityContextHolder.clearContext();
      filterChain.doFilter(request, response);
      return;
    }

    if (username != null
        && !username.isBlank()
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      // get user from database
      UserDetails user;
      try {
        user = userService.loadUserByUsername(username);
      } catch (AuthenticationException exception) {
        SecurityContextHolder.clearContext();
        filterChain.doFilter(request, response);
        return;
      }
      // check that token is valid
      if (isTokenValid(jwt, user)) {
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      }
    }

    filterChain.doFilter(request, response);
  }

  private boolean isTokenValid(String jwt, UserDetails user) {
    try {
      return jwtService.isTokenValid(jwt, user);
    } catch (JwtException | IllegalArgumentException exception) {
      SecurityContextHolder.clearContext();
      return false;
    }
  }
}
