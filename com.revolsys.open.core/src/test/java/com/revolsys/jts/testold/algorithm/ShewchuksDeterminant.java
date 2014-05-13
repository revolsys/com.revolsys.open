package com.revolsys.jts.testold.algorithm;

import com.revolsys.jts.geom.Point;

/**
 * This implementation is a port of Shewchuks original implementation in c
 * which is placed in the public domain:
 *
 * [...]
 *  Placed in the public domain by
 *  Jonathan Richard Shewchuk
 *  School of Computer Science
 *  Carnegie Mellon University
 *  5000 Forbes Avenue
 *  Pittsburgh, Pennsylvania  15213-3891
 *  jrs@cs.cmu.edu
 *  [...]
 *
 *  See http://www.cs.cmu.edu/~quake/robust.html for information about the original implementation
 *
 * The strategy used during porting has been to resemble the original as much as possible in order to
 * be able to proofread the result. This strategy has not been followed in the following cases:
 *
 * - function "exactinit" has been replaced by a static block, in order to ensure that the code is only
 *   executed once.
 *
 * - The main part of the so called "tail" functions has been removed in favor to "head" functions. This means
 *   that all calls to such a main functions has been replaced with on call to the head function and one call
 *   to the tail function. The rationale for this is that the corresponding source macros has multiple output parameters
 *   which is not supported in Java. Using objects to pass the results to the caller was not considered for performance
 *   reasons. It is assumed that the extra allocations of memory would affect performance negatively.
 *
 * - The porting of the two_two_diff methods and the other methods involved was tricky since the original macros
 *   had lots of output parameters. These methods has the highest probability of bugs as a result of the porting
 *   operation. Each original function(macro) has been replaced with one method for each output parameter. They are
 *   named as *__x0, *__x2 etc. where the postfix is named after the original output parameter in the SOURCE CODE.
 *   The rational for the naming has been to facilitate proofreading. Each of these methods has an annotation above
 *   it that shows the original macro.
 *
 * - One bug has been found in the original source. The use of the prefix incrementation operator in
 *   fast_expansion_sum_zeroelim caused the code to access memory outside of the array boundary. This is not
 *   allowed in Java which is the reason why this bug was discovered. It is unclear if the code worked correctly
 *   as a compiled c-program. It has been confirmed by valgrind that this actually happens in the original code.
 *   The author of the original code has been contacted, and we are waiting for an answer. All occurences of the
 *   prefix incrementation operator in that function have been replaced with the postfix version. This change looks
 *   reasonable by a quick look at the code, but this needs to be more thoroughly analyzed.
 *
 * - Function orientationIndex is new and its contract is copied from
 *   com.revolsys.jts.testold.algorithm.CGAlgorithms.orientationIndex so that the current implementation of that method
 *   can be easily replaced.
 *
 * Some relevant comments in the original code has been kept untouched in its entirety. For more in-depth information
 * refer to the original source.
 */

/*****************************************************************************/
/*                                                                           */
/*  Routines for Arbitrary Precision Floating-point Arithmetic               */
/*  and Fast Robust Geometric Predicates                                     */
/*  (predicates.c)                                                           */
/*                                                                           */
/*  May 18, 1996                                                             */
/*                                                                           */
/*  Placed in the public domain by                                           */
/*  Jonathan Richard Shewchuk                                                */
/*  School of Computer Science                                               */
/*  Carnegie Mellon University                                               */
/*  5000 Forbes Avenue                                                       */
/*  Pittsburgh, Pennsylvania  15213-3891                                     */
/*  jrs@cs.cmu.edu                                                           */
/*                                                                           */
/*  This file contains C implementation of algorithms for exact addition     */
/*    and multiplication of floating-point numbers, and predicates for       */
/*    robustly performing the orientation and incircle tests used in         */
/*    computational geometry.  The algorithms and underlying theory are      */
/*    described in Jonathan Richard Shewchuk.  "Adaptive Precision Floating- */
/*    Point Arithmetic and Fast Robust Geometric Predicates."  Technical     */
/*    Report CMU-CS-96-140, School of Computer Science, Carnegie Mellon      */
/*    University, Pittsburgh, Pennsylvania, May 1996.  (Submitted to         */
/*    Discrete & Computational Geometry.)                                    */
/*                                                                           */
/*  This file, the paper listed above, and other information are available   */
/*    from the Web page http://www.cs.cmu.edu/~quake/robust.html .           */
/*                                                                           */
/*****************************************************************************/

