/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.gis.format.saif.io.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.namespace.QName;

public class CsnIterator {
  public static final int ATTRIBUTE_NAME = 7;

  public static final int ATTRIBUTE_PATH = 13;

  public static final int ATTRIBUTE_TYPE = 8;

  public static final int CLASS_NAME = 4;

  public static final int COLLECTION_ATTRIBUTE = 9;

  public static final int COLLECTION_ATTRIBUTE_TYPE = 10;

  public static final int COMPONENT_NAME = 5;

  public static final int END_DEFINITION = 3;

  public static final int END_DOCUMENT = 1;

  public static final int EXCLUDE_TYPE = 15;

  public static final int FLOAT_VALUE = 18;

  public static final int FORCE_TYPE = 14;

  private static final Object IN_ATTRIBUTE = "attribute";

  private static final Object IN_DEFAULT = "default";

  private static final Object IN_DEFINITION = "definition";

  private static final Object IN_DOCUMENT = "document";

  private static final Object IN_RESTRICTION_VALUES = "restrictionValues";

  private static final String IN_RESTRICTIONS = "restricted";

  public static final int INTEGER_VALUE = 19;

  public static final int OPTIONAL_ATTRIBUTE = 6;

  private static final Set<String> RESERVED_WORDS = new HashSet<String>(
    Arrays.asList(new String[] {
      "subclass", "values", "comments", "attributes", "subclassing",
      "classAttributes", "defaults", "constraints", "restricted",
      "classAttributeValues", "classAttributeDefaults"// "primitiveType",
    }));

  public static final int START_DEFINITION = 2;

  public static final int START_DOCUMENT = 0;

  public static final int STRING_ATTRIBUTE = 11;

  public static final int STRING_ATTRIBUTE_LENGTH = 12;

  public static final int STRING_LENGTH = 17;

  public static final int TAG_NAME = 20;

  public static final int UNKNOWN = -1;

  public static final int VALUE = 16;

  private StringBuffer buffer = new StringBuffer();

  private int columnNumber;

  private int currentColumnNumber;

  private int currentLineNumber;

  private int eventType = START_DOCUMENT;

  private String fileName;

  private String line;

  private int lineNumber = 0;

  private int nextEventType = START_DOCUMENT;

  private Object nextToken;

  private final BufferedReader reader;

  private final Stack<Object> scopeStack = new Stack<Object>();

  private Object value;

  public CsnIterator(
    final File file)
    throws IOException {
    this(file.getName(), new FileReader(file));
  }

  public CsnIterator(
    final String fileName,
    final InputStream in)
    throws IOException {
    this(fileName, new InputStreamReader(in));
  }

  public CsnIterator(
    final String fileName,
    final Reader reader)
    throws IOException {
    this.reader = new BufferedReader(reader);
    scopeStack.push(IN_DOCUMENT);
    processNext();
  }

  public void close()
    throws IOException {
    reader.close();
  }

  private String findClassName(
    final StringBuffer buffer)
    throws IOException {
    final String className = findUpperName(buffer);
    final StringBuffer newBuffer = getStrippedBuffer();
    // If the class name is fullowed by '::' get and return the schema name
    if (newBuffer.charAt(0) == ':' && newBuffer.charAt(1) == ':') {
      removeExtraToken(0, 2);
      final String schemaName = findUpperName(getStrippedBuffer());
      return (className + "::" + schemaName);
    } else {
      return className;
    }
  }

  private String findEnumTag(
    final StringBuffer buffer) {
    int endIndex = 1;
    boolean validChar = true;
    final int len = buffer.length();
    while (validChar && endIndex < len) {
      final char c = buffer.charAt(endIndex);

      if (isCharacter(c) || c == '-') {
        endIndex++;
      } else {
        validChar = false;
      }
    }
    final String name = buffer.substring(0, endIndex);
    removeToken(0, endIndex);
    return name;
  }

  private Integer findInteger() {
    int endIndex = 0;
    boolean validChar = true;
    final int len = buffer.length();
    while (validChar && endIndex < len) {
      final char c = buffer.charAt(endIndex);

      if (isDigit(c)) {
        endIndex++;
      } else {
        validChar = false;
      }
    }
    final String number = buffer.substring(0, endIndex);
    if (number.length() > 0) {
      removeToken(0, endIndex);
      return Integer.valueOf(number);
    } else {
      return null;
    }
  }

