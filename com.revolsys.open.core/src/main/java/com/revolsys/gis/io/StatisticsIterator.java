package com.revolsys.gis.io;

import java.util.Iterator;

import com.revolsys.record.Record;

public class StatisticsIterator implements Iterator<Record> {
  private final Iterator<Record> iterator;

  private Statistics statistics;

  public StatisticsIterator(final Iterator<Record> iterator, final Statistics statistics) {
    this.iterator = iterator;
    setStatistics(statistics);
  }

  /**
   * @return the stats
   */
  public Statistics getStatistics() {
    return this.statistics;
  }

  @Override
  public boolean hasNext() {
    final boolean hasNext = this.iterator.hasNext();
    if (!hasNext) {
      this.statistics.disconnect();
    }
    return hasNext;
  }

  @Override
  public Record next() {
    final Record object = this.iterator.next();
    if (object != null) {
      this.statistics.add(object);
    }
    return object;
  }

  @Override
  public void remove() {
    this.iterator.remove();
  }

  /**
   * @param stats the stats to set
   */
  public void setStatistics(final Statistics statistics) {
    this.statistics = statistics;
    statistics.connect();
  }

}
