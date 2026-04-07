package com.kdc.ohhcode.constants;

import com.kdc.ohhcode.entities.enums.SnippetStatus;

import java.util.List;

public class AppConstants {

  public static final List<String> ALLOWED_TAGS =
      List.of(
          "Array",
          "String",
          "Hash Table",
          "Math",
          "Dynamic Programming",
          "Sorting",
          "Greedy",
          "Depth-First Search",
          "Binary Search",
          "Database",
          "Bit Manipulation",
          "Matrix",
          "Tree",
          "Breadth-First Search",
          "Two Pointers",
          "Prefix Sum",
          "Heap (Priority Queue)",
          "Simulation",
          "Counting",
          "Graph Theory",
          "Binary Tree",
          "Stack",
          "Sliding Window",
          "Enumeration",
          "Design",
          "Backtracking",
          "Union-Find",
          "Number Theory",
          "Linked List",
          "Ordered Set",
          "Segment Tree",
          "Monotonic Stack",
          "Divide and Conquer",
          "Trie",
          "Combinatorics",
          "Bitmask",
          "Queue",
          "Recursion",
          "Geometry",
          "Binary Indexed Tree",
          "Memoization",
          "Binary Search Tree",
          "Hash Function",
          "Topological Sort",
          "Shortest Path",
          "String Matching",
          "Rolling Hash",
          "Game Theory",
          "Interactive",
          "Data Stream",
          "Monotonic Queue",
          "Brainteaser",
          "Doubly-Linked List",
          "Merge Sort",
          "Randomized",
          "Counting Sort",
          "Iterator",
          "Concurrency",
          "Quickselect",
          "Suffix Array",
          "Sweep Line",
          "Probability and Statistics",
          "Minimum Spanning Tree",
          "Bucket Sort",
          "Shell",
          "Reservoir Sampling",
          "Eulerian Circuit",
          "Radix Sort",
          "Strongly Connected Component",
          "Rejection Sampling",
          "Object Oriented Programming",
          "Spring/Spring Boot",
          "Biconnected Component");

  public static final String  FALLBACK_RESPONSE = """
                {
                  "isValid": false,
                  "meta": {
                    "title": "Analysis Failed",
                    "tags": [],
                    "difficulty": "Unknown",
                    "language": "English",
                    "codeLanguage": "Unknown"
                  },
                  "message": "AI processing failed"
                }
                """;

  public static String getMessage(SnippetStatus status) {
    return switch (status) {
      case UPLOADED -> "Ready to analyze";
      case ANALYZING -> "Snippet is getting analyzed.";
      case ANALYZED -> "Completed";
      case FAILED -> "Analysis failed";
      default -> "";
    };
  }

  public static final List<String> ALLOWED_TYPES =
          List.of("image/jpeg", "image/png", "image/gif", "image/webp");
}
