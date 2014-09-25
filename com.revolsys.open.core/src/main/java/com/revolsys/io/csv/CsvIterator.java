package com.revolsys.io.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.revolsys.io.FileUtil;

public class CsvIterator implements Iterator<List<String>>,
  Iterable<List<String>> {

  /** The current record. */
  private List<String> currentRecord;

  /** Flag indicating if there are more records to be read. */
  private boolean hasNext = true;

  /** The reader to */
  private final BufferedReader in;

  /**
   * Constructs CSVReader with supplied separator and quote char.
   * 
   * @param reader The reader to the CSV file.
   * @throws IOException
   */
  public CsvIterator(final Reader in) {
    this.in = new BufferedReader(in);

    readNextRecord();
  }

  /**
   * Closes the underlying reader.
   * 
   * @throws IOException if the close fails
   */
  public void close() {
    FileUtil.closeSilent(in);
  }

  /**
   * Reads the next line from the file.
   * 
   * @return the next line from the file without trailing newline
   * @throws IOException if bad things happen during the read
   */
  private String getNextLine() {
    try {
      final String nextLine = in.readLine();
      if (nextLine == null) {
        hasNext = false;
        close();
      }
      return nextLine;
    } catch (final IOException e) {
      hasNext = false;
      close();
      return null;
    }
  }

  /**
   * Returns <tt>true</tt> if the iteration has more elements.
   * 
   * @return <tt>true</tt> if the iterator has more elements.
   */
  @Override
  public boolean hasNext() {
    return hasNext;
  }

  @Override
  public Iterator<List<String>> iterator() {
    return this;
  }

  /**
   * Return the next record from the iterator.
   * 
   * @return The record
   */
  @Override
  public List<String> next() {
    if (!hasNext) {
      throw new NoSuchElementException("No more elements");
    } else {
      final List<String> object = currentRecord;
      readNextRecord();
      return object;
    }
  }

  /**
   * Parses an incoming String and returns an array of elements.
   * 
   * @param nextLine the string to parse
   * @return the comma-tokenized list of elements, or null if nextLine is null
   * @throws IOException if bad things happen during the read
   */
  private List<String> parseLine(String nextLine) {
    if (nextLine.length() == 0) {
      return Collections.emptyList();
    } else {

      final List<String> fields = new ArrayList<String>();
      StringBuilder sb = new StringBuilder();
      boolean inQuotes = false;
      boolean hadQuotes = false;
      do {
        if (inQuotes) {
          sb.append("\n");
          nextLine = getNextLine();
          if (nextLine == null) {
            break;
          }
        }
        for (int i = 0; i < nextLine.length(); i++) {
          final char c = nextLine.charAt(i);
          if (c == '"') {
            hadQuotes = true;
            if (inQuotes && nextLine.length() > (i + 1)
              && nextLine.charAt(i + 1) == '"') {
              sb.append(nextLine.charAt(i + 1));
              i++;
            } else {
              inQuotes = !inQuotes;
              if (i > 2 && nextLine.charAt(i - 1) != ','
                && nextLine.length() > (i + 1) && nextLine.charAt(i + 1) != ',') {
                sb.append(c);
              }
            }
          } else if (c == ',' && !inQuotes) {
            hadQuotes = false;
            if (hadQuotes || sb.length() > 0) {
              fields.add(sb.toString());
            } else {
              fields.add(null);
            }
            sb = new StringBuilder();
          } else {
            sb.append(c);
          }
        }
      } while (inQuotes);
      if (sb.length() > 0 || fields.size() > 0) {
        fields.add(sb.toString());
      }
      return fields;
    }
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   * 
   * @return a string array with each comma-separated element as a separate
   *         entry.
   * @throws IOException if bad things happen during the read
   */
  private List<String> readNextRecord() {
    final String nextLine = getNextLine();
    if (hasNext) {
      currentRecord = parseLine(nextLine);
      return currentRecord;
    } else {
      return null;
    }
  }

  /**
   * Removing items from the iterator is not supported.
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
