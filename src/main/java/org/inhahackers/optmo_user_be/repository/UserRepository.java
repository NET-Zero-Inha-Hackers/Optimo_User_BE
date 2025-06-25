package org.inhahackers.optmo_user_be.repository;

import org.inhahackers.optmo_user_be.entity.AuthProvider;
import org.inhahackers.optmo_user_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
    Optional<User> findByEmail(String email);
}
