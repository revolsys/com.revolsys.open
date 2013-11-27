package com.revolsys.io.ecsv;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.ecsv.type.EcsvFieldType;
import com.revolsys.io.ecsv.type.EcsvFieldTypeRegistry;

public class EcsvDataObjectIterator extends AbstractIterator<DataObject>
  implements DataObjectIterator, EcsvConstants {
  private static final Logger LOG = Logger.getLogger(EcsvDataObjectIterator.class);

  private final DataObjectFactory dataObjectFactory;

  private EcsvFieldTypeRegistry fieldTypeRegistry = EcsvFieldTypeRegistry.INSTANCE;

  /** The reader to */
  private BufferedReader in;

  /** The metadata for the data being read by this iterator. */
  private DataObjectMetaData metaData;

  private final Map<String, DataObjectMetaData> metaDataMap = new HashMap<String, DataObjectMetaData>();

  /**
   * The current record number.
   */
  private int recordCount = 0;

  private final Resource resource;

  public EcsvDataObjectIterator(final Resource resource) {
    this(resource, new ArrayDataObjectFactory());
  }

  /**
   * Constructs CSVReader with supplied separator and quote char.
   * 
   * @param reader
   * @throws IOException
   */
  public EcsvDataObjectIterator(final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    this.resource = resource;
    this.dataObjectFactory = dataObjectFactory;
  }

  private void addMetaData(final Map<String, Object> map) {
    final String typePath = (String)map.get(NAME);
    final List<Attribute> attributes = new ArrayList<Attribute>();
    final Map<String, List<?>> attributeProperties = (Map<String, List<?>>)map.get(ATTRIBUTE_PROPERTIES);
    if (attributeProperties != null) {
      final List<?> names = attributeProperties.get(EcsvProperties.ATTRIBUTE_NAME);
      if (names != null) {
        for (int i = 0; i < names.size(); i++) {
          final String name = (String)names.get(i);
          final String attributeTypeName = getAttributeHeader(
            attributeProperties, EcsvProperties.ATTRIBUTE_TYPE, i);
          final Integer attributeLength = getAttributeHeader(
            attributeProperties, EcsvProperties.ATTRIBUTE_LENGTH, i);
          final Integer attributeScale = getAttributeHeader(
            attributeProperties, EcsvProperties.ATTRIBUTE_SCALE, i);
          final Boolean attributeTypeRequired = getAttributeHeader(
            attributeProperties, EcsvProperties.ATTRIBUTE_REQUIRED, i);
          DataType type = null;
          if (attributeTypeName != null) {
            type = DataTypes.getType(attributeTypeName);
          }
          if (type == null) {
            type = DataTypes.STRING;
          }
          attributes.add(new Attribute(name, type, attributeLength,
            attributeScale, attributeTypeRequired));
        }
      }
    }
    final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(
      typePath, getProperties(), attributes);
    metaDataMap.put(typePath, metaData);
  }

  /**
   * Closes the underlying reader.
   * 
   * @throws IOException if the close fails
   */
  @Override
  protected void doClose() {
    FileUtil.closeSilent(in);
  }

  @Override
  protected void doInit() {
    if (in == null) {
      try {
        this.in = new BufferedReader(
          FileUtil.createUtf8Reader(resource.getInputStream()));
        readFileProperties();
        readRecordHeader();
        final GeometryFactory geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
        fieldTypeRegistry = new EcsvFieldTypeRegistry(geometryFactory);
      } catch (final IOException e) {
        throw new RuntimeException("Unable to read file " + resource, e);
      }
    }
  }

  private <V> V getAttributeHeader(
    final Map<String, List<?>> attributeProperties, final String attributeType,
    final int i) {
    final List<?> values = attributeProperties.get(attributeType);
    if (values == null) {
      return null;
    } else {
      return (V)values.get(i);
    }
  }

  @Override
  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  @Override
  protected DataObject getNext() {
    try {
      final String[] record = readNextRecord();
      if (record != null && record.length > 0) {
        if (record.length > 1 || !record[0].equals(MULTI_LINE_LIST_END)) {
          return parseDataObject(record);
        }
      }
      throw new NoSuchElementException();
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Reads the next line from the file.
   * 
   * @return the next line from the file without trailing newline
   * @throws IOException if bad things happen during the read
   */
  private String getNextLine() throws IOException {
    final String nextLine = in.readLine();
    return nextLine;
  }

  private String getTypeNameParameter(String type) {
    type = type.substring(type.indexOf(TYPE_PARAMETER_START) + 1,
      type.indexOf(TYPE_PARAMETER_END));
    return type;
  }

  private boolean isBlockEnd(final String[] record, final String endCharacter) {
    if (record != null) {
      if (record.length > 1) {
        return false;
      } else if (record.length == 1) {
        if (!record[0].equals(endCharacter)) {
          return false;
        }
      }
    }
    return true;
  }

  private Object parseCommaList(final DataType dataType, String[] record)
    throws IOException {
    final List<Object> values = new ArrayList<Object>();
    int offset = 3;
    do {
      for (int i = offset; i < record.length; i++) {
        final String text = record[i];
        if (text.equals(COLLECTION_END)) {
          return values;
        } else {
          final Object value = parseValue(dataType, text, null);
          values.add(value);
        }
      }
      record = readNextRecord();
      offset = 0;
    } while (record != null);
    return values;
  }

  /**
   * Parse a record containing an array of String values into a DataObject with
   * the strings converted to the objects based on the attribute data type.
   * 
   * @param record The record.
   * @return The DataObject.
   */
  private DataObject parseDataObject(final String[] record) {
    recordCount++;
    final DataObject object = dataObjectFactory.createDataObject(metaData);
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      final DataType type = metaData.getAttributeType(i);
      String string = null;
      if (i < record.length) {
        string = record[i];
      }
      try {
        final Object value = parseValue(type, string, null);
        if (value != null) {
          object.setValue(i, value);
        }
      } catch (final Throwable e) {
        LOG.error(
          "Value " + string + " invalid for field "
            + metaData.getAttributeName(i) + " for record " + recordCount, e);
      }
    }
    return object;
  }

  /**
   * Parses an incoming String and returns an array of elements.
   * 
   * @param nextLine the string to parse
   * @return the comma-tokenized list of elements, or null if nextLine is null
   * @throws IOException if bad things happen during the read
   */
  private String[] parseLine(final String nextLine, final boolean readLine)
    throws IOException {
    String line = nextLine;
    if (line.length() == 0) {
      return new String[0];
    } else {

      final List<String> fields = new ArrayList<String>();
      StringBuffer sb = new StringBuffer();
      boolean inQuotes = false;
      boolean hadQuotes = false;
      do {
        if (inQuotes && readLine) {
          sb.append("\n");
          line = getNextLine();
          if (line == null) {
            break;
          }
        }
        for (int i = 0; i < line.length(); i++) {
          final char c = line.charAt(i);
          if (c == DOUBLE_QUOTE) {
            hadQuotes = true;
            if (inQuotes && line.length() > (i + 1)
              && line.charAt(i + 1) == DOUBLE_QUOTE) {
              sb.append(line.charAt(i + 1));
              i++;
            } else {
              inQuotes = !inQuotes;
              if (i > 2 && line.charAt(i - 1) != FIELD_SEPARATOR
                && line.length() > (i + 1)
                && line.charAt(i + 1) != FIELD_SEPARATOR) {
                sb.append(c);
              }
            }
          } else if (c == FIELD_SEPARATOR && !inQuotes) {
            hadQuotes = false;
            if (hadQuotes || sb.length() > 0) {
              fields.add(sb.toString());
            } else {
              fields.add(null);
            }
            sb = new StringBuffer();
          } else {
            sb.append(c);
          }
        }
      } while (inQuotes);
      if (sb.length() > 0 || fields.size() > 0) {
        if (hadQuotes || sb.length() > 0) {
          fields.add(sb.toString());
        } else {
          fields.add(null);
        }
      }
      return fields.toArray(new String[0]);
    }
  }

  @SuppressWarnings("unchecked")
  private Object parseValue(final DataType type, final String text,
    final String[] record) throws IOException {
    if (type.equals(DataTypes.MAP)) {
      return readMap();
    } else {
      final EcsvFieldType fieldType = fieldTypeRegistry.getFieldType(type);
      return fieldType.parseValue(text);
    }
  }

  private Object parseValue(final String[] record) throws IOException {
    final String type = record[1];
    final String text = record[2];
    if (type.startsWith("List<")) {
      final String typePath = type.substring(5, type.indexOf(">"));
      final DataType dataType = DataTypes.getType(typePath);
      if (text.equals(COLLECTION_START)) {
        return parseCommaList(dataType, record);
      } else {
        // return parseMultiLineList(typePath, record);
        return null;
      }
    } else {
      final DataType dataType = DataTypes.getType(type);
      final Object value = parseValue(dataType, text, record);
      return value;
    }
  }

  private void readFileProperties() throws IOException {
    String[] record = readNextRecord();
    while (record != null) {
      if (record.length >= 3) {
        final String name = record[0];
        if (name.equals(RECORDS)) {
          final String typePath = getTypeNameParameter(record[1]);
          metaData = metaDataMap.get(typePath);
          if (metaData == null) {
            throw new IllegalArgumentException("No typeDefinition for "
              + typePath);
          }
          return;
        } else if (name.equals(TYPE_DEFINITION)) {
          final Object value = parseValue(record);
          addMetaData((Map<String, Object>)value);

        } else {
          final Object value = parseValue(record);
          setProperty(name, value);
        }
      } else if (record.length < 3) {
        throw new IllegalArgumentException(
          "Expecting a name, type, value triplet not" + Arrays.toString(record));
      }
      record = readNextRecord();
    }
    if (metaData == null) {
      if (metaDataMap.size() == 1) {
        metaData = metaDataMap.values().iterator().next();
      }
    }
  }

  private Object readMap() throws IOException {
    final Map<String, Object> properties = new LinkedHashMap<String, Object>();
    String[] record = readNextRecord();
    while (!isBlockEnd(record, MAP_END)) {
      if (record.length == 3) {
        final String name = record[0];
        final Object value = parseValue(record);
        properties.put(name, value);
      }
      record = readNextRecord();
    }
    return properties;
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   * 
   * @return a string array with each comma-separated element as a separate
   *         entry.
   * @throws IOException if bad things happen during the read
   */
  private String[] readNextRecord() throws IOException {
    final String nextLine = getNextLine();
    if (nextLine == null) {
      return null;
    } else {
      return parseLine(nextLine, true);
    }
  }

  /**
   * Read the record header block.
   * 
   * @throws IOException If there was an error reading the header.
   */
  private void readRecordHeader() throws IOException {
    // if (hasNext) {
    // int headerIndex = 0;
    // for (String[] line = readNextRecord(); line != null && line.length > 0;
    // line = readNextRecord()) {
    // final List<String> attributeHeaderTypeNames =
    // getProperty(EcsvProperties.ATTRIBUTE_HEADER_TYPES);
    // final List<String> attributeHeaderTypes = parseListValue(
    // attributeHeaderTypeNames, DataTypes.QNAME);
    // if (headerIndex < attributeHeaderTypes.size()) {
    // final String headerType = attributeHeaderTypes.get(headerIndex);
    // attributeHeaderValues.put(headerType, Arrays.asList(line));
    // headerIndex++;
    // }
    // }
    // // createMetaData();
    // }
  }

  /**
   * Removing items from the iterator is not supported.
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
