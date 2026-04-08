package com.kdc.ohhcode.services.snippet;

import com.kdc.ohhcode.constants.AppConstants;
import com.kdc.ohhcode.dtos.snippet.AnalyzeRequest;
import com.kdc.ohhcode.entities.SnippetEntity;
import com.kdc.ohhcode.entities.enums.Language;
import com.kdc.ohhcode.entities.enums.SnippetStatus;
import com.kdc.ohhcode.repositories.SnippetRepository;
import com.kdc.ohhcode.util.AiSnippetAnalyzer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnippetAnalysisService {

  private static final int MAX_RETRIES = 2;
  private final AiSnippetAnalyzer aiSnippetAnalyzer;
  private final SnippetRepository snippetRepository;

  @Async
  @Transactional
  public void processAnalysisAsync(UUID snippetId, UUID userId, Language language) {

    log.info("AI processing started for snippet {}", snippetId);

    SnippetEntity snippet =
        snippetRepository
            .findById(snippetId)
            .orElseThrow(() -> new AccessDeniedException("Unauthorized"));

    if (!snippet.getUser().getId().equals(userId)) {
      throw new AccessDeniedException("Unauthorized");
    }

    try {
      processAnalysis(snippet, language);
      markSuccess(snippet);

      log.info("AI processing completed for snippet {}", snippet.getId());

    } catch (Exception e) {

      log.error("AI processing failed for snippet {}", snippetId, e);
      markFailure(snippet);
    }

    snippetRepository.save(snippet);
  }

  private String callWithRetry(SnippetEntity snippet, Language language) {
    int attempt = 0;
    while (attempt < MAX_RETRIES) {
      try {
        return callWithTimeout(snippet, language);
      } catch (Exception e) {
        attempt++;
        log.info("Attempt - {} :  AI processing failed for snippet {}", attempt, snippet.getId());
      }
    }
    throw new RuntimeException("AI processing failed for snippet " + snippet.getId());
  }

  private String callWithTimeout(SnippetEntity snippet, Language language) {
    return CompletableFuture.supplyAsync(() -> aiSnippetAnalyzer.aiSnippetAnalyzer(snippet, language))
        .orTimeout(60, TimeUnit.SECONDS)
        .completeOnTimeout(AppConstants.FALLBACK_RESPONSE, 60, TimeUnit.SECONDS)
        .exceptionally(
            ex -> {
              log.error("failed generating ai response for snippet {}", snippet.getId(), ex);
              return AppConstants.FALLBACK_RESPONSE;
            })
        .join();
  }

  private void processAnalysis(SnippetEntity snippet, Language language) {
    String rawResponse = callWithRetry(snippet, language);

    boolean isValid = aiSnippetAnalyzer.extractIsValid(rawResponse);

    Set<String> aiTags =
        aiSnippetAnalyzer.extractTags(rawResponse).stream()
            .filter(AppConstants.ALLOWED_TAGS::contains)
            .collect(Collectors.toSet());

    if (snippet.getTags().isEmpty()) {
      snippet.setTags(aiTags);
    }

    snippet.setAnalysis(rawResponse);
    snippet.setStatus(isValid ? SnippetStatus.ANALYZED : SnippetStatus.FAILED);
  }

  private void markSuccess(SnippetEntity snippet) {
    snippet.setLastAnalyzedAt(Instant.now());
  }

  private void markFailure(SnippetEntity snippet) {
    snippet.setStatus(SnippetStatus.FAILED);
    snippet.setAnalysis(AppConstants.FALLBACK_RESPONSE);
    snippet.setLastAnalyzedAt(Instant.now());
  }
}
