package com.revolsys.geometry.test.old.perf.geom.prep;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.util.SineStarFactory;
import com.revolsys.geometry.test.old.perf.ThreadTestCase;
import com.revolsys.geometry.test.old.perf.ThreadTestRunner;

/**
 * Tests for race conditons in the Geometry classes.
 *
 * @author Martin Davis
 *
 */
public class PreparedGeometryThreadSafeTest extends ThreadTestCase {
  public static void main(final String[] args) {
    ThreadTestRunner.run(new PreparedGeometryThreadSafeTest());
  }

  GeometryFactory factory = GeometryFactory.fixed(0, 1.0);

  protected Geometry g;

  int nPts = 1000;

  protected Geometry pg;

  public PreparedGeometryThreadSafeTest() {

  }

  @Override
  public Runnable getRunnable(final int threadIndex) {
    return new Runnable() {

      @Override
      public void run() {
        while (true) {
          // System.out.println(threadIndex);
          PreparedGeometryThreadSafeTest.this.pg.intersects(PreparedGeometryThreadSafeTest.this.g);
        }
      }

    };
  }

  Geometry newSineStar(final Point origin, final double size, final int nPts) {
    final SineStarFactory gsf = new SineStarFactory(this.factory);
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    gsf.setArmLengthRatio(0.1);
    gsf.setNumArms(20);
    final Geometry poly = gsf.newSineStar();
    return poly;
  }

  @Override
  public void setup() {
    final Geometry sinePoly = newSineStar(new PointDoubleXY((double)0, 0),
      100000.0, this.nPts);
    this.pg = sinePoly.prepare();
    this.g = newSineStar(new PointDoubleXY((double)10, 10), 100000.0, 100);
  }
}
