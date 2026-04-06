package com.kdc.ohhcode.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdc.ohhcode.constants.AppConstants;
import com.kdc.ohhcode.entities.SnippetEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
//import org.springframework.ai.openaisdk.OpenAiSdkChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSnippetAnalyzer {

  private final ChatClient chatClient;
  ObjectMapper mapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Value("classpath:prompt/snippet-analyzer-prompt.st")
  private Resource promptResource;

  public String aiSnippetAnalyzer(SnippetEntity snippetEntity) {
    String language = snippetEntity.getLanguage().name();
    String difficulty = snippetEntity.getDifficulty().name();
    String url = snippetEntity.getUrl();

    String prompt = createPrompt(language, difficulty);

    Media snippet = new Media(MimeTypeUtils.IMAGE_PNG, URI.create(url));
    return chatClient
        .prompt()
        .system(prompt)
        .user(
            u ->
                u.text(
                        """
                STRICT:
                - Return ONLY valid JSON
                - No markdown / no explanation

                If NO DSA problem or code found:
                { "meta": { "title": "Invalid Input" } }
                """)
                    .media(snippet))
        .call()
        .content();
  }

  private String createPrompt(String language, String difficulty) {

    log.info("Creating prompt... language={}, difficulty={}", language, difficulty);

    try {
      String template =
          new String(promptResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

      String safeLanguage = language != null ? language : "ENGLISH";
      String safeDifficulty = difficulty != null ? difficulty : "MEDIUM";
      String tagsForPrompt =
          AppConstants.ALLOWED_TAGS.stream()
              .map(tag -> " - " + tag)
              .collect(Collectors.joining("\n"));

      return template
          .replace("{{language}}", safeLanguage)
          .replace("{{difficulty}}", safeDifficulty)
          .replace("{{tags}}", tagsForPrompt);
    } catch (IOException e) {
      throw new RuntimeException("Failed to build prompt", e);
    }
  }

  public boolean extractIsValid(String rawResponse) {
    try {
      JsonNode rootNode = mapper.readTree(rawResponse);
      JsonNode isValidNode = rootNode.get("isValid");
      return isValidNode != null && isValidNode.asBoolean();
    } catch (Exception e) {
      return false;
    }
  }

  public Set<String> extractTags(String rawResponse) {
    try {
      JsonNode root = mapper.readTree(rawResponse);
      JsonNode tagsNode = root.path("meta").path("tags");

      Set<String> tags = new HashSet<>();
      if (tagsNode.isArray()) {
        for (JsonNode tag : tagsNode) {
          tags.add(tag.asText());
        }
      }
      return tags;
    } catch (Exception e) {
      return Collections.emptySet();
    }
  }
}
