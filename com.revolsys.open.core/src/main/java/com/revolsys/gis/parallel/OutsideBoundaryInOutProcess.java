package com.revolsys.gis.parallel;

import com.revolsys.data.record.Record;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class OutsideBoundaryInOutProcess extends
  BaseInOutProcess<Record, Record> {

  private OutsideBoundaryObjects outsideBoundaryObjects;

  public OutsideBoundaryObjects getOutsideBoundaryObjects() {
    return outsideBoundaryObjects;
  }

  @Override
  protected void process(final Channel<Record> in,
    final Channel<Record> out, final Record object) {
    if (outsideBoundaryObjects.boundaryContains(object)) {
      outsideBoundaryObjects.removeObject(object);
      out.write(object);
    } else {
      outsideBoundaryObjects.addObject(object);
    }
  }

  public void setOutsideBoundaryObjects(
    final OutsideBoundaryObjects outsideBoundaryObjects) {
    this.outsideBoundaryObjects = outsideBoundaryObjects;
  }

}
