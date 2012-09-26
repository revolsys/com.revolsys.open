package com.revolsys.gis.model.geometry.operation;

import java.util.List;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.gis.model.geometry.Polygonal;
import com.revolsys.gis.model.geometry.operation.chain.SegmentIntersectionDetector;
import com.revolsys.gis.model.geometry.operation.chain.SegmentString;
import com.revolsys.gis.model.geometry.operation.chain.SegmentStringUtil;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.FastSegmentSetIntersectionFinder;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.LineIntersector;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.RobustLineIntersector;
import com.revolsys.gis.model.geometry.util.PolygonUtil;

abstract class AbstractPolygonContains extends PolygonPredicate {
  protected boolean requireSomePointInInterior = true;

  private boolean hasSegmentIntersection = false;

  private boolean hasProperIntersection = false;

  private boolean hasNonProperIntersection = false;

  public AbstractPolygonContains(final Polygon polygon) {
    super(polygon);
  }

  protected boolean eval(final Geometry geometry) {
    final boolean isAllInTargetArea = PolygonUtil.isAllTestComponentsInTarget(polygon,geometry);
    if (!isAllInTargetArea) {
      return false;
    }

    if (requireSomePointInInterior && geometry.getDimension() == 0) {
      final boolean isAnyInTargetInterior = PolygonUtil.isAnyTestComponentInTargetInterior(polygon,geometry);
      return isAnyInTargetInterior;
    } else {

      final boolean properIntersectionImpliesNotContained = isProperIntersectionImpliesNotContainedSituation(geometry);

      findAndClassifyIntersections(geometry);

      if (properIntersectionImpliesNotContained && hasProperIntersection) {
        return false;
      } else if (hasSegmentIntersection && !hasNonProperIntersection) {
        return false;
      } else if (hasSegmentIntersection) {
        return fullTopologicalPredicate(geometry);
      } else if (geometry instanceof Polygonal) {
        final boolean isTargetInTestArea = PolygonUtil.isAnyTargetComponentInAreaTest(
          geometry, polygon.getCoordinatesLists());
        if (isTargetInTestArea) {
          return false;
        }
      }
      return true;
    }
  }

  private void findAndClassifyIntersections(final Geometry geometry) {
    final List<SegmentString> lineSegStr = SegmentStringUtil.extractSegmentStrings(geometry);

    final LineIntersector li = new RobustLineIntersector();
    final SegmentIntersectionDetector intDetector = new SegmentIntersectionDetector(
      li);
    intDetector.setFindAllIntersectionTypes(true);
    FastSegmentSetIntersectionFinder.get(polygon).intersects(lineSegStr, intDetector);

    hasSegmentIntersection = intDetector.hasIntersection();
    hasProperIntersection = intDetector.hasProperIntersection();
    hasNonProperIntersection = intDetector.hasNonProperIntersection();
  }

  protected abstract boolean fullTopologicalPredicate(Geometry geometry);

  private boolean isProperIntersectionImpliesNotContainedSituation(
    final Geometry geometry) {
    if (geometry instanceof Polygonal) {
      return true;
    } else if (isSingleShell(polygon)) {
      return true;
    } else {
      return false;
    }
  }

  private boolean isSingleShell(final Geometry geometry) {
    if (geometry.getGeometryCount() == 1) {
      final Polygon poly = geometry.getGeometry(0);
      final int numRings = poly.getRingCount();
      if (numRings == 1) {
        return true;
      }
    }
    return false;
  }

}
