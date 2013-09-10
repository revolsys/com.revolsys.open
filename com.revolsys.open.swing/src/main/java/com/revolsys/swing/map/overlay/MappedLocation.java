package com.revolsys.swing.map.overlay;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.wkt.WktWriter;
import com.revolsys.swing.map.layer.raster.filter.WarpAffineFilter;
import com.revolsys.swing.map.layer.raster.filter.WarpFilter;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class MappedLocation implements MapSerializer {
  private final Coordinates sourcePixel;

  private final Point targetPoint;

  public MappedLocation(final Coordinates sourcePixel, final Point targetPoint) {
    this.sourcePixel = sourcePixel;
    this.targetPoint = targetPoint;
  }

  public MappedLocation(final Map<String, Object> map) {
    final double sourceX = CollectionUtil.getDouble(map, "sourceX", 0.0);
    final double sourceY = CollectionUtil.getDouble(map, "sourceY", 0.0);
    this.sourcePixel = new DoubleCoordinates(sourceX, sourceY);
    this.targetPoint = GeometryFactory.getFactory().createGeometry(
      (String)map.get("target"));
  }

  public double getAccuracy() {
    // TODO Auto-generated method stub
    return 0;
  }

  public Coordinates getSourcePixel() {
    return sourcePixel;
  }

  public LineString getSourceToTargetLine(final WarpFilter filter) {
    if (filter == null) {
      return null;
    } else {
      final Coordinates sourcePixel = getSourcePixel();
      final Coordinates sourcePoint = filter.sourcePixelToTargetPoint(sourcePixel);
      final GeometryFactory geometryFactory = filter.getGeometryFactory();
      return geometryFactory.createLineString(sourcePoint, getTargetPoint());
    }
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
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("sourceX", sourcePixel.getX());
    map.put("sourceY", sourcePixel.getY());
    map.put("target", WktWriter.toString(targetPoint, true));
    return map;
  }

  @Override
  public String toString() {
    return sourcePixel + "->" + targetPoint;
  }
}
