/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.server.core.deserializer.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Header implements Iterable<HeaderField>, Cloneable {
  public static List<String> splitValuesByComma(final String headerValue) {
    final List<String> singleValues = new ArrayList<>();

    final String[] parts = headerValue.split(",");
    for (final String value : parts) {
      singleValues.add(value.trim());
    }

    return singleValues;
  }

  private Map<String, HeaderField> headers = new HashMap<>();

  private int lineNumber;

  public Header(final int lineNumer) {
    this.lineNumber = lineNumer;
  }

  public void addHeader(final String name, final List<String> values, final int lineNumber) {
    final HeaderField headerField = getHeaderFieldOrDefault(name, lineNumber);
    final List<String> headerValues = headerField.getValues();

    for (final String value : values) {
      if (!headerValues.contains(value)) {
        headerValues.add(value);
      }
    }
  }

  public void addHeader(final String name, final String value, final int lineNumber) {
    final HeaderField headerField = getHeaderFieldOrDefault(name, lineNumber);
    final List<String> headerValues = headerField.getValues();

    if (!headerValues.contains(value)) {
      headerValues.add(value);
    }
  }

  @Override
  public Header clone() throws CloneNotSupportedException {
    final Header clone = (Header)super.clone();
    clone.lineNumber = this.lineNumber;
    clone.headers = new HashMap<>();
    for (final Map.Entry<String, HeaderField> entries : this.headers.entrySet()) {
      clone.headers.put(entries.getKey(), entries.getValue().clone());
    }

    return clone;
  }

  public boolean exists(final String name) {
    final HeaderField field = this.headers.get(name.toLowerCase(Locale.ENGLISH));

    return field != null && !field.getValues().isEmpty();
  }

  public String getHeader(final String name) {
    final HeaderField headerField = getHeaderField(name);

    return headerField == null ? null : headerField.getValue();
  }

  public HeaderField getHeaderField(final String name) {
    return this.headers.get(name.toLowerCase(Locale.ENGLISH));
  }

  private HeaderField getHeaderFieldOrDefault(final String name, final int lineNumber) {
    HeaderField headerField = this.headers.get(name.toLowerCase(Locale.ENGLISH));
    if (headerField == null) {
      headerField = new HeaderField(name, lineNumber);
      this.headers.put(name.toLowerCase(Locale.ENGLISH), headerField);
    }

    return headerField;
  }

  public List<String> getHeaders(final String name) {
    final HeaderField headerField = getHeaderField(name);

    return headerField == null ? new ArrayList<>() : headerField.getValues();
  }

  public int getLineNumber() {
    return this.lineNumber;
  }

  @Override
  public Iterator<HeaderField> iterator() {
    return new Iterator<>() {
      Iterator<String> keyIterator = Header.this.headers.keySet().iterator();

      @Override
      public boolean hasNext() {
        return this.keyIterator.hasNext();
      }

      @Override
      public HeaderField next() {
        return Header.this.headers.get(this.keyIterator.next());
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public void removeHeader(final String name) {
    this.headers.remove(name.toLowerCase(Locale.ENGLISH));
  }

  public void replaceHeaderField(final HeaderField headerField) {
    this.headers.put(headerField.getFieldName().toLowerCase(Locale.ENGLISH), headerField);
  }

  public Map<String, List<String>> toMultiMap() {
    final Map<String, List<String>> singleMap = new HashMap<>();

    for (final Map.Entry<String, HeaderField> entries : this.headers.entrySet()) {
      final HeaderField field = entries.getValue();
      singleMap.put(field.getFieldName(), field.getValues());
    }

    return singleMap;
  }

  public Map<String, String> toSingleMap() {
    final Map<String, String> singleMap = new HashMap<>();

    for (final Map.Entry<String, HeaderField> entries : this.headers.entrySet()) {
      final HeaderField field = entries.getValue();
      singleMap.put(field.getFieldName(), getHeader(entries.getKey()));
    }

    return singleMap;
  }
}
