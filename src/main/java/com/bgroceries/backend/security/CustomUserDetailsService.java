package com.bgroceries.backend.security;

import com.bgroceries.backend.entity.User;
import com.bgroceries.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // identifier is the JWT subject: a phone number, or a username for social accounts.
        User user = userRepository.findByPhoneNumber(identifier)
                .or(() -> userRepository.findByUsernameIgnoreCase(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + identifier));

        String role = user.getRole() != null ? user.getRole() : "USER";

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getPhoneNumber() != null ? user.getPhoneNumber() : user.getUsername())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + role)))
                .disabled(!Boolean.TRUE.equals(user.getEnabled()))
                .build();
    }
}
