package com.revolsys.swing.map.overlay;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.beans.AbstractPropertyChangeObject;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.wkt.WktWriter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.swing.map.layer.raster.filter.WarpAffineFilter;
import com.revolsys.swing.map.layer.raster.filter.WarpFilter;
import com.revolsys.util.CollectionUtil;

public class MappedLocation extends AbstractPropertyChangeObject implements
  MapSerializer {
  private Point sourcePixel;

  private Point targetPoint;

  public MappedLocation(final Map<String, Object> map) {
    final double sourceX = CollectionUtil.getDouble(map, "sourceX", 0.0);
    final double sourceY = CollectionUtil.getDouble(map, "sourceY", 0.0);
    this.sourcePixel = new PointDouble(sourceX, sourceY);
    this.targetPoint = GeometryFactory.getFactory().geometry(
      (String)map.get("target"));
  }

  public MappedLocation(final Point sourcePixel, final Point targetPoint) {
    this.sourcePixel = sourcePixel;
    this.targetPoint = targetPoint;
  }

  public Point getSourcePixel() {
    return sourcePixel;
  }

  public Point getSourcePoint(final WarpFilter filter,
    final BoundingBox boundingBox) {
    if (filter == null) {
      return null;
    } else {
      final Point sourcePixel = getSourcePixel();
      final Point sourcePoint = filter.sourcePixelToTargetPoint(boundingBox,
        sourcePixel);
      final com.revolsys.jts.geom.GeometryFactory geometryFactory = filter.getGeometryFactory();
      return geometryFactory.point(sourcePoint);
    }
  }

  public LineString getSourceToTargetLine(final WarpFilter filter) {
    if (filter == null) {
      return null;
    } else {
      final com.revolsys.jts.geom.GeometryFactory geometryFactory = filter.getGeometryFactory();
      final Point sourcePixel = getSourcePixel();
      final Point sourcePoint = filter.sourcePixelToTargetPoint(sourcePixel);
      final Point targetPoint = getTargetPoint();
      return geometryFactory.lineString(sourcePoint, targetPoint);
    }
  }

  public LineString getSourceToTargetLine(final WarpFilter filter,
    final BoundingBox boundingBox) {
    if (filter == null) {
      return null;
    } else {
      final Point sourcePixel = getSourcePixel();
      final Point sourcePoint = filter.sourcePixelToTargetPoint(boundingBox,
        sourcePixel);
      final com.revolsys.jts.geom.GeometryFactory geometryFactory = filter.getGeometryFactory();
      return geometryFactory.lineString(sourcePoint, getTargetPoint());
    }
  }

  public Point getTargetPixel(final BoundingBox boundingBox,
    final int imageWidth, final int imageHeight) {
    final com.revolsys.jts.geom.GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final Point targetPointCoordinates = (Point)targetPoint.copy(geometryFactory);
    return WarpAffineFilter.targetPointToPixel(boundingBox,
      targetPointCoordinates, imageWidth, imageHeight);
  }

  public Point getTargetPoint() {
    return targetPoint;
  }

  public void setSourcePixel(final Point sourcePixel) {
    final Object oldValue = this.sourcePixel;
    this.sourcePixel = sourcePixel;
    firePropertyChange("sourcePixel", oldValue, sourcePixel);
  }

  public void setTargetPoint(final Point targetPoint) {
    final Object oldValue = this.targetPoint;
    this.targetPoint = targetPoint;
    firePropertyChange("targetPoint", oldValue, targetPoint);
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
