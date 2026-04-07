package com.kdc.ohhcode.security.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.kdc.ohhcode.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@Component
@RequiredArgsConstructor
public class AppConfig {

  private static final long MAX_SIZE = 2 * 1024 * 1024; // 2 MB

  @Value("${cloudinary.cloud_name}")
  private String cloudName;

  @Value("${cloudinary.api_key}")
  private String cloudinaryApiKey;

  @Value("${cloudinary.api_secret}")
  private String cloudinaryApiSecret;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
    return configuration.getAuthenticationManager();
  }

  public String generateHash(MultipartFile file) {

    try {
      byte[] bytes = file.getBytes();

      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(bytes);

      StringBuilder hex = new StringBuilder();
      for (byte b : hash) {
        hex.append(String.format("%02x", b));
      }

      return hex.toString();

    } catch (Exception e) {
      throw new RuntimeException("Hash generation failed", e);
    }
  }

  public Cloudinary cloudinary() {
    Map<String, Object> config = new HashMap<>();
    config.put("cloud_name", cloudName);
    config.put("api_key", cloudinaryApiKey);
    config.put("api_secret", cloudinaryApiSecret);
    return new Cloudinary(config);
  }

  public String uploadImage(MultipartFile file) {
    if (file.getSize() > MAX_SIZE) {
      throw new IllegalArgumentException("File size cannot be greater than " + MAX_SIZE);
    }

    String contentType = file.getContentType();
    if (contentType == null || !AppConstants.ALLOWED_TYPES.contains(contentType)) {
      throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, GIF, WEBP allowed");
    }
    try {
      Map uploadUrl =
          cloudinary().uploader().upload(file.getBytes(), ObjectUtils.asMap("format", "png"));

      return uploadUrl.get("secure_url").toString();

    } catch (IOException e) {
      throw new RuntimeException("Image upload failed" + e.getMessage());
    }
  }

  @Bean
  public ChatClient chatClient(ChatClient.Builder clientBuilder) {
    return clientBuilder.build();
  }
}
