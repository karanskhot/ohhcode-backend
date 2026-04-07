package com.kdc.ohhcode.util;

import com.kdc.ohhcode.entities.enums.Difficulty;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SnippetDifficultyConverter implements Converter<String, Difficulty> {

  @Override
  public Difficulty convert(String source) {
    return Difficulty.valueOf(source.trim().toUpperCase());
  }
}
