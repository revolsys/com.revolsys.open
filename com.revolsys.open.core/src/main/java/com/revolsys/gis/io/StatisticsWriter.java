package com.revolsys.gis.io;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanNameAware;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.Writer;

public class StatisticsWriter extends AbstractWriter<DataObject> implements
  BeanNameAware {
  private String beanName;

  private Statistics statistics;

  private Writer<DataObject> writer;

  public StatisticsWriter() {
  }

  public StatisticsWriter(final Writer<DataObject> writer) {
    setWriter(writer);
  }

  @Override
  public void close() {
    writer.close();
    statistics.disconnect();
  }

  @Override
  public void flush() {
    writer.flush();
  }

  /**
   * @return the statistics
   */
  public Statistics getStatistics() {
    return statistics;
  }

  public Writer<DataObject> getWriter() {
    return writer;
  }

  @PostConstruct
  public void init() {
    if (statistics == null) {
      setStatistics(new Statistics("Write " + writer));
    }
    statistics.connect();
  }

  public void setBeanName(final String beanName) {
    this.beanName = beanName.replaceAll("Stats", "");
  }

  /**
   * @param statistics the statistics to set
   */
  public void setStatistics(final Statistics statistics) {
    this.statistics = statistics;
  }

  public void setWriter(final Writer<DataObject> writer) {
    this.writer = writer;
  }

  @Override
  public String toString() {
    return writer.toString();
  }

  public void write(final DataObject object) {
    if (object != null) {
      writer.write(object);
      statistics.add(object);
    }
  }
}
