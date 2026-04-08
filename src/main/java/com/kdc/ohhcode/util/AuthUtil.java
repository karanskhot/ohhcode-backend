package com.kdc.ohhcode.util;

import com.kdc.ohhcode.dtos.auth.TokenResponseDto;
import com.kdc.ohhcode.entities.SnippetEntity;
import com.kdc.ohhcode.entities.UserEntity;
import com.kdc.ohhcode.entities.enums.Role;
import com.kdc.ohhcode.repositories.SnippetRepository;
import com.kdc.ohhcode.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthUtil {

  private final UserRepository userRepository;
  private final SnippetRepository snippetRepository;

  @Value("${jwt.secret.key}")
  private String JWT_SECRET;

  @Value("${jwt.expiry.ms}")
  private long JWT_EXPIRY_MS;

  public TokenResponseDto generateToken(String username, String role) {

    Date issuedAt = new Date();
    Date expiration = new Date(issuedAt.getTime() + JWT_EXPIRY_MS);
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);

    String token =
        Jwts.builder()
            .subject(username)
            .claims(claims)
            .issuedAt(issuedAt)
            .expiration(expiration)
            .signWith(generateSecretKey())
            .compact();

    return new TokenResponseDto(token, null, Role.valueOf(role), issuedAt, expiration);
  }

  public Claims verifySignatureAndGetClaims(String token) {
    return Jwts.parser()
        .verifyWith(generateSecretKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public SecretKey generateSecretKey() {
    return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
  }

  public ResponseCookie createHttpOnlyResponseCookie(String token) {
    return ResponseCookie.from("token", token)
        .httpOnly(true)
        .secure(false)
        .domain("localhost")
        .path("/")
        .maxAge(JWT_EXPIRY_MS / 1000)
        .sameSite("Lax")
        .build();
  }

  public UserEntity getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated())
      throw new AccessDeniedException("User is not authenticated");
    return userRepository
        .findByUsername(authentication.getName())
        .orElseThrow(() -> new AccessDeniedException("User is not authenticated"));
  }

  public SnippetEntity validateSnippetOwnership(UUID snippetId, UUID userId) {
    return snippetRepository
        .findByIdAndUserId(snippetId, userId)
        .orElseThrow(() -> new AccessDeniedException("User not authorized to view snippet."));
  }
}
