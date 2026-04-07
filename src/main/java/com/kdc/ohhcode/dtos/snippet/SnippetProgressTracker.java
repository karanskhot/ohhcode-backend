package com.kdc.ohhcode.dtos.snippet;

import com.kdc.ohhcode.entities.enums.SnippetStatus;

import java.util.UUID;

public record SnippetProgressTracker(
        UUID snippetId,
        SnippetStatus status,
        String message
) {
}
