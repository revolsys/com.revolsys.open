/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/format/xbase/io/XbaseFileReader.java $
 * $Author:paul.austin@revolsys.com $
 * $Date:2007-06-09 09:28:28 -0700 (Sat, 09 Jun 2007) $
 * $Revision:265 $

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
package com.revolsys.gis.format.xbase.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.io.AbstractReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.io.DataObjectReader;
import com.revolsys.gis.io.EndianInput;
import com.revolsys.gis.io.EndianInputStream;
import com.revolsys.io.FileUtil;

public class XbaseFileReader extends AbstractReader<DataObject> implements
  DataObjectReader, com.revolsys.gis.data.io.DataObjectReader {
  private static final Logger log = Logger.getLogger(XbaseFileReader.class);

  private Map<String, Map<String, String>> attributeNames = new HashMap<String, Map<String, String>>();

  private DataObject currentDataObject;

  private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

  private final List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();

  private File file;

  private String filePrefix;

  private final boolean hasNext = true;

  private EndianInput in;

  private Date lastModifiedDate;

  private boolean loadNextObject = true;

  private RandomAccessFile memoIn;

  private DataObjectMetaData metaData;

  private int numRecords;

  private byte[] recordBuffer;

  private int recordSize;

  private String typeName;

  private int version;

  public XbaseFileReader() {
  }

  public XbaseFileReader(
    final File file) {
    this(file, null);
  }

  public XbaseFileReader(
    final File file,
    final String typeName) {
    setFile(file);
    setTypeName(typeName);
  }

  public void close() {
    try {
      in.close();
    } catch (final IOException e) {
      log.error(e.getMessage(), e);
    }
    try {
      if (memoIn != null) {
        memoIn.close();
      }
    } catch (final IOException e) {
      log.error(e.getMessage(), e);
    }

  }

  public String getAttributeName(
    final String typeName,
    final String columnName) {
    final Map<String, String> names = getAttributeNames(typeName);
    return names.get(columnName);
  }

  public Map<String, Map<String, String>> getAttributeNames() {
    return attributeNames;
  }

  public Map<String, String> getAttributeNames(
    final String typeName) {
    Map<String, String> names = attributeNames.get(typeName);
    if (names == null) {
      names = new HashMap<String, String>();
      attributeNames.put(typeName, names);
    }
    return names;
  }

  private Boolean getBoolean(
    final int startIndex) {
    final char c = (char)recordBuffer[startIndex];
    switch (c) {
      case 't':
      case 'T':
      case 'y':
      case 'Y':
        return Boolean.TRUE;

      case 'f':
      case 'F':
      case 'n':
      case 'N':
        return Boolean.FALSE;
      default:
        return null;
    }
  }

  private Date getDate(
    final int startIndex,
    final int len) {
    final String dateString = getString(startIndex, len);
    try {
      if (dateString == null || dateString.trim().length() == 0) {
        return null;
      } else {
        return new Date(dateFormat.parse(dateString.trim()).getTime());
      }
    } catch (final ParseException e) {
      throw new IllegalStateException("'" + dateString
        + "' is not a date in 'YYYYMMDD' format");
    }
  }

  public String getFilePrefix() {
    return filePrefix;
  }

  /**
   * Get the date the DBF file was last modified.
   * 
   * @return The last modification date.
   */
  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  private Object getMemo(
    final byte[] record,
    final int startIndex,
    final int len)
    throws IOException {
    final String memoIndexString = new String(record, startIndex, len).trim();
    if (memoIndexString.length() != 0) {
      final int memoIndex = Integer.parseInt(memoIndexString.trim());
      if (memoIn == null) {
        final File memoFile = new File(file.getParentFile(), typeName + ".dbt");
        if (memoFile.exists()) {
          if (log.isInfoEnabled()) {
            log.info("Opening memo file: " + memoFile);
          }
          memoIn = new RandomAccessFile(memoFile, "r");
        } else {
          return null;
        }
      }
      memoIn.seek(memoIndex * 512);
      final StringBuffer memo = new StringBuffer(512);

      final byte[] memoBuffer = new byte[512];
      while (memoIn.read(memoBuffer) != -1) {
        int i = 0;
        while (i < memoBuffer.length) {
          if (memoBuffer[i] == 0x1A) {
            return memo.toString();
          }
          memo.append((char)memoBuffer[i]);
          i++;
        }
      }
      return memo.toString();
    }
    return null;
  }

  public DataObjectMetaData getMetaData() {
    // TODO Auto-generated method stub
    return null;
  }

  private BigDecimal getNumber(
    final int startIndex,
    final int len) {
    BigDecimal number = null;
    final String numberString = getString(startIndex, len);
    if (numberString.length() != 0) {
      number = new BigDecimal(numberString.trim());
    }
    return number;
  }

  /**
   * Get the number of records in the DBF file.
   * 
   * @return The number of records in the DBF file.
   */
  public int getNumRecords() {
    return numRecords;
  }

  public String toString() {
    return file.getAbsolutePath();
  }

  private String getString(
    final int startIndex,
    final int len) {
    return new String(recordBuffer, startIndex, len).trim();
  }

  /**
   * Get the DBF file specification version.
   * 
   * @return The DBF file specification version.
   */
  public int getVersion() {
    return version;
  }

  public boolean hasNext() {
    if (!hasNext) {
      return false;
    } else if (loadNextObject) {
      return loadNextRecord() != null;
    } else {
      return true;
    }
  }

  public Iterator<DataObject> iterator() {
    open();
    return this;
  }

  protected DataObject loadDataObject()
    throws IOException {
    if (in.read(recordBuffer) != recordBuffer.length) {
      throw new IllegalStateException("Unexpected end of file");
    }
    currentDataObject = metaData.createDataObject();
    int startIndex = 0;
    for (final FieldDefinition field : fieldDefinitions) {
      final int len = field.getLength();
      Object value = null;

      switch (field.getType()) {
        case FieldDefinition.CHARACTER_TYPE:
          value = getString(startIndex, len);
        break;
        case FieldDefinition.FLOAT_TYPE:
        case FieldDefinition.NUMBER_TYPE:
          value = getNumber(startIndex, len);
        break;
        case FieldDefinition.LOGICAL_TYPE:
          value = getBoolean(startIndex);
        break;
        case FieldDefinition.DATE_TYPE:
          value = getDate(startIndex, len);
        break;
        case FieldDefinition.MEMO_TYPE:
          value = getMemo(recordBuffer, startIndex, len);
        break;
        default:
        break;
      }
      startIndex += len;
      final String fieldName = field.getName();
      String attributeName = getAttributeName(typeName, fieldName);
      if (attributeName == null) {
        attributeName = fieldName;
      }
      currentDataObject.setValue(attributeName, value);
    }
    loadNextObject = false;
    return currentDataObject;
  }

  private void loadHeader()
    throws IOException {
    version = in.read();
    final int year = in.read();
    final int month = in.read();
    final int day = in.read();
    lastModifiedDate = new Date(year, month, day);
    numRecords = in.readLEInt();
    in.readLEShort();
    recordSize = in.readLEShort();
    recordBuffer = new byte[recordSize - 1];

    in.skipBytes(2);
    in.read();
    in.read();
    in.skipBytes(12);
    in.read();
    in.read();
    in.skipBytes(2);
  }

  protected DataObject loadNextRecord() {
    try {
      int deleteFlag = ' ';
      do {
        deleteFlag = in.read();
        if (deleteFlag == -1) {
          return null;
        }
        if (deleteFlag == ' ') {
          return loadDataObject();
        } else if (deleteFlag != 0x1A) {
          skipDataObject();
        }
      } while (deleteFlag == '*');
      return null;
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected DataObjectMetaData loadSchema(
    final EndianInput in)
    throws IOException {
    final XbaseSchemaReader xbaseSchemaReader = new XbaseSchemaReader(in,
      typeName, fieldDefinitions);

    return xbaseSchemaReader.getMetaData();
  }

  public DataObject next() {
    if (hasNext()) {
      loadNextObject = true;
      return currentDataObject;
    } else {
      throw new NoSuchElementException();
    }
  }

  public void open() {
    if (in == null) {
      try {
        final FileInputStream fileIn = new FileInputStream(file);
        in = new EndianInputStream(fileIn);
        loadHeader();
        metaData = loadSchema(in);
      } catch (final IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  public void setAttributeName(
    final String typeName,
    final String columnName,
    final String attributeName) {
    final Map<String, String> names = getAttributeNames(typeName);
    names.put(columnName, attributeName);
  }

  public void setAttributeNames(
    final Map<String, Map<String, String>> attributeNames) {
    this.attributeNames = attributeNames;
  }

  public void setAttributeNames(
    final String typeName,
    final Map<String, String> attributeNames) {
    this.attributeNames.put(typeName, attributeNames);
  }

  public File getFile() {
    return file;
  }

  public void setFile(
    final File file) {
    this.file = file;
    filePrefix = FileUtil.getFileNamePrefix(file);
    if (typeName == null) {
      this.typeName = filePrefix;
    }
  }

  public void setTypeName(
    final String typeName) {
    if (typeName != null) {
      this.typeName = typeName;
    }
  }

  protected void skipDataObject()
    throws IOException {
    in.skipBytes(recordBuffer.length);
  }
}
