package com.kdc.ohhcode.dtos.snippet;

import com.kdc.ohhcode.entities.enums.Difficulty;

import java.util.Set;

public record SnippetUpdateRequestDto(
        String title,
        Difficulty difficulty,
        Set<String> tags,
        Boolean important,
        String memoryNotes
) {}
