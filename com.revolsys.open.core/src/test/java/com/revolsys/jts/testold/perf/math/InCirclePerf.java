package com.revolsys.jts.testold.perf.math;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.util.Stopwatch;

/**
 * Test performance of evaluating Triangle predicate computations
 * using 
 * various extended precision APIs.
 * 
 * @author Martin Davis
 *
 */
public class InCirclePerf {

  public static void main(final String[] args) throws Exception {
    final InCirclePerf test = new InCirclePerf();
    test.run();
  }

  Coordinates pa = new Coordinate(687958.05, 7460725.97,
    Coordinates.NULL_ORDINATE);

  Coordinates pb = new Coordinate(687957.43, 7460725.93,
    Coordinates.NULL_ORDINATE);

  Coordinates pc = new Coordinate(687957.58, 7460721, Coordinates.NULL_ORDINATE);

  Coordinates pp = new Coordinate(687958.13, 7460720.99,
    Coordinates.NULL_ORDINATE);

  public InCirclePerf() {
  }

  public void run() {
    // System.out.println("InCircle perf");
    final int n = 1000000;
    final double doubleTime = runDouble(n);
    final double ddSelfTime = runDDSelf(n);
    final double ddSelf2Time = runDDSelf2(n);
    final double ddTime = runDD(n);
    // double ddSelfTime = runDoubleDoubleSelf(10000000);

    // System.out.println("DD VS double performance factor      = " + ddTime
    // / doubleTime);
    // System.out.println("DDSelf VS double performance factor  = " + ddSelfTime
    // / doubleTime);
    // System.out.println("DDSelf2 VS double performance factor = " +
    // ddSelf2Time
    // / doubleTime);
  }

  public double runDD(final int nIter) {
    final Stopwatch sw = new Stopwatch();
    for (int i = 0; i < nIter; i++) {
      TriPredicate.isInCircleDD(this.pa, this.pb, this.pc, this.pp);
    }
    sw.stop();
    // System.out.println("DD:       nIter = " + nIter + "   time = "
    // + sw.getTimeString());
    return sw.getTime() / (double)nIter;
  }

  public double runDDSelf(final int nIter) {
    final Stopwatch sw = new Stopwatch();
    for (int i = 0; i < nIter; i++) {
      TriPredicate.isInCircleDD2(this.pa, this.pb, this.pc, this.pp);
    }
    sw.stop();
    // System.out.println("DD-Self:  nIter = " + nIter + "   time = "
    // + sw.getTimeString());
    return sw.getTime() / (double)nIter;
  }

  public double runDDSelf2(final int nIter) {
    final Stopwatch sw = new Stopwatch();
    for (int i = 0; i < nIter; i++) {
      TriPredicate.isInCircleDD3(this.pa, this.pb, this.pc, this.pp);
    }
    sw.stop();
    // System.out.println("DD-Self2: nIter = " + nIter + "   time = "
    // + sw.getTimeString());
    return sw.getTime() / (double)nIter;
  }

  public double runDouble(final int nIter) {
    final Stopwatch sw = new Stopwatch();
    for (int i = 0; i < nIter; i++) {
      TriPredicate.isInCircle(this.pa, this.pb, this.pc, this.pp);
    }
    sw.stop();
    // System.out.println("double:   nIter = " + nIter + "   time = "
    // + sw.getTimeString());
    return sw.getTime() / (double)nIter;
  }

}
