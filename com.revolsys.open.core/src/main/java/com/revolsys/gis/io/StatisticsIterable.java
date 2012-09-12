package com.revolsys.gis.io;

import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanNameAware;

import com.revolsys.gis.data.model.DataObject;

public class StatisticsIterable implements Iterable<DataObject>, BeanNameAware {
  private String beanName;

  private Iterable<DataObject> iterable;

  private Statistics statistics;

  public StatisticsIterable() {
  }

  public StatisticsIterable(final Iterable<DataObject> iterable) {
    setIterable(iterable);
  }

  public Iterable<DataObject> getIterable() {
    return iterable;
  }

  /**
   * @return the stats
   */
  public Statistics getStatistics() {
    return statistics;
  }

  @PostConstruct
  public void init() {
    if (this.statistics == null) {
      setStatistics(new Statistics("Read " + beanName + " "
        + iterable.toString()));
    }
  }

  @Override
  public Iterator<DataObject> iterator() {
    if (this.statistics == null) {
      setStatistics(new Statistics("Read " + beanName + " "
        + iterable.toString()));
    }
    return new StatisticsIterator(iterable.iterator(), statistics);
  }

  @Override
  public void setBeanName(final String beanName) {
    this.beanName = beanName.replaceAll("Stats", "");
  }

  public void setIterable(final Iterable<DataObject> iterable) {
    this.iterable = iterable;
  }

  /**
   * @param stats the stats to set
   */
  public void setStatistics(final Statistics statistics) {
    this.statistics = statistics;
  }

  @Override
  public String toString() {
    return iterable.toString();
  }

}
