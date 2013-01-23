package com.revolsys.io.carto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.io.FileUtil;
import com.revolsys.spring.SpringUtil;

public class CartoCssParser {

  public static CartoCssDocument parse(Resource resource) {
    return new CartoCssParser(resource).parse();
  }

  private BufferedReader reader;

  private String currentLine = "";

  private int lineNumber = 0;

  private int currentIndex = 0;

  private int currentCharacter;

  public CartoCssParser(Resource resource) {
    this(SpringUtil.getReader(resource));
  }

  public CartoCssParser(Reader reader) {
    this.reader = new BufferedReader(reader);
  }

  public CartoCssDocument parse() {
    List<RuleSet> ruleSets = new ArrayList<RuleSet>();
    try {
      getNextCharacter();
      while (skipWhitespaceAndComments()) {
        RuleSet ruleSet = parseRuleSet();
        if (ruleSet != null) {
          ruleSets.add(ruleSet);
        }
      }
    } catch (IOException e) {
      throwException("Error reading ", e);
    } finally {
      FileUtil.closeSilent(reader);
    }
    return new CartoCssDocument(ruleSets);
  }

  private RuleSet parseRuleSet() throws IOException {
    while (currentCharacter != -1 && !Character.isWhitespace(currentCharacter)
      && currentCharacter != '/') {
      Selector selector = parseSelector();
      if (selector != null) {
        Map<CartoCssProperty, String> declarations = parseDeclarations();
        return new RuleSet(selector, declarations);
      }
    }
    return null;
  }

  private Map<CartoCssProperty, String> parseDeclarations() throws IOException {
    Map<CartoCssProperty, String> declarations = new LinkedHashMap<CartoCssProperty, String>();
    skipWhitespaceAndComments();
    while (currentCharacter != '}') {
      String name = getString(':');
      if (!StringUtils.hasText(name)) {
        throwException("Expecting a property name before ':'");
      }
      CartoCssProperty propertyName = CartoCssProperty.getProperty(name);
      skipWhitespaceAndComments();
      String value = getString(';');
      if (!StringUtils.hasText(value)) {
        throwException("Expecting a property value before ';'");
      }
      skipWhitespaceAndComments();
      declarations.put(propertyName, value);
    }
    return declarations;
  }

  private Selector parseSelector() throws IOException {
    String selector = getString('{');
    if (selector == null) {
      return null;
    } else {
      if (selector.length() == 0) {
        throwException("A selector must be specified");
      }
      return new IdSelector(selector.substring(1));
    }
  }

  private String getString(char endCharacter) throws IOException {
    StringBuffer buffer = new StringBuffer();
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

  private void throwException(String message, Throwable e) {
    throw new RuntimeException(message + "line #" + lineNumber + ", index #"
      + currentIndex + "\n" + currentLine, e);
  }

  private void throwException(String message) {
    throw new RuntimeException(message + "line #" + lineNumber + ", index #"
      + currentIndex + "\n" + currentLine);
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
}
