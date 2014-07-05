package com.revolsys.gis.io;

import com.revolsys.data.record.Record;

public class StatisticsRecordIterator implements RecordIterator {
  private RecordIterator reader;

  private Statistics statistics;

  public StatisticsRecordIterator() {
  }

  public StatisticsRecordIterator(final RecordIterator reader) {
    setReader(reader);
  }

  @Override
  public void close() {
    reader.close();
    statistics.disconnect();
  }

  /**
   * @return the reader
   */
  public RecordIterator getReader() {
    return reader;
  }

  /**
   * @return the stats
   */
  public Statistics getStatistics() {
    return statistics;
  }

  @Override
  public boolean hasNext() {
    return reader.hasNext();
  }

  @Override
  public Record next() {
    final Record object = reader.next();
    if (object != null) {
      statistics.add(object);
    }
    return object;
  }

  @Override
  public void open() {
    reader.open();
  }

  @Override
  public void remove() {
    reader.remove();

  }

  /**
   * @param reader the reader to set
   */
  public void setReader(final RecordIterator reader) {
    this.reader = reader;
    if (statistics == null) {
      setStatistics(new Statistics("Read " + reader.toString()));
    }
  }

  /**
   * @param stats the stats to set
   */
  public void setStatistics(final Statistics statistics) {
    if (this.statistics != null) {

    }
    this.statistics = statistics;
    if (statistics != null) {
      statistics.connect();
    }
  }

  @Override
  public String toString() {
    return reader.toString();
  }

}
