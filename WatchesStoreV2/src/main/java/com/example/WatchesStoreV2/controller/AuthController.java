package com.example.WatchesStoreV2.controller;

import com.example.WatchesStoreV2.base.ControllerBase;
import com.example.WatchesStoreV2.dto.auth.request.RegisterDto;
import com.example.WatchesStoreV2.entity.User;
import com.example.WatchesStoreV2.service.authService.AuthService;
import com.example.WatchesStoreV2.service.component.UserComponentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController extends ControllerBase {

    private final AuthService authService;
    private final UserComponentService userComponentService;

    @PostMapping("/sign-in")
    public ResponseEntity<?> authenticateUser(Authentication authentication,HttpServletResponse response) {
        return ResponseEntity.ok(authService.getJwtTokensAfterAuthentication(authentication,response));
    }

    @PreAuthorize("hasAuthority('SCOPE_REFRESH_TOKEN')")
    @PostMapping ("/refresh-token")
    public ResponseEntity<?> getAccessToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader){
        return ResponseEntity.ok(authService.getAccessTokenUsingRefreshToken(authorizationHeader));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto registerDto) {

        String checkUser = this.userComponentService.checkUser(registerDto);

        if (checkUser.equals("email"))
            return response("Email already registered", HttpStatus.BAD_REQUEST);
        else if (checkUser.equals("username"))
            return response("Duplicated username", HttpStatus.BAD_REQUEST);

        User user = this.authService.register(registerDto);

        if (user != null)
            return response(user, HttpStatus.OK);
        else
            return response("Error in processing", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        String forgotPassword = this.authService.forgotPassword(email);

        if (forgotPassword.equals("success"))
            return response("The password reset email has been sent.", HttpStatus.OK);
        else if (forgotPassword.equals("user not found"))
            return response("User not found", HttpStatus.NOT_FOUND);
        else
            return response("Error in forgotPassword", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
