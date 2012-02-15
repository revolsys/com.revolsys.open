package com.revolsys.gis.parallel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.io.Statistics;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class StatisticsProcess extends BaseInOutProcess<DataObject, DataObject> {

  private Statistics statistics;

  @Override
  protected void postRun(
    final Channel<DataObject> in,
    final Channel<DataObject> out) {
    if (statistics != null) {
      statistics.disconnect();
    }
  }

  @Override
  protected void preRun(
    final Channel<DataObject> in,
    final Channel<DataObject> out) {
    statistics = new Statistics(getBeanName());
    statistics.connect();
  }

  @Override
  protected void process(
    final Channel<DataObject> in,
    final Channel<DataObject> out,
    final DataObject object) {
    statistics.add(object);
    out.write(object);
  }

}
