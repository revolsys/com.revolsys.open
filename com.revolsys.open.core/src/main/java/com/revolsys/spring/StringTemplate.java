package com.revolsys.spring;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

public class StringTemplate implements Serializable {

  private static final Pattern NAMES_PATTERN = Pattern.compile("\\{([^/]+?)\\}");

  private static final String VALUE_REGEX = "(.*)";

  private final List<String> variableNames;

  private final String uriTemplate;

  public StringTemplate(String uriTemplate) {
    Parser parser = new Parser(uriTemplate);
    this.uriTemplate = uriTemplate;
    this.variableNames = parser.getVariableNames();
  }

  public List<String> getVariableNames() {
    return variableNames;
  }

  public String expand(Map<String, ?> uriVariables) {
    Assert.notNull(uriVariables, "'uriVariables' must not be null");
    Object[] values = new Object[this.variableNames.size()];
    if (uriVariables != null) {
      for (int i = 0; i < this.variableNames.size(); i++) {
        String name = this.variableNames.get(i);
        if (uriVariables.containsKey(name)) {
          values[i] = uriVariables.get(name);

        }
      }
    }
    return expand(values);
  }

  private String expand(Object... uriVariableValues) {
    Matcher matcher = NAMES_PATTERN.matcher(this.uriTemplate);
    StringBuffer buffer = new StringBuffer();
    int i = 0;
    while (matcher.find()) {
      Object uriVariable = uriVariableValues[i++];
      String replacement;
      if (uriVariable == null) {
        replacement = Matcher.quoteReplacement("");
      } else {
        replacement = Matcher.quoteReplacement(uriVariable.toString());
      }
      matcher.appendReplacement(buffer, replacement);
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }

  @Override
  public String toString() {
    return uriTemplate;
  }

  /**
   * Static inner class to parse URI template strings into a matching regular
   * expression.
   */
  private static class Parser {

    private final List<String> variableNames = new LinkedList<String>();

    private final StringBuilder patternBuilder = new StringBuilder();

    private Parser(String uriTemplate) {
      Assert.hasText(uriTemplate, "'uriTemplate' must not be null");
      Matcher m = NAMES_PATTERN.matcher(uriTemplate);
      int end = 0;
      while (m.find()) {
        this.patternBuilder.append(quote(uriTemplate, end, m.start()));
        this.patternBuilder.append(VALUE_REGEX);
        this.variableNames.add(m.group(1));
        end = m.end();
      }
      this.patternBuilder.append(quote(uriTemplate, end, uriTemplate.length()));
      int lastIdx = this.patternBuilder.length() - 1;
      if (lastIdx >= 0 && this.patternBuilder.charAt(lastIdx) == '/') {
        this.patternBuilder.deleteCharAt(lastIdx);
      }
    }

    private String quote(String fullPath, int start, int end) {
      if (start == end) {
        return "";
      }
      return Pattern.quote(fullPath.substring(start, end));
    }

    private List<String> getVariableNames() {
      return Collections.unmodifiableList(this.variableNames);
    }

    private Pattern getMatchPattern() {
      return Pattern.compile(this.patternBuilder.toString());
    }
  }

}
