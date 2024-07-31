package com.example.WatchesStoreV2.service.authService.impl;

import com.example.WatchesStoreV2.config.jwtConfig.JwtTokenGenerator;
import com.example.WatchesStoreV2.dto.auth.request.RegisterDto;
import com.example.WatchesStoreV2.dto.auth.request.ResetPassword;
import com.example.WatchesStoreV2.dto.auth.response.AuthResponseDto;
import com.example.WatchesStoreV2.helper.ResetTokenGenerator;
import com.example.WatchesStoreV2.helper.TokenType;
import com.example.WatchesStoreV2.entity.RefreshTokenEntity;
import com.example.WatchesStoreV2.entity.User;
import com.example.WatchesStoreV2.mapper.UserMapper;
import com.example.WatchesStoreV2.repository.RefreshTokenRepository;
import com.example.WatchesStoreV2.repository.UserRepository;
import com.example.WatchesStoreV2.service.authService.AuthService;
import com.example.WatchesStoreV2.service.component.MailService;
import com.example.WatchesStoreV2.service.component.UserComponentService;
import com.mongodb.MongoException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserComponentService userComponentService;
    private final MailService mailService;

    @Override
    public AuthResponseDto getJwtTokensAfterAuthentication(Authentication authentication, HttpServletResponse response) {
        try {
            var user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> {
                        log.error("[AuthService:userSignInAuth] User :{} not found", authentication.getName());
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "USER NOT FOUND ");
                    });

            String accessToken = jwtTokenGenerator.generateAccessToken(authentication);
            String refreshToken = jwtTokenGenerator.generateRefreshToken(authentication);

            saveUserRefreshToken(user, refreshToken);

            log.info("[AuthService:userSignInAuth] Access token for user:{}, has been generated", user.getUsername());
            return AuthResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .accessTokenExpiry(15 * 60)
                    .userName(user.getUsername())
                    .tokenType(TokenType.Bearer)
                    .build();


        } catch (Exception e) {
            log.error("[AuthService:userSignInAuth]Exception while authenticating the user due to :" + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Please Try Again");
        }
    }

    @Override
    public Object getAccessTokenUsingRefreshToken(String authorizationHeader) {
        if (!authorizationHeader.startsWith(TokenType.Bearer.name())) {
            return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Please verify your token type");
        }

        final String refreshToken = authorizationHeader.substring(7);

        //Find refreshToken from database and should not be revoked : Same thing can be done through filter.
        var refreshTokenEntity = refreshTokenRepository.findByRefreshToken(refreshToken)
                .filter(tokens -> !tokens.isRevoked())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Refresh token revoked"));

        User user = refreshTokenEntity.getUser();

        //Now create the Authentication object
        Authentication authentication = createAuthenticationObject(user);

        //Use the authentication object to generate new accessToken as the Authentication object that we will have may not contain correct role.
        String accessToken = jwtTokenGenerator.generateAccessToken(authentication);

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .accessTokenExpiry(5 * 60)
                .userName(user.getUsername())
                .tokenType(TokenType.Bearer)
                .build();
    }

    @Override
    public User register(RegisterDto registerDto) {
        try {
            User user = UserMapper.registerUser(registerDto);
            user.setPassword(this.passwordEncoder.encode(user.getPassword()));

            this.userComponentService.saveUser(user);
            this.mailService.welcome(registerDto.getEmail(), user.getUsername());

            return user;
        } catch (MongoException e) {
            return null;
        }
    }

    @Override
    public void cleanRefreshTokenRevoked() {
        log.info("[AuthService:cleanRefreshTokenRevoked] Clean refresh token revoked started");
        try {
            List<RefreshTokenEntity> refreshTokens = refreshTokenRepository.findAllByRevoked(true);
            if (!refreshTokens.isEmpty()) {
                refreshTokenRepository.deleteAll(refreshTokens);
                log.info("[AuthService:cleanRefreshTokenRevoked] Clean refresh token revoked successful");
            }
        } catch (MongoException e) {
            e.printStackTrace();
            throw new MongoException("Can't clean refresh token revoked");
        }
    }

    @Override
    public String forgotPassword(String email) {
        try {
            Optional<User> user = this.userRepository.findByEmail(email);
            String resetToken = ResetTokenGenerator.generateRandomString();
            if (user.isPresent()) {
                this.mailService.sendResetToken(email, resetToken, user.get().getUsername());
                String tokenEncode = this.passwordEncoder.encode(resetToken);

                user.get().setToken(tokenEncode);

                this.userRepository.save(user.get());
                return "success";
            }
            else
                return "user not found";
        } catch (MongoException e) {
            e.printStackTrace();
            throw new MongoException("Can't generate token reset password");
        }
    }

    @Override
    public User resetPassword(ResetPassword resetPassword) {
        if (resetPassword.getNewPassword().isEmpty())
            return null;

        try {
            String token = this.passwordEncoder.encode(resetPassword.getToken());
            Optional<User> user = this.userRepository.findByToken(token);

            if (user.isPresent() && !user.get().getToken().isEmpty()) {

                user.get().setPassword(this.passwordEncoder.encode(resetPassword.getNewPassword()));
                user.get().setToken(null);
                this.userRepository.save(user.get());

                return user.get();
            }
            else
                return null;

        } catch (MongoException e) {
            e.printStackTrace();
            throw new MongoException("Can't reset password");
        }
    }

    private void saveUserRefreshToken(User user, String refreshToken) {
        var refreshTokenEntity = RefreshTokenEntity.builder()
                .user(user)
                .refreshToken(refreshToken)
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);
    }

    private static Authentication createAuthenticationObject(User userInfoEntity) {
        // Extract user details from UserDetailsEntity
        String username = userInfoEntity.getEmail();
        String password = userInfoEntity.getPassword();
        String roles = userInfoEntity.getRoles();

        // Extract authorities from roles (comma-separated)
        String[] roleArray = roles.split(",");
        GrantedAuthority[] authorities = Arrays.stream(roleArray)
                .map(role -> (GrantedAuthority) role::trim)
                .toArray(GrantedAuthority[]::new);

        return new UsernamePasswordAuthenticationToken(username, password, Arrays.asList(authorities));
    }
}