/*****************************************************************************/
/*                                                                           */
/*  Using this code:                                                         */
/*                                                                           */
/*  First, read the short or long version of the paper (from the Web page    */
/*    above).                                                                */
/*                                                                           */
/*  Be sure to call exactinit() once, before calling any of the arithmetic   */
/*    functions or geometric predicates.  Also be sure to turn on the        */
/*    optimizer when compiling this file.                                    */
/*                                                                           */
/*                                                                           */
/*  Several geometric predicates are defined.  Their parameters are all      */
/*    points.  Each point is an array of two or three floating-point         */
/*    numbers.  The geometric predicates, described in the papers, are       */
/*                                                                           */
/*    orient2d(pa, pb, pc)                                                   */
/*    orient2dfast(pa, pb, pc)                                               */
/*    orient3d(pa, pb, pc, pd)                                               */
/*    orient3dfast(pa, pb, pc, pd)                                           */
/*    incircle(pa, pb, pc, pd)                                               */
/*    incirclefast(pa, pb, pc, pd)                                           */
/*    insphere(pa, pb, pc, pd, pe)                                           */
/*    inspherefast(pa, pb, pc, pd, pe)                                       */
/*                                                                           */
/*  Those with suffix "fast" are approximate, non-robust versions.  Those    */
/*    without the suffix are adaptive precision, robust versions.  There     */
/*    are also versions with the suffices "exact" and "slow", which are      */
/*    non-adaptive, exact arithmetic versions, which I use only for timings  */
/*    in my arithmetic papers.                                               */
/*                                                                           */
/*                                                                           */
/*  An expansion is represented by an array of floating-point numbers,       */
/*    sorted from smallest to largest magnitude (possibly with interspersed  */
/*    zeros).  The length of each expansion is stored as a separate integer, */
/*    and each arithmetic function returns an integer which is the length    */
/*    of the expansion it created.                                           */
/*                                                                           */
/*  Several arithmetic functions are defined.  Their parameters are          */
/*                                                                           */
/*    e, f           Input expansions                                        */
/*    elen, flen     Lengths of input expansions (must be >= 1)              */
/*    h              Output expansion                                        */
/*    b              Input scalar                                            */
/*                                                                           */
/*  The arithmetic functions are                                             */
/*                                                                           */
/*    grow_expansion(elen, e, b, h)                                          */
/*    grow_expansion_zeroelim(elen, e, b, h)                                 */
/*    expansion_sum(elen, e, flen, f, h)                                     */
/*    expansion_sum_zeroelim1(elen, e, flen, f, h)                           */
/*    expansion_sum_zeroelim2(elen, e, flen, f, h)                           */
/*    fast_expansion_sum(elen, e, flen, f, h)                                */
/*    fast_expansion_sum_zeroelim(elen, e, flen, f, h)                       */
/*    linear_expansion_sum(elen, e, flen, f, h)                              */
/*    linear_expansion_sum_zeroelim(elen, e, flen, f, h)                     */
/*    scale_expansion(elen, e, b, h)                                         */
/*    scale_expansion_zeroelim(elen, e, b, h)                                */
/*    compress(elen, e, h)                                                   */
/*                                                                           */
/*  All of these are described in the long version of the paper; some are    */
/*    described in the short version.  All return an integer that is the     */
/*    length of h.  Those with suffix _zeroelim perform zero elimination,    */
/*    and are recommended over their counterparts.  The procedure            */
/*    fast_expansion_sum_zeroelim() (or linear_expansion_sum_zeroelim() on   */
/*    processors that do not use the round-to-even tiebreaking rule) is      */
/*    recommended over expansion_sum_zeroelim().  Each procedure has a       */
/*    little note next to it (in the code below) that tells you whether or   */
/*    not the output expansion may be the same array as one of the input     */
/*    expansions.                                                            */
/*                                                                           */
/*                                                                           */
/*  If you look around below, you'll also find macros for a bunch of         */
/*    simple unrolled arithmetic operations, and procedures for printing     */
/*    expansions (commented out because they don't work with all C           */
/*    compilers) and for generating random floating-point numbers whose      */
/*    significand bits are all random.  Most of the macros have undocumented */
/*    requirements that certain of their parameters should not be the same   */
/*    variable; for safety, better to make sure all the parameters are       */
/*    distinct variables.  Feel free to send email to jrs@cs.cmu.edu if you  */
/*    have questions.                                                        */
/*                                                                           */
/*****************************************************************************/

