package com.kdc.ohhcode.services.auth;

import com.kdc.ohhcode.dtos.auth.*;
import com.kdc.ohhcode.entities.UserEntity;
import com.kdc.ohhcode.entities.enums.Role;
import com.kdc.ohhcode.repositories.UserRepository;

import com.kdc.ohhcode.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final AuthUtil authUtil;

  public RegisterResponseDto register(RegisterRequestDto registerRequestDto) {

    if (userRepository.existsByUsername(registerRequestDto.username())) {
      throw new DataIntegrityViolationException("user already exists.");
    }
    Role role = registerRequestDto.role() != null ? registerRequestDto.role() : Role.ROLE_USER;

    UserEntity user =
        UserEntity.builder()
            .firstName(registerRequestDto.firstName())
            .lastName(registerRequestDto.lastName())
            .username(registerRequestDto.username())
            .password(passwordEncoder.encode(registerRequestDto.password()))
            .role(role)
            .enabled(true)
            .build();

    userRepository.save(user);

    return new RegisterResponseDto("Registration successful", user.getUsername());
  }

  public LoginResponseDto login(LoginRequestDto loginRequestDto) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequestDto.username(), loginRequestDto.password()));

    UserEntity user =
        userRepository
            .findByUsername(loginRequestDto.username())
            .orElseThrow(() -> new BadCredentialsException(null) {});

    String role = user.getRole().name();

    TokenResponseDto tokenResponseDto = authUtil.generateToken(user.getUsername(), role);

    return new LoginResponseDto(user.getUsername(), tokenResponseDto);
  }

  public CurrenUserDto getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated())
      throw new AccessDeniedException("User is not authenticated");
    UserEntity user =
        userRepository
            .findByUsername(authentication.getName())
            .orElseThrow(() -> new AccessDeniedException("User is not authenticated"));
    return new CurrenUserDto(
        user.getFirstName(), user.getLastName(), user.getRole(), user.getUsername());
  }
}
