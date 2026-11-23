package com.workzeno.workzeno.identity.user.repository;

import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.identity.user.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(UserRole role);


    List<User> findAllByEmailIn(Collection<String> emails);
}

