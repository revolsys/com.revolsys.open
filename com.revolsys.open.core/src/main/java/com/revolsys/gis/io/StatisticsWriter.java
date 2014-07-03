package com.revolsys.gis.io;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanNameAware;

import com.revolsys.data.record.Record;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.Writer;

public class StatisticsWriter extends AbstractWriter<Record> implements
  BeanNameAware {
  private String beanName;

  private Statistics statistics;

  private Writer<Record> writer;

  public StatisticsWriter() {
  }

  public StatisticsWriter(final Writer<Record> writer) {
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

  public Writer<Record> getWriter() {
    return writer;
  }

  @PostConstruct
  public void init() {
    if (statistics == null) {
      setStatistics(new Statistics("Write " + writer));
    }
    statistics.connect();
  }

  @Override
  public void setBeanName(final String beanName) {
    this.beanName = beanName.replaceAll("Stats", "");
  }

  /**
   * @param statistics the statistics to set
   */
  public void setStatistics(final Statistics statistics) {
    this.statistics = statistics;
  }

  public void setWriter(final Writer<Record> writer) {
    this.writer = writer;
  }

  @Override
  public String toString() {
    return writer.toString();
  }

  @Override
  public void write(final Record object) {
    if (object != null) {
      writer.write(object);
      statistics.add(object);
    }
  }
}