public class ShewchuksDeterminant {

  private static final double splitter;

  private static final double resulterrbound;

  private static final double ccwerrboundA;

  private static final double ccwerrboundB;

  private static final double ccwerrboundC;

  /*****************************************************************************/
  /*                                                                           */
  /* exactinit() Initialize the variables used for exact arithmetic. */
  /*                                                                           */
  /* `epsilon' is the largest power of two such that 1.0 + epsilon = 1.0 in */
  /* floating-point arithmetic. `epsilon' bounds the relative roundoff */
  /* error. It is used for floating-point error analysis. */
  /*                                                                           */
  /* `splitter' is used to split floating-point numbers into two half- */
  /* length significands for exact multiplication. */
  /*                                                                           */
  /* I imagine that a highly optimizing compiler might be too smart for its */
  /* own good, and somehow cause this routine to fail, if it pretends that */
  /* floating-point arithmetic is too much like real arithmetic. */
  /*                                                                           */
  /* Don't change this routine unless you fully understand it. */
  /*                                                                           */
  /*****************************************************************************/

  static {
    double epsilon_temp;
    double splitter_temp;
    double half;
    double check, lastcheck;
    int every_other;

    every_other = 1;
    half = 0.5;
    epsilon_temp = 1.0;
    splitter_temp = 1.0;
    check = 1.0;
    /* Repeatedly divide `epsilon' by two until it is too small to add to */
    /* one without causing roundoff. (Also check if the sum is equal to */
    /* the previous sum, for machines that round up instead of using exact */
    /* rounding. Not that this library will work on such machines anyway. */
    do {
      lastcheck = check;
      epsilon_temp *= half;
      if (every_other != 0) {
        splitter_temp *= 2.0;
      }
      every_other = every_other == 0 ? 1 : 0;
      check = 1.0 + epsilon_temp;
    } while (check != 1.0 && check != lastcheck);
    splitter_temp += 1.0;

    /* Error bounds for orientation and incircle tests. */
    resulterrbound = (3.0 + 8.0 * epsilon_temp) * epsilon_temp;
    ccwerrboundA = (3.0 + 16.0 * epsilon_temp) * epsilon_temp;
    ccwerrboundB = (2.0 + 12.0 * epsilon_temp) * epsilon_temp;
    ccwerrboundC = (9.0 + 64.0 * epsilon_temp) * epsilon_temp * epsilon_temp;
    splitter = splitter_temp;
  }

  private static double Absolute(final double a) {
    return a >= 0.0 ? a : -a;
  }

  /*****************************************************************************/
  /*                                                                           */
  /* fast_expansion_sum_zeroelim() Sum two expansions, eliminating zero */
  /* components from the output expansion. */
  /*                                                                           */
  /* Sets h = e + f. See the long version of my paper for details. */
  /*                                                                           */
  /* If round-to-even is used (as with IEEE 754), maintains the strongly */
  /* nonoverlapping property. (That is, if e is strongly nonoverlapping, h */
  /* will be also.) Does NOT maintain the nonoverlapping or nonadjacent */
  /* properties. */
  /*                                                                           */
  /*****************************************************************************/

