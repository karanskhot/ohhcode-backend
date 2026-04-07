package com.kdc.ohhcode.mappers;

import com.kdc.ohhcode.dtos.snippet.SnippetResponseDto;
import com.kdc.ohhcode.entities.SnippetEntity;
import org.springframework.stereotype.Component;

@Component
public class SnippetMapper {

    public SnippetResponseDto toDto(SnippetEntity snippetEntity) {
        return new SnippetResponseDto(
                snippetEntity.getId(),
                snippetEntity.getTitle(),
                snippetEntity.getUrl(),
                snippetEntity.getHashCode(),
                snippetEntity.getUser()
                             .getId(),
                snippetEntity.getMemoryNotes(),
                snippetEntity.getDifficulty(),
                snippetEntity.getImportant(),
                snippetEntity.getStatus(),
                snippetEntity.getLanguage(),
                snippetEntity.getTags(),
                snippetEntity.getAnalysis(),
                snippetEntity.getCreatedAt(),
                snippetEntity.getUpdatedAt(),
                snippetEntity.getLastAnalyzedAt()
                );
    }
}
