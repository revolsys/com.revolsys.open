package com.revolsys.jtstest.testbuilder.geom;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.CoordinateSequenceFilter;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;

public class VertexLocater {
  public static class Location {
    private final Coordinates pt;

    private final int[] index;

    Location(final Coordinates pt, final int index) {
      this.pt = pt;
      this.index = new int[1];
      this.index[0] = index;
    }

    public Coordinates getCoordinate() {
      return pt;
    }

    public int[] getIndices() {
      return index;
    }
  }

  static class NearestVertexFilter implements CoordinateSequenceFilter {
    private double tolerance = 0.0;

    private final Coordinates basePt;

    private Coordinates nearestPt = null;

    private int vertexIndex = -1;

    public NearestVertexFilter(final Coordinates basePt, final double tolerance) {
      this.basePt = basePt;
      this.tolerance = tolerance;
    }

    @Override
    public void filter(final CoordinatesList seq, final int i) {
      final Coordinates p = seq.getCoordinate(i);
      final double dist = p.distance(basePt);
      if (dist > tolerance) {
        return;
      }

      nearestPt = p;
      vertexIndex = i;

    }

    public int getIndex() {
      return vertexIndex;
    }

    public Coordinates getVertex() {
      return nearestPt;
    }

    @Override
    public boolean isDone() {
      return nearestPt != null;
    }

    @Override
    public boolean isGeometryChanged() {
      return false;
    }
  }

  static class NearVerticesFilter implements CoordinateSequenceFilter {
    private double tolerance = 0.0;

    private final Coordinates queryPt;

    private final List locations = new ArrayList();

    public NearVerticesFilter(final Coordinates queryPt, final double tolerance) {
      this.queryPt = queryPt;
      this.tolerance = tolerance;
    }

    @Override
    public void filter(final CoordinatesList seq, final int i) {
      final Coordinates p = seq.getCoordinate(i);
      final double dist = p.distance(queryPt);
      if (dist > tolerance) {
        return;
      }

      locations.add(new Location(p, i));

    }

    public List getLocations() {
      return locations;
    }

    @Override
    public boolean isDone() {
      // evaluate all points
      return false;
    }

    @Override
    public boolean isGeometryChanged() {
      return false;
    }
  }

  public static Coordinates locateVertex(final Geometry geom,
    final Coordinates testPt, final double tolerance) {
    final VertexLocater finder = new VertexLocater(geom);
    return finder.getVertex(testPt, tolerance);
  }

  private final Geometry geom;

  private Coordinates vertexPt;

  private int vertexIndex = -1;

  public VertexLocater(final Geometry geom) {
    this.geom = geom;
  }

  public int getIndex() {
    return vertexIndex;
  }

  public List getLocations(final Coordinates testPt, final double tolerance) {
    final NearVerticesFilter filter = new NearVerticesFilter(testPt, tolerance);
    geom.apply(filter);
    return filter.getLocations();
  }

  public Coordinates getVertex(final Coordinates testPt, final double tolerance) {
    final NearestVertexFilter filter = new NearestVertexFilter(testPt,
      tolerance);
    geom.apply(filter);
    vertexPt = filter.getVertex();
    vertexIndex = filter.getIndex();
    return vertexPt;
  }
}
