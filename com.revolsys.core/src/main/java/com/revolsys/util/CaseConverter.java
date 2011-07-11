package com.revolsys.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CaseConverter {
  public static final String LOWER_CAMEL_CASE_RE = "";

  public static String captialize(final String text) {
    return Character.toUpperCase(text.charAt(0))
      + text.substring(1).toLowerCase();
  }

  public static List<String> splitWords(final String text) {
    if (text == null) {
      return Collections.emptyList();
    } else {
      final Pattern p = Pattern.compile("([\\p{Lu}\\d']+)$" + "|"
        + "([\\p{Lu}\\d']+)[ _]" + "|" + "([\\p{L}\\d'][^\\p{Lu} _]*)");
      final Matcher m = p.matcher(text);
      final List<String> words = new ArrayList<String>();
      while (m.find()) {
        for (int i = 1; i <= m.groupCount(); i++) {
          final String group = m.group(i);
          if (group != null) {
            words.add(m.group(i));
          }
        }
      }
      return words;
    }
  }

  public static String toCapitalizedWords(final String text) {
    final List<String> words = splitWords(text);
    final StringBuffer result = new StringBuffer();
    for (final Iterator<String> iter = words.iterator(); iter.hasNext();) {
      final String word = iter.next();
      result.append(captialize(word));
      if (iter.hasNext()) {
        result.append(" ");
      }
    }
    return result.toString();
  }

  public static String toLowerCamelCase(final String text) {
    final List<String> words = splitWords(text);
    if (words.size() == 0) {
      return "";
    } else if (words.size() == 1) {
      return words.get(0).toLowerCase();
    } else {
      final StringBuffer result = new StringBuffer();
      final Iterator<String> iter = words.iterator();
      result.append(iter.next().toLowerCase());
      while (iter.hasNext()) {
        final String word = iter.next();
        result.append(captialize(word));
      }
      return result.toString();
    }
  }

  public static String toLowerUnderscore(final String text) {
    final List<String> words = splitWords(text);
    final StringBuffer result = new StringBuffer();
    for (final Iterator<String> iter = words.iterator(); iter.hasNext();) {
      final String word = iter.next();
      result.append(word.toLowerCase());
      if (iter.hasNext()) {
        result.append("_");
      }
    }
    return result.toString();
  }

  public static String toSentence(final String text) {
    final List<String> words = splitWords(text);
    if (words.size() == 0) {
      return "";
    } else if (words.size() == 1) {
      return captialize(words.get(0));
    } else {
      final StringBuffer result = new StringBuffer();
      final Iterator<String> iter = words.iterator();
      result.append(captialize(iter.next()));
      while (iter.hasNext()) {
        final String word = iter.next();
        result.append(word.toLowerCase());
        if (iter.hasNext()) {
          result.append(" ");
        }
      }
      return result.toString();
    }
  }

  public static String toUpperCamelCase(final String text) {
    final List<String> words = splitWords(text);
    final StringBuffer result = new StringBuffer();
    for (final Iterator<String> iter = words.iterator(); iter.hasNext();) {
      final String word = iter.next();
      result.append(captialize(word));
    }
    return result.toString();
  }

  public static String toUpperUnderscore(final String text) {
    final List<String> words = splitWords(text);
    final StringBuffer result = new StringBuffer();
    for (final Iterator<String> iter = words.iterator(); iter.hasNext();) {
      final String word = iter.next();
      result.append(word.toUpperCase());
      if (iter.hasNext()) {
        result.append("_");
      }
    }
    return result.toString();
  }

  private CaseConverter() {
  }

  public static String toLowerFirstChar(String text) {
    if (text.length() > 0) {
      char c = text.charAt(0);
      return Character.toLowerCase(c) + text.substring(1);
    } else {
      return text;
    }
  }
}
