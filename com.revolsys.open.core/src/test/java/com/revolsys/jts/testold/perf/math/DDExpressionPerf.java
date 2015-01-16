package com.revolsys.jts.testold.perf.math;

import java.math.BigDecimal;

import com.revolsys.jts.math.DD;
import com.revolsys.jts.util.Stopwatch;

/**
 * Times evaluating floating-point expressions using
 * various extended precision APIs.
 *
 * @author Martin Davis
 *
 */
public class DDExpressionPerf {

  public static void main(final String[] args) throws Exception {
    final DDExpressionPerf test = new DDExpressionPerf();
    test.run();
  }

  public DDExpressionPerf() {
  }

  public void run() {
    final int n = 1000000;
    final double doubleTime = runDouble(n);
    final double ddTime = runDoubleDouble(n);
    final double ddSelfTime = runDoubleDoubleSelf(n);
    final double bigDecTime = runBigDecimal(n);

    // System.out.println("BigDecimal VS double performance factor = "
    // + bigDecTime / doubleTime);
    // System.out.println("BigDecimal VS DD performance factor = " + bigDecTime
    // / ddTime);

    // System.out.println("DD VS double performance factor = " + ddTime
    // / doubleTime);
    // System.out.println("DD-Self VS double performance factor = " + ddSelfTime
    // / doubleTime);

  }

  public double runBigDecimal(final int nIter) {
    final Stopwatch sw = new Stopwatch();
    for (int i = 0; i < nIter; i++) {

      final BigDecimal a = new BigDecimal(9.0).setScale(20);
      final BigDecimal factor = new BigDecimal(10.0).setScale(20);
      final BigDecimal aMul = factor.multiply(a);
      final BigDecimal aDiv = a.divide(factor, BigDecimal.ROUND_HALF_UP);

      final BigDecimal det = a.multiply(a).subtract(aMul.multiply(aDiv));
      // System.out.println(aDiv);
      // System.out.println(det);
    }
    sw.stop();
    // System.out.println("BigDecimal:      nIter = " + nIter + "   time = "
    // + sw.getTimeString());
    return sw.getTime() / (double)nIter;
  }

  public double runDouble(final int nIter) {
    final Stopwatch sw = new Stopwatch();
    for (int i = 0; i < nIter; i++) {
      final double a = 9.0;
      final double factor = 10.0;

      final double aMul = factor * a;
      final double aDiv = a / factor;

      final double det = a * a - aMul * aDiv;
      // System.out.println(det);
    }
    sw.stop();
    // System.out.println("double:          nIter = " + nIter + "   time = "
    // + sw.getTimeString());
    return sw.getTime() / (double)nIter;
  }

  public double runDoubleDouble(final int nIter) {
    final Stopwatch sw = new Stopwatch();
    for (int i = 0; i < nIter; i++) {

      final DD a = new DD(9.0);
      final DD factor = new DD(10.0);
      final DD aMul = factor.multiply(a);
      final DD aDiv = a.divide(factor);

      final DD det = a.multiply(a).subtract(aMul.multiply(aDiv));
      // System.out.println(aDiv);
      // System.out.println(det);
    }
    sw.stop();
    // System.out.println("DD:              nIter = " + nIter + "   time = "
    // + sw.getTimeString());
    return sw.getTime() / (double)nIter;
  }

  // *
  public double runDoubleDoubleSelf(final int nIter) {
    final Stopwatch sw = new Stopwatch();
    for (int i = 0; i < nIter; i++) {

      final double a = 9.0;
      final double factor = 10.0;
      final DD c = new DD(9.0);
      c.selfMultiply(factor);
      final DD b = new DD(9.0);
      b.selfDivide(factor);

      final DD a2 = new DD(a);
      a2.selfMultiply(a);
      final DD b2 = new DD(b);
      b2.selfMultiply(c);
      a2.selfDivide(b2);
      final DD det = a2;
      // System.out.println(aDiv);
      // System.out.println(det);
    }
    sw.stop();
    // System.out.println("DD-Self:         nIter = " + nIter + "   time = "
    // + sw.getTimeString());
    return sw.getTime() / (double)nIter;
  }

  // */

  public double xrunDoubleDoubleSelf(final int nIter) {
    final Stopwatch sw = new Stopwatch();
    for (int i = 0; i < nIter; i++) {

      final DD a = new DD(9.0);
      final DD factor = new DD(10.0);
      final DD aMul = factor.multiply(a);
      final DD aDiv = a.divide(factor);

      final DD det = a.multiply(a).subtract(aMul.multiply(aDiv));
      // System.out.println(aDiv);
      // System.out.println(det);
    }
    sw.stop();
    // // System.out.println("DD:              nIter = " + nIter + "   time = "
    // + sw.getTimeString());
    return sw.getTime() / (double)nIter;
  }
}
