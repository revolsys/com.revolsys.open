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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.EnumerationDataType;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.io.saif.SaifConstants;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class OsnSerializer {
  private static final String ATTRIBUTE_SCOPE = "attribute";

  private static final String COLLECTION_SCOPE = "collection";

  private static final String DATE = "/Date";

  private static final String DOCUMENT_SCOPE = "document";

  private static final Logger LOG = LoggerFactory.getLogger(OsnSerializer.class);

  private static final String SPATIAL_OBJECT = "/SpatialObject";

  private final OsnConverterRegistry converters;

  private boolean endElement = false;

  private File file;

  private String indent = "";

  private boolean indentEnabled = true;

  private short index = 0;

  private final String lineSeparator;

  private long maxSize = Long.MAX_VALUE;

  private OutputStream out;

  private final String prefix;

  private final LinkedList<Object> scope = new LinkedList<Object>();

  private int size = 0;

  private final String path;

  public OsnSerializer(final String path, final File file, final long maxSize,
    final OsnConverterRegistry converters) throws IOException {
    this.path = path;
    this.file = file;
    this.maxSize = maxSize;
    this.converters = converters;
    prefix = ObjectSetUtil.getObjectSubsetPrefix(file);
    openFile();
    scope.addLast(DOCUMENT_SCOPE);
    lineSeparator = "\r\n";
  }

  public void attribute(final String name, final double value,
    final boolean endLine) throws IOException {
    attribute(name, new BigDecimal(value), endLine);
  }

  public void attribute(final String name, final Object value,
    final boolean endLine) throws IOException {
    attributeName(name);
    attributeValue(value);
    if (endLine || indentEnabled) {
      endLine();
    }

  }

  public void attributeEnum(final String name, final String value,
    final boolean endLine) throws IOException {
    attributeName(name);
    write(value);
    endAttribute();
    if (endLine || indentEnabled) {
      endLine();
    }

  }

  public void attributeName(final String name) throws IOException {
    endElement = false;
    serializeIndent();
    write(name + ":");
    scope.addLast(ATTRIBUTE_SCOPE);
  }

  public void attributeValue(final Object value) throws IOException {
    serializeValue(value);
    endAttribute();
  }

  public void close() throws IOException {
    while (!scope.isEmpty()) {
      final Object scope = this.scope.getLast();
      if (scope == COLLECTION_SCOPE) {
        endCollection();
      } else if (scope == ATTRIBUTE_SCOPE) {
        if (indentEnabled) {
          endLine();
        }
        this.scope.removeLast();
      } else if (scope != DOCUMENT_SCOPE
        && (scope instanceof DataObject || scope instanceof String)) {
        endObject();
      } else {
        if (indentEnabled) {
          endLine();
        }
        this.scope.removeLast();
      }
    }
    write('\n');
    out.close();
  }

  private void decreaseIndent() {
    if (indentEnabled) {
      indent = indent.substring(1);
    }
  }

  public void endAttribute() {
    scope.removeLast();
  }

  public void endCollection() throws IOException {
    endElement = true;
    decreaseIndent();
    serializeIndent();
    write('}');
    if (indentEnabled) {
      endLine();
    }
    endAttribute();
  }

  public void endLine() throws IOException {
    write(lineSeparator);
  }

  public void endObject() throws IOException {
    endElement = true;
    decreaseIndent();
    serializeIndent();
    write(')');
    if (indentEnabled) {
      endLine();
    }
    endAttribute();
  }

  private void increaseIndent() {
    if (indentEnabled) {
      indent += '\t';
    }
  }

  public boolean isIndentEnabled() {
    return indentEnabled;
  }

  private void openFile() throws IOException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Creating object subset '" + file.getName() + "'");
    }
    out = new BufferedOutputStream(new FileOutputStream(file), 4096);
  }

  private void openNextFile() throws IOException {
    out.flush();
    out.close();
    index++;
    final String fileName = ObjectSetUtil.getObjectSubsetName(prefix, index);
    file = new File(file.getParentFile(), fileName);
    size = 0;
    openFile();
  }

  public void serialize(final DataObject object) throws IOException {
    serializeStartObject(object);
    serializeAttributes(object);
    endObject();
  }

  private void serialize(final Date date) throws IOException {
    startObject(DATE);
    if (date.equals(DateConverter.NULL_DATE)) {
      attribute("year", new BigDecimal(0), false);
    } else {
      final GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(date);
      final int day = cal.get(Calendar.DAY_OF_MONTH);

      if (day < 10) {
        attributeName("day");
        write("0" + day);
        endAttribute();
        endLine();
      } else {
        attribute("day", new BigDecimal(day), true);
      }

      final int month = cal.get(Calendar.MONTH) + 1;
      if (month < 10) {
        attributeName("month");
        write("0" + month);
        endAttribute();
        endLine();
      } else {
        attribute("month", new BigDecimal(month), true);
      }

      final int year = cal.get(Calendar.YEAR);
      attribute("year", new BigDecimal(year), true);
    }
    endObject();
  }

  private void serialize(final Geometry geometry) throws IOException {
    final String type = (String)JtsGeometryUtil.getGeometryProperty(geometry,
      "type");
    OsnConverter converter = converters.getConverter(type);
    if (converter == null) {
      if (geometry instanceof Point) {
        if (converter == null) {
          converter = converters.getConverter(SaifConstants.POINT);
        }

      } else if (geometry instanceof LineString) {
        if (converter == null) {
          converter = converters.getConverter(SaifConstants.ARC);
        }
      }
    }
    converter.write(this, geometry);
  }

  public void serialize(final List<Object> list) throws IOException {
    serializeCollection("List", list);
  }

  public void serialize(final Set<Object> set) throws IOException {
    serializeCollection("Set", set);
  }

  public void serialize(final String string) throws IOException {
    write('"');
    String escapedString = string.replaceAll("\\\\", "\\\\\\\\");
    escapedString = escapedString.replaceAll("(\\\\)?\\x22", "\\\\\"");
    write(escapedString);
    write('"');
  }

  public void serializeAttribute(final String name, final Object value)
    throws IOException {
    attributeName(name);
    if ((value instanceof Geometry) && name.equals("position")) {
      startObject(SPATIAL_OBJECT);
      attributeName("geometry");
      attributeValue(value);
      endAttribute();
      endObject();
    } else {
      attributeValue(value);
    }
  }

  public void serializeAttributes(final DataObject object) throws IOException {
    final DataObjectMetaData type = object.getMetaData();
    final int attributeCount = type.getAttributeCount();
    for (int i = 0; i < attributeCount; i++) {
      final Object value = object.getValue(i);
      if (value != null) {
        final String name = type.getAttributeName(i);
        final DataType dataType = type.getAttributeType(i);
        if (dataType instanceof EnumerationDataType) {
          attributeName(name);
          write(value.toString());
          endAttribute();
        } else {
          serializeAttribute(name, value);
        }
        if (indentEnabled) {
          endLine();
        }
        if (!endElement) {
          if (i < attributeCount - 1) {
            endLine();
          } else if (type.getTypeName().equals("/Coord3D")) {
            for (final Iterator<Object> scopes = scope.iterator(); scopes.hasNext();) {
              final Object parent = scopes.next();
              if (parent instanceof DataObject) {
                final DataObject parentObject = (DataObject)parent;
                if (parentObject.getMetaData()
                  .getTypeName()
                  .equals(SaifConstants.TEXT_ON_CURVE)) {
                  endLine();
                }
              }
            }
          } else {
            endLine();
          }
        }
      }
    }
  }

  private void serializeCollection(final String name,
    final Collection<Object> collection) throws IOException {
    startCollection(name);
    for (final Object value : collection) {
      serializeValue(value);
      if (indentEnabled || !endElement) {
        endLine();
      }
    }
    endCollection();
  }

  public void serializeDataObject(final DataObject object) throws IOException {
    if (size >= maxSize) {
      openNextFile();
      size = 0;
    }
    serialize(object);
  }

  public void serializeIndent() throws IOException {
    if (indentEnabled) {
      write(indent);
    }
  }

  public void serializeStartObject(final DataObject object) throws IOException {
    final DataObjectMetaData type = object.getMetaData();
    final String path = type.getPath();
    startObject(path);
  }

  @SuppressWarnings("unchecked")
  public void serializeValue(final Object value) throws IOException {
    if (scope.getLast() == COLLECTION_SCOPE) {
      serializeIndent();
    }
    if (value == null) {
      write("nil");
    } else {
      if (value instanceof List) {
        serialize((List<Object>)value);
      } else if (value instanceof Set) {
        serialize((Set<Object>)value);
      } else if (value instanceof String) {
        serialize((String)value);
      } else if (value instanceof DataObject) {
        serialize((DataObject)value);
      } else if (value instanceof Date) {
        serialize((Date)value);
      } else if (value instanceof Geometry) {
        final Geometry geometry = (Geometry)value;
        serialize(geometry);
      } else {
        write(value.toString());
      }
    }
  }

  public void setIndentEnabled(final boolean indentEnabled) {
    this.indentEnabled = indentEnabled;
  }

  public void startCollection(final String name) throws IOException {
    endElement = false;
    write(name);
    write('{');
    if (indentEnabled) {
      endLine();
    }
    increaseIndent();
    scope.addLast(COLLECTION_SCOPE);
  }

  public void startObject(final String path) throws IOException {
    endElement = false;
    final String[] elements = path.replaceAll("^/+", "").split("/");
    if (elements.length == 1) {
      write(elements[0]);
    } else {
      final String typeName = elements[1];
      final String schema = elements[0];
      write(typeName);
      write("::");
      write(schema);
    }
    write('(');
    if (indentEnabled) {
      endLine();
    }
    increaseIndent();
    scope.addLast(path);
  }

  @Override
  public String toString() {
    return path.toString();
  }

  public void write(final byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  public void write(final byte[] b, final int off, final int len)
    throws IOException {
    out.write(b, off, len);
    size += len;
  }

  public void write(final int b) throws IOException {
    out.write(b);
    size += 1;
  }

  public void write(final String s) throws IOException {
    final byte[] bytes = s.getBytes();
    write(bytes, 0, bytes.length);
  }
}
