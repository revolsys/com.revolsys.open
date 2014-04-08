package test.jts.perf.operation.buffer;

import java.util.Iterator;
import java.util.List;

import test.jts.TestFiles;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.io.WKTFileReader;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.util.Stopwatch;

public class FileBufferPerfTest {
  static final int MAX_ITER = 1;

  static PrecisionModel pm = new PrecisionModel();

  static GeometryFactory fact = new GeometryFactory(pm, 0);

  static WKTReader wktRdr = new WKTReader(fact);

  public static void main(final String[] args) {
    final FileBufferPerfTest test = new FileBufferPerfTest();
    try {
      test.test();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  GeometryFactory factory = GeometryFactory.getFactory();

  boolean testFailed = false;

  public FileBufferPerfTest() {
  }

  void runAll(final List polys, final double distance) {
    System.out.println("Geom count = " + polys.size() + "   distance = "
      + distance);
    final Stopwatch sw = new Stopwatch();
    for (final Iterator i = polys.iterator(); i.hasNext();) {
      final Geometry g = (Geometry)i.next();
      g.buffer(distance);
      System.out.print(".");
    }
    System.out.println();
    System.out.println("   Time = " + sw.getTimeString());
  }

  public void test() throws Exception {
    test(TestFiles.DATA_DIR + "africa.wkt");
    // test(TestFiles.DATA_DIR + "world.wkt");
    // test(TestFiles.DATA_DIR + "bc-250k.wkt");
    // test(TestFiles.DATA_DIR + "bc_20K.wkt");

    // test("C:\\data\\martin\\proj\\jts\\data\\veg.wkt");
  }

  public void test(final String filename) throws Exception {
    final WKTFileReader fileRdr = new WKTFileReader(filename, wktRdr);
    final List polys = fileRdr.read();

    runAll(polys, 0.01);
    runAll(polys, 0.1);
    runAll(polys, 1.0);
    runAll(polys, 10.0);
    runAll(polys, 100.0);
    runAll(polys, 1000.0);
  }
}
