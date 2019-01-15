package com.revolsys.util.count;

import javax.annotation.PostConstruct;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.Writer;
import com.revolsys.record.Record;

public class LabelCountWriter extends AbstractRecordWriter {
  private LabelCountMap labelCountMap;

  private Writer<Record> writer;

  public LabelCountWriter() {
  }

  public LabelCountWriter(final Writer<Record> writer) {
    setWriter(writer);
  }

  @Override
  public void close() {
    this.writer.close();
    this.labelCountMap.disconnect();
  }

  @Override
  public void flush() {
    this.writer.flush();
  }

  /**
   * @return the labelCountMap
   */
  public LabelCountMap getLabelCountMap() {
    return this.labelCountMap;
  }

  public Writer<Record> getWriter() {
    return this.writer;
  }

  @PostConstruct
  public void init() {
    if (this.labelCountMap == null) {
      setLabelCountMap(new LabelCountMap("Write " + this.writer));
    }
    this.labelCountMap.connect();
  }

  public LabelCountWriter setCounts(final LabelCountMap labelCountMap) {
    this.labelCountMap = labelCountMap;
    return this;
  }

  /**
   * @param labelCountMap the labelCountMap to set
   */
  public void setLabelCountMap(final LabelCountMap labelCountMap) {
    this.labelCountMap = labelCountMap;
  }

  public void setWriter(final Writer<Record> writer) {
    this.writer = writer;
  }

  @Override
  public String toString() {
    return this.writer.toString();
  }

  @Override
  public void write(final Record object) {
    if (object != null) {
      this.writer.write(object);
      this.labelCountMap.addCount(object);
    }
  }
}
