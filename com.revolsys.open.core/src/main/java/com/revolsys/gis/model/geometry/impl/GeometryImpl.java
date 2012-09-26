package com.revolsys.gis.model.geometry.impl;

import java.util.Collections;
import java.util.List;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.operation.overlay.OverlayOp;
import com.revolsys.gis.model.geometry.operation.overlay.SnapIfNeededOverlayOp;
import com.revolsys.gis.model.geometry.operation.relate.RelateOp;
import com.revolsys.gis.model.geometry.util.WktWriter;
import com.revolsys.io.AbstractObjectWithProperties;
import com.vividsolutions.jts.geom.IntersectionMatrix;

public abstract class GeometryImpl extends AbstractObjectWithProperties
  implements Geometry {
  private final GeometryFactory geometryFactory;

  private BoundingBox boundingBox;

  protected GeometryImpl(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @Override
  public Geometry buffer(final double value) {
    // TODO Auto-generated method stub
    return this;
  }

  @Override
  public GeometryImpl clone() {
    try {
      return (GeometryImpl)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new UnsupportedOperationException();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <G extends Geometry> G cloneGeometry() {
    return (G)clone();
  }

  @Override
  public boolean contains(Geometry geometry) {
    final BoundingBox boundingBox = getBoundingBox();
    final BoundingBox boundingBox2 = getConvertedBoundingBox(geometry);
    if (boundingBox.contains(boundingBox2)) {
      geometry = getConvertedGeometry(geometry);
      // // optimization for rectangle arguments
      // if (isRectangle()) {
      // return RectangleContains.contains((Polygon) this, g);
      // }
      // general case
      return doContains(geometry);
    } else {
      return false;
    }

  }

  @Override
  public boolean coveredBy(Geometry geometry) {
    geometry = getConvertedGeometry(geometry);
    return geometry.covers(this);
  }

  @Override
  public boolean covers(Geometry geometry) {

    final BoundingBox boundingBox = getBoundingBox();
    final BoundingBox boundingBox2 = getConvertedBoundingBox(geometry);
    if (boundingBox.covers(boundingBox2)) {
      // // optimization for rectangle arguments
      // if (isRectangle) {
      // return true;
      // }
      geometry = getConvertedGeometry(geometry);
      return doCovers(geometry);
    } else {
      return false;
    }
  }

  @Override
  public boolean crosses(Geometry geometry) {
    final BoundingBox boundingBox2 = getConvertedBoundingBox(geometry);
    final BoundingBox boundingBox = getBoundingBox();
    if (boundingBox.intersects(boundingBox2)) {
      geometry = getConvertedGeometry(geometry);
      return doCrossses(geometry);
    } else {
      return false;
    }
  }

  @Override
  public boolean disjoint(final Geometry geometry) {
    return !intersects(geometry);
  }

  public double getArea() {
    return 0;
  }

  public double getLength() {
    return 0.0;
  }

  protected boolean doContains(final Geometry geometry) {
    return relate(geometry).isContains();
  }

  protected boolean doCovers(final Geometry geometry) {
    return relate(geometry).isCovers();
  }

  protected boolean doCrossses(final Geometry geometry) {
    return relate(geometry).isCrosses(getDimension(), geometry.getDimension());
  }

  protected boolean doIntersects(final Geometry geometry) {
    return relate(geometry).isIntersects();
  }

  public boolean touches(Geometry geometry) {
    final BoundingBox boundingBox2 = getConvertedBoundingBox(geometry);
    final BoundingBox boundingBox = getBoundingBox();
    if (boundingBox.intersects(boundingBox2)) {
      geometry = getConvertedGeometry(geometry);
      return doTouches(geometry);
    } else {
      return false;
    }
  }

  protected boolean doTouches(Geometry geometry) {
    return relate(geometry).isTouches(getDimension(), geometry.getDimension());
  }

  public boolean within(Geometry geometry) {
    geometry = getConvertedGeometry(geometry);
    return geometry.contains(this);
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (boundingBox == null) {
      boundingBox = new BoundingBox(this);
    }
    return boundingBox;
  }

  protected BoundingBox getConvertedBoundingBox(final Geometry geometry) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    final com.revolsys.gis.model.geometry.GeometryFactory geometryFactory = getGeometryFactory();
    return boundingBox.convert(geometryFactory);
  }

  protected Geometry getConvertedGeometry(Geometry geometry) {
    final com.revolsys.gis.model.geometry.GeometryFactory geometryFactory = getGeometryFactory();
    geometry = geometryFactory.getGeometry(geometry);
    return geometry;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <G extends Geometry> List<G> getGeometries() {
    return (List<G>)Collections.singletonList(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <G extends Geometry> G getGeometry(final int i) {
    return (G)getGeometries().get(0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <F extends com.revolsys.gis.model.geometry.GeometryFactory> F getGeometryFactory() {
    return (F)geometryFactory;
  }

  @Override
  public byte getNumAxis() {
    return geometryFactory.getNumAxis();
  }

  @Override
  public int getGeometryCount() {
    return 1;
  }

  @Override
  public int getSrid() {
    return geometryFactory.getCoordinateSystem().getId();
  }

  @Override
  public Geometry intersection(Geometry geometry) {
    // TODO: MD - add optimization for P-A case using Point-In-Polygon
    final com.revolsys.gis.model.geometry.GeometryFactory geometryFactory = getGeometryFactory();
    if (this.isEmpty() || geometry.isEmpty()) {
      return geometryFactory.createGeometryCollection();
      // } else if (isGeometryCollection(this)) {
      // final Geometry g2 = other;
      // return GeometryCollectionMapper.map((GeometryCollection)this,
      // new GeometryCollectionMapper.MapOp() {
      // public Geometry map(Geometry g) {
      // return g.intersection(g2);
      // }
      // });
    } else {
      // if (isGeometryCollection(other))
      // return other.intersection(this);

      // checkNotGeometryCollection(this);
      // checkNotGeometryCollection(other);
      geometry = getConvertedGeometry(geometry);
      return SnapIfNeededOverlayOp.overlayOp(this, geometry,
        OverlayOp.INTERSECTION);
    }
  }

  public boolean intersects(Geometry geometry) {
    final BoundingBox boundingBox = getBoundingBox();
    final BoundingBox boundingBox2 = getConvertedBoundingBox(geometry);
    if (boundingBox.intersects(boundingBox2)) {
      // if (isRectangle()) {
      // return RectangleIntersects.intersects((Polygon) this, g);
      // }
      // if (g.isRectangle()) {
      // return RectangleIntersects.intersects((Polygon) g, this);
      // }
      // general case
      geometry = getConvertedGeometry(geometry);
      return doIntersects(geometry);
    } else {
      return false;
    }
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public boolean isValid() {
    return false;
  }

  @Override
  public IntersectionMatrix relate(Geometry geometry) {
    geometry = getConvertedGeometry(geometry);
    return RelateOp.relate(this, geometry);
  }

  @Override
  public String toString() {
    return WktWriter.toString(this);
  }
}
