package com.kdc.ohhcode.controllers.auth;

import com.kdc.ohhcode.dtos.auth.*;
import com.kdc.ohhcode.services.auth.AuthService;
import com.kdc.ohhcode.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final AuthUtil authUtil;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponseDto> register(
      @Valid @RequestBody RegisterRequestDto registerRequestDto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequestDto));
  }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
        @Valid @RequestBody LoginRequestDto loginRequestDto) {
      LoginResponseDto loginResponseDto = authService.login(loginRequestDto);
      ResponseCookie cookie =
          authUtil.createHttpOnlyResponseCookie(loginResponseDto.authData().token());

      return ResponseEntity.status(HttpStatus.OK)
          .header(HttpHeaders.SET_COOKIE, cookie.toString())
          .body(loginResponseDto);
    }

  @GetMapping("/me")
  public ResponseEntity<CurrentUserDto> getCurrentUser() {
    return ResponseEntity.status(HttpStatus.OK).body(authService.getCurrentUser());
  }
}