  private static int fast_expansion_sum_zeroelim(final int elen,
    final double[] e, final int flen, final double[] f, final double[] h) /*
                                                                           * h
                                                                           * cannot
                                                                           * be
                                                                           * e
                                                                           * or
                                                                           * f.
                                                                           */
  {
    double Q;
    double Qnew;
    double hh;

    int eindex, findex, hindex;
    double enow, fnow;

    enow = e[0];
    fnow = f[0];
    eindex = findex = 0;
    if (fnow > enow == fnow > -enow) {
      Q = enow;
      enow = e[eindex++];
    } else {
      Q = fnow;
      fnow = f[findex++];
    }
    hindex = 0;
    if (eindex < elen && findex < flen) {
      if (fnow > enow == fnow > -enow) {
        Qnew = Fast_Two_Sum_Head(enow, Q);
        hh = Fast_Two_Sum_Tail(enow, Q, Qnew);
        enow = e[eindex++];
      } else {
        Qnew = Fast_Two_Sum_Head(fnow, Q);
        hh = Fast_Two_Sum_Tail(fnow, Q, Qnew);
        fnow = f[findex++];
      }
      Q = Qnew;
      if (hh != 0.0) {
        h[hindex++] = hh;
      }
      while (eindex < elen && findex < flen) {
        if (fnow > enow == fnow > -enow) {
          Qnew = Two_Sum_Head(Q, enow);
          hh = Two_Sum_Tail(Q, enow, Qnew);
          enow = e[eindex++];
        } else {
          Qnew = Two_Sum_Head(Q, fnow);
          hh = Two_Sum_Tail(Q, fnow, Qnew);
          fnow = f[findex++];
        }
        Q = Qnew;
        if (hh != 0.0) {
          h[hindex++] = hh;
        }
      }
    }
    while (eindex < elen) {
      Qnew = Two_Sum_Head(Q, enow);
      hh = Two_Sum_Tail(Q, enow, Qnew);
      enow = e[eindex++];
      Q = Qnew;
      if (hh != 0.0) {
        h[hindex++] = hh;
      }
    }
    while (findex < flen) {
      Qnew = Two_Sum_Head(Q, fnow);
      hh = Two_Sum_Tail(Q, fnow, Qnew);
      fnow = f[findex++];
      Q = Qnew;
      if (hh != 0.0) {
        h[hindex++] = hh;
      }
    }
    if (Q != 0.0 || hindex == 0) {
      h[hindex++] = Q;
    }
    return hindex;
  }

  private static double Fast_Two_Sum_Head(final double a, final double b) {
    final double x = a + b;

    return x;
  }

  private static double Fast_Two_Sum_Tail(final double a, final double b,
    final double x) {
    final double bvirt = x - a;
    final double y = b - bvirt;

    return y;
  }

  private static double orient2d(final Point pa, final Point pb,
    final Point pc) {
    double detsum;

    final double detleft = (pa.getX() - pc.getX()) * (pb.getY() - pc.getY());
    final double detright = (pa.getY() - pc.getY()) * (pb.getX() - pc.getX());
    final double det = detleft - detright;

    if (detleft > 0.0) {
      if (detright <= 0.0) {
        return det;
      } else {
        detsum = detleft + detright;
      }
    } else if (detleft < 0.0) {
      if (detright >= 0.0) {
        return det;
      } else {
        detsum = -detleft - detright;
      }
    } else {
      return det;
    }

    final double errbound = ccwerrboundA * detsum;
    if (det >= errbound || -det >= errbound) {
      return det;
    }

    return orient2dadapt(pa, pb, pc, detsum);
  }

