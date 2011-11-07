package com.revolsys.gis.io;

import java.util.Iterator;

import com.revolsys.gis.data.model.DataObject;

public class StatisticsIterator implements Iterator<DataObject> {
  private final Iterator<DataObject> iterator;

  private Statistics statistics;

  public StatisticsIterator(
    final Iterator<DataObject> iterator,
    final Statistics statistics) {
    this.iterator = iterator;
    setStatistics(statistics);
  }

  /**
   * @return the stats
   */
  public Statistics getStatistics() {
    return statistics;
  }

  public boolean hasNext() {
    final boolean hasNext = iterator.hasNext();
    if (!hasNext) {
      statistics.disconnect();
    }
    return hasNext;
  }

  public DataObject next() {
    final DataObject object = iterator.next();
    if (object != null) {
      statistics.add(object);
    }
    return object;
  }

  public void remove() {
    iterator.remove();
  }

  /**
   * @param stats the stats to set
   */
  public void setStatistics(
    final Statistics statistics) {
    this.statistics = statistics;
    statistics.connect();
  }

}
