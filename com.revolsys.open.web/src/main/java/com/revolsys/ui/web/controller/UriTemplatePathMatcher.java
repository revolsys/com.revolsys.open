package com.revolsys.ui.web.controller;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.PathMatcher;

import com.revolsys.util.CompareUtil;

public class UriTemplatePathMatcher implements PathMatcher {

  public String extractPathWithinPattern(
    String pattern,
    String path) {
    if (matchStart(pattern, path)) {
      return path;
    } else {
      return "";
    }
  }

  public boolean isPattern(
    String path) {
    return path.contains("{") || path.contains("*") || path.contains("?");
  }

  public boolean match(
    String pattern,
    String path) {
    Matcher matcher = getMatcher(pattern, path);
    return matcher.matches();
  }

  public boolean matchStart(
    String pattern,
    String path) {
    Matcher matcher = getMatcher(pattern, path);
    if (matcher.find()) {
      return matcher.start() == 0;
    } else {
      return false;
    }
  }

  private Matcher getMatcher(
    String pattern,
    String path) {
    String regex = pattern;
//    regex = regex.replaceAll("\\*\\*", "(.*)");
    regex = regex.replaceAll("\\*", "([^/]+)");
    regex = regex.replaceAll("\\(\\[\\^/\\]\\+\\)\\(\\[\\^/\\]\\+\\)", "(.*)");
    regex = regex.replaceAll("\\?", "(.?)");
    regex = regex.replaceAll("\\{[^\\}]+\\}", "([^/]+)");
    Pattern rePattern = Pattern.compile(regex);
    Matcher matcher = rePattern.matcher(path);
    return matcher;
  }

  public String combine(
    String arg0,
    String arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  public Map<String, String> extractUriTemplateVariables(
    String pattern,
    String path) {
    Map<String, String> variables = new LinkedHashMap<String, String>();
    return variables;
  }

  public Comparator<String> getPatternComparator(
    String arg0) {
    return new Comparator<String>() {
      public int compare(
        String pattern1,
        String Pattern2) {
      // TODO improve
        return -CompareUtil.compare(pattern1.length(), Pattern2.length());
      }
    };
  }

}