  private String findLowerName(
    final StringBuffer buffer) {
    if (isLowerCase(buffer.charAt(0))) {
      return findName(buffer);
    } else {
      return null;
    }
  }

  private String findName(
    final StringBuffer buffer) {
    int endIndex = 0;
    boolean validChar = true;
    final int len = buffer.length();
    while (validChar && endIndex < len) {
      final char c = buffer.charAt(endIndex);

      if (isCharacter(c)) {
        endIndex++;
      } else {
        validChar = false;
      }
    }
    final String name = buffer.substring(0, endIndex);
    removeToken(0, endIndex);
    return name;
  }

  private int findStartDefinition(
    final StringBuffer buffer)
    throws IOException {
    if (buffer.charAt(0) != '<') {
      return UNKNOWN;
    } else {
      removeToken(0, 1);
      scopeStack.push(IN_DEFINITION);
      return START_DEFINITION;

    }
  }

  private String findUpperName(
    final StringBuffer buffer) {
    if (isUpperCase(buffer.charAt(0))) {
      return findName(buffer);
    } else {
      return null;
    }
  }

  public boolean getBooleanValue() {
    return ((Boolean)value).booleanValue();
  }

  private StringBuffer getBuffer()
    throws IOException {
    if (buffer == null || buffer.length() == 0) {
      line = reader.readLine();
      lineNumber++;
      while (line != null && (line.startsWith("//") || line.length() == 0)) {
        line = reader.readLine();
        lineNumber++;
      }
      if (line != null) {
        buffer.append(line);
      } else {
        buffer = null;
      }
    }
    return buffer;
  }

  public int getEventType() {
    return eventType;
  }

  public float getFloatValue() {
    return ((Float)value).floatValue();
  }

  public int getIntegerValue() {
    return ((Integer)value).intValue();
  }

  public int getNextEventType() {
    return nextEventType;
  }

  public QName getQNameValue() {
    final String name = getStringValue();
    return QNameCache.getName(name);
  }

  public String getStringValue() {
    return (String)value;
  }

  private StringBuffer getStrippedBuffer()
    throws IOException {
    StringBuffer buffer = stripWhitespace(getBuffer());
    while (buffer != null && buffer.length() == 0) {
      buffer = stripWhitespace(getBuffer());
    }
    return buffer;
  }

  public Object getValue() {
    return value;
  }

  private boolean isCharacter(
    final char c) {
    return isLowerCase(c) || isUpperCase(c) || isDigit(c) || c == '_';
  }

  private boolean isDigit(
    final char c) {
    return (c >= '0' && c <= '9');
  }

  private boolean isLowerCase(
    final char c) {
    return (c >= 'a' && c <= 'z');
  }

  private boolean isReservedWord(
    final String name)
    throws IOException {
    buffer = getStrippedBuffer();
    if (RESERVED_WORDS.contains(name) && buffer.charAt(0) == ':') {
      removeExtraToken(0, 1);
      setNextToken(name);
      scopeStack.pop();
      scopeStack.push(name);
      return true;
    } else {
      return false;
    }
  }

  private boolean isUpperCase(
    final char c) {
    return (c >= 'A' && c <= 'Z');
  }

  public int next()
    throws IOException {
    eventType = nextEventType;
    value = nextToken;
    processNext();
    return eventType;
  }

