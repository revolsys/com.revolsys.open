package com.revolsys.gis.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;

public class CsvIterator implements Iterator<DataObject> {
  /** The current database object. */
  private DataObject currentObject;

  private final DataObjectFactory dataObjectFactory;

  /** The values for each record header type. */
  private List<String> fieldNames = new ArrayList<String>();

  /** Flag indicating if there are more records to be read. */
  private boolean hasNext = true;

  /** The reader to */
  private final BufferedReader in;

  /** The metadata for the data being read by this iterator. */
  private DataObjectMetaDataImpl metaData;

  /** The map of file property names and values. */
  private final Map<String, Object> properties = new HashMap<String, Object>();

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
  public CsvIterator(
    final Reader in,
    final DataObjectFactory dataObjectFactory)
    throws IOException {
    this.in = new BufferedReader(in);
    this.dataObjectFactory = dataObjectFactory;
    readRecordHeader();

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
    final List<String> names = fieldNames;
    for (int i = 0; i < names.size(); i++) {
      final String name = names.get(i);

      final DataType type = DataTypes.STRING;

      attributes.add(new Attribute(name, type, false));
    }
    final QName typeName = new QName("object");
    metaData = new DataObjectMetaDataImpl(typeName, properties, attributes);
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

  public Map<String, Object> getProperties() {
    return properties;
  }

  @SuppressWarnings("unchecked")
  public <V> V getProperty(
    final String name) {
    return (V)properties.get(name);
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
      String value = null;
      if (i < record.length) {
        value = record[i];
        if (value != null) {
          object.setValue(i, value);
        }
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
          if (c == CsvConstants.QUOTE_CHARACTER) {
            hadQuotes = true;
            if (inQuotes && line.length() > (i + 1)
              && line.charAt(i + 1) == CsvConstants.QUOTE_CHARACTER) {
              sb.append(line.charAt(i + 1));
              i++;
            } else {
              inQuotes = !inQuotes;
              if (i > 2 && line.charAt(i - 1) != CsvConstants.FIELD_SEPARATOR
                && line.length() > (i + 1)
                && line.charAt(i + 1) != CsvConstants.FIELD_SEPARATOR) {
                sb.append(c);
              }
            }
          } else if (c == CsvConstants.FIELD_SEPARATOR && !inQuotes) {
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
      final String[] line = readNextRecord();
      fieldNames = Arrays.asList(line);

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