  /*****************************************************************************/
  /*                                                                           */
  /* orient2d() Adaptive exact 2D orientation test. Robust. */
  /*                                                                           */
  /* Return a positive value if the points pa, pb, and pc occur */
  /* in counterclockwise order; a negative value if they occur */
  /* in clockwise order; and zero if they are collinear. The */
  /* result is also a rough approximation of twice the signed */
  /* area of the triangle defined by the three points. */
  /*                                                                           */
  /* The last three use exact arithmetic to ensure a correct answer. The */
  /* result returned is the determinant of a matrix. In orient2d() only, */
  /* this determinant is computed adaptively, in the sense that exact */
  /* arithmetic is used only to the degree it is needed to ensure that the */
  /* returned value has the correct sign. Hence, orient2d() is usually quite */
  /* fast, but will run more slowly when the input points are collinear or */
  /* nearly so. */
  /*                                                                           */
  /*****************************************************************************/

  private static double orient2dadapt(final Point pa, final Point pb,
    final Point pc, final double detsum) {

    final double acx = pa.getX() - pc.getX();
    final double bcx = pb.getX() - pc.getX();
    final double acy = pa.getY() - pc.getY();
    final double bcy = pb.getY() - pc.getY();

    final double detleft = Two_Product_Head(acx, bcy);
    final double detlefttail = Two_Product_Tail(acx, bcy, detleft);

    final double detright = Two_Product_Head(acy, bcx);
    final double detrighttail = Two_Product_Tail(acy, bcx, detright);

    final double B[] = new double[4];
    B[2] = Two_Two_Diff__x2(detleft, detlefttail, detright, detrighttail);
    B[1] = Two_Two_Diff__x1(detleft, detlefttail, detright, detrighttail);
    B[0] = Two_Two_Diff__x0(detleft, detlefttail, detright, detrighttail);
    B[3] = Two_Two_Diff__x3(detleft, detlefttail, detright, detrighttail);

    double det = B[0] + B[1] + B[2] + B[3];
    // det = estimate(4, B);
    double errbound = ccwerrboundB * detsum;
    if (det >= errbound || -det >= errbound) {
      return det;
    }

    final double acxtail = Two_Diff_Tail(pa.getX(), pc.getX(), acx);
    final double bcxtail = Two_Diff_Tail(pb.getX(), pc.getX(), bcx);
    final double acytail = Two_Diff_Tail(pa.getY(), pc.getY(), acy);
    final double bcytail = Two_Diff_Tail(pb.getY(), pc.getY(), bcy);

    if (acxtail == 0.0 && acytail == 0.0 && bcxtail == 0.0 && bcytail == 0.0) {
      return det;
    }

    errbound = ccwerrboundC * detsum + resulterrbound * Absolute(det);
    det += acx * bcytail + bcy * acxtail - (acy * bcxtail + bcx * acytail);
    if (det >= errbound || -det >= errbound) {
      return det;
    }

    double s1 = Two_Product_Head(acxtail, bcy);
    double s0 = Two_Product_Tail(acxtail, bcy, s1);

    double t1 = Two_Product_Head(acytail, bcx);
    double t0 = Two_Product_Tail(acytail, bcx, t1);

    double u3 = Two_Two_Diff__x3(s1, s0, t1, t0);
    final double u[] = new double[4];
    u[2] = Two_Two_Diff__x2(s1, s0, t1, t0);
    u[1] = Two_Two_Diff__x1(s1, s0, t1, t0);
    u[0] = Two_Two_Diff__x0(s1, s0, t1, t0);

    u[3] = u3;
    final double C1[] = new double[8];
    final int C1length = fast_expansion_sum_zeroelim(4, B, 4, u, C1);

    s1 = Two_Product_Head(acx, bcytail);
    s0 = Two_Product_Tail(acx, bcytail, s1);

    t1 = Two_Product_Head(acy, bcxtail);
    t0 = Two_Product_Tail(acy, bcxtail, t1);

    u3 = Two_Two_Diff__x3(s1, s0, t1, t0);
    u[2] = Two_Two_Diff__x2(s1, s0, t1, t0);
    u[1] = Two_Two_Diff__x1(s1, s0, t1, t0);
    u[0] = Two_Two_Diff__x0(s1, s0, t1, t0);

    u[3] = u3;
    final double C2[] = new double[12];
    final int C2length = fast_expansion_sum_zeroelim(C1length, C1, 4, u, C2);

    s1 = Two_Product_Head(acxtail, bcytail);
    s0 = Two_Product_Tail(acxtail, bcytail, s1);

    t1 = Two_Product_Head(acytail, bcxtail);
    t0 = Two_Product_Tail(acytail, bcxtail, t1);

    u3 = Two_Two_Diff__x3(s1, s0, t1, t0);
    u[2] = Two_Two_Diff__x2(s1, s0, t1, t0);
    u[1] = Two_Two_Diff__x1(s1, s0, t1, t0);
    u[0] = Two_Two_Diff__x0(s1, s0, t1, t0);

    u[3] = u3;
    final double D[] = new double[16];
    final int Dlength = fast_expansion_sum_zeroelim(C2length, C2, 4, u, D);

    return D[Dlength - 1];
  }

