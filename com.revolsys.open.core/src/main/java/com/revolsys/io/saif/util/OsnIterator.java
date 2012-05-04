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
package com.revolsys.io.saif.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Stack;
import java.util.zip.ZipFile;

import com.revolsys.gis.parser.ParseException;

public class OsnIterator implements Iterator<Object> {
  public static final Object BOOLEAN_VALUE = new Integer(9);

  public static final Object END_DOCUMENT = new Integer(1);

  public static final Object END_LIST = new Integer(12);

  public static final Object END_OBJECT = new Integer(3);

  public static final Object END_RELATION = new Integer(16);

  public static final Object END_SET = new Integer(14);

  public static final Object ENUM_TAG = new Integer(8);

  private static final Object IN_ATTRIBUTE = "attribute";

  private static final Object IN_DOCUMENT = "document";

  private static final Object IN_LIST = "list";

  private static final Object IN_OBJECT = "object";

  private static final Object IN_RELATION = "relation";

  private static final Object IN_SET = "set";

  private static final boolean[] IS_DIGIT_CHARACTER = new boolean[256];

  private static final boolean[] IS_LOWER_CASE_CHARACTER = new boolean[256];

  private static final boolean[] IS_NAME_CHARACTER = new boolean[256];

  private static final boolean[] IS_NUMBER_CHARACTER = new boolean[256];

  private static final boolean[] IS_UPPER_CASE_CHARACTER = new boolean[256];

  private static final boolean[] IS_WHITESPACE_CHARACTER = new boolean[256];

  public static final Object NULL_VALUE = new Integer(10);

  public static final Object NUMERIC_VALUE = new Integer(5);

  public static final Object START_ATTRIBUTE = new Integer(4);

  public static final Object START_DEFINITION = new Integer(2);

  public static final Object START_DOCUMENT = new Integer(0);

  public static final Object START_LIST = new Integer(11);

  public static final Object START_RELATION = new Integer(15);

  public static final Object START_SET = new Integer(13);

  public static final Object TEXT_VALUE = new Integer(7);

  public static final Object UNKNOWN = new Integer(-1);

  static {
    for (int c = 'a'; c <= 'z'; c++) {
      IS_LOWER_CASE_CHARACTER[c] = true;
      IS_NAME_CHARACTER[c] = true;
    }
    for (int c = 'A'; c <= 'Z'; c++) {
      IS_UPPER_CASE_CHARACTER[c] = true;
      IS_NAME_CHARACTER[c] = true;
    }
    for (int c = '0'; c <= '9'; c++) {
      IS_DIGIT_CHARACTER[c] = true;
      IS_NAME_CHARACTER[c] = true;
      IS_NUMBER_CHARACTER[c] = true;
    }
    IS_NUMBER_CHARACTER['+'] = true;
    IS_NUMBER_CHARACTER['-'] = true;
    IS_NUMBER_CHARACTER['.'] = true;
    IS_NAME_CHARACTER['_'] = true;
    IS_WHITESPACE_CHARACTER[0] = true;
    IS_WHITESPACE_CHARACTER[' '] = true;
    IS_WHITESPACE_CHARACTER['\t'] = true;
    IS_WHITESPACE_CHARACTER['\n'] = true;
    IS_WHITESPACE_CHARACTER['\r'] = true;
    IS_WHITESPACE_CHARACTER['/'] = true;
  }

  private final byte[] buffer = new byte[4096];

  private int bufferIndex = 0;

  private int bufferLength = 0;

  private int columnNumber = 1;

  // private StringBuffer buffer = new StringBuffer();

  private int currentCharacter;

  private int currentColumnNumber = 0;

  private int currentLineNumber = 1;

  private Object eventType = START_DOCUMENT;

  private final String fileName;

  private final InputStream in;

  private int lineNumber = 0;

  private final Stack<Object> scopeStack = new Stack<Object>();

  private Object value;

  public OsnIterator(final File directory, final String fileName)
    throws IOException {
    this(fileName, new ObjectSetInputStream(directory, fileName));
  }

  public OsnIterator(final String fileName, final InputStream in)
    throws IOException {
    this.in = new BufferedInputStream(in);
    this.fileName = fileName;
    scopeStack.push(IN_DOCUMENT);
  }

  public OsnIterator(final ZipFile zipFile, final String fileName)
    throws IOException {
    this(fileName, new ObjectSetInputStream(zipFile, fileName));
  }

  private void checkStartCollection(final String name) throws IOException {
    skipWhitespace();
    if (!isNextCharacter('{')) {
      throw new IllegalStateException("Expecting a '{' to start a " + name);
    }
  }

