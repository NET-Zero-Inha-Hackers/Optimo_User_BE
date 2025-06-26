package org.inhahackers.optmo_user_be.controller;

import org.inhahackers.optmo_user_be.dto.ElecRequest;
import org.inhahackers.optmo_user_be.dto.OAuthUserInfo;
import org.inhahackers.optmo_user_be.dto.UserResponse;
import org.inhahackers.optmo_user_be.entity.AuthProvider;
import org.inhahackers.optmo_user_be.entity.User;
import org.inhahackers.optmo_user_be.exception.JwtAuthenticationException;
import org.inhahackers.optmo_user_be.exception.UserNotFoundException;
import org.inhahackers.optmo_user_be.service.JwtTokenService;
import org.inhahackers.optmo_user_be.service.OAuthTokenService;
import org.inhahackers.optmo_user_be.service.UserService;
import org.inhahackers.optmo_user_be.util.AuthorizationHeaderUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final OAuthTokenService oAuthTokenService;

    public UserController(UserService userService, JwtTokenService jwtTokenService, OAuthTokenService oAuthTokenService) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
        this.oAuthTokenService = oAuthTokenService;
    }

    @GetMapping("/api/user")
    public ResponseEntity<?> getUser(@RequestParam(name = "email") String email) {
        try {
            // 유효성 검사
            if (email == null || email.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Email parameter is required");
            }

            // 사용자 조회 또는 생성
            User user = userService.findOrCreateUserByEmail(email);

            // JWT 토큰 발급
            String newToken = jwtTokenService.generateToken(
                    user.getId(), user.getEmail(), user.getRole().name()
            );

            // 응답 DTO 생성
            UserResponse response = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .provider(user.getProvider())
                    .totalUseElecEstimate(user.getTotalUseElecEstimate())
                    .totalLlmElecEstimate(user.getTotalLlmElecEstimate())
                    .build();

            // 헤더에 JWT 포함 응답
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + newToken);
            headers.set("Content-Type", "application/json");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(response);

        } catch (JwtAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage());
        }
    }

    @PostMapping("/api/user/oauth")
    public ResponseEntity<?> getUserByOauthToken(
            @RequestHeader(name = "Authorization") String authHeader,
            @RequestParam(name = "provider") String providerStr) {
        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);

            OAuthUserInfo userInfo = oAuthTokenService.verifyAndGetUserInfo(token, AuthProvider.valueOf(providerStr));
            User user = userService.findOrCreateUser(userInfo.toUserOAuthRequest());

            String jwtToken = jwtTokenService.generateToken(
                    user.getId(), user.getEmail(), user.getRole().name());

            UserResponse response = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .provider(user.getProvider())
                    .totalLlmElecEstimate(user.getTotalLlmElecEstimate())
                    .totalUseElecEstimate(user.getTotalUseElecEstimate())
                    .build();

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwtToken)
                    .body(response);

        } catch (JwtAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage());

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User Not Found: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage());
        }
    }

    @GetMapping("/api/user/jwt")
    public ResponseEntity<?> jwtToken(@RequestHeader(name = "Authorization") String authHeader) {

        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);

            Long userId = jwtTokenService.extractUserId(token);
            String email = jwtTokenService.extractEmail(token);

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException(email));

            UserResponse response = UserResponse.builder()
                    .id(userId)
                    .email(email)
                    .name(user.getName())
                    .profileImage(user.getProfileImage())
                    .provider(user.getProvider())
                    .totalUseElecEstimate(user.getTotalUseElecEstimate())
                    .totalLlmElecEstimate(user.getTotalLlmElecEstimate())
                    .build();

            return ResponseEntity.ok()
                    .body(response);

        } catch (JwtAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage());

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User Not Found: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage());
        }
    }

    @PatchMapping("/api/user/electricity")
    public ResponseEntity<?> electricity(
            @RequestHeader(name = "Authorization") String authHeader,
            @RequestBody ElecRequest elecRequest) {
        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);

            // 3. 토큰에서 userId 추출 및 처리
            Long userId = jwtTokenService.extractUserId(token);

            userService.increaseElecEstimate(userId, elecRequest);

            // 4. 성공 응답
            return ResponseEntity.ok()
                    .header("Content-Type", "text/plain")
                    .body("Successfully Increased Elec and Cost Estimate");

        }  catch (JwtAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage());

        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User Not Found: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage());
        }
    }
}