  private void processAttribute(
    final StringBuffer buffer)
    throws IOException {
    StringBuffer localBuffer = buffer;
    if (nextEventType == OPTIONAL_ATTRIBUTE) {
      final String attributeName = findLowerName(localBuffer);
      if (attributeName != null) {
        final StringBuffer newBuffer = getStrippedBuffer();
        if (newBuffer.charAt(0) == ']') {
          removeToken(0, 1);
          nextEventType = ATTRIBUTE_NAME;
          setNextToken(attributeName);
          // scopeStack.pop();
        } else {
          throw new IllegalStateException(
            "Expecting end of optional attribute name ']'");
        }
      } else {
        throw new IllegalStateException("Expecting an attribute name");
      }
    } else if (nextEventType == COLLECTION_ATTRIBUTE) {
      final String className = findClassName(localBuffer);
      if (className != null) {
        final StringBuffer newBuffer = getStrippedBuffer();
        if (newBuffer.charAt(0) == ')') {
          nextEventType = CLASS_NAME;
          setNextToken(className);
          removeExtraToken(0, 1);
          scopeStack.pop();
        } else {
          throw new IllegalStateException("Expecting a ')");
        }
      } else {
        throw new IllegalStateException("Expecting a class name");
      }
    } else if (nextEventType == STRING_ATTRIBUTE) {
      final Integer length = findInteger();
      if (length != null) {
        localBuffer = getStrippedBuffer();
        if (localBuffer.charAt(0) == ')') {
          nextEventType = STRING_LENGTH;
          setNextToken(length);
          removeToken(0, 1);
          scopeStack.pop();
        } else {
          throw new IllegalStateException("Expecting a ')");
        }
      } else {
        throw new IllegalStateException("Expecting a length for the string");
      }
    } else {
      final String className = findClassName(localBuffer);
      if (className != null) {
        if (className.equals("Set") || className.equals("List")
          || className.equals("Relation")) {
          final StringBuffer newBuffer = getStrippedBuffer();
          if (newBuffer.charAt(0) == '(') {
            nextEventType = COLLECTION_ATTRIBUTE;
            removeExtraToken(0, 1);

          } else {
            throw new IllegalStateException("Expecting a '(");
          }
        } else if (className.equals("String")) {
          final StringBuffer newBuffer = getStrippedBuffer();
          if (newBuffer.charAt(0) == '(') {
            nextEventType = STRING_ATTRIBUTE;
            removeExtraToken(0, 1);
          } else {
            nextEventType = STRING_ATTRIBUTE;
            scopeStack.pop();
          }
        } else {
          nextEventType = ATTRIBUTE_TYPE;
          scopeStack.pop();
        }
        setNextToken(className);
      } else {
        throw new IllegalStateException("Expecting a class name");
      }
    }
  }

  private int processAttributeName(
    final StringBuffer buffer)
    throws IOException {
    final String attributeName = findLowerName(buffer);
    if (attributeName == null) {
      return UNKNOWN;
    } else if (isReservedWord(attributeName)) {
      return COMPONENT_NAME;
    } else {
      setNextToken(attributeName);
      scopeStack.push(IN_ATTRIBUTE);
      return ATTRIBUTE_NAME;
    }
  }

  private int processAttributePath()
    throws IOException {
    StringBuffer buffer = getStrippedBuffer();
    final StringBuffer attributePath = new StringBuffer();
    String attributeName = findLowerName(buffer);
    if (attributeName == null) {
      return UNKNOWN;
    } else if (isReservedWord(attributeName)) {
      return COMPONENT_NAME;
    } else {
      attributePath.append(attributeName);
      buffer = getStrippedBuffer();
      while (buffer.charAt(0) != ':') {
        if (buffer.charAt(0) == '{' && buffer.charAt(1) == '}') {
          removeExtraToken(0, 2);
          attributePath.append("{}");
        }
        buffer = getStrippedBuffer();
        if (buffer.charAt(0) == '*') {
          removeExtraToken(0, 1);
          buffer = getStrippedBuffer();
          attributeName = findName(buffer);
          if (attributeName != null) {
            attributePath.append('*').append(attributeName);
          } else {
            throw new IllegalArgumentException("Expecting an attribute name");
          }
        }
        buffer = getStrippedBuffer();
        if (buffer.charAt(0) == '.') {
          removeExtraToken(0, 1);
          buffer = getStrippedBuffer();
          attributeName = findLowerName(buffer);
          if (attributeName != null) {
            attributePath.append('.').append(attributeName);
          } else {
            throw new IllegalArgumentException("Expecting an attribute name");
          }
        }
      }
      removeExtraToken(0, 1);
      setNextToken(attributePath.toString());
      return ATTRIBUTE_PATH;
    }
  }

