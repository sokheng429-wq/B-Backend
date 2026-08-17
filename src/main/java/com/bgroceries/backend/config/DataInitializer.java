package com.bgroceries.backend.config;

import com.bgroceries.backend.entity.User;
import com.bgroceries.backend.repository.UserRepository;
import com.bgroceries.backend.util.PhoneUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default admin account on first startup so there is always a way in.
 * Login with username "admin" / password "admin123".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .fullName("Admin")
                    .email("admin@bgroceries.com")
                    .phoneNumber(PhoneUtil.normalize("010000000"))
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Seeded default admin -> username: admin, password: admin123 (CHANGE IT!)");
        }
    }
}
