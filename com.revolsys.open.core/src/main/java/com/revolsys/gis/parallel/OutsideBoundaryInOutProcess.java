package com.revolsys.gis.parallel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class OutsideBoundaryInOutProcess extends
  BaseInOutProcess<DataObject, DataObject> {

  private OutsideBoundaryObjects outsideBoundaryObjects;

  @Override
  protected void process(Channel<DataObject> in, Channel<DataObject> out,
    DataObject object) {
    if (outsideBoundaryObjects.boundaryContains(object)) {
      outsideBoundaryObjects.removeObject(object);
      out.write(object);
    } else {
      outsideBoundaryObjects.addObject(object);
    }
  }

  public OutsideBoundaryObjects getOutsideBoundaryObjects() {
    return outsideBoundaryObjects;
  }

  public void setOutsideBoundaryObjects(
    OutsideBoundaryObjects outsideBoundaryObjects) {
    this.outsideBoundaryObjects = outsideBoundaryObjects;
  }

}
