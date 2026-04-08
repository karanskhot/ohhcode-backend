package com.kdc.ohhcode.entities;

import com.kdc.ohhcode.entities.enums.Difficulty;
import com.kdc.ohhcode.entities.enums.Language;
import com.kdc.ohhcode.entities.enums.SnippetStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Internal;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(
    name = "snippets",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "hash_code"}))
public class SnippetEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String title;

  @Column(name = "url", nullable = false)
  private String url;

  @Column(name = "hash_code", nullable = false)
  private String hashCode;

  @Column(name = "important")
  @Builder.Default
  private Boolean important = false;

  @Column(name = "memory_notes")
  private String memoryNotes;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private SnippetStatus status;

  @Column(name = "difficulty")
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Difficulty difficulty = Difficulty.MEDIUM;

  @Column(name = "language")
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Language language =  Language.ENGLISH;

  @Column(name = "analysis", columnDefinition = "TEXT")
  private String analysis;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "snippet_tags", joinColumns = @JoinColumn(name = "snippet_id"))
  @Column(name = "tag")
  private Set<String> tags = new HashSet<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "last_analyzed_at")
  private Instant lastAnalyzedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