  /**
   * Returns the index of the direction of the point <code>q</code> relative to
   * a vector specified by <code>p1-p2</code>.
   * 
   * @param p1
   *          the origin point of the vector
   * @param p2
   *          the final point of the vector
   * @param q
   *          the point to compute the direction to
   * 
   * @return 1 if q is counter-clockwise (left) from p1-p2;
   * -1 if q is clockwise (right) from p1-p2;
   * 0 if q is collinear with p1-p2
   */
  public static int orientationIndex(final Point p1, final Point p2,
    final Point q) {
    final double orientation = orient2d(p1, p2, q);
    if (orientation > 0.0) {
      return 1;
    }
    if (orientation < 0.0) {
      return -1;
    }
    return 0;
  }

  /**
   * Implements a filter for computing the orientation index of three coordinates.
   * <p>
   * If the orientation can be computed safely using standard DP
   * arithmetic, this routine returns the orientation index.
   * Otherwise, a value i > 1 is returned.
   * In this case the orientation index must 
   * be computed using some other method.
   * 
   * @param pa a coordinate
   * @param pb a coordinate
   * @param pc a coordinate
   * @return the orientation index if it can be computed safely, or
   * i > 1 if the orientation index cannot be computed safely
   */
  public static int orientationIndexFilter(final Point pa,
    final Point pb, final Point pc) {
    double detsum;

    final double detleft = (pa.getX() - pc.getX()) * (pb.getY() - pc.getY());
    final double detright = (pa.getY() - pc.getY()) * (pb.getX() - pc.getX());
    final double det = detleft - detright;

    if (detleft > 0.0) {
      if (detright <= 0.0) {
        return signum(det);
      } else {
        detsum = detleft + detright;
      }
    } else if (detleft < 0.0) {
      if (detright >= 0.0) {
        return signum(det);
      } else {
        detsum = -detleft - detright;
      }
    } else {
      return signum(det);
    }

    final double ERR_BOUND = 1e-15;
    final double errbound = ERR_BOUND * detsum;
    // double errbound = ccwerrboundA * detsum;
    if (det >= errbound || -det >= errbound) {
      return signum(det);
    }

    return 2;
  }

  private static int signum(final double x) {
    if (x > 0) {
      return 1;
    }
    if (x < 0) {
      return -1;
    }
    return 0;
  }

  private static double SplitHi(final double a) {
    final double c = splitter * a; // porting issue: why this cast?
    final double abig = c - a; // porting issue: why this cast?
    final double ahi = c - abig;

    return ahi;
  }

  private static double SplitLo(final double a) {
    final double c = splitter * a; // porting issue: why this cast?
    final double abig = c - a; // porting issue: why this cast?
    final double ahi = c - abig;
    final double alo = a - ahi;

    return alo;
  }

