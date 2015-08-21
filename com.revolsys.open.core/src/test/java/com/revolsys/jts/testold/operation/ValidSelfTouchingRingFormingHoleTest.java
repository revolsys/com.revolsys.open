package com.revolsys.jts.testold.operation;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.operation.valid.IsValidOp;
import com.revolsys.geometry.wkb.WKTReader;

import junit.framework.TestCase;

/**
 * Tests allowing IsValidOp to validate polygons with
 * Self-Touching Rings forming holes.
 * Mainly tests that configuring {@link IsValidOp} to allow validating
 * the STR validates polygons with this condition, and does not validate
 * polygons with other kinds of self-intersection (such as ones with Disconnected Interiors).
 * Includes some basic tests to confirm that other invalid cases remain detected correctly,
 * but most of this testing is left to the existing XML validation tests.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class ValidSelfTouchingRingFormingHoleTest extends TestCase {
  private static WKTReader rdr = new WKTReader();

  public static void main(final String[] args) {
    junit.textui.TestRunner.run(ValidSelfTouchingRingFormingHoleTest.class);
  }

  public ValidSelfTouchingRingFormingHoleTest(final String name) {
    super(name);
  }

  private void checkIsValidDefault(final String wkt, final boolean expected) {
    final Geometry geom = fromWKT(wkt);
    final IsValidOp validator = new IsValidOp(geom);
    final boolean isValid = validator.isValid();
    assertTrue(isValid == expected);
  }

  private void checkIsValidSTR(final String wkt, final boolean expected) {
    final Geometry geom = fromWKT(wkt);
    final IsValidOp validator = new IsValidOp(geom);
    validator.setSelfTouchingRingFormingHoleValid(true);
    final boolean isValid = validator.isValid();
    assertTrue(isValid == expected);
  }

  Geometry fromWKT(final String wkt) {
    Geometry geom = null;
    try {
      geom = rdr.read(wkt);
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
    return geom;
  }

  /**
   * Ensure that the Disconnected Interior condition is not validated
   */
  public void testDisconnectedInteriorShellSelfTouchAtNonVertex() {
    final String wkt = "POLYGON ((40 180, 40 60, 240 60, 240 180, 140 60, 40 180))";
    checkIsValidSTR(wkt, false);
    checkIsValidDefault(wkt, false);
  }

  /**
   * Ensure that the Disconnected Interior condition is not validated
   */
  public void testDisconnectedInteriorShellSelfTouchAtVertex() {
    final String wkt = "POLYGON ((20 20, 20 100, 140 100, 140 180, 260 180, 260 100, 140 100, 140 20, 20 20))";
    checkIsValidSTR(wkt, false);
    checkIsValidDefault(wkt, false);
  }

  /**
   * Tests a geometry with both a shell self-touch and a hole self=touch.
   * This is valid if STR is allowed, but invalid in OGC
   */
  public void testShellAndHoleSelfTouch() {
    final String wkt = "POLYGON ((0 0, 0 340, 320 340, 320 0, 120 0, 180 100, 60 100, 120 0, 0 0),   (80 300, 80 180, 200 180, 200 240, 280 200, 280 280, 200 240, 200 300, 80 300))";
    checkIsValidSTR(wkt, true);
    checkIsValidDefault(wkt, false);
  }

  public void testShellCross() {
    final String wkt = "POLYGON ((20 20, 120 20, 120 220, 240 220, 240 120, 20 120, 20 20))";
    checkIsValidSTR(wkt, false);
    checkIsValidDefault(wkt, false);
  }

  public void testShellCrossAndSTR() {
    final String wkt = "POLYGON ((20 20, 120 20, 120 220, 180 220, 140 160, 200 160, 180 220, 240 220, 240 120, 20 120,  20 20))";
    checkIsValidSTR(wkt, false);
    checkIsValidDefault(wkt, false);
  }

  /**
   * Tests a geometry representing the same area as in {@link #testShellAndHoleSelfTouch}
   * but using a shell-hole touch and a hole-hole touch.
   * This is valid in OGC.
   */
  public void testShellHoleAndHoleHoleTouch() {
    final String wkt = "POLYGON ((0 0, 0 340, 320 340, 320 0, 120 0, 0 0),   (120 0, 180 100, 60 100, 120 0),   (80 300, 80 180, 200 180, 200 240, 200 300, 80 300),  (200 240, 280 200, 280 280, 200 240))";
    checkIsValidSTR(wkt, true);
    checkIsValidDefault(wkt, true);
  }

  /**
   * Tests an overlapping hole condition, where one of the holes is created by a shell self-touch.
   * This is never vallid.
   */
  public void testShellSelfTouchHoleOverlappingHole() {
    final String wkt = "POLYGON ((0 0, 220 0, 220 200, 120 200, 140 100, 80 100, 120 200, 0 200, 0 0),   (200 80, 20 80, 120 200, 200 80))";
    checkIsValidSTR(wkt, false);
    checkIsValidDefault(wkt, false);
  }

}
