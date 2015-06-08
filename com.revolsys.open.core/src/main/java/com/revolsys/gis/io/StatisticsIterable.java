package com.revolsys.gis.io;

import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanNameAware;

import com.revolsys.data.record.Record;

public class StatisticsIterable implements Iterable<Record>, BeanNameAware {
  private String statsName;

  private Iterable<Record> iterable;

  private Statistics statistics;

  public StatisticsIterable() {
  }

  public StatisticsIterable(final Iterable<Record> iterable) {
    setIterable(iterable);
  }

  public Iterable<Record> getIterable() {
    return this.iterable;
  }

  /**
   * @return the stats
   */
  public Statistics getStatistics() {
    return this.statistics;
  }

  public String getStatsName() {
    return this.statsName;
  }

  @PostConstruct
  public void init() {
    if (this.statistics == null) {
      setStatistics(new Statistics("Read " + this.statsName + " " + this.iterable.toString()));
    }
  }

  @Override
  public Iterator<Record> iterator() {
    if (this.statistics == null) {
      setStatistics(new Statistics("Read " + this.statsName + " " + this.iterable.toString()));
    }
    return new StatisticsIterator(this.iterable.iterator(), this.statistics);
  }

  @Override
  public void setBeanName(final String beanName) {
    if (this.statsName == null) {
      this.statsName = beanName.replaceAll("Stats", "");
    }
  }

  public void setIterable(final Iterable<Record> iterable) {
    this.iterable = iterable;
  }

  /**
   * @param stats the stats to set
   */
  public void setStatistics(final Statistics statistics) {
    this.statistics = statistics;
  }

  public void setStatsName(final String statsName) {
    this.statsName = statsName;
  }

  @Override
  public String toString() {
    return this.iterable.toString();
  }

}
