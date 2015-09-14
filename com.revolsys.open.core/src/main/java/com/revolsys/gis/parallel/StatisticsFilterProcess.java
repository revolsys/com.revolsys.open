package com.revolsys.gis.parallel;

import com.revolsys.gis.io.Statistics;
import com.revolsys.parallel.process.FilterProcess;
import com.revolsys.record.Record;

public class StatisticsFilterProcess extends FilterProcess<Record> {

  private Statistics acceptStatistics;

  private Statistics rejectStatistics;

  @Override
  protected void destroy() {
    if (this.acceptStatistics != null) {
      this.acceptStatistics.disconnect();
    }
    if (this.rejectStatistics != null) {
      this.rejectStatistics.disconnect();
    }
  }

  public Statistics getAcceptStatistics() {
    return this.acceptStatistics;
  }

  public Statistics getRejectStatistics() {
    return this.rejectStatistics;
  }

  @Override
  protected void init() {
    super.init();
    if (this.acceptStatistics != null) {
      this.acceptStatistics.connect();
    }
    if (this.rejectStatistics != null) {
      this.rejectStatistics.connect();
    }
  }

  @Override
  protected void postAccept(final Record object) {
    if (this.acceptStatistics != null) {
      this.acceptStatistics.add(object);
    }
  }

  @Override
  protected void postReject(final Record object) {
    if (this.rejectStatistics != null) {
      this.rejectStatistics.add(object);
    }
  }

  public void setAcceptStatistics(final Statistics acceptStatistics) {
    this.acceptStatistics = acceptStatistics;
  }

  public void setRejectStatistics(final Statistics rejectStatistics) {
    this.rejectStatistics = rejectStatistics;
  }

}
