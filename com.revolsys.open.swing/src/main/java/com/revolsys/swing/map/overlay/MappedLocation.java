package com.revolsys.swing.map.overlay;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.swing.map.layer.raster.filter.WarpAffineFilter;
import com.revolsys.swing.map.layer.raster.filter.WarpFilter;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class MappedLocation {
  private Coordinates sourcePixel;

  private Point targetPoint;

  public MappedLocation(final Coordinates sourcePixel, final Point targetPoint) {
    this.sourcePixel = sourcePixel;
    this.targetPoint = targetPoint;
  }

  public MappedLocation(final int dimension) {
  }

  public double getAccuracy() {
    // TODO Auto-generated method stub
    return 0;
  }

  public Coordinates getSourcePixel() {
    return sourcePixel;
  }

  public LineString getSourceToTargetLine(final WarpFilter filter) {
    final Coordinates sourcePixel = getSourcePixel();
    final Coordinates sourcePoint = filter.sourcePixelToTargetPoint(sourcePixel);
    final GeometryFactory geometryFactory = filter.getGeometryFactory();
    return geometryFactory.createLineString(sourcePoint, getTargetPoint());
  }

  public Coordinates getTargetPixel(final BoundingBox boundingBox,
    final int imageWidth, final int imageHeight) {
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final Coordinates targetPointCoordinates = CoordinatesUtil.get(geometryFactory.copy(targetPoint));
    return WarpAffineFilter.targetPointToPixel(boundingBox,
      targetPointCoordinates, imageWidth, imageHeight);
  }

  public Point getTargetPoint() {
    return targetPoint;
  }

  @Override
  public String toString() {
    return sourcePixel + "->" + targetPoint;
  }
}