  public void processAttributes()
    throws IOException {
    final StringBuffer buffer = getStrippedBuffer();
    if (buffer.charAt(0) == '>') {
      removeToken(0, 1);
      scopeStack.pop();
      scopeStack.pop();
      nextEventType = END_DEFINITION;
    } else if (buffer.charAt(0) == '[') {
      nextEventType = OPTIONAL_ATTRIBUTE;
      setNextToken(Boolean.TRUE);
      removeToken(0, 1);
      scopeStack.push(IN_ATTRIBUTE);
    } else {
      nextEventType = processAttributeName(buffer);
      if (nextEventType == UNKNOWN) {
        throw new IllegalStateException(
          "Expecting an attribute name or component name");
      }
    }
  }

  public void processClassAttributes()
    throws IOException {
    final StringBuffer buffer = getStrippedBuffer();
    if (buffer.charAt(0) == '>') {
      removeToken(0, 1);
      scopeStack.pop();
      scopeStack.pop();
      nextEventType = END_DEFINITION;
    } else if (buffer.charAt(0) == '[') {
      nextEventType = OPTIONAL_ATTRIBUTE;
      setNextToken(Boolean.TRUE);
      removeToken(0, 1);
      scopeStack.push(IN_ATTRIBUTE);
    } else {
      nextEventType = processAttributeName(buffer);
      if (nextEventType == UNKNOWN) {
        throw new IllegalStateException(
          "Expecting an attribute name or component name");
      }
    }
  }

  public void processComments()
    throws IOException {
    if (processValue() == VALUE) {
      scopeStack.pop();
    } else {
      throw new IllegalStateException("Expecting comment string");
    }
  }

