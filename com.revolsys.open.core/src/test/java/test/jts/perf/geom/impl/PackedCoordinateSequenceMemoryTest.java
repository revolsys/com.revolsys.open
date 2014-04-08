package test.jts.perf.geom.impl;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.CoordinateArraySequenceFactory;
import com.revolsys.jts.util.GeometricShapeFactory;

public class PackedCoordinateSequenceMemoryTest {

  static final int GEOMS = 1000;

  static final int GEOM_SIZE = 1000;

  public static void main(final String args[]) {
    final PackedCoordinateSequenceMemoryTest test = new PackedCoordinateSequenceMemoryTest();
    test.run();
  }

  // PackedCoordinateSequenceFactory coordSeqFact = new
  // PackedCoordinateSequenceFactory(PackedCoordinateSequenceFactory.DOUBLE, 2);
  CoordinateArraySequenceFactory coordSeqFact = CoordinateArraySequenceFactory.instance();

  GeometryFactory geomFact = new GeometryFactory(this.coordSeqFact);

  PackedCoordinateSequenceMemoryTest() {

  }

  Geometry createGeometry() {
    final GeometricShapeFactory shapeFact = new GeometricShapeFactory(
      this.geomFact);
    shapeFact.setSize(100.0);
    shapeFact.setNumPoints(GEOM_SIZE);
    return shapeFact.createCircle();
  }

  void run() {
    runToMemoryOverflow();
  }

  void runToMemoryOverflow() {
    final List geoms = new ArrayList();
    while (true) {
      geoms.add(createGeometry());
      System.out.println(geoms.size());
    }
  }
}
