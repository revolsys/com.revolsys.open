package com.revolsys.gis.parallel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.jts.geom.Geometry;

public class GeometryBufferProcess extends
  BaseInOutProcess<DataObject, DataObject> {

  private int buffer;

  public int getBuffer() {
    return buffer;
  }

  @Override
  protected void process(final Channel<DataObject> in,
    final Channel<DataObject> out, final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry == null) {
      out.write(object);
    } else {
      final Geometry bufferedGeometry = geometry.buffer(buffer);
      final DataObject newObject = DataObjectUtil.copy(object, bufferedGeometry);
      out.write(newObject);
    }
  }

  public void setBuffer(final int buffer) {
    this.buffer = buffer;
  }

}
