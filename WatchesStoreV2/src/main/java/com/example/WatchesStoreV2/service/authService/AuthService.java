package com.example.WatchesStoreV2.service.authService;

import com.example.WatchesStoreV2.dto.auth.request.RegisterDto;
import com.example.WatchesStoreV2.dto.auth.request.ResetPassword;
import com.example.WatchesStoreV2.dto.auth.response.AuthResponseDto;
import com.example.WatchesStoreV2.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    AuthResponseDto getJwtTokensAfterAuthentication(Authentication authentication, HttpServletResponse response);
    Object getAccessTokenUsingRefreshToken(String authorizationHeader);
    User register(RegisterDto registerDto);
    void cleanRefreshTokenRevoked();
    String forgotPassword(String email);
    User resetPassword(ResetPassword resetPassword);
}
