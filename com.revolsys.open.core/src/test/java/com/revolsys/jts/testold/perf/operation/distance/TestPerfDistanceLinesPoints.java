package com.revolsys.jts.testold.perf.operation.distance;

import java.util.List;

import com.revolsys.jts.densify.Densifier;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.io.WKTFileReader;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.operation.distance.DistanceOp;
import com.revolsys.jts.operation.distance.IndexedFacetDistance;
import com.revolsys.jts.util.Stopwatch;

/**
 * Tests performance of {@link IndexedFacetDistance} versus standard 
 * {@link DistanceOp}
 * using a grid of points to a target set of lines 
 * 
 * @author Martin Davis
 *
 */
public class TestPerfDistanceLinesPoints {
  static final boolean USE_INDEXED_DIST = true;

  static GeometryFactory geomFact = GeometryFactory.getFactory();

  static final int MAX_ITER = 1;

  static final int NUM_TARGET_ITEMS = 4000;

  static final double EXTENT = 1000;

  static final int NUM_PTS_SIDE = 100;

  public static void main(final String[] args) {
    final TestPerfDistanceLinesPoints test = new TestPerfDistanceLinesPoints();
    try {
      test.test();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  boolean verbose = true;

  public TestPerfDistanceLinesPoints() {
  }

  void computeDistance(final Geometry[] pts, final Geometry geom) {
    IndexedFacetDistance bbd = null;
    if (USE_INDEXED_DIST) {
      bbd = new IndexedFacetDistance(geom);
    }
    for (final Geometry pt : pts) {
      if (USE_INDEXED_DIST) {
        final double dist = bbd.getDistance(pt);
        // double dist = bbd.getDistanceWithin(pts[i].getCoordinate(), 100000);
      } else {
        final double dist = geom.distance(pt);
      }
    }
  }

  Geometry createDiagonalCircles(final double extent, final int nSegs) {
    final Polygon[] circles = new Polygon[nSegs];
    final double inc = extent / nSegs;
    for (int i = 0; i < nSegs; i++) {
      final double ord = i * inc;
      final Coordinates p = new Coordinate((double)ord, ord, Coordinates.NULL_ORDINATE);
      final Geometry pt = geomFact.point(p);
      circles[i] = (Polygon)pt.buffer(inc / 2);
    }
    return geomFact.createMultiPolygon(circles);

  }

  Geometry createDiagonalLine(final double extent, final int nSegs) {
    final Coordinates[] pts = new Coordinates[nSegs + 1];
    pts[0] = new Coordinate((double)0, 0, Coordinates.NULL_ORDINATE);
    final double inc = extent / nSegs;
    for (int i = 1; i <= nSegs; i++) {
      final double ord = i * inc;
      pts[i] = new Coordinate((double)ord, ord, Coordinates.NULL_ORDINATE);
    }
    return geomFact.lineString(pts);
  }

  Geometry createLine(final double extent, final int nSegs) {
    final Coordinates[] pts = new Coordinates[] {
      new Coordinate((double)0, 0, Coordinates.NULL_ORDINATE), new Coordinate((double)0, extent, Coordinates.NULL_ORDINATE),
      new Coordinate((double)extent, extent, Coordinates.NULL_ORDINATE), new Coordinate((double)extent, 0, Coordinates.NULL_ORDINATE)

    };
    final Geometry outline = geomFact.lineString(pts);
    final double inc = extent / nSegs;
    return Densifier.densify(outline, inc);

  }

  Geometry[] createPoints(final Envelope extent, final int nPtsSide) {
    final Geometry[] pts = new Geometry[nPtsSide * nPtsSide];
    int index = 0;
    final double xinc = extent.getWidth() / nPtsSide;
    final double yinc = extent.getHeight() / nPtsSide;
    for (int i = 0; i < nPtsSide; i++) {
      for (int j = 0; j < nPtsSide; j++) {
        pts[index++] = geomFact.point(new Coordinate((double)extent.getMinX() + i
          * xinc, extent.getMinY() + j * yinc, Coordinates.NULL_ORDINATE));
      }
    }
    return pts;
  }

  Geometry loadData(final String file) throws Exception {
    final List geoms = loadWKT(file);
    return geomFact.buildGeometry(geoms);
  }

  List loadWKT(final String filename) throws Exception {
    final WKTReader rdr = new WKTReader();
    final WKTFileReader fileRdr = new WKTFileReader(filename, rdr);
    return fileRdr.read();
  }

  public void test() throws Exception {

    // test(200);
    // if (true) return;

    // test(5000);
    // test(8001);

    // test(50);
    test(100);
    test(200);
    test(500);
    test(1000);
    // test(5000);
    // test(10000);
    // test(50000);
    // test(100000);
  }

  public void test(final Geometry[] pts, final Geometry target) {
    if (this.verbose) {
      System.out.println("Query points = " + pts.length
        + "     Target points = " + target.getVertexCount());
      // if (! verbose) System.out.print(num + ", ");
    }

    final Stopwatch sw = new Stopwatch();
    final double dist = 0.0;
    for (int i = 0; i < MAX_ITER; i++) {
      computeDistance(pts, target);
    }
    if (!this.verbose) {
      System.out.println(sw.getTimeString());
    }
    if (this.verbose) {
      final String name = USE_INDEXED_DIST ? "IndexedFacetDistance"
        : "Distance";
      System.out.println(name + " - Run time: " + sw.getTimeString());
      // System.out.println("       (Distance = " + dist + ")\n");
      System.out.println();
    }
  }

  public void test(final int num) throws Exception {
    // Geometry lines = createLine(EXTENT, num);
    final Geometry target = createDiagonalCircles(EXTENT, NUM_TARGET_ITEMS);
    final Geometry[] pts = createPoints(target.getEnvelopeInternal(), num);

    /*
     * Geometry target =
     * loadData("C:\\data\\martin\\proj\\jts\\testing\\distance\\bc_coast.wkt");
     * Envelope bcEnv_Albers = new Envelope(-45838, 1882064, 255756, 1733287);
     * Geometry[] pts = createPoints(bcEnv_Albers, num);
     */
    test(pts, target);
  }

  public void xtest(final int num) throws Exception {
    final Geometry target = loadData("C:\\proj\\JTS\\test\\g2e\\ffmwdec08.wkt");
    final Envelope bcEnv_Albers = new Envelope(-45838, 1882064, 255756, 1733287);
    final Geometry[] pts = createPoints(bcEnv_Albers, num);

    test(pts, target);
  }

}
