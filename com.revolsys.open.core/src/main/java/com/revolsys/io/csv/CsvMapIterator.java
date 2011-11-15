package com.revolsys.io.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class CsvMapIterator implements Iterator<Map<String, Object>> {

  /** The values for each record header type. */
  private List<String> fieldNames = new ArrayList<String>();

  /** The reader to */
  private final CsvIterator in;

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
  public CsvMapIterator(
    final Reader in)
    throws IOException {
    this.in = new CsvIterator(in);
    readRecordHeader();
  }

  public void close() {
    in.close();
  }

  /**
   * Returns <tt>true</tt> if the iteration has more elements.
   * 
   * @return <tt>true</tt> if the iterator has more elements.
   */
  public boolean hasNext() {
    return in.hasNext();
  }

  /**
   * Return the next DataObject from the iterator.
   * 
   * @return The DataObject
   */
  public Map<String, Object> next() {
    if (hasNext()) {
      final List<String> record = in.next();
      return parseMap(record);
    } else {
      throw new NoSuchElementException("No more elements");
    }
  }

  private Map<String, Object> parseMap(
    final List<String> record) {
    recordCount++;
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    for (int i = 0; i < fieldNames.size() && i < record.size(); i++) {
      final String fieldName = fieldNames.get(i);
      final String value = record.get(i);
      if (value != null) {
        map.put(fieldName, value);
      }
    }
    return map;
  }

  /**
   * Read the record header block.
   * 
   * @throws IOException If there was an error reading the header.
   */
  private void readRecordHeader()
    throws IOException {
    if (hasNext()) {
      fieldNames = in.next();
    }
  }

  /**
   * Removing items from the iterator is not supported.
   */
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