  private static double Two_Diff_Head(final double a, final double b) {
    final double x = a - b;

    return x;
  }

  private static double Two_Diff_Tail(final double a, final double b,
    final double x) {
    final double bvirt = a - x; // porting issue: why this cast?
    final double avirt = x + bvirt;
    final double bround = bvirt - b;
    final double around = a - avirt;
    final double y = around + bround;

    return y;
  }

  // #define Two_One_Diff(a1, a0, b, x2, x1, x0)
  private static double Two_One_Diff__x0(final double a1, final double a0,
    final double b) {
    final double _i = Two_Diff_Head(a0, b);
    final double x0 = Two_Diff_Tail(a0, b, _i);

    return x0;
  }

  // #define Two_One_Diff(a1, a0, b, x2, x1, x0)
  private static double Two_One_Diff__x1(final double a1, final double a0,
    final double b) {
    final double _i = Two_Diff_Head(a0, b);
    final double x2 = Two_Sum_Head(a1, _i);
    final double x1 = Two_Sum_Tail(a1, _i, x2);

    return x1;
  }

  // #define Two_One_Diff(a1, a0, b, x2, x1, x0)
  private static double Two_One_Diff__x2(final double a1, final double a0,
    final double b) {
    final double _i = Two_Diff_Head(a0, b);
    final double x2 = Two_Sum_Head(a1, _i);

    return x2;
  }

  private static double Two_Product_Head(final double a, final double b) {
    final double x = a * b;

    return x;
  }

  private static double Two_Product_Tail(final double a, final double b,
    final double x) {
    final double ahi = SplitHi(a);
    final double alo = SplitLo(a);
    final double bhi = SplitHi(b);
    final double blo = SplitLo(b);

    final double err1 = x - ahi * bhi;
    final double err2 = err1 - alo * bhi;
    final double err3 = err2 - ahi * blo;
    final double y = alo * blo - err3;

    return y;
  }

  private static double Two_Sum_Head(final double a, final double b) {
    final double x = a + b;

    return x;
  }

  private static double Two_Sum_Tail(final double a, final double b,
    final double x) {
    final double bvirt = x - a;
    final double avirt = x - bvirt;
    final double bround = b - bvirt;
    final double around = a - avirt;

    final double y = around + bround;

    return y;
  }

  // #define Two_Two_Diff(a1, a0, b1, b0, x3, x2, x1, x0)
  private static double Two_Two_Diff__x0(final double a1, final double a0,
    final double b1, final double b0) {
    final double x0 = Two_One_Diff__x0(a1, a0, b0);

    return x0;
  }

  // #define Two_Two_Diff(a1, a0, b1, b0, x3, x2, x1, x0)
  private static double Two_Two_Diff__x1(final double a1, final double a0,
    final double b1, final double b0) {
    final double _j = Two_One_Diff__x2(a1, a0, b0);
    final double _0 = Two_One_Diff__x1(a1, a0, b0);

    final double x1 = Two_One_Diff__x0(_j, _0, b1);

    return x1;
  }

  // #define Two_Two_Diff(a1, a0, b1, b0, x3, x2, x1, x0)
  private static double Two_Two_Diff__x2(final double a1, final double a0,
    final double b1, final double b0) {
    final double _j = Two_One_Diff__x2(a1, a0, b0);
    final double _0 = Two_One_Diff__x1(a1, a0, b0);

    final double x2 = Two_One_Diff__x1(_j, _0, b1);

    return x2;
  }

  // #define Two_Two_Diff(a1, a0, b1, b0, x3, x2, x1, x0)
  private static double Two_Two_Diff__x3(final double a1, final double a0,
    final double b1, final double b0) {
    final double _j = Two_One_Diff__x2(a1, a0, b0);
    final double _0 = Two_One_Diff__x1(a1, a0, b0);

    final double x3 = Two_One_Diff__x2(_j, _0, b1);

    return x3;
  }

}