  public void processComponent(
    final String componentName)
    throws IOException {
    final String methodName = "process"
      + Character.toUpperCase(componentName.charAt(0))
      + componentName.substring(1);
    try {
      final Method method = getClass().getMethod(methodName, new Class[0]);
      method.invoke(this, new Object[0]);
    } catch (final SecurityException e) {
      throw new RuntimeException("Unable to access method '" + methodName
        + "': " + e.getMessage(), e);
    } catch (final NoSuchMethodException e) {
      throw new RuntimeException("No process method available for component '"
        + componentName + "': " + e.getMessage(), e);
    } catch (final IllegalAccessException e) {
      throw new RuntimeException("Unable to access method '" + methodName
        + "': " + e.getMessage(), e);
    } catch (final InvocationTargetException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException)cause;
      } else if (cause instanceof Error) {
        throw (Error)cause;
      } else if (cause instanceof IOException) {
        throw (IOException)cause;
      } else {
        throw new RuntimeException(cause.getMessage(), cause);
      }
    }
  }

  public void processDefaults()
    throws IOException {
    final StringBuffer buffer = getStrippedBuffer();
    if (buffer.charAt(0) == '>') {
      removeToken(0, 1);
      scopeStack.pop();
      scopeStack.pop();
      nextEventType = END_DEFINITION;
    } else {
      nextEventType = processAttributePath();
      if (nextEventType == ATTRIBUTE_PATH) {
        scopeStack.push(IN_DEFAULT);
      }
    }
  }

  private void processDefinitions(
    final StringBuffer buffer)
    throws IOException {
    final char c = buffer.charAt(0);
    // End of Definition
    if (c == '>') {
      nextEventType = END_DEFINITION;
      removeToken(0, 1);
      scopeStack.pop();
    } else if (isUpperCase(c)) {
      // Parent name definition
      final String className = findClassName(buffer);
      nextEventType = CLASS_NAME;
      setNextToken(className);
      final StringBuffer newBuffer = getStrippedBuffer();
      if (newBuffer.charAt(0) == ',') {
        removeExtraToken(0, 1);
      }
    } else if (isLowerCase(c)) {
      final String componentName = findLowerName(buffer);
      final StringBuffer newBuffer = getStrippedBuffer();
      if (newBuffer.charAt(0) == ':') {
        removeExtraToken(0, 1);
        nextEventType = COMPONENT_NAME;
        setNextToken(componentName);
        scopeStack.push(componentName);
      } else {
        throw new IllegalStateException(
          "Expecting component definition ending in a ':'");

      }
    } else {
      throw new IllegalStateException(
        "Expecting end of definition, class name or definition component");
    }
  }

  private int processDigitString() {
    int endIndex = 0;
    boolean validChar = true;
    boolean hasPeriod = false;
    final int len = buffer.length();
    while (validChar && endIndex < len) {
      final char c = buffer.charAt(endIndex);

      if (c == '-' && endIndex == 0) {
        endIndex++;
      } else if (isDigit(c)) {
        endIndex++;
      } else if (c == '.') {
        if (hasPeriod) {
          nextEventType = UNKNOWN;
          return nextEventType;
        } else {
          hasPeriod = true;
          endIndex++;
        }
      } else {
        validChar = false;
      }
    }
    final String number = buffer.substring(0, endIndex);
    if (number.length() > 0) {
      removeToken(0, endIndex);
      setNextToken(new BigDecimal(number));
      nextEventType = VALUE;
    }
    return nextEventType;
  }

  private void processDocument(
    final StringBuffer buffer)
    throws IOException {
    nextEventType = findStartDefinition(buffer);
    if (nextEventType == UNKNOWN) {
      throw new IllegalStateException(lineNumber + ":"
        + "Expecting start of an object definition");
    }
  }

  private void processNext()
    throws IOException {
    final StringBuffer buffer = getStrippedBuffer();
    setNextToken(null);
    if (buffer == null) {
      nextEventType = END_DOCUMENT;
    } else {
      final Object scope = scopeStack.peek();
      if (scope == IN_DOCUMENT) {
        processDocument(buffer);
      } else if (RESERVED_WORDS.contains(scope)) {
        processComponent((String)scope);
      } else if (scope == IN_DEFINITION) {
        processDefinitions(buffer);
      } else if (scope == IN_ATTRIBUTE) {
        processAttribute(buffer);
      } else if (scope == IN_DEFAULT) {
        processValue();
        scopeStack.pop();
      } else if (scope == IN_RESTRICTIONS) {
        processRestricted();
      } else if (scope == IN_RESTRICTION_VALUES) {
        processRestrictionValues();
      }
    }
  }

  private int processRange() {
    int endIndex = 0;
    boolean validChar = true;
    int periodIndex = -1;
    boolean hasSecondPeriod = false;
    final int len = buffer.length();
    while (validChar && endIndex < len) {
      final char c = buffer.charAt(endIndex);

      if (c == '-' && (endIndex == 0 || periodIndex == endIndex - 1)) {
        endIndex++;
      } else if (isDigit(c)) {
        endIndex++;
      } else if (c == '.') {
        if (hasSecondPeriod) {
          throw new IllegalStateException(
            "A .. has already been defined for the range");
        } else if (periodIndex == -1) {
          periodIndex = endIndex;
          endIndex++;
        } else if (periodIndex == endIndex - 1) {
          endIndex++;
          hasSecondPeriod = true;
          periodIndex = endIndex;
        } else {
          throw new IllegalStateException("A range must have two '..'");
        }
      } else {
        validChar = false;
      }
    }
    if (!hasSecondPeriod) {
      throw new IllegalStateException("A range must have two '..'");
    } else if (periodIndex == endIndex) {
      throw new IllegalStateException("A range must be in the format '99..99'");
    }
    final String range = buffer.substring(0, endIndex);
    if (range.length() > 0) {
      removeToken(0, endIndex);
      setNextToken(range);
      nextEventType = VALUE;
    }
    return nextEventType;
  }

  public void processRestricted()
    throws IOException {
    final StringBuffer newBuffer = getStrippedBuffer();

    if (newBuffer.charAt(0) == '>') {
      removeToken(0, 1);
      nextEventType = END_DEFINITION;
      scopeStack.pop();
      scopeStack.pop();
    } else {
      nextEventType = processAttributePath();
      if (nextEventType == COMPONENT_NAME) {
        processComponent(getStringValue());
      } else if (nextEventType == ATTRIBUTE_PATH) {
        scopeStack.push(IN_RESTRICTION_VALUES);
      }
    }
  }

  private void processRestrictionValues()
    throws IOException {
    StringBuffer buffer = getStrippedBuffer();
    final char c = buffer.charAt(0);
    if (c == '^') {
      nextEventType = FORCE_TYPE;
      removeToken(0, 1);
      setNextToken(Boolean.TRUE);
    } else if (c == '~') {
      nextEventType = EXCLUDE_TYPE;
      removeToken(0, 1);
      setNextToken(Boolean.TRUE);
    } else {
      if (isUpperCase(c)) {
        setNextToken(findClassName(buffer));
        nextEventType = CLASS_NAME;
      } else {
        if (processValue() == UNKNOWN) {
          processRange();
        }
      }
      buffer = getStrippedBuffer();
      if (buffer.charAt(0) == '|') {
        removeExtraToken(0, 1);
      } else {
        scopeStack.pop();
      }
    }
  }

  public void processSubclass()
    throws IOException {
    final String className = findClassName(getStrippedBuffer());
    if (className != null) {
      nextEventType = CLASS_NAME;
      setNextToken(className);
      scopeStack.pop();
    } else {
      throw new IllegalStateException("Expecting class name");
    }
  }

  private int processValue()
    throws IOException {
    StringBuffer buffer = getStrippedBuffer();
    char c = buffer.charAt(0);
    if (c == '"') {
      removeToken(0, 1);
      final StringBuffer text = new StringBuffer();
      int endIndex = 0;
      c = buffer.charAt(endIndex);
      int len = buffer.length();
      while (c != '"') {
        endIndex++;
        while (endIndex == len) {
          text.append(buffer.substring(0, endIndex));
          removeExtraToken(0, endIndex);
          text.append('\n');
          endIndex = 0;
          buffer = getBuffer();
          if (buffer != null) {
            len = buffer.length();
          } else {
            throw new IllegalStateException("Unnexpected end of file");
          }
        }
        c = buffer.charAt(endIndex);
      }
      text.append(buffer.substring(0, endIndex));
      removeToken(0, endIndex + 1);
      setNextToken(text.toString());
      nextEventType = VALUE;
    } else if (isLowerCase(c) || c == '$') {
      final String enumTag = findEnumTag(buffer);
      if (enumTag.equals("true")) {
        setNextToken(Boolean.TRUE);
      } else if (enumTag.equals("false")) {
        setNextToken(Boolean.FALSE);
      } else {
        setNextToken(enumTag);
      }
      nextEventType = VALUE;
    } else if (isUpperCase(c)) {
      final String enumTag = findEnumTag(buffer);
      if (enumTag.equals("true")) {
        setNextToken(Boolean.TRUE);
      } else if (enumTag.equals("false")) {
        setNextToken(Boolean.FALSE);
      } else {
        setNextToken(enumTag);
      }
      nextEventType = VALUE;
    } else if (c == '-' || c == '+' || isDigit(c)) {
      processDigitString();
    } else {
      nextEventType = UNKNOWN;
    }
    return nextEventType;
  }

  public void processValues()
    throws IOException {
    final String tagName = findLowerName(getStrippedBuffer());
    if (tagName == null) {
      if (getStrippedBuffer().charAt(0) == '>') {
        nextEventType = END_DEFINITION;
        removeToken(0, 1);
        scopeStack.pop();
        scopeStack.pop();
      } else {
        throw new IllegalStateException(
          "Expecting a tag value, component name or end of definition");
      }
    } else if (tagName.equals("comments")
      && getStrippedBuffer().charAt(0) == ':') {
      removeToken(0, 1);
      scopeStack.pop();
      nextEventType = COMPONENT_NAME;
      scopeStack.push(tagName);
      setNextToken(tagName);
    } else {
      nextEventType = TAG_NAME;
      setNextToken(tagName);
    }
  }

  private void removeExtraToken(
    final int start,
    final int end) {
    currentColumnNumber += end;
    buffer.delete(start, end);
  }

  private void removeToken(
    final int start,
    final int end) {
    lineNumber = currentLineNumber;
    columnNumber = currentColumnNumber;
    currentColumnNumber += end;
    buffer.delete(start, end);
  }

  private void setNextToken(
    final Object token) {
    this.nextToken = token;
  }

  private StringBuffer stripWhitespace(
    final StringBuffer buffer) {
    if (buffer == null) {
      return null;
    }
    final int len = buffer.length();
    int endIndex = 0;
    while (endIndex < len && Character.isWhitespace(buffer.charAt(endIndex))) {
      endIndex++;
    }
    removeToken(0, endIndex);
    return buffer;
  }

  @Override
  public String toString() {
    return fileName + "[" + lineNumber + "," + columnNumber + "]";
  }
}
