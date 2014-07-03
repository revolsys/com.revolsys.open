package com.revolsys.gis.parallel;

import com.revolsys.data.record.Record;
import com.revolsys.gis.io.Statistics;
import com.revolsys.parallel.process.FilterProcess;

public class StatisticsFilterProcess extends FilterProcess<Record> {

  private Statistics acceptStatistics;

  private Statistics rejectStatistics;

  @Override
  protected void destroy() {
    if (acceptStatistics != null) {
      acceptStatistics.disconnect();
    }
    if (rejectStatistics != null) {
      rejectStatistics.disconnect();
    }
  }

  public Statistics getAcceptStatistics() {
    return acceptStatistics;
  }

  public Statistics getRejectStatistics() {
    return rejectStatistics;
  }

  @Override
  protected void init() {
    super.init();
    if (acceptStatistics != null) {
      acceptStatistics.connect();
    }
    if (rejectStatistics != null) {
      rejectStatistics.connect();
    }
  }

  @Override
  protected void postAccept(final Record object) {
    if (acceptStatistics != null) {
      acceptStatistics.add(object);
    }
  }

  @Override
  protected void postReject(final Record object) {
    if (rejectStatistics != null) {
      rejectStatistics.add(object);
    }
  }

  public void setAcceptStatistics(final Statistics acceptStatistics) {
    this.acceptStatistics = acceptStatistics;
  }

  public void setRejectStatistics(final Statistics rejectStatistics) {
    this.rejectStatistics = rejectStatistics;
  }

}
