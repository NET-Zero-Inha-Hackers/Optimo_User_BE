package org.inhahackers.optmo_user_be.service;

import lombok.RequiredArgsConstructor;
import org.inhahackers.optmo_user_be.dto.ElecRequest;
import org.inhahackers.optmo_user_be.dto.UserOAuthRequest;
import org.inhahackers.optmo_user_be.entity.AuthProvider;
import org.inhahackers.optmo_user_be.entity.Role;
import org.inhahackers.optmo_user_be.entity.User;
import org.inhahackers.optmo_user_be.exception.UserNotFoundException;
import org.inhahackers.optmo_user_be.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findOrCreateUser(UserOAuthRequest request) {
        return userRepository.findByProviderAndProviderId(request.getProvider(), request.getProviderId())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(request.getEmail())
                            .name(request.getName())
                            .profileImage(request.getProfileImage())
                            .provider(request.getProvider())
                            .providerId(request.getProviderId())
                            .role(Role.ROLE_USER)
                            .totalUseElecEstimate(0L)
                            .totalLlmElecEstimate(0L)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    public void increaseElecEstimate(Long userId, ElecRequest elecRequest) {
        User user;
        try {
            user = userRepository.findById(userId).orElse(null);
        } catch (NoSuchElementException e) {
            throw new UserNotFoundException("userId: " + userId);
        }

        user.setTotalUseElecEstimate(user.getTotalUseElecEstimate() + elecRequest.getUseElecEstimate());
        user.setTotalLlmElecEstimate(user.getTotalLlmElecEstimate() + elecRequest.getLlmElecEstimate());
        userRepository.save(user);
    }

    public User findOrCreateUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(null)
                            .profileImage(null)
                            .provider(AuthProvider.EMAIL)
                            .providerId(UUID.randomUUID().toString())
                            .role(Role.ROLE_USER)
                            .totalUseElecEstimate(0L)
                            .totalLlmElecEstimate(0L)
                            .build();
                    return userRepository.save(newUser);
                });
    }
}
