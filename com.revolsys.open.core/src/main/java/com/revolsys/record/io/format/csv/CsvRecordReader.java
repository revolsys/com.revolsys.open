package com.revolsys.record.io.format.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
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

  @Override
  protected void closeDo() {
    super.closeDo();
    FileUtil.closeSilent(this.in);
    this.in = null;
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

  /**
   * Reads the next line from the file.
   *
   * @return the next line from the file without trailing newline
   * @throws IOException if bad things happen during the read
   */
  private String getNextLine() throws IOException {
    final BufferedReader in = this.in;
    if (in == null) {
      throw new NoSuchElementException();
    } else {
      final String nextLine = this.in.readLine();
      if (nextLine == null) {
        throw new NoSuchElementException();
      }
      return nextLine;
    }
  }

  @Override
  protected void initDo() {
    super.initDo();
    try {
      this.in = this.resource.newBufferedReader();
      final List<String> line = readNextRow();
      final String filename = this.resource.getFilename();
      newRecordDefinition(filename, line);
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
   * Parses an incoming String and returns an array of elements.
   *
   * @param nextLine the string to parse
   * @return the comma-tokenized list of elements, or null if nextLine is null
   * @throws IOException if bad things happen during the read
   */
  private List<String> parseLine(final String nextLine, final boolean readLine) throws IOException {
    String line = nextLine;
    if (line.length() == 0) {
      return Collections.emptyList();
    } else {
      final List<String> values = new ArrayList<>();
      StringBuilder sb = new StringBuilder();
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
          if (c == '"') {
            hadQuotes = true;
            if (inQuotes && line.length() > i + 1 && line.charAt(i + 1) == '"') {
              sb.append(line.charAt(i + 1));
              i++;
            } else {
              inQuotes = !inQuotes;
              if (i > 2 && line.charAt(i - 1) != this.fieldSeparator && line.length() > i + 1
                && line.charAt(i + 1) != this.fieldSeparator) {
                sb.append(c);
              }
            }
          } else if (c == this.fieldSeparator && !inQuotes) {
            hadQuotes = false;
            if (hadQuotes || sb.length() > 0) {
              values.add(sb.toString());
            } else {
              values.add(null);
            }
            sb = new StringBuilder();
          } else {
            sb.append(c);
          }
        }
      } while (inQuotes);
      if (sb.length() > 0 || values.size() > 0) {
        if (hadQuotes || sb.length() > 0) {
          values.add(sb.toString());
        } else {
          values.add(null);
        }
      }
      return values;
    }
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   *
   * @return a string array with each comma-separated element as a separate
   *         entry.
   * @throws IOException if bad things happen during the read
   */
  private List<String> readNextRow() throws IOException {
    final String nextLine = getNextLine();
    return parseLine(nextLine, true);
  }
}