  private Object checkStartObject() throws IOException {
    skipWhitespace();
    if (isNextCharacter('(')) {
      scopeStack.push(IN_OBJECT);
      return START_DEFINITION;
    } else {
      return UNKNOWN;
    }
  }

  public void close() throws IOException {
    in.close();
  }

  private Object findAttributeName() throws IOException {
    value = findLowerName(true);
    if (value == null) {
      return UNKNOWN;
    } else {
      skipWhitespace();
      if (isNextCharacter(':')) {
        scopeStack.push(IN_ATTRIBUTE);
        return START_ATTRIBUTE;
      } else {
        return UNKNOWN;
      }
    }
  }

  private String findClassName() throws IOException {
    final String className = findUpperName(true);
    // If the class name is fullowed by '::' get and return the schema name
    if (currentCharacter == ':' && getNextCharacter() == ':') {
      getNextCharacter();
      final String schemaName = findUpperName(false);
      return (className + "::" + schemaName).intern();
    } else {
      return className;
    }
  }

  private Object findEndCollection() throws IOException {
    if (isNextCharacter('}')) {
      final Object scope = scopeStack.pop();
      if (scope == IN_LIST) {
        return END_LIST;
      } else if (scope == IN_SET) {
        return END_SET;
      } else if (scope == IN_RELATION) {
        return END_RELATION;
      } else {
        return UNKNOWN;
      }
    } else {
      return UNKNOWN;
    }
  }

  private Object findEndObject() throws IOException {
    if (isNextCharacter(')')) {
      scopeStack.pop();
      return END_OBJECT;
    } else {
      return UNKNOWN;
    }
  }

  private Object findExpression() throws IOException {
    Object eventType = UNKNOWN;
    final int c = currentCharacter;
    if (IS_NUMBER_CHARACTER[c]) {
      eventType = processDigitString();
    } else if (c == '"') {
      eventType = processTextString();
    } else if (IS_LOWER_CASE_CHARACTER[c]) {
      final String name = findLowerName(true);
      if (name.equals("true")) {
        value = Boolean.TRUE;
        eventType = BOOLEAN_VALUE;
      } else if (name.equals("false")) {
        value = Boolean.FALSE;
        eventType = BOOLEAN_VALUE;
      } else if (name.equals("nil")) {
        value = null;
        eventType = NULL_VALUE;
      } else {
        value = name;
        eventType = ENUM_TAG;
      }
    } else if (IS_UPPER_CASE_CHARACTER[c]) {
      final String name = findClassName();
      if (name.equals("List")) {
        checkStartCollection(name);
        eventType = START_LIST;
        scopeStack.push(IN_LIST);
      } else if (name.equals("Set")) {
        checkStartCollection(name);
        eventType = START_SET;
        scopeStack.push(IN_SET);
      } else if (name.equals("Relation")) {
        checkStartCollection(name);
        eventType = START_RELATION;
        scopeStack.push(IN_RELATION);
      } else {
        value = name;
        eventType = checkStartObject();
        if (eventType == UNKNOWN) {
          throwParseError("Expecting a '('");
        }
      }
    }
    return eventType;
  }

  private String findLowerName(final boolean tokenStart) throws IOException {
    if (IS_LOWER_CASE_CHARACTER[currentCharacter]) {
      return findName(tokenStart);
    } else {
      return null;
    }
  }

  private String findName(final boolean tokenStart) throws IOException {
    if (tokenStart) {
      lineNumber = currentLineNumber;
      columnNumber = currentColumnNumber;
    }
    final StringBuffer name = new StringBuffer();
    int c = currentCharacter;
    while (c != -1 && IS_NAME_CHARACTER[c]) {
      name.append((char)c);
      c = getNextCharacter();
    }
    return name.toString().intern();
  }

  private Object findStartObject() throws IOException {
    value = findClassName();
    if (value == null) {
      return UNKNOWN;
    } else {
      return checkStartObject();
    }
  }

  private String findUpperName(final boolean tokenStart) throws IOException {
    if (IS_UPPER_CASE_CHARACTER[currentCharacter]) {
      return findName(tokenStart);
    } else {
      return null;
    }
  }

  public Boolean getBooleanValue() {
    if (value == null) {
      return null;
    } else if (value instanceof Boolean) {
      return (Boolean)value;

    } else {
      return Boolean.valueOf(value.toString());
    }
  }

  public double getDoubleValue() {
    return ((BigDecimal)value).doubleValue();
  }

  public Object getEventType() {
    return eventType;
  }

  public float getFloatValue() {
    return ((BigDecimal)value).floatValue();
  }

