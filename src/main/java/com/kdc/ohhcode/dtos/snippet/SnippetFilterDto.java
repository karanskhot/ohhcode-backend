package com.kdc.ohhcode.dtos.snippet;

import com.kdc.ohhcode.entities.enums.Difficulty;
import com.kdc.ohhcode.entities.enums.SnippetStatus;

import java.time.LocalDate;
import java.util.Set;

public record SnippetFilterDto(
        String search,
        Set<String> tags,
        SnippetStatus status,
        Difficulty difficulty,
        LocalDate startDate,
        LocalDate endDate
){}
