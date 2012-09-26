package com.revolsys.gis.model.geometry.impl;

import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.gis.model.geometry.MultiLinearRing;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.gis.model.geometry.operation.PolygonContains;
import com.revolsys.gis.model.geometry.operation.PolygonCovers;
import com.revolsys.gis.model.geometry.operation.chain.SegmentString;
import com.revolsys.gis.model.geometry.operation.chain.SegmentStringUtil;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.FastSegmentSetIntersectionFinder;
import com.revolsys.gis.model.geometry.util.PolygonUtil;

public class PolygonImpl extends GeometryImpl implements Polygon,
  Iterable<LinearRing> {

  private MultiLinearRing rings;

  protected PolygonImpl(GeometryFactory geometryFactory, List<LinearRing> rings) {
    super(geometryFactory);
    this.rings = geometryFactory.createMultiLinearRing(rings);
  }

  @Override
  public Iterator<LinearRing> iterator() {
    List<LinearRing> geometries = rings.getGeometries();
    return geometries.iterator();
  }

  public int getBoundaryDimension() {
    return 1;
  }

  public double getLength() {
    double length = 0.0;
    for (LinearRing ring : getRings()) {
      length += ring.getLength();
    }
    return length;
  }

  public double getArea() {
    double area = 0.0;
    MultiLinearRing rings = getRings();
    LinearRing exteriorRing = rings.getGeometry(0);
    area += Math.abs(CoordinatesListUtil.signedArea(exteriorRing));
    for (int i = 1; i < rings.getGeometryCount(); i++) {
      LinearRing ring = rings.getGeometry(i);
      area -= Math.abs(CoordinatesListUtil.signedArea(ring));
    }
    return area;
  }

  @Override
  public PolygonImpl clone() {
    return (PolygonImpl)super.clone();
  }

  @Override
  public MultiLinearRing getRings() {
    return rings;
  }

  @Override
  public boolean isEmpty() {
    return rings.isEmpty();
  }

  @Override
  public List<CoordinatesList> getCoordinatesLists() {
    return rings.getCoordinatesLists();
  }

  @Override
  public LinearRing getRing(int index) {
    return getRings().getGeometry(index);
  }

  @Override
  public LinearRing getExteriorRing() {
    return getRing(0);
  }

  @Override
  public int getRingCount() {
    return rings.getGeometryCount();
  }

  @Override
  public int getDimension() {
    return 2;
  }

  @Override
  protected boolean doIntersects(Geometry geometry) {
    /**
     * Do point-in-poly tests first, since they are cheaper and may result in a
     * quick positive result. If a point of any test components lie in target,
     * result is true
     */
    boolean isInPrepGeomArea = PolygonUtil.isAnyTestComponentInTarget(this,
      geometry);
    if (isInPrepGeomArea) {
      return true;
    } else {
      /**
       * If any segments intersect, result is true
       */
      List<SegmentString> lineSegStr = SegmentStringUtil.extractSegmentStrings(geometry);
      boolean segsIntersect = FastSegmentSetIntersectionFinder.get(this)
        .intersects(lineSegStr);
      if (segsIntersect) {
        return true;
      } else {
        /**
         * If the test has dimension = 2 as well, it is necessary to test for
         * proper inclusion of the target. Since no segments intersect, it is
         * sufficient to test representative points.
         */
        if (geometry.getDimension() == 2) {
          // TODO: generalize this to handle GeometryCollections
          boolean isPrepGeomInArea = PolygonUtil.isAnyTargetComponentInAreaTest(
            geometry, getCoordinatesLists());
          if (isPrepGeomInArea) {
            return true;
          }
        }
      }
      return false;
    }
  }

  protected boolean doCovers(Geometry geometry) {
    return new PolygonCovers(this).covers(geometry);
  }

  @Override
  protected boolean doContains(Geometry geometry) {
    return new PolygonContains(this).contains(geometry);
  }
}