  public int getIntegerValue() {
    return ((BigDecimal)value).intValue();
  }

  private int getNextCharacter() {
    if (bufferIndex == bufferLength) {
      try {
        bufferLength = in.read(buffer);
      } catch (final IOException e) {
        return -1;
      }
      if (bufferLength == -1) {
        return -1;
      } else {
        bufferIndex = 0;
      }
    }
    currentCharacter = buffer[bufferIndex];
    bufferIndex++;

    // currentCharacter = in.read();
    // line.append((char)currentCharacter);
    currentColumnNumber++;
    return currentCharacter;
  }

  public String getPathValue() {
    final String name = getStringValue();
    if (name != null) {
      return PathCache.getName(name);
    } else {
      return null;
    }
  }

  public String getStringValue() {
    if (value != null) {
      return value.toString();
    }
    return null;
  }

  public Object getValue() {
    return value;
  }

  public boolean hasNext() {
    return true;
  }

  private boolean isNextCharacter(final int c) throws IOException {
    if (currentCharacter == c) {
      getNextCharacter();
      return true;
    } else {
      return false;
    }
  }

  public Object next() {
    try {
      if (skipWhitespace() == -1) {
        return END_DOCUMENT;
      }
      eventType = UNKNOWN;
      value = null;
      final Object scope = scopeStack.peek();
      if (scope == IN_DOCUMENT) {
        processDocument();
      } else if (scope == IN_OBJECT) {
        processObject();
      } else if (scope == IN_ATTRIBUTE) {
        processAttribute();
      } else if (scope == IN_LIST) {
        processList();
      } else if (scope == IN_SET) {
        eventType = findExpression();
        processSet();
      } else if (scope == IN_RELATION) {
        processRelation();
      }
      return eventType;
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public String nextAttributeName() {
    Object currentEventType = getEventType();
    if (currentEventType != OsnIterator.START_ATTRIBUTE) {
      currentEventType = next();
      if (currentEventType == OsnIterator.END_OBJECT) {
        return null;
      } else if (currentEventType != OsnIterator.START_ATTRIBUTE) {
        throwParseError("Excepecting an attribute name");
      }
    }
    return getStringValue();
  }

  public Boolean nextBooleanAttribute(final String name) {
    final String attributeName = nextAttributeName();
    if (attributeName == null || !attributeName.equals(name)) {
      throwParseError("Expecting attribute " + name);
    }
    return nextBooleanValue();
  }

  public Boolean nextBooleanValue() {
    if (eventType != OsnIterator.BOOLEAN_VALUE) {
      if (eventType == END_OBJECT) {
        return null;
      } else if (next() != OsnIterator.BOOLEAN_VALUE) {
        throwParseError("Excepecting an boolean value");
      }
    }
    return getBooleanValue();
  }

  public double nextDoubleAttribute(final String name) {
    final String attributeName = nextAttributeName();
    if (attributeName == null || !attributeName.equals(name)) {
      throwParseError("Expecting attribute " + name);
    }
    return nextDoubleValue();
  }

  public double nextDoubleValue() {
    if (eventType != OsnIterator.NUMERIC_VALUE) {
      if (eventType == END_OBJECT) {
        return 0;
      } else if (next() != OsnIterator.NUMERIC_VALUE) {
        throwParseError("Excepecting an numeric value");
      }
    }
    return getDoubleValue();
  }

  public void nextEndObject() {
    if (next() != OsnIterator.END_OBJECT) {
      throwParseError("Expecting End Object");
    }
  }

  public int nextIntValue() {
    if (eventType != OsnIterator.NUMERIC_VALUE) {
      if (eventType == END_OBJECT) {
        return 0;
      } else if (next() != OsnIterator.NUMERIC_VALUE) {
        throwParseError("Excepecting an numeric value");
      }
    }
    return getIntegerValue();
  }

  public String nextObjectName() {
    Object currentEventType = getEventType();
    if (currentEventType != OsnIterator.START_DEFINITION) {
      if (currentEventType == END_OBJECT) {
        return null;
      } else {
        currentEventType = next();
        if (currentEventType == OsnIterator.END_OBJECT) {
          return null;
        } else if (currentEventType != OsnIterator.START_DEFINITION) {
          throwParseError("Excepecting an attribute name");
        }
      }
    }
    return getPathValue();
  }

  public String nextStringAttribute(final String name) {
    final String attributeName = nextAttributeName();
    if (attributeName == null) {
      return null;
    } else if (!attributeName.equals(name)) {
      throwParseError("Expecting attribute " + name);
    }
    return nextStringValue();
  }

  public String nextStringValue() {
    if (eventType != OsnIterator.TEXT_VALUE
      && eventType != OsnIterator.ENUM_TAG) {
      if (eventType == END_OBJECT) {
        return null;
      } else if (next() != OsnIterator.TEXT_VALUE
        && eventType != OsnIterator.ENUM_TAG) {
        throwParseError("Excepecting an text value");
      }
    }
    return getStringValue();
  }

  public Object nextValue() {
    if (eventType != OsnIterator.BOOLEAN_VALUE
      && eventType != OsnIterator.NUMERIC_VALUE
      && eventType != OsnIterator.TEXT_VALUE
      && eventType != OsnIterator.ENUM_TAG) {
      if (eventType == END_OBJECT) {
        return null;
      } else if (next() != OsnIterator.TEXT_VALUE
        && eventType != OsnIterator.NUMERIC_VALUE
        && eventType != OsnIterator.BOOLEAN_VALUE
        && eventType != OsnIterator.ENUM_TAG) {
        throwParseError("Excepecting a value");
      }
    }
    return getValue();
  }

  private void processAttribute() throws IOException {
    scopeStack.pop();
    eventType = findExpression();
    if (eventType == UNKNOWN) {
      throwParseError("Expecting an expression");
    }
  }

  private Object processDigitString() throws IOException {
    final StringBuffer number = new StringBuffer();
    int c = currentCharacter;
    while (IS_NUMBER_CHARACTER[(char)c]) {
      number.append((char)c);
      c = getNextCharacter();
    }
    if (number.length() > 0) {
      setNextToken(new BigDecimal(number.toString()));
      eventType = NUMERIC_VALUE;
    }
    return eventType;
  }

  private void processDocument() throws IOException {
    eventType = findStartObject();
    if (eventType == UNKNOWN) {
      throwParseError("Expecting start of an object definition");
    }
  }

  private void processList() throws IOException {
    eventType = findExpression();
    if (eventType == UNKNOWN) {
      eventType = findEndCollection();
      if (eventType == UNKNOWN) {
        throwParseError("Expecting an expression or end of a list");
      }
    }
  }

  private void processObject() throws IOException {
    skipWhitespace();
    eventType = findAttributeName();
    if (eventType == UNKNOWN) {
      skipWhitespace();
      eventType = findEndObject();
      if (eventType == UNKNOWN) {
        throwParseError("Expecting start of an attribute definition or end of object definition");
      }
    }
  }

  private void processRelation() throws IOException {
    eventType = findStartObject();
    if (eventType == UNKNOWN) {
      eventType = findEndCollection();
      if (eventType == UNKNOWN) {
        throwParseError("Expecting an expression or end of a relation");
      }
    }
  }

  private void processSet() throws IOException {
    if (eventType == UNKNOWN) {
      eventType = findEndCollection();
      if (eventType == UNKNOWN) {
        throwParseError("Expecting an expression or end of a set");
      }
    }
  }

  private Object processTextString() throws IOException {
    lineNumber = currentLineNumber;
    columnNumber = currentColumnNumber;

    final StringBuffer text = new StringBuffer();
    char c = (char)getNextCharacter();
    while (c != '"') {
      if (c == '\\') {
        text.append((char)getNextCharacter());
      } else {
        text.append(c);
      }
      c = (char)getNextCharacter();
    }

    if (text.length() > 0 && text.charAt(text.length() - 1) == '\n') {
      text.deleteCharAt(text.length() - 1);
    }
    final String string = text.toString();
    setNextToken(string);
    eventType = TEXT_VALUE;
    getNextCharacter();
    return eventType;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  private void setNextToken(final Object token) {
    value = token;
  }

  public int skipWhitespace() {
    int c = currentCharacter;
    while (c != -1 && IS_WHITESPACE_CHARACTER[c]) {
      if (c == '\n') {
        // line.setLength(0);
        currentLineNumber++;
        currentColumnNumber = 1;
        c = getNextCharacter();
      } else if (c == '/') {
        c = getNextCharacter();
        if (c == '/') {
          do {
            c = getNextCharacter();
          } while (c != -1 && c != '\n');
        } else {
          return currentCharacter;
        }
      } else {
        c = getNextCharacter();
      }
    }
    return c;
  }

  public void throwParseError(final String message) {
    final int startIndex = Math.max(bufferIndex - 40, 0);
    final int endIndex = Math.min(80, bufferLength - 1 - startIndex);
    throw new ParseException(toString(), message + " got '"
      + (char)currentCharacter + "' context="
      + new String(buffer, startIndex, endIndex));
  }

  @Override
  public String toString() {
    return fileName + "[" + lineNumber + "," + columnNumber + "]";
  }
}
