package com.revolsys.gis.ecsv.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.AbstractObjectWithProperties;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

public class EcsvIterator extends AbstractObjectWithProperties implements
  Iterator<DataObject> {
  private static final Logger LOG = Logger.getLogger(EcsvIterator.class);

  /** The values for each record header type. */
  private final Map<QName, List<String>> attributeHeaderValues = new LinkedHashMap<QName, List<String>>();

  /** The current database object. */
  private DataObject currentObject;

  private final DataObjectFactory dataObjectFactory;

  /** Date format for reading date time instances. */
  private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
    "yyyy-MM-dd'T'HH:mm:ss");

  /** An empty geometry. */
  private final Geometry emptyGeometry;

  /** A reader for WKT Geometry. */
  private final WKTReader geometryReader;

  /** Flag indicating if there are more records to be read. */
  private boolean hasNext = true;

  /** The reader to */
  private final BufferedReader in;

  /** The metadata for the data being read by this iterator. */
  private DataObjectMetaDataImpl metaData;

  /**
   * The current record number.
   */
  private int recordCount = 0;

  /**
   * Constructs CSVReader with supplied separator and quote char.
   * 
   * @param reader
   * @throws IOException
   */
  public EcsvIterator(
    final Reader in,
    final GeometryFactory geometryFactory,
    final DataObjectFactory dataObjectFactory)
    throws IOException {
    this.in = new BufferedReader(in);
    this.dataObjectFactory = dataObjectFactory;
    readFileProperties();
    readRecordHeader();
    GeometryFactory geomFactory = geometryFactory;
    final QName srid = getProperty(EcsvConstants.SRID);
    if (srid != null) {
      final Integer sridNum = Integer.valueOf(srid.getLocalPart());
      setProperty("coordinateSystem",
        EpsgCoordinateSystems.getCoordinateSystem(sridNum));
      geomFactory = new GeometryFactory(geometryFactory.getPrecisionModel(),
        sridNum);

    }
    geometryReader = new WKTReader(geomFactory);
    emptyGeometry = geomFactory.createPoint((Coordinate)null);
    if (hasNext) {
      readNextObject();
    }
  }

  /**
   * Closes the underlying reader.
   * 
   * @throws IOException if the close fails
   */
  public void close()
    throws IOException {
    in.close();
  }

  private void createMetaData()
    throws IOException {
    final List<Attribute> attributes = new ArrayList<Attribute>();
    final List<String> names = attributeHeaderValues.get(EcsvConstants.ATTRIBUTE_NAME);
    for (int i = 0; i < names.size(); i++) {
      final String name = names.get(i);
      final String attributeTypeName = getAttributeHeader(
        EcsvConstants.ATTRIBUTE_TYPE, i);
      final int attributeLength = getAttributeHeaderInt(
        EcsvConstants.ATTRIBUTE_LENGTH, i);
      final int attributeScale = getAttributeHeaderInt(
        EcsvConstants.ATTRIBUTE_SCALE, i);
      final boolean attributeTypeRequired = getAttributeHeaderBoolean(
        EcsvConstants.ATTRIBUTE_REQUIRED, i);
      DataType type = null;
      if (attributeTypeName != null) {
        final QName dataTypeName = QName.valueOf(attributeTypeName);
        type = DataTypes.getType(dataTypeName);
      }
      if (type == null) {
        type = DataTypes.STRING;
      }
      attributes.add(new Attribute(name, type, attributeLength, attributeScale,
        attributeTypeRequired));
    }
    final QName typeName = getProperty(EcsvConstants.TYPE_NAME);
    metaData = new DataObjectMetaDataImpl(typeName, getProperties(), attributes);
  }

  public String getAttributeHeader(
    final QName attributeHeaderType,
    final int index) {
    final List<String> values = attributeHeaderValues.get(attributeHeaderType);
    if (values != null) {
      if (index < values.size()) {
        return values.get(index);
      }
    }
    return null;
  }

  public boolean getAttributeHeaderBoolean(
    final QName attributeHeaderType,
    final int index) {
    final String value = getAttributeHeader(attributeHeaderType, index);
    if (value != null) {
      return Boolean.parseBoolean(value);
    } else {
      return false;
    }
  }

  public Integer getAttributeHeaderInt(
    final QName attributeHeaderType,
    final int index) {
    final String value = getAttributeHeader(attributeHeaderType, index);
    if (value != null) {
      return Integer.valueOf(value);
    } else {
      return Integer.MAX_VALUE;
    }
  }

  public DataObjectMetaDataImpl getMetaData() {
    return metaData;
  }

  /**
   * Reads the next line from the file.
   * 
   * @return the next line from the file without trailing newline
   * @throws IOException if bad things happen during the read
   */
  private String getNextLine()
    throws IOException {
    final String nextLine = in.readLine();
    if (nextLine == null) {
      hasNext = false;
      in.close();
    }
    return nextLine;
  }

  /**
   * Returns <tt>true</tt> if the iteration has more elements.
   * 
   * @return <tt>true</tt> if the iterator has more elements.
   */
  public boolean hasNext() {
    return hasNext;
  }

  /**
   * Return the next DataObject from the iterator.
   * 
   * @return The DataObject
   */
  public DataObject next() {
    if (!hasNext) {
      throw new NoSuchElementException("No more elements");
    } else {
      final DataObject object = currentObject;
      readNextObject();
      return object;
    }
  }

  /**
   * Parse a record containing an array of String values into a DataObject with
   * the strings converted to the objects based on the attribute data type.
   * 
   * @param record The record.
   * @return The DataObject.
   */
  private DataObject parseDataObject(
    final String[] record) {
    recordCount++;
    final DataObject object = dataObjectFactory.createDataObject(metaData);
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      final DataType type = metaData.getAttributeType(i);
      String string = null;
      if (i < record.length) {
        string = record[i];
      }
      try {
        final Object value = parseValue(type, string);
        if (value != null) {
          object.setValue(i, value);
        }
      } catch (final Throwable e) {
        LOG.error("Value " + string + " invalid for field "
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
  private String[] parseLine(
    final String nextLine,
    final boolean readLine)
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
          if (c == EcsvConstants.QUOTE_CHARACTER) {
            hadQuotes = true;
            if (inQuotes && line.length() > (i + 1)
              && line.charAt(i + 1) == EcsvConstants.QUOTE_CHARACTER) {
              sb.append(line.charAt(i + 1));
              i++;
            } else {
              inQuotes = !inQuotes;
              if (i > 2 && line.charAt(i - 1) != EcsvConstants.FIELD_SEPARATOR
                && line.length() > (i + 1)
                && line.charAt(i + 1) != EcsvConstants.FIELD_SEPARATOR) {
                sb.append(c);
              }
            }
          } else if (c == EcsvConstants.FIELD_SEPARATOR && !inQuotes) {
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
  public <V> List<V> parseListValue(
    final List<String> values,
    final DataType valueType) {
    if (values != null) {
      final List<V> parsedValues = new ArrayList<V>();
      for (final String value : values) {
        final V parsedValue = (V)parseValue(valueType, value);
        parsedValues.add(parsedValue);
      }
      return parsedValues;
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private <V> V parseValue(
    final DataType type,
    final String string) {
    Object value = null;
    if (string != null && string.length() > 0) {
      if (type == DataTypes.DATE || type == DataTypes.DATE_TIME) {
        try {
          value = dateTimeFormat.parse(string);
        } catch (final ParseException e) {
          throw new IllegalArgumentException(string + " is not a valid Date");
        }
      } else if (type == DataTypes.INTEGER) {
        value = new BigInteger(string);
      } else if (type == DataTypes.BOOLEAN) {
        value = Boolean.parseBoolean(string);
      } else if (type == DataTypes.INT) {
        value = Integer.parseInt(string);
      } else if (type == DataTypes.DOUBLE) {
        value = Double.parseDouble(string);
      } else if (type == DataTypes.QNAME) {
        value = QName.valueOf(string);
      } else if (type == DataTypes.DECIMAL) {
        value = new BigDecimal(string);
      } else if (type == DataTypes.GEOMETRY) {
        try {
          value = geometryReader.read(string);
        } catch (final com.vividsolutions.jts.io.ParseException e) {
          throw new IllegalArgumentException(string
            + " is not a valid GEOMETRY");
        }
      } else if (type == DataTypes.LIST) {
        if (string != null) {
          try {
            final String[] values = parseLine(string, false);
            value = Arrays.asList(values);
          } catch (final IOException e) {
            return null;
          }
        } else {
          return null;
        }
      } else {
        value = string;
      }
    } else {
      if (type == DataTypes.GEOMETRY) {
        value = emptyGeometry;
      }
    }
    return (V)value;
  }

  /**
   * Read the file header block.
   * 
   * @throws IOException If there was an error reading the header.
   */
  private void readFileProperties()
    throws IOException {
    for (String[] line = readNextRecord(); line != null && line.length > 0; line = readNextRecord()) {
      if (line.length == 3) {
        final String name = line[0];
        final QName typeName = QName.valueOf(line[1]);
        final String string = line[2];
        final DataType type = DataTypes.getType(typeName);
        final Object value = parseValue(type, string);
        setProperty(name, value);
      }
    }
  }

  private void readNextObject() {
    try {
      final String[] record = readNextRecord();
      if (record != null && record.length > 0) {
        currentObject = parseDataObject(record);
      } else {
        hasNext = false;
        close();
        currentObject = null;
      }
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   * 
   * @return a string array with each comma-separated element as a separate
   *         entry.
   * @throws IOException if bad things happen during the read
   */
  private String[] readNextRecord()
    throws IOException {
    final String nextLine = getNextLine();
    if (hasNext) {
      return parseLine(nextLine, true);
    } else {
      return null;
    }
  }

  /**
   * Read the record header block.
   * 
   * @throws IOException If there was an error reading the header.
   */
  private void readRecordHeader()
    throws IOException {
    if (hasNext) {
      int headerIndex = 0;
      for (String[] line = readNextRecord(); line != null && line.length > 0; line = readNextRecord()) {
        final List<String> attributeHeaderTypeNames = getProperty(EcsvConstants.ATTRIBUTE_HEADER_TYPES);
        final List<QName> attributeHeaderTypes = parseListValue(
          attributeHeaderTypeNames, DataTypes.QNAME);
        if (headerIndex < attributeHeaderTypes.size()) {
          final QName headerType = attributeHeaderTypes.get(headerIndex);
          attributeHeaderValues.put(headerType, Arrays.asList(line));
          headerIndex++;
        }
      }
      createMetaData();
    }
  }

  /**
   * Removing items from the iterator is not supported.
   */
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
