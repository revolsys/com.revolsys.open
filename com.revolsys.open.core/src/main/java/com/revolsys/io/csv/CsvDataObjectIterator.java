package com.revolsys.io.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.FileUtil;

public class CsvDataObjectIterator extends AbstractIterator<DataObject>
  implements DataObjectIterator {

  private final DataObjectFactory dataObjectFactory;

  /** The reader to */
  private BufferedReader in;

  /** The metadata for the data being read by this iterator. */
  private DataObjectMetaData metaData;

  private final Resource resource;

  /**
   * Constructs CSVReader with supplied separator and quote char.
   * 
   * @param reader
   * @throws IOException
   */
  public CsvDataObjectIterator(final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    this.resource = resource;
    this.dataObjectFactory = dataObjectFactory;
  }

  private void createMetaData(final String[] fieldNames) throws IOException {
    final List<Attribute> attributes = new ArrayList<Attribute>();
    for (final String name : fieldNames) {
      final DataType type = DataTypes.STRING;
      attributes.add(new Attribute(name, type, false));
    }
    final String filename = FileUtil.getBaseName(resource.getFilename());
    final QName typeName = QName.valueOf(filename);
    metaData = new DataObjectMetaDataImpl(typeName, getProperties(), attributes);
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  protected void doClose() {
    FileUtil.closeSilent(in);
  }

  @Override
  protected void doInit() {
    try {
      this.in = new BufferedReader(new InputStreamReader(
        this.resource.getInputStream(), CsvConstants.CHARACTER_SET));
      final String[] line = readNextRecord();
      createMetaData(line);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to open " + resource, e);
    }
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  @Override
  protected DataObject getNext() {
    try {
      final String[] record = readNextRecord();
      if (record != null && record.length > 0) {
        return parseDataObject(record);
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
   * Parse a record containing an array of String values into a DataObject with
   * the strings converted to the objects based on the attribute data type.
   * 
   * @param record The record.
   * @return The DataObject.
   */
  private DataObject parseDataObject(final String[] record) {
    final DataObject object = dataObjectFactory.createDataObject(metaData);
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
