package com.revolsys.io.carto;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.io.FileUtil;
import com.revolsys.spring.SpringUtil;

public class CartoCssParser {

  public static CartoCssDocument parse(final File file) {
    final FileSystemResource resource = new FileSystemResource(file);
    return parse(resource);
  }

  public static CartoCssDocument parse(final Resource resource) {
    return new CartoCssParser(resource).parse();
  }

  private final BufferedReader reader;

  private String currentLine = "";

  private int lineNumber = 0;

  private int currentIndex = 0;

  private int currentCharacter;

  public CartoCssParser(final Reader reader) {
    this.reader = new BufferedReader(reader);
  }

  public CartoCssParser(final Resource resource) {
    this(SpringUtil.getReader(resource));
  }

  private int getNextCharacter() throws IOException {
    if (currentIndex >= currentLine.length()) {
      currentLine = "";
      currentIndex = 0;
    }
    while (!StringUtils.hasText(currentLine)) {
      currentLine = reader.readLine();
      if (currentLine == null) {
        currentLine = "";
        currentCharacter = -1;
        return currentCharacter;
      } else {
        lineNumber++;
      }
    }
    currentCharacter = currentLine.charAt(currentIndex++);
    return currentCharacter;
  }

  private String getString(final char endCharacter) throws IOException {
    final StringBuffer buffer = new StringBuffer();
    while (currentCharacter != endCharacter) {
      buffer.append((char)currentCharacter);
      getNextCharacter();
      if (currentCharacter == -1) {
        return null;
      }
    }
    getNextCharacter();
    String selector = buffer.toString().trim();
    selector = selector.replaceAll("/\\*[^(*/)]+\\*/", "");
    return selector;
  }

  public CartoCssDocument parse() {
    final List<RuleSet> ruleSets = new ArrayList<RuleSet>();
    try {
      getNextCharacter();
      while (skipWhitespaceAndComments()) {
        final RuleSet ruleSet = parseRuleSet();
        if (ruleSet != null) {
          ruleSets.add(ruleSet);
        }
      }
    } catch (final IOException e) {
      throwException("Error reading ", e);
    } finally {
      FileUtil.closeSilent(reader);
    }
    return new CartoCssDocument(ruleSets);
  }

  private Map<CartoCssProperty, String> parseDeclarations() throws IOException {
    final Map<CartoCssProperty, String> declarations = new LinkedHashMap<CartoCssProperty, String>();
    skipWhitespaceAndComments();
    while (currentCharacter != '}') {
      final String name = getString(':');
      if (!StringUtils.hasText(name)) {
        throwException("Expecting a property name before ':'");
      }
      final CartoCssProperty propertyName = CartoCssProperty.getProperty(name);
      skipWhitespaceAndComments();
      final String value = getString(';');
      if (!StringUtils.hasText(value)) {
        throwException("Expecting a property value before ';'");
      }
      skipWhitespaceAndComments();
      declarations.put(propertyName, value);
    }
    return declarations;
  }

  private RuleSet parseRuleSet() throws IOException {
    while (currentCharacter != -1 && !Character.isWhitespace(currentCharacter)
      && currentCharacter != '/') {
      final Selector selector = parseSelector();
      if (selector != null) {
        final Map<CartoCssProperty, String> declarations = parseDeclarations();
        return new RuleSet(selector, declarations);
      }
    }
    return null;
  }

  private Selector parseSelector() throws IOException {
    final String selector = getString('{');
    if (selector == null) {
      return null;
    } else {
      if (selector.length() == 0) {
        throwException("A selector must be specified");
      }
      return new IdSelector(selector.substring(1));
    }
  }

  private boolean skipComment() throws IOException {
    if (getNextCharacter() != '*') {
      throwException("Expecting a '*' not '" + (char)currentCharacter + "'");
    }
    do {
      while (getNextCharacter() == '*') {
        if (getNextCharacter() == '/') {
          if (getNextCharacter() != -1) {
            return false;
          } else {
            return true;
          }
        }
      }
    } while (currentCharacter != -1);
    return false;
  }

  private boolean skipWhitespace() throws IOException {
    while (Character.isWhitespace(currentCharacter)) {
      getNextCharacter();
    }
    if (currentCharacter == -1) {
      return false;
    } else {
      return true;
    }
  }

  private boolean skipWhitespaceAndComments() throws IOException {
    while (currentCharacter != -1) {
      if (Character.isWhitespace(currentCharacter)) {
        skipWhitespace();
      } else if (currentCharacter == '/') {
        skipComment();
      } else {
        return true;
      }
    }
    return false;
  }

  private void throwException(final String message) {
    throw new RuntimeException(message + "line #" + lineNumber + ", index #"
      + currentIndex + "\n" + currentLine);
  }

  private void throwException(final String message, final Throwable e) {
    throw new RuntimeException(message + "line #" + lineNumber + ", index #"
      + currentIndex + "\n" + currentLine, e);
  }
}
