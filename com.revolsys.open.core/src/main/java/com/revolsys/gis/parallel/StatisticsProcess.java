package com.revolsys.gis.parallel;

import com.revolsys.data.record.Record;
import com.revolsys.gis.io.Statistics;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class StatisticsProcess extends BaseInOutProcess<Record, Record> {

  private Statistics statistics;

  @Override
  protected void postRun(final Channel<Record> in, final Channel<Record> out) {
    if (this.statistics != null) {
      this.statistics.disconnect();
    }
  }

  @Override
  protected void preRun(final Channel<Record> in, final Channel<Record> out) {
    this.statistics = new Statistics(getBeanName());
    this.statistics.connect();
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    this.statistics.add(object);
    out.write(object);
  }

}
