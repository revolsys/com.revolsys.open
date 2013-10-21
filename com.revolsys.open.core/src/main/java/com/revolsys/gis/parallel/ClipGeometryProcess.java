package com.revolsys.gis.parallel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class ClipGeometryProcess extends
  BaseInOutProcess<DataObject, DataObject> {

  private Polygon clipPolygon;

  /**
   * @return the clipPolygon
   */
  public Polygon getClipPolygon() {
    return clipPolygon;
  }

  @Override
  protected void process(final Channel<DataObject> in,
    final Channel<DataObject> out, final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry != null) {
      final Geometry intersection = geometry.intersection(clipPolygon);
      if (!intersection.isEmpty()
        && intersection.getClass() == geometry.getClass()) {
        if (intersection instanceof LineString) {
          final LineString lineString = (LineString)intersection;
          final Coordinate c0 = lineString.getCoordinateN(0);
          if (Double.isNaN(c0.z)) {
            JtsGeometryUtil.addElevation(c0, (LineString)geometry);
          }
          final Coordinate cN = lineString.getCoordinateN(lineString.getNumPoints() - 1);
          if (Double.isNaN(cN.z)) {
            JtsGeometryUtil.addElevation(cN, (LineString)geometry);
          }
        }
        JtsGeometryUtil.copyUserData(geometry, intersection);

        object.setGeometryValue(intersection);
        out.write(object);
      }
    } else {
      out.write(object);
    }
  }

  /**
   * @param clipPolygon the clipPolygon to set
   */
  public void setClipPolygon(final Polygon clipPolygon) {
    this.clipPolygon = clipPolygon;
  }

}
