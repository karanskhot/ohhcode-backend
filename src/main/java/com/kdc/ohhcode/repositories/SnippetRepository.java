package com.kdc.ohhcode.repositories;

import com.kdc.ohhcode.entities.SnippetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SnippetRepository extends JpaRepository<SnippetEntity, UUID>, JpaSpecificationExecutor<SnippetEntity> {

  boolean existsByHashCode(String hashCode);

  Optional<SnippetEntity> findByUserIdAndHashCode(UUID id, String hashCode);

  Optional<SnippetEntity> findByIdAndUserId(UUID id, UUID userId);

  Optional<SnippetEntity> findFirstByHashCode(String hashCode);

  List<SnippetEntity> findAllByUserId(UUID userId);

  @Modifying
  @Query(
"""
UPDATE SnippetEntity s
SET s.status = 'ANALYZING', s.analysis = null
WHERE s.id = :id AND s.status != 'ANALYZING'
""")
  int markAsAnalyzing(UUID id);
}
