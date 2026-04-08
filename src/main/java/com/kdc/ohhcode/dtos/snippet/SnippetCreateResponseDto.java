package com.kdc.ohhcode.dtos.snippet;

import java.util.UUID;

public record SnippetCreateResponseDto(
        String message,
        UUID snippetId
) {}
