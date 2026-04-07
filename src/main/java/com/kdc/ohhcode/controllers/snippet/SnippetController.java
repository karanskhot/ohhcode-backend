package com.kdc.ohhcode.controllers.snippet;

import com.kdc.ohhcode.dtos.snippet.SnippetProgressTracker;
import com.kdc.ohhcode.dtos.snippet.SnippetRequestDto;
import com.kdc.ohhcode.dtos.snippet.SnippetResponseDto;
import com.kdc.ohhcode.services.snippet.SnippetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

  @GetMapping
  public ResponseEntity<List<SnippetResponseDto>> getAllSnippets() {
    return ResponseEntity.status(HttpStatus.OK).body(snippetService.getAllCodeSnippets());
  }

  @GetMapping("/{id}")
  public ResponseEntity<SnippetResponseDto> getSnippet(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.OK).body(snippetService.getCodeSnippetById(id));
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
}
