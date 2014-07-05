package com.revolsys.io.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.io.RecordIterator;
import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.property.AttributeProperties;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.io.FileUtil;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.ExceptionUtil;

public class CsvRecordIterator extends AbstractIterator<Record>
  implements RecordIterator {

  private final char fieldSeparator;

  private String pointXAttributeName;

  private String pointYAttributeName;

  private String geometryColumnName;

  private DataType geometryType;

  private Integer geometrySrid;

  private GeometryFactory geometryFactory;

  private RecordFactory dataObjectFactory;

  /** The reader to */
  private BufferedReader in;

  /** The metadata for the data being read by this iterator. */
  private RecordDefinition metaData;

  private Resource resource;

  private boolean hasPointFields;

  public CsvRecordIterator(final Resource resource) {
    this(resource, new ArrayRecordFactory(), CsvConstants.FIELD_SEPARATOR);
  }

  public CsvRecordIterator(final Resource resource,
    final char fieldSeparator) {
    this(resource, new ArrayRecordFactory(), fieldSeparator);
  }

  public CsvRecordIterator(final Resource resource,
    final RecordFactory dataObjectFactory) {
    this(resource, dataObjectFactory, CsvConstants.FIELD_SEPARATOR);
  }

  public CsvRecordIterator(final Resource resource,
    final RecordFactory dataObjectFactory, final char fieldSeparator) {
    this.resource = resource;
    this.dataObjectFactory = dataObjectFactory;
    this.fieldSeparator = fieldSeparator;
  }

  private void createMetaData(final String[] fieldNames) throws IOException {
    final List<Attribute> attributes = new ArrayList<Attribute>();
    Attribute geometryAttribute = null;
    for (final String name : fieldNames) {
      DataType type = DataTypes.STRING;
      if (name.equals(geometryColumnName)) {
        type = DataTypes.GEOMETRY;
      }
      final Attribute attribute = new Attribute(name, type, false);
      if (name.equals(geometryColumnName)) {
        geometryAttribute = attribute;
      }
      attributes.add(attribute);
    }
    hasPointFields = StringUtils.hasText(pointXAttributeName)
      && StringUtils.hasText(pointYAttributeName);
    if (geometryColumnName != null || hasPointFields) {
      if (hasPointFields) {
        geometryType = DataTypes.POINT;
      } else {
        pointXAttributeName = null;
        pointYAttributeName = null;
      }
      if (!StringUtils.hasText(geometryColumnName)) {
        geometryColumnName = "GEOMETRY";
      }
      if (geometryAttribute == null) {
        geometryAttribute = new Attribute(geometryColumnName, geometryType,
          true);
        attributes.add(geometryAttribute);
      }
      if (geometryFactory == null) {
        geometryFactory = GeometryFactory.wgs84();
      }
      geometryAttribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
        geometryFactory);
    }
    final String filename = FileUtil.getBaseName(resource.getFilename());
    metaData = new RecordDefinitionImpl(filename, getProperties(), attributes);
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  protected void doClose() {
    FileUtil.closeSilent(in);
    dataObjectFactory = null;
    geometryFactory = null;
    in = null;
    metaData = null;
    resource = null;
  }

  @Override
  protected void doInit() {
    try {
      pointXAttributeName = getProperty("pointXAttributeName");
      pointYAttributeName = getProperty("pointYAttributeName");
      geometryColumnName = getProperty("geometryColumnName");
      geometrySrid = StringConverterRegistry.toObject(DataTypes.INT,
        getProperty("geometrySrid"));
      if (geometrySrid != null) {
        geometryFactory = GeometryFactory.floating3(geometrySrid);
      }
      final DataType geometryType = DataTypes.getType((String)getProperty("geometryType"));
      if (Geometry.class.isAssignableFrom(geometryType.getJavaClass())) {
        this.geometryType = geometryType;
      } else {
        this.geometryType = DataTypes.GEOMETRY;
      }
      geometryFactory = getProperty("geometryFactory");

      this.in = new BufferedReader(
        FileUtil.createUtf8Reader(this.resource.getInputStream()));
      final String[] line = readNextRecord();
      createMetaData(line);
    } catch (final IOException e) {
      ExceptionUtil.log(getClass(), "Unable to open " + resource, e);
    }
  }

  @Override
  public RecordDefinition getMetaData() {
    return metaData;
  }

  @Override
  protected Record getNext() {
    try {
      final String[] record = readNextRecord();
      if (record != null && record.length > 0) {
        return parseRecord(record);
      } else {
        throw new NoSuchElementException();
      }
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
    if (nextLine == null) {
      throw new NoSuchElementException();
    }
    return nextLine;
  }

  /**
   * Parse a record containing an array of String values into a Record with
   * the strings converted to the objects based on the attribute data type.
   * 
   * @param record The record.
   * @return The Record.
   */
  private Record parseRecord(final String[] record) {
    final Record object = dataObjectFactory.createRecord(metaData);
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      String value = null;
      if (i < record.length) {
        value = record[i];
        if (value != null) {
          final DataType dataType = metaData.getAttributeType(i);
          final Object convertedValue = StringConverterRegistry.toObject(
            dataType, value);
          object.setValue(i, convertedValue);
        }
      }
    }
    if (hasPointFields) {
      final Double x = CollectionUtil.getDouble(object, pointXAttributeName);
      final Double y = CollectionUtil.getDouble(object, pointYAttributeName);
      if (x != null && y != null) {
        final Geometry geometry = geometryFactory.point(x, y);
        object.setGeometryValue(geometry);
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
          if (c == CsvConstants.QUOTE_CHARACTER) {
            hadQuotes = true;
            if (inQuotes && line.length() > (i + 1)
              && line.charAt(i + 1) == CsvConstants.QUOTE_CHARACTER) {
              sb.append(line.charAt(i + 1));
              i++;
            } else {
              inQuotes = !inQuotes;
              if (i > 2 && line.charAt(i - 1) != fieldSeparator
                && line.length() > (i + 1)
                && line.charAt(i + 1) != fieldSeparator) {
                sb.append(c);
              }
            }
          } else if (c == fieldSeparator && !inQuotes) {
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

  /**
   * Reads the next line from the buffer and converts to a string array.
   * 
   * @return a string array with each comma-separated element as a separate
   *         entry.
   * @throws IOException if bad things happen during the read
   */
  private String[] readNextRecord() throws IOException {
    final String nextLine = getNextLine();
    return parseLine(nextLine, true);
  }
}
