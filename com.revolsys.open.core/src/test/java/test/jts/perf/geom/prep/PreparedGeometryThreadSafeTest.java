package test.jts.perf.geom.prep;

import test.jts.perf.ThreadTestCase;
import test.jts.perf.ThreadTestRunner;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.geom.prep.PreparedGeometry;
import com.revolsys.jts.geom.prep.PreparedGeometryFactory;
import com.revolsys.jts.geom.util.SineStarFactory;

/**
 * Tests for race conditons in the PreparedGeometry classes.
 * 
 * @author Martin Davis
 *
 */
public class PreparedGeometryThreadSafeTest extends ThreadTestCase {
  public static void main(final String[] args) {
    ThreadTestRunner.run(new PreparedGeometryThreadSafeTest());
  }

  int nPts = 1000;

  GeometryFactory factory = new GeometryFactory(new PrecisionModel(1.0));

  protected PreparedGeometry pg;

  protected Geometry g;

  public PreparedGeometryThreadSafeTest() {

  }

  Geometry createSineStar(final Coordinate origin, final double size,
    final int nPts) {
    final SineStarFactory gsf = new SineStarFactory(this.factory);
    gsf.setCentre(origin);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    gsf.setArmLengthRatio(0.1);
    gsf.setNumArms(20);
    final Geometry poly = gsf.createSineStar();
    return poly;
  }

  @Override
  public Runnable getRunnable(final int threadIndex) {
    return new Runnable() {

      @Override
      public void run() {
        while (true) {
          System.out.println(threadIndex);
          PreparedGeometryThreadSafeTest.this.pg.intersects(PreparedGeometryThreadSafeTest.this.g);
        }
      }

    };
  }

  @Override
  public void setup() {
    final Geometry sinePoly = createSineStar(new Coordinate(0, 0), 100000.0,
      this.nPts);
    this.pg = PreparedGeometryFactory.prepare(sinePoly);
    this.g = createSineStar(new Coordinate(10, 10), 100000.0, 100);
  }
}
