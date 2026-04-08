package com.kdc.ohhcode.dtos.snippet;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.kdc.ohhcode.entities.enums.Difficulty;
import com.kdc.ohhcode.entities.enums.Language;
import com.kdc.ohhcode.entities.enums.SnippetStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record SnippetResponseDto(
        UUID id,
        String title,
        String url,
        String hash,
        UUID userId,
        String memoryNotes,
        Difficulty difficulty,
        Boolean important,
        SnippetStatus status,
        Language language,
        Set<String> tags,
        @JsonRawValue
        String analysis,
        Instant createdAt,
        Instant updatedAt,
        Instant lastAnalyzedAt
        ) {
}
