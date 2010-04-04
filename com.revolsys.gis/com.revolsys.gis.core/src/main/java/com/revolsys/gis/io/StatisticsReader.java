package com.revolsys.gis.io;

import com.revolsys.gis.data.model.DataObject;

public class StatisticsReader implements DataObjectReader {
  private DataObjectReader reader;

  private Statistics statistics;

  public StatisticsReader() {
  }

  public StatisticsReader(
    final DataObjectReader reader) {
    setReader(reader);
  }

  public void close() {
    reader.close();
    statistics.disconnect();
  }

  /**
   * @return the reader
   */
  public DataObjectReader getReader() {
    return reader;
  }

  public String toString() {
    return reader.toString();
  }

  /**
   * @return the stats
   */
  public Statistics getStatistics() {
    return statistics;
  }

  public boolean hasNext() {
    return reader.hasNext();
  }

  public DataObject next() {
    final DataObject object = reader.next();
    if (object != null) {
      statistics.add(object);
    }
    return object;
  }

  public void open() {
    reader.open();
  }

  public void remove() {
    reader.remove();

  }

  /**
   * @param reader the reader to set
   */
  public void setReader(
    final DataObjectReader reader) {
    this.reader = reader;
    if (statistics == null) {
      setStatistics(new Statistics("Read " + reader.toString()));
    }
  }

  /**
   * @param stats the stats to set
   */
  public void setStatistics(
    final Statistics statistics) {
    if (this.statistics != null) {

    }
    this.statistics = statistics;
    if (statistics != null) {
      statistics.connect();
    }
  }

}
