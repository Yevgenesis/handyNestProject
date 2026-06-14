package com.handynest.auth.security;

import com.handynest.identity.User;
import com.handynest.identity.UserAccountState;
import com.handynest.identity.UserRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = findActiveUserByEmail(username);

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPassword(),
        user.getRoles().stream()
            .map(auth -> new SimpleGrantedAuthority(auth.name()))
            .collect(Collectors.toList()));
  }

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null && authentication.isAuthenticated()) {
      if (!(authentication.getPrincipal() instanceof UserDetails userDetails)) {
        return null;
      }

      return findActiveUserByEmail(userDetails.getUsername());
    }
    return null;
  }

  private User findActiveUserByEmail(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (!UserAccountState.isSessionAllowed(user)) {
      throw new UsernameNotFoundException("User not found");
    }
    return user;
  }
}
