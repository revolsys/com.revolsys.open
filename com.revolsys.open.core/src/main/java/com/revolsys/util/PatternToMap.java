package com.revolsys.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternToMap {
  private final Pattern pattern;

  private final List<String> fieldNames;

  public PatternToMap(final String pattern, final List<String> fieldNames) {
    this.pattern = Pattern.compile(pattern);
    this.fieldNames = fieldNames;
  }

  public PatternToMap(final String pattern, final String... fieldNames) {
    this(pattern, Arrays.asList(fieldNames));
  }

  public Map<String, String> toMap(final String text) {
    final Matcher matcher = this.pattern.matcher(text);
    if (matcher.matches()) {
      final Map<String, String> map = new LinkedHashMap<>();
      for (int i = 0; i < this.fieldNames.size(); i++) {
        final String fieldName = this.fieldNames.get(i);
        final String value = matcher.group(i + 1);
        map.put(fieldName, value);
      }
      return map;
    } else {
      return Collections.emptyMap();
    }
  }

}
