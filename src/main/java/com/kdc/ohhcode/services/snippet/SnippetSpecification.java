package com.kdc.ohhcode.services.snippet;

import com.kdc.ohhcode.entities.SnippetEntity;
import com.kdc.ohhcode.entities.enums.Difficulty;
import com.kdc.ohhcode.entities.enums.SnippetStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class SnippetSpecification {

  private SnippetSpecification() {}

  public static Specification<SnippetEntity> hasUser(UUID userId) {
    return (root, query, cb) ->
        userId == null ? null : cb.equal(root.get("user").get("id"), userId);
  }

  public static Specification<SnippetEntity> hasStatus(SnippetStatus status) {
    return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
  }

  public static Specification<SnippetEntity> hasDifficulty(Difficulty difficulty) {
    return (root, query, cb) ->
        difficulty == null ? null : cb.equal(root.get("difficulty"), difficulty);
  }

  public static Specification<SnippetEntity> hasTags(Set<String> tags) {
    return (root, query, cb) -> {
      if (tags == null || tags.isEmpty()) return null;

      query.distinct(true);

      Set<String> normalized =
          tags.stream().map(tag -> tag.toLowerCase().trim()).collect(Collectors.toSet());

      Join<Object, Object> join = root.join("tags", JoinType.LEFT);
      return join.in(normalized);
    };
  }

  public static Specification<SnippetEntity> hasCreatedBetween(
      LocalDate startDate, LocalDate endDate) {

    return (root, query, cb) -> {
      if (startDate == null && endDate == null) return null;

      if (startDate != null && endDate != null) {
        return cb.between(
            root.get("createdAt"),
            startDate.atStartOfDay(ZoneOffset.UTC).toInstant(),
            endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
      }

      if (startDate != null) {
        return cb.greaterThanOrEqualTo(
            root.get("createdAt"), startDate.atStartOfDay(ZoneOffset.UTC).toInstant());
      }

      return cb.lessThanOrEqualTo(
          root.get("createdAt"), endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
    };
  }

  public static Specification<SnippetEntity> hasSearch(String search) {
    return (root, query, cb) -> {
      if (search == null || search.isBlank()) return null;

      String pattern = "%" + search.toLowerCase().trim() + "%";

      return cb.or(
          cb.like(cb.lower(cb.coalesce(root.get("title"), "")), pattern),
          cb.like(cb.lower(cb.coalesce(root.get("memoryNotes"), "")), pattern));
    };
  }
}
