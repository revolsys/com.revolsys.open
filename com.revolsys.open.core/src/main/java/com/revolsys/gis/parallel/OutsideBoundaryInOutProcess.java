package com.revolsys.gis.parallel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class OutsideBoundaryInOutProcess extends
  BaseInOutProcess<DataObject, DataObject> {

  private OutsideBoundaryObjects outsideBoundaryObjects;

  public OutsideBoundaryObjects getOutsideBoundaryObjects() {
    return outsideBoundaryObjects;
  }

  @Override
  protected void process(
    final Channel<DataObject> in,
    final Channel<DataObject> out,
    final DataObject object) {
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
