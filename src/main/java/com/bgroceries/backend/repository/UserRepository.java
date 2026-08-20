package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    Optional<User> findByTelegram(String telegram);

    boolean existsByTelegram(String telegram);

    Optional<User> findByFacebook(String facebook);

    boolean existsByFacebook(String facebook);

    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByFacebookId(String facebookId);

    Optional<User> findByTelegramId(String telegramId);

    Optional<User> findByTelegramUserId(Long telegramUserId);

    Optional<User> findByFullNameIgnoreCase(String fullName);
}
