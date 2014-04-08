package test.jts.perf.dissolve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import test.jts.junit.GeometryUtils;
import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

import com.revolsys.jts.dissolve.LineDissolver;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.util.LinearComponentExtracter;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.operation.linemerge.LineMerger;
import com.revolsys.jts.util.Memory;

public class LineDissolvePerfTest extends PerformanceTestCase {
  public static void main(final String args[]) {
    PerformanceTestRunner.run(LineDissolvePerfTest.class);
  }

  Collection data;

  public LineDissolvePerfTest(final String name) {
    super(name);
    setRunSize(new int[] {
      1, 2, 3, 4, 5
    });
    setRunIterations(1);
  }

  private Geometry dissolveLines(final Collection lines) {
    final Geometry linesGeom = extractLines(lines);
    return dissolveLines(linesGeom);
  }

  private Geometry dissolveLines(final Geometry lines) {
    final Geometry dissolved = lines.union();
    final LineMerger merger = new LineMerger();
    merger.add(dissolved);
    final Collection mergedColl = merger.getMergedLineStrings();
    final Geometry merged = lines.getFactory().buildGeometry(mergedColl);
    return merged;
  }

  Geometry extractLines(final Collection geoms) {
    GeometryFactory factory = null;
    final List lines = new ArrayList();
    for (final Iterator i = geoms.iterator(); i.hasNext();) {
      final Geometry g = (Geometry)i.next();
      if (factory == null) {
        factory = g.getFactory();
      }
      lines.addAll(LinearComponentExtracter.getLines(g));
    }
    return factory.buildGeometry(geoms);
  }

  public void runBruteForce_World() {
    final Geometry result = dissolveLines(this.data);
    System.out.println(Memory.allString());
  }

  public void runDissolver_World() {
    final LineDissolver dis = new LineDissolver();
    dis.add(this.data);
    final Geometry result = dis.getResult();
    System.out.println();
    System.out.println(Memory.allString());
  }

  @Override
  public void setUp() throws IOException, ParseException {
    System.out.println("Loading data...");
    this.data = GeometryUtils.readWKTFile("/Users/mdavis/myproj/jts/svn/jts-topo-suite/trunk/jts/testdata/world.wkt");
  }

}
