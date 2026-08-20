package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.LoginSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginSessionRepository extends JpaRepository<LoginSession, Long> {

    Optional<LoginSession> findByToken(String token);
}
