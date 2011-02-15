package com.revolsys.gis.parallel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.AbstractInOutProcess;

public class SynchronizationProcess extends AbstractInOutProcess<DataObject,DataObject> {
  private int count = 0;

  @Override
  public synchronized Channel<DataObject> getIn() {
    count++;
    return super.getIn();
  }

  @Override
  protected void run(
    final Channel<DataObject> in,
    final Channel<DataObject> out) {
    do {
      for (DataObject object = in.read(); object != null; object = in.read()) {
        out.write(object);
      }
      count--;
    } while (count > 0);
  }
}
