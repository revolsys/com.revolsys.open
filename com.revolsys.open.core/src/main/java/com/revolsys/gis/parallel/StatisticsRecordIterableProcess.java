package com.revolsys.gis.parallel;

import com.revolsys.data.record.Record;
import com.revolsys.gis.io.Statistics;
import com.revolsys.parallel.channel.Channel;

public class StatisticsRecordIterableProcess extends
IterableProcess<Record> {

  private Statistics statistics;

  public StatisticsRecordIterableProcess() {
  }

  @Override
  protected void destroy() {
    super.destroy();
    if (this.statistics != null) {
      this.statistics.disconnect();
      this.statistics = null;
    }
  }

  public Statistics getStatistics() {
    return this.statistics;
  }

  public void setStatistics(final Statistics statistics) {
    this.statistics = statistics;
    if (statistics != null) {
      statistics.connect();
    }
  }

  @Override
  protected void write(final Channel<Record> out, final Record record) {
    if (record != null) {
      this.statistics.add(record);
      out.write(record);
    }
  }
}
