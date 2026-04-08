package com.kdc.ohhcode.services.snippet;

import com.kdc.ohhcode.constants.AppConstants;
import com.kdc.ohhcode.dtos.snippet.*;
import com.kdc.ohhcode.entities.SnippetEntity;
import com.kdc.ohhcode.entities.UserEntity;
import com.kdc.ohhcode.entities.enums.Difficulty;
import com.kdc.ohhcode.entities.enums.Language;
import com.kdc.ohhcode.entities.enums.SnippetStatus;
import com.kdc.ohhcode.mappers.SnippetMapper;
import com.kdc.ohhcode.repositories.SnippetRepository;
import com.kdc.ohhcode.security.config.AppConfig;
import com.kdc.ohhcode.util.AuthUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnippetService {

  private final SnippetRepository snippetRepository;
  private final AppConfig appConfig;
  private final AuthUtil authUtil;
  private final SnippetMapper snippetMapper;
  private final SnippetAnalysisService snippetAnalysisService;

  @Transactional
  public SnippetCreateResponseDto createSnippet(SnippetRequestDto snippetRequestDto) {

    UserEntity user = authUtil.getCurrentUser();

    if (snippetRequestDto.snippetImage().isEmpty()) {
      throw new IllegalArgumentException("Code snippet image is required");
    }

    String hash = appConfig.generateHash(snippetRequestDto.snippetImage());

    Optional<SnippetEntity> snippet = snippetRepository.findByUserIdAndHashCode(user.getId(), hash);

    if (snippet.isPresent()) {
      return new SnippetCreateResponseDto(
          "You already have this snippet added.", snippet.get().getId());
    }

    Optional<SnippetEntity> globalSnippet = snippetRepository.findFirstByHashCode(hash);
    String url;
    if (globalSnippet.isPresent()) {
      url = globalSnippet.get().getUrl();
    } else {
      url = appConfig.uploadImage(snippetRequestDto.snippetImage());
    }

    SnippetEntity snippetEntity =
        SnippetEntity.builder()
            .title(snippetRequestDto.title())
            .url(url)
            .hashCode(hash)
            .status(SnippetStatus.UPLOADED)
            .tags(Collections.emptySet())
            .user(user)
            .analysis(null)
            .lastAnalyzedAt(null)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

    snippetRepository.save(snippetEntity);
    return new SnippetCreateResponseDto("Snippet added successfully.", snippetEntity.getId());
  }

  public SnippetResponseDto getSnippet(UUID id) {
    UserEntity user = authUtil.getCurrentUser();
    SnippetEntity snippet = authUtil.validateSnippetOwnership(id, user.getId());
    return snippetMapper.toDto(snippet);
  }

  @Transactional
  public void deleteCodeSnippet(UUID id) {
    UserEntity user = authUtil.getCurrentUser();
    SnippetEntity snippet = authUtil.validateSnippetOwnership(id, user.getId());
    snippetRepository.delete(snippet);
  }

  @Transactional
  public SnippetProgressTracker startAnalysis(UUID snippetId, AnalyzeRequest analyzeRequest) {
    UserEntity user = authUtil.getCurrentUser();

    Language language =
        (analyzeRequest == null || analyzeRequest.language() == null)
            ? Language.ENGLISH
            : analyzeRequest.language();
    // 1: fetch snippet by snippetId and userId -- throw authentication error if not exist
    SnippetEntity snippet = authUtil.validateSnippetOwnership(snippetId, user.getId());

    if (snippet.getLastAnalyzedAt() != null
        && snippet.getLastAnalyzedAt().isAfter(Instant.now().minusSeconds(30))) {
      throw new IllegalStateException("Please wait before generating analysis for snippet");
    }

    int updated = snippetRepository.markAsAnalyzing(snippetId);
    if (updated == 0) {
      return new SnippetProgressTracker(
          snippetId, SnippetStatus.ANALYZING, AppConstants.getMessage(SnippetStatus.ANALYZING));
    }

    snippetAnalysisService.processAnalysisAsync(snippetId, user.getId(), language);
    return new SnippetProgressTracker(
        snippet.getId(), SnippetStatus.ANALYZING, AppConstants.getMessage(SnippetStatus.ANALYZING));
  }

  public void deleteAnalysis(UUID id) {
    UserEntity user = authUtil.getCurrentUser();
    SnippetEntity snippet = authUtil.validateSnippetOwnership(id, user.getId());

    if (snippet.getStatus() == SnippetStatus.ANALYZING) {
      throw new IllegalStateException("Analysis is in progress. Cannot delete now.");
    }
    snippet.setAnalysis(null);
    snippet.setStatus(SnippetStatus.UPLOADED);
    snippetRepository.save(snippet);
  }

  public Page<SnippetResponseDto> getAllSnippetsSpec(SnippetFilterDto filter, Pageable pageable) {
    UserEntity user = authUtil.getCurrentUser();
    log.info("CURRENT USER ID : {} " ,  user.getId());
    Specification<SnippetEntity> spec =
        Specification.where(SnippetSpecification.hasUser(user.getId()))
            .and(SnippetSpecification.hasStatus(filter.status()))
            .and(SnippetSpecification.hasDifficulty(filter.difficulty()))
            .and(SnippetSpecification.hasTags(filter.tags()))
            .and(SnippetSpecification.hasCreatedBetween(filter.startDate(), filter.endDate()))
            .and(SnippetSpecification.hasSearch(filter.search()));

    Page<SnippetEntity> snippetsPage = snippetRepository.findAll(spec, pageable);
    return snippetsPage.map(snippetMapper::toDto);
  }
}
