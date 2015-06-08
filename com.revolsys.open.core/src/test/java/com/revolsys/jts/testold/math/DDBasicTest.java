package com.revolsys.jts.testold.math;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.math.DD;

/**
 * Tests basic arithmetic operations for {@link DD}s.
 *
 * @author Martin Davis
 *
 */
public class DDBasicTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(DDBasicTest.class);
  }

  public DDBasicTest(final String name) {
    super(name);
  }

  private void checkAddMult2(final DD dd) {
    final DD sum = dd.add(dd);
    final DD prod = dd.multiply(new DD(2.0));
    checkErrorBound("AddMult2", sum, prod, 0.0);
  }

  void checkBinomial2(final double a, final double b) {
    // binomial product
    final DD add = new DD(a);
    final DD bdd = new DD(b);
    final DD aPlusb = add.add(bdd);
    final DD aSubb = add.subtract(bdd);
    final DD abProd = aPlusb.multiply(aSubb);
    // System.out.println("(a+b)^2 = " + abSq);

    // expansion
    final DD a2dd = add.multiply(add);
    final DD b2dd = bdd.multiply(bdd);

    // System.out.println("2ab+b^2 = " + sum);

    // this should equal b^2
    final DD diff = abProd.subtract(a2dd).negate();
    // System.out.println("(a+b)^2 - a^2 = " + diff);

    final DD delta = diff.subtract(b2dd);

    // System.out.println();
    // System.out.println("A = " + a + ", B = " + b);
    // System.out.println("[DD] (a+b)(a-b) = " + abProd
    // + "   -((a^2 - b^2) - a^2) = " + diff + "   delta = " + delta);
    // printBinomialSquareDouble(a,b);

    final boolean isSame = diff.equals(b2dd);
    assertTrue(isSame);
    final boolean isDeltaZero = delta.isZero();
    assertTrue(isDeltaZero);
  }

  /**
   * Computes (a+b)^2 in two different ways and compares the result.
   * For correct results, a and b should be integers.
   *
   * @param a
   * @param b
   */
  void checkBinomialSquare(final double a, final double b) {
    // binomial square
    final DD add = new DD(a);
    final DD bdd = new DD(b);
    final DD aPlusb = add.add(bdd);
    final DD abSq = aPlusb.multiply(aPlusb);
    // System.out.println("(a+b)^2 = " + abSq);

    // expansion
    final DD a2dd = add.multiply(add);
    final DD b2dd = bdd.multiply(bdd);
    final DD ab = add.multiply(bdd);
    final DD sum = b2dd.add(ab).add(ab);

    // System.out.println("2ab+b^2 = " + sum);

    final DD diff = abSq.subtract(a2dd);
    // System.out.println("(a+b)^2 - a^2 = " + diff);

    final DD delta = diff.subtract(sum);

    // System.out.println();
    // System.out.println("A = " + a + ", B = " + b);
    // System.out.println("[DD]     2ab+b^2 = " + sum + "   (a+b)^2 - a^2 = "
    // + diff + "   delta = " + delta);
    printBinomialSquareDouble(a, b);

    final boolean isSame = diff.equals(sum);
    assertTrue(isSame);
    final boolean isDeltaZero = delta.isZero();
    assertTrue(isDeltaZero);
  }

  private void checkDivideMultiply(final DD a, final DD b, final double errBound) {
    final DD a2 = a.divide(b).multiply(b);
    checkErrorBound("DivideMultiply", a, a2, errBound);
  }

  private void checkErrorBound(final String tag, final DD x, final DD y, final double errBound) {
    final DD err = x.subtract(y).abs();
    // System.out.println(tag + " err=" + err);
    final boolean isWithinEps = err.doubleValue() <= errBound;
    assertTrue(isWithinEps);
  }

  private void checkMultiplyDivide(final DD a, final DD b, final double errBound) {
    final DD a2 = a.multiply(b).divide(b);
    checkErrorBound("MultiplyDivide", a, a2, errBound);
  }

  private void checkPow(final double x, final int exp, final double errBound) {
    final DD xdd = new DD(x);
    final DD pow = xdd.pow(exp);
    // System.out.println("Pow(" + x + ", " + exp + ") = " + pow);
    final DD pow2 = slowPow(xdd, exp);

    final double err = pow.subtract(pow2).doubleValue();

    final boolean isOK = err < errBound;
    if (!isOK) {
      // System.out.println("Test slowPow value " + pow2);
    }

    assertTrue(err <= errBound);
  }

  private void checkReciprocal(final double x, final double errBound) {
    final DD xdd = new DD(x);
    final DD rr = xdd.reciprocal().reciprocal();

    final double err = xdd.subtract(rr).doubleValue();

    // System.out.println("DD Recip = " + xdd + " DD delta= " + err
    // + " double recip delta= " + (x - 1.0 / (1.0 / x)));

    assertTrue(err <= errBound);
  }

  private void checkSqrt(final DD x, final double errBound) {
    final DD sqrt = x.sqrt();
    final DD x2 = sqrt.multiply(sqrt);
    checkErrorBound("Sqrt", x, x2, errBound);
  }

  private void checkTrunc(final DD x, final DD expected) {
    final DD trunc = x.trunc();
    final boolean isEqual = trunc.equals(expected);
    assertTrue(isEqual);
  }

  void printBinomialSquareDouble(final double a, final double b) {
    final double sum = 2 * a * b + b * b;
    final double diff = (a + b) * (a + b) - a * a;
    // System.out.println("[double] 2ab+b^2= " + sum + "   (a+b)^2-a^2= " + diff
    // + "   delta= " + (sum - diff));
  }

  private DD slowPow(final DD x, final int exp) {
    if (exp == 0) {
      return DD.valueOf(1.0);
    }

    final int n = Math.abs(exp);
    // MD - could use binary exponentiation for better precision & speed
    DD pow = new DD(x);
    for (int i = 1; i < n; i++) {
      pow = pow.multiply(x);
    }
    if (exp < 0) {
      return pow.reciprocal();
    }
    return pow;
  }

  public void testAddMult2() {
    checkAddMult2(new DD(3));
    checkAddMult2(DD.PI);
  }

  public void testBinom() {
    checkBinomialSquare(100.0, 1.0);
    checkBinomialSquare(1000.0, 1.0);
    checkBinomialSquare(10000.0, 1.0);
    checkBinomialSquare(100000.0, 1.0);
    checkBinomialSquare(1000000.0, 1.0);
    checkBinomialSquare(1e8, 1.0);
    checkBinomialSquare(1e10, 1.0);
    checkBinomialSquare(1e14, 1.0);
    // Following call will fail, because it requires 32 digits of precision
    // checkBinomialSquare(1e16, 1.0);

    checkBinomialSquare(1e14, 291.0);
    checkBinomialSquare(5e14, 291.0);
    checkBinomialSquare(5e14, 345291.0);
  }

  public void testBinomial2() {
    checkBinomial2(100.0, 1.0);
    checkBinomial2(1000.0, 1.0);
    checkBinomial2(10000.0, 1.0);
    checkBinomial2(100000.0, 1.0);
    checkBinomial2(1000000.0, 1.0);
    checkBinomial2(1e8, 1.0);
    checkBinomial2(1e10, 1.0);
    checkBinomial2(1e14, 1.0);

    checkBinomial2(1e14, 291.0);

    checkBinomial2(5e14, 291.0);
    checkBinomial2(5e14, 345291.0);
  }

  public void testDivideMultiply() {
    checkDivideMultiply(DD.PI, DD.E, 1e-30);
    checkDivideMultiply(new DD(39.4), new DD(10), 1e-30);
  }

  public void testMultiplyDivide() {
    checkMultiplyDivide(DD.PI, DD.E, 1e-30);
    checkMultiplyDivide(DD.TWO_PI, DD.E, 1e-30);
    checkMultiplyDivide(DD.PI_2, DD.E, 1e-30);
    checkMultiplyDivide(new DD(39.4), new DD(10), 1e-30);
  }

  public void testNaN() {
    assertTrue(DD.valueOf(1).divide(DD.valueOf(0)).isNaN());
    assertTrue(DD.valueOf(1).multiply(DD.NaN).isNaN());
  }

  public void testPow() {
    checkPow(0, 3, 16 * DD.EPS);
    checkPow(14, 3, 16 * DD.EPS);
    checkPow(3, -5, 16 * DD.EPS);
    checkPow(-3, 5, 16 * DD.EPS);
    checkPow(-3, -5, 16 * DD.EPS);
    checkPow(0.12345, -5, 1e5 * DD.EPS);
  }

  public void testReciprocal() {
    // error bounds are chosen to be "close enough" (i.e. heuristically)

    // for some reason many reciprocals are exact
    checkReciprocal(3.0, 0);
    checkReciprocal(99.0, 1e-29);
    checkReciprocal(999.0, 0);
    checkReciprocal(314159269.0, 0);
  }

  public void testSqrt() {
    // the appropriate error bound is determined empirically
    checkSqrt(DD.PI, 1e-30);
    checkSqrt(DD.E, 1e-30);
    checkSqrt(new DD(999.0), 1e-28);
  }

  public void testTrunc() {
    checkTrunc(DD.valueOf(1e16).subtract(DD.valueOf(1)), DD.valueOf(1e16).subtract(DD.valueOf(1)));
    // the appropriate error bound is determined empirically
    checkTrunc(DD.PI, DD.valueOf(3));
    checkTrunc(DD.valueOf(999.999), DD.valueOf(999));

    checkTrunc(DD.E.negate(), DD.valueOf(-2));
    checkTrunc(DD.valueOf(-999.999), DD.valueOf(-999));
  }

}
