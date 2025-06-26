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

import java.util.*;

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
                    // 랜덤 이름 리스트 (0~19 인덱스 사용)
                    List<String> nameList = Arrays.asList(
                            "느긋한고양이", "신중한여우", "무심한늑대", "예민한수달", "조용한하마",
                            "낯가리는펭귄", "단단한거북이", "엉뚱한판다", "의심많은토끼", "낙천적인호랑이",
                            "서툰다람쥐", "고집센고릴라", "쓸쓸한두더지", "멍때리는코끼리", "침착한까마귀",
                            "뚝심있는바다표범", "현실적인부엉이", "겸손한늑대", "정리왕두루미", "직진하는개미핥기"
                    );

                    // 랜덤 인덱스 생성 (0~19)
                    Random rand = new Random();
                    int randomIndex = rand.nextInt(20);

                    User newUser = User.builder()
                            .email(email)
                            .name(nameList.get(randomIndex)) // 랜덤 이름 설정
                            .profileImage(String.valueOf(randomIndex)) // 인덱스를 문자열로 저장
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
