/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jts.operation.buffer.validate;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

/**
 * Validates that the result of a buffer operation
 * is geometrically correct, within a computed tolerance.
 * <p>
 * This is a heuristic test, and may return false positive results
 * (I.e. it may fail to detect an invalid result.)
 * It should never return a false negative result, however
 * (I.e. it should never report a valid result as invalid.)
 * <p>
 * This test may be (much) more expensive than the original
 * buffer computation.
 *
 * @author Martin Davis
 */
public class BufferResultValidator {
  private static boolean VERBOSE = false;

  /**
   * Maximum allowable fraction of buffer distance the 
   * actual distance can differ by.
   * 1% sometimes causes an error - 1.2% should be safe.
   */
  private static final double MAX_ENV_DIFF_FRAC = .012;

  public static boolean isValid(final Geometry g, final double distance,
    final Geometry result) {
    final BufferResultValidator validator = new BufferResultValidator(g,
      distance, result);
    if (validator.isValid()) {
      return true;
    }
    return false;
  }

  /**
   * Checks whether the geometry buffer is valid, 
   * and returns an error message if not.
   * 
   * @param g
   * @param distance
   * @param result
   * @return an appropriate error message
   * or null if the buffer is valid
   */
  public static String isValidMsg(final Geometry g, final double distance,
    final Geometry result) {
    final BufferResultValidator validator = new BufferResultValidator(g,
      distance, result);
    if (!validator.isValid()) {
      return validator.getErrorMessage();
    }
    return null;
  }

  private final Geometry input;

  private final double distance;

  private final Geometry result;

  private boolean isValid = true;

  private String errorMsg = null;

  private Point errorLocation = null;

  private Geometry errorIndicator = null;

  public BufferResultValidator(final Geometry input, final double distance,
    final Geometry result) {
    this.input = input;
    this.distance = distance;
    this.result = result;
  }

  private void checkArea() {
    final double inputArea = input.getArea();
    final double resultArea = result.getArea();

    if (distance > 0.0 && inputArea > resultArea) {
      isValid = false;
      errorMsg = "Area of positive buffer is smaller than input";
      errorIndicator = result;
    }
    if (distance < 0.0 && inputArea < resultArea) {
      isValid = false;
      errorMsg = "Area of negative buffer is larger than input";
      errorIndicator = result;
    }
    report("Area");
  }

  private void checkDistance() {
    final BufferDistanceValidator distValid = new BufferDistanceValidator(
      input, distance, result);
    if (!distValid.isValid()) {
      isValid = false;
      errorMsg = distValid.getErrorMessage();
      errorLocation = distValid.getErrorLocation();
      errorIndicator = distValid.getErrorIndicator();
    }
    report("Distance");
  }

  private void checkEnvelope() {
    if (distance < 0.0) {
      return;
    }

    double padding = distance * MAX_ENV_DIFF_FRAC;
    if (padding == 0.0) {
      padding = 0.001;
    }

    final BoundingBox expectedEnv = input.getBoundingBox().expand(distance);

    final BoundingBox bufEnv = result.getBoundingBox().expand(padding);

    if (!bufEnv.covers(expectedEnv)) {
      isValid = false;
      errorMsg = "Buffer envelope is incorrect";
      GeometryFactory r = input.getGeometryFactory();
      errorIndicator = bufEnv.toGeometry();
    }
    report("BoundingBoxDoubleGf");
  }

  private void checkExpectedEmpty() {
    // can't check areal features
    if (input.getDimension() >= 2) {
      return;
    }
    // can't check positive distances
    if (distance > 0.0) {
      return;
    }

    // at this point can expect an empty result
    if (!result.isEmpty()) {
      isValid = false;
      errorMsg = "Result is non-empty";
      errorIndicator = result;
    }
    report("ExpectedEmpty");
  }

  private void checkPolygonal() {
    if (!(result instanceof Polygon || result instanceof MultiPolygon)) {
      isValid = false;
    }
    errorMsg = "Result is not polygonal";
    errorIndicator = result;
    report("Polygonal");
  }

  /**
   * Gets a geometry which indicates the location and nature of a validation failure.
   * <p>
   * If the failure is due to the buffer curve being too far or too close 
   * to the input, the indicator is a line segment showing the location and size
   * of the discrepancy.
   * 
   * @return a geometric error indicator
   * or null if no error was found
   */
  public Geometry getErrorIndicator() {
    return errorIndicator;
  }

  public Point getErrorLocation() {
    return errorLocation;
  }

  public String getErrorMessage() {
    return errorMsg;
  }

  public boolean isValid() {
    checkPolygonal();
    if (!isValid) {
      return isValid;
    }
    checkExpectedEmpty();
    if (!isValid) {
      return isValid;
    }
    checkEnvelope();
    if (!isValid) {
      return isValid;
    }
    checkArea();
    if (!isValid) {
      return isValid;
    }
    checkDistance();
    return isValid;
  }

  private void report(final String checkName) {
    if (!VERBOSE) {
      return;
    }
    System.out.println("Check " + checkName + ": "
      + (isValid ? "passed" : "FAILED"));
  }
}
