package com.revolsys.record.io.format.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.logging.Logs;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.spring.resource.Resource;

public class CsvRecordReader extends AbstractRecordReader {
  private final char fieldSeparator;

  private BufferedReader in;

  private Resource resource;

  public CsvRecordReader(final Resource resource) {
    this(resource, ArrayRecord.FACTORY, Csv.FIELD_SEPARATOR);
  }

  public CsvRecordReader(final Resource resource, final char fieldSeparator) {
    this(resource, ArrayRecord.FACTORY, fieldSeparator);
  }

  public CsvRecordReader(final Resource resource,
    final RecordFactory<? extends Record> recordFactory) {
    this(resource, recordFactory, Csv.FIELD_SEPARATOR);
  }

  public CsvRecordReader(final Resource resource,
    final RecordFactory<? extends Record> recordFactory, final char fieldSeparator) {
    super(recordFactory);
    this.resource = resource;
    this.fieldSeparator = fieldSeparator;
  }

  private void addValue(final List<String> values, final StringBuilder sb,
    final boolean hadQuotes) {
    if (hadQuotes || sb.length() > 0) {
      values.add(sb.toString());
      sb.setLength(0);
    } else {
      values.add(null);
    }
  }

  @Override
  protected void closeDo() {
    super.closeDo();
    final BufferedReader in = this.in;
    if (in != null) {
      try {
        in.close();
      } catch (final IOException e) {
      }
      this.in = null;
    }
    this.resource = null;
  }

  @Override
  protected Record getNext() {
    try {
      final List<String> row = readNextRow();
      if (row != null && row.size() > 0) {
        return parseRecord(row);
      } else {
        throw new NoSuchElementException();
      }
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  protected void initDo() {
    super.initDo();
    try {
      this.in = this.resource.newBufferedReader();
      final List<String> line = readNextRow();
      final String baseName = this.resource.getBaseName();
      newRecordDefinition(baseName, line);
    } catch (final IOException e) {
      Logs.error(this, "Unable to open " + this.resource, e);
    } catch (final NoSuchElementException e) {
    }
  }

  @Override
  protected GeometryFactory loadGeometryFactory() {
    return EsriCoordinateSystems.getGeometryFactory(this.resource);
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   *
   * @return a string array with each comma-separated element as a separate
   *         entry.
   * @throws IOException if bad things happen during the read
   */
  private List<String> readNextRow() throws IOException {
    final char fieldSeparator = this.fieldSeparator;
    final BufferedReader in = this.in;
    if (in == null) {
      throw new NoSuchElementException();
    } else {
      final List<String> values = new ArrayList<>();
      final StringBuilder sb = new StringBuilder();
      boolean inQuotes = false;
      boolean hadQuotes = false;
      while (true) {
        final int character = in.read();
        switch (character) {
          case -1:
            return returnEof(values, sb, hadQuotes);
          case '"':
            hadQuotes = true;
            if (inQuotes) {
              in.mark(1);
              final int nextCharacter = in.read();
              if ('"' == nextCharacter) {
                sb.append('"');
              } else {
                inQuotes = false;
                in.reset();
              }
            } else {
              inQuotes = true;
            }
          break;
          case '\n':
            if (inQuotes) {
              sb.append('\n');
            } else {
              if (values.isEmpty()) {
                if (sb.length() > 0) {
                  values.add(sb.toString());
                  return values;
                } else {
                  // skip empty lines
                }
              } else {
                addValue(values, sb, hadQuotes);
                return values;
              }
            }
          break;
          case '\r':
            in.mark(1);
            final int nextCharacter = in.read();
            in.reset();
            if (nextCharacter == '\n') {
            } else {
              if (inQuotes) {
                sb.append('\n');
              } else {
                if (values.isEmpty()) {
                  if (sb.length() > 0) {
                    values.add(sb.toString());
                    return values;
                  } else {
                    // skip empty lines
                  }
                } else {
                  addValue(values, sb, hadQuotes);
                  return values;
                }
              }
            }
          break;
          default:
            if (character == fieldSeparator) {
              if (inQuotes) {
                sb.append(fieldSeparator);
              } else {
                addValue(values, sb, hadQuotes);
                hadQuotes = false;
              }
            } else {
              sb.append((char)character);
            }
          break;
        }
      }
    }
  }

  private List<String> returnEof(final List<String> values, final StringBuilder sb,
    final boolean hadQuotes) {
    if (values.isEmpty()) {
      if (sb.length() > 0) {
        values.add(sb.toString());
      } else {
        throw new NoSuchElementException();
      }
    } else {
      addValue(values, sb, hadQuotes);
    }
    return values;
  }
}
