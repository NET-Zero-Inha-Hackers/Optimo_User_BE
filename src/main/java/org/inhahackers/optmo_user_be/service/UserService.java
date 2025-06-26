package org.inhahackers.optmo_user_be.service;

import lombok.RequiredArgsConstructor;
import org.inhahackers.optmo_user_be.dto.ElecAndCostRequest;
import org.inhahackers.optmo_user_be.dto.UserOAuthRequest;
import org.inhahackers.optmo_user_be.entity.Role;
import org.inhahackers.optmo_user_be.entity.User;
import org.inhahackers.optmo_user_be.exception.UserNotFoundException;
import org.inhahackers.optmo_user_be.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

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
                            .totalUseCostEstimate(0L)
                            .totalUseCostEstimate(0L)
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

    public void increaseElecAndCostEstimate(Long userId, ElecAndCostRequest elecAndCostRequest) {
        User user;
        try {
            user = userRepository.findById(userId).orElse(null);
        } catch (NoSuchElementException e) {
            throw new UserNotFoundException("userId: " + userId);
        }

        user.setTotalUseElecEstimate(user.getTotalUseElecEstimate() + elecAndCostRequest.getUseElecEstimate());
        user.setTotalLlmElecEstimate(user.getTotalLlmElecEstimate() + elecAndCostRequest.getLlmElecEstimate());
        user.setTotalUseCostEstimate(user.getTotalUseCostEstimate() + elecAndCostRequest.getUseCostEstimate());
        user.setTotalLlmCostEstimate(user.getTotalLlmCostEstimate() + elecAndCostRequest.getLlmCostEstimate());
        userRepository.save(user);
    }
}
