package com.revolsys.gis.parallel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.AbstractInOutProcess;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryBufferProcess extends AbstractInOutProcess<DataObject> {

  private int buffer;

  public int getBuffer() {
    return buffer;
  }

  @Override
  protected void run(
    final Channel<DataObject> in,
    final Channel<DataObject> out) {
    while (true) {
      final DataObject object = in.read();
      if (object != null) {
        final Geometry geometry = object.getGeometryValue();
        if (geometry == null) {
          out.write(object);
        } else {
          final Geometry bufferedGeometry = geometry.buffer(buffer);
          final DataObject newObject = DataObjectUtil.copy(object,
            bufferedGeometry);
          out.write(newObject);
        }
      }
    }
  }

  public void setBuffer(
    final int buffer) {
    this.buffer = buffer;
  }

}
