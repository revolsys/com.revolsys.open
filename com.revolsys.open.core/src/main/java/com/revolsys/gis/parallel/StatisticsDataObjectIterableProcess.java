package com.revolsys.gis.parallel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.io.Statistics;
import com.revolsys.parallel.channel.Channel;

public class StatisticsDataObjectIterableProcess extends
  IterableProcess<DataObject> {

  private Statistics statistics;

  public StatisticsDataObjectIterableProcess() {
  }

  @Override
  protected void destroy() {
    super.destroy();
    if (statistics != null) {
      statistics.disconnect();
      statistics = null;
    }
  }

  public Statistics getStatistics() {
    return statistics;
  }

  public void setStatistics(final Statistics statistics) {
    this.statistics = statistics;
    if (statistics != null) {
      statistics.connect();
    }
  }

  @Override
  protected void write(final Channel<DataObject> out, final DataObject record) {
    if (record != null) {
      statistics.add(record);
      out.write(record);
    }
  }
}
