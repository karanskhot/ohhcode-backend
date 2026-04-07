package com.kdc.ohhcode.controllers.snippet;

import com.kdc.ohhcode.dtos.snippet.SnippetFilterDto;
import com.kdc.ohhcode.dtos.snippet.SnippetProgressTracker;
import com.kdc.ohhcode.dtos.snippet.SnippetRequestDto;
import com.kdc.ohhcode.dtos.snippet.SnippetResponseDto;
import com.kdc.ohhcode.services.snippet.SnippetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/snippets")
public class SnippetController {

  private final SnippetService snippetService;

  @PostMapping
  public ResponseEntity<SnippetResponseDto> createSnippet(
      @ModelAttribute @Valid SnippetRequestDto snippetRequestDto) {

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(snippetService.createSnippet(snippetRequestDto));
  }

  @PostMapping("/{id}/analyze")
  public ResponseEntity<SnippetProgressTracker> createSnippetAnalysis(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.CREATED).body(snippetService.startAnalysis(id));
  }



  @GetMapping("/{id}")
  public ResponseEntity<SnippetResponseDto> getSnippet(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.OK).body(snippetService.getSnippet(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSnippet(@PathVariable UUID id) {
    snippetService.deleteCodeSnippet(id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/analyze")
  public ResponseEntity<Void> deleteAnalysis(@PathVariable UUID id) {
    snippetService.deleteAnalysis(id);
    return ResponseEntity.noContent().build();
  }


  @GetMapping
  public ResponseEntity<Page<SnippetResponseDto>> getAllSnippets(
          @ModelAttribute SnippetFilterDto filter,
          @RequestParam(defaultValue = "createdAt") String sortBy,
          @RequestParam(defaultValue = "desc") String sortOrder,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "20") int pageSize) {

    List<String> allowedSortFields = List.of("createdAt", "lastAnalyzedAt");

    if (!allowedSortFields.contains(sortBy)) {
      sortBy = "createdAt";
    }

    Sort.Direction direction =
            sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

    Sort sort = Sort.by(direction, sortBy);

    int safePageSize = Math.min(pageSize, 10);

    Pageable pageable = PageRequest.of(page, safePageSize, sort);

    return ResponseEntity.ok(snippetService.getAllSnippetsSpec(filter, pageable));
  }
}
