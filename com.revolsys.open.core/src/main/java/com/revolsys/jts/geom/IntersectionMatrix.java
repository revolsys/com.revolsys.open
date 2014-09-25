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
package com.revolsys.jts.geom;

/**
 * Models a <b>Dimensionally Extended Nine-Intersection Model (DE-9IM)</b> matrix. 
 * DE-9IM matrices (such as "212FF1FF2")
 * specify the topological relationship between two {@link Geometry}s. 
 * This class can also represent matrix patterns (such as "T*T******")
 * which are used for matching instances of DE-9IM matrices.
 *
 *  Methods are provided to:
 *  <UL>
 *    <LI> set and query the elements of the matrix in a convenient fashion
 *    <LI> convert to and from the standard string representation (specified in
 *    SFS Section 2.1.13.2).
 *    <LI> test to see if a matrix matches a given pattern string.
 *  </UL>
 *  <P>
 *
 *  For a description of the DE-9IM and the spatial predicates derived from it, 
 *  see the <i><A
 *  HREF="http://www.opengis.org/techno/specs.htm">OGC 99-049 OpenGIS Simple Features
 *  Specification for SQL</A></i>, as well as
 *  <i>OGC 06-103r4 OpenGIS 
 *  Implementation Standard for Geographic information - 
 *  Simple feature access - Part 1: Common architecture</i>
 *  (which provides some further details on certain predicate specifications).
 * <p>
 * The entries of the matrix are defined by the constants in the {@link Dimension} class.
 * The indices of the matrix represent the topological locations 
 * that occur in a geometry (Interior, Boundary, Exterior).  
 * These are provided as constants in the {@link Location} class.
 *  
 *
 *@version 1.7
 */
public class IntersectionMatrix implements Cloneable {
  public static final int EXTERIOR = Location.EXTERIOR.getIndex();

  public static final int BOUNDARY = Location.BOUNDARY.getIndex();

  public static final int INTERIOR = Location.INTERIOR.getIndex();

  /**
   *  Tests if the dimension value matches <tt>TRUE</tt>
   *  (i.e.  has value 0, 1, 2 or TRUE).
   *
   *@param  actualDimensionValue     a number that can be stored in the <code>IntersectionMatrix</code>
   *      . Possible values are <code>{TRUE, FALSE, DONTCARE, 0, 1, 2}</code>.
   *@return true if the dimension value matches TRUE
   */
  public static boolean isTrue(final int actualDimensionValue) {
    if (actualDimensionValue >= 0 || actualDimensionValue == Dimension.TRUE) {
      return true;
    }
    return false;
  }

  /**
   *  Tests if the dimension value satisfies the dimension symbol.
   *
   *@param  actualDimensionValue     a number that can be stored in the <code>IntersectionMatrix</code>
   *      . Possible values are <code>{TRUE, FALSE, DONTCARE, 0, 1, 2}</code>.
   *@param  requiredDimensionSymbol  a character used in the string
   *      representation of an <code>IntersectionMatrix</code>. Possible values
   *      are <code>{T, F, * , 0, 1, 2}</code>.
   *@return                          true if the dimension symbol matches
   *      the dimension value
   */
  public static boolean matches(final int actualDimensionValue,
    final char requiredDimensionSymbol) {
    if (requiredDimensionSymbol == Dimension.SYM_DONTCARE) {
      return true;
    }
    if (requiredDimensionSymbol == Dimension.SYM_TRUE
      && (actualDimensionValue >= 0 || actualDimensionValue == Dimension.TRUE)) {
      return true;
    }
    if (requiredDimensionSymbol == Dimension.SYM_FALSE
      && actualDimensionValue == Dimension.FALSE) {
      return true;
    }
    if (requiredDimensionSymbol == Dimension.SYM_P
      && actualDimensionValue == Dimension.P) {
      return true;
    }
    if (requiredDimensionSymbol == Dimension.SYM_L
      && actualDimensionValue == Dimension.L) {
      return true;
    }
    if (requiredDimensionSymbol == Dimension.SYM_A
      && actualDimensionValue == Dimension.A) {
      return true;
    }
    return false;
  }

  /**
   *  Tests if each of the actual dimension symbols in a matrix string satisfies the
   *  corresponding required dimension symbol in a pattern string.
   *
   *@param  actualDimensionSymbols    nine dimension symbols to validate.
   *      Possible values are <code>{T, F, * , 0, 1, 2}</code>.
   *@param  requiredDimensionSymbols  nine dimension symbols to validate
   *      against. Possible values are <code>{T, F, * , 0, 1, 2}</code>.
   *@return                           true if each of the required dimension
   *      symbols encompass the corresponding actual dimension symbol
   */
  public static boolean matches(final String actualDimensionSymbols,
    final String requiredDimensionSymbols) {
    final IntersectionMatrix m = new IntersectionMatrix(actualDimensionSymbols);
    return m.matches(requiredDimensionSymbols);
  }

  /**
   *  Internal representation of this <code>IntersectionMatrix</code>.
   */
  private final int[][] matrix;

  /**
   *  Creates an <code>IntersectionMatrix</code> with <code>FALSE</code>
   *  dimension values.
   */
  public IntersectionMatrix() {
    matrix = new int[3][3];
    setAll(Dimension.FALSE);
  }

  /**
   *  Creates an <code>IntersectionMatrix</code> with the same elements as
   *  <code>other</code>.
   *
   *@param  other  an <code>IntersectionMatrix</code> to copy
   */
  public IntersectionMatrix(final IntersectionMatrix other) {
    this();
    matrix[INTERIOR][INTERIOR] = other.matrix[INTERIOR][INTERIOR];
    matrix[INTERIOR][BOUNDARY] = other.matrix[INTERIOR][BOUNDARY];
    matrix[INTERIOR][EXTERIOR] = other.matrix[INTERIOR][EXTERIOR];
    matrix[BOUNDARY][INTERIOR] = other.matrix[BOUNDARY][INTERIOR];
    matrix[BOUNDARY][BOUNDARY] = other.matrix[BOUNDARY][BOUNDARY];
    matrix[BOUNDARY][EXTERIOR] = other.matrix[BOUNDARY][EXTERIOR];
    matrix[EXTERIOR][INTERIOR] = other.matrix[EXTERIOR][INTERIOR];
    matrix[EXTERIOR][BOUNDARY] = other.matrix[EXTERIOR][BOUNDARY];
    matrix[EXTERIOR][EXTERIOR] = other.matrix[EXTERIOR][EXTERIOR];
  }

  /**
   *  Creates an <code>IntersectionMatrix</code> with the given dimension
   *  symbols.
   *
   *@param  elements  a String of nine dimension symbols in row major order
   */
  public IntersectionMatrix(final String elements) {
    this();
    set(elements);
  }

  /**
   * Adds one matrix to another.
   * Addition is defined by taking the maximum dimension value of each position
   * in the summand matrices.
   *
   * @param im the matrix to add
   */
  public void add(final IntersectionMatrix im) {
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        setAtLeast(i, j, im.get(i, j));
      }
    }
  }

  /**
   *  Returns the value of one of this matrix
   *  entries.
   *  The value of the provided index is one of the 
   *  values from the {@link Location} class.  
   *  The value returned is a constant 
   *  from the {@link Dimension} class.
   *
   *@param  row     the row of this <code>IntersectionMatrix</code>, indicating
   *      the interior, boundary or exterior of the first <code>Geometry</code>
   *@param  column  the column of this <code>IntersectionMatrix</code>,
   *      indicating the interior, boundary or exterior of the second <code>Geometry</code>
   *@return         the dimension value at the given matrix position.
   */
  public int get(final int row, final int column) {
    return matrix[row][column];
  }

  /**
   *  Tests whether this <code>IntersectionMatrix</code> is
   *  T*****FF*.
   *
   *@return    <code>true</code> if the first <code>Geometry</code> contains the
   *      second
   */
  public boolean isContains() {
    return isTrue(matrix[INTERIOR][INTERIOR])
      && matrix[EXTERIOR][INTERIOR] == Dimension.FALSE
      && matrix[EXTERIOR][BOUNDARY] == Dimension.FALSE;
  }

  /**
   *  Returns <code>true</code> if this <code>IntersectionMatrix</code> is
   *    <code>T*F**F***</code>
   * or <code>*TF**F***</code>
   * or <code>**FT*F***</code>
   * or <code>**F*TF***</code>
   *
   *@return    <code>true</code> if the first <code>Geometry</code>
   * is covered by the second
   */
  public boolean isCoveredBy() {
    final boolean hasPointInCommon = isTrue(matrix[INTERIOR][INTERIOR])
      || isTrue(matrix[INTERIOR][BOUNDARY])
      || isTrue(matrix[BOUNDARY][INTERIOR])
      || isTrue(matrix[BOUNDARY][BOUNDARY]);

    return hasPointInCommon && matrix[INTERIOR][EXTERIOR] == Dimension.FALSE
      && matrix[BOUNDARY][EXTERIOR] == Dimension.FALSE;
  }

  /**
   *  Returns <code>true</code> if this <code>IntersectionMatrix</code> is
   *    <code>T*****FF*</code>
   * or <code>*T****FF*</code>
   * or <code>***T**FF*</code>
   * or <code>****T*FF*</code>
   *
   *@return    <code>true</code> if the first <code>Geometry</code> covers the
   *      second
   */
  public boolean isCovers() {
    final boolean hasPointInCommon = isTrue(matrix[INTERIOR][INTERIOR])
      || isTrue(matrix[INTERIOR][BOUNDARY])
      || isTrue(matrix[BOUNDARY][INTERIOR])
      || isTrue(matrix[BOUNDARY][BOUNDARY]);

    return hasPointInCommon && matrix[EXTERIOR][INTERIOR] == Dimension.FALSE
      && matrix[EXTERIOR][BOUNDARY] == Dimension.FALSE;
  }

  /**
   * Tests whether this geometry crosses the
   * specified geometry.
   * <p>
   * The <code>crosses</code> predicate has the following equivalent definitions:
   * <ul>
   * <li>The geometries have some but not all interior points in common.
   * <li>The DE-9IM Intersection Matrix for the two geometries is
   *   <ul>
   *    <li>T*T****** (for P/L, P/A, and L/A situations)
   *    <li>T*****T** (for L/P, L/A, and A/L situations)
   *    <li>0******** (for L/L situations)
   *   </ul>
   * </ul>
   * For any other combination of dimensions this predicate returns <code>false</code>.
   * <p>
   * The SFS defined this predicate only for P/L, P/A, L/L, and L/A situations.
   * JTS extends the definition to apply to L/P, A/P and A/L situations as well.
   * This makes the relation symmetric.
   *
   *@param  dimensionOfGeometryA  the dimension of the first <code>Geometry</code>
   *@param  dimensionOfGeometryB  the dimension of the second <code>Geometry</code>
   *@return                       <code>true</code> if the two <code>Geometry</code>s
   *      related by this <code>IntersectionMatrix</code> cross.
   */
  public boolean isCrosses(final int dimensionOfGeometryA,
    final int dimensionOfGeometryB) {
    if ((dimensionOfGeometryA == Dimension.P && dimensionOfGeometryB == Dimension.L)
      || (dimensionOfGeometryA == Dimension.P && dimensionOfGeometryB == Dimension.A)
      || (dimensionOfGeometryA == Dimension.L && dimensionOfGeometryB == Dimension.A)) {
      return isTrue(matrix[INTERIOR][INTERIOR])
        && isTrue(matrix[INTERIOR][EXTERIOR]);
    }
    if ((dimensionOfGeometryA == Dimension.L && dimensionOfGeometryB == Dimension.P)
      || (dimensionOfGeometryA == Dimension.A && dimensionOfGeometryB == Dimension.P)
      || (dimensionOfGeometryA == Dimension.A && dimensionOfGeometryB == Dimension.L)) {
      return isTrue(matrix[INTERIOR][INTERIOR])
        && isTrue(matrix[EXTERIOR][INTERIOR]);
    }
    if (dimensionOfGeometryA == Dimension.L
      && dimensionOfGeometryB == Dimension.L) {
      return matrix[INTERIOR][INTERIOR] == 0;
    }
    return false;
  }

  /**
   *  Returns <code>true</code> if this <code>IntersectionMatrix</code> is
   *  FF*FF****.
   *
   *@return    <code>true</code> if the two <code>Geometry</code>s related by
   *      this <code>IntersectionMatrix</code> are disjoint
   */
  public boolean isDisjoint() {
    return matrix[INTERIOR][INTERIOR] == Dimension.FALSE
      && matrix[INTERIOR][BOUNDARY] == Dimension.FALSE
      && matrix[BOUNDARY][INTERIOR] == Dimension.FALSE
      && matrix[BOUNDARY][BOUNDARY] == Dimension.FALSE;
  }

  /**
   *  Tests whether the argument dimensions are equal and 
   *  this <code>IntersectionMatrix</code> matches
   *  the pattern <tt>T*F**FFF*</tt>.
   *  <p>
   *  <b>Note:</b> This pattern differs from the one stated in 
   *  <i>Simple feature access - Part 1: Common architecture</i>.
   *  That document states the pattern as <tt>TFFFTFFFT</tt>.  This would
   *  specify that
   *  two identical <tt>POINT</tt>s are not equal, which is not desirable behaviour.
   *  The pattern used here has been corrected to compute equality in this situation.
   *
   *@param  dimensionOfGeometryA  the dimension of the first <code>Geometry</code>
   *@param  dimensionOfGeometryB  the dimension of the second <code>Geometry</code>
   *@return                       <code>true</code> if the two <code>Geometry</code>s
   *      related by this <code>IntersectionMatrix</code> are equal; the
   *      <code>Geometry</code>s must have the same dimension to be equal
   */
  public boolean isEquals(final int dimensionOfGeometryA,
    final int dimensionOfGeometryB) {
    if (dimensionOfGeometryA != dimensionOfGeometryB) {
      return false;
    }
    return isTrue(matrix[INTERIOR][INTERIOR])
      && matrix[INTERIOR][EXTERIOR] == Dimension.FALSE
      && matrix[BOUNDARY][EXTERIOR] == Dimension.FALSE
      && matrix[EXTERIOR][INTERIOR] == Dimension.FALSE
      && matrix[EXTERIOR][BOUNDARY] == Dimension.FALSE;
  }

  /**
   *  Returns <code>true</code> if <code>isDisjoint</code> returns false.
   *
   *@return    <code>true</code> if the two <code>Geometry</code>s related by
   *      this <code>IntersectionMatrix</code> intersect
   */
  public boolean isIntersects() {
    return !isDisjoint();
  }

  /**
   *  Returns <code>true</code> if this <code>IntersectionMatrix</code> is
   *  <UL>
   *    <LI> T*T***T** (for two points or two surfaces)
   *    <LI> 1*T***T** (for two curves)
   *  </UL>.
   *
   *@param  dimensionOfGeometryA  the dimension of the first <code>Geometry</code>
   *@param  dimensionOfGeometryB  the dimension of the second <code>Geometry</code>
   *@return                       <code>true</code> if the two <code>Geometry</code>s
   *      related by this <code>IntersectionMatrix</code> overlap. For this
   *      function to return <code>true</code>, the <code>Geometry</code>s must
   *      be two points, two curves or two surfaces.
   */
  public boolean isOverlaps(final int dimensionOfGeometryA,
    final int dimensionOfGeometryB) {
    if ((dimensionOfGeometryA == Dimension.P && dimensionOfGeometryB == Dimension.P)
      || (dimensionOfGeometryA == Dimension.A && dimensionOfGeometryB == Dimension.A)) {
      return isTrue(matrix[INTERIOR][INTERIOR])
        && isTrue(matrix[INTERIOR][EXTERIOR])
        && isTrue(matrix[EXTERIOR][INTERIOR]);
    }
    if (dimensionOfGeometryA == Dimension.L
      && dimensionOfGeometryB == Dimension.L) {
      return matrix[INTERIOR][INTERIOR] == 1
        && isTrue(matrix[INTERIOR][EXTERIOR])
        && isTrue(matrix[EXTERIOR][INTERIOR]);
    }
    return false;
  }

  /**
   *  Returns <code>true</code> if this <code>IntersectionMatrix</code> is
   *  FT*******, F**T***** or F***T****.
   *
   *@param  dimensionOfGeometryA  the dimension of the first <code>Geometry</code>
   *@param  dimensionOfGeometryB  the dimension of the second <code>Geometry</code>
   *@return                       <code>true</code> if the two <code>Geometry</code>
   *      s related by this <code>IntersectionMatrix</code> touch; Returns false
   *      if both <code>Geometry</code>s are points.
   */
  public boolean isTouches(final int dimensionOfGeometryA,
    final int dimensionOfGeometryB) {
    if (dimensionOfGeometryA > dimensionOfGeometryB) {
      // no need to get transpose because pattern matrix is symmetrical
      return isTouches(dimensionOfGeometryB, dimensionOfGeometryA);
    }
    if ((dimensionOfGeometryA == Dimension.A && dimensionOfGeometryB == Dimension.A)
      || (dimensionOfGeometryA == Dimension.L && dimensionOfGeometryB == Dimension.L)
      || (dimensionOfGeometryA == Dimension.L && dimensionOfGeometryB == Dimension.A)
      || (dimensionOfGeometryA == Dimension.P && dimensionOfGeometryB == Dimension.A)
      || (dimensionOfGeometryA == Dimension.P && dimensionOfGeometryB == Dimension.L)) {
      return matrix[INTERIOR][INTERIOR] == Dimension.FALSE
        && (isTrue(matrix[INTERIOR][BOUNDARY])
          || isTrue(matrix[BOUNDARY][INTERIOR]) || isTrue(matrix[BOUNDARY][BOUNDARY]));
    }
    return false;
  }

  /**
   *  Tests whether this <code>IntersectionMatrix</code> is
   *  T*F**F***.
   *
   *@return    <code>true</code> if the first <code>Geometry</code> is within
   *      the second
   */
  public boolean isWithin() {
    return isTrue(matrix[INTERIOR][INTERIOR])
      && matrix[INTERIOR][EXTERIOR] == Dimension.FALSE
      && matrix[BOUNDARY][EXTERIOR] == Dimension.FALSE;
  }

  /**
   *  Returns whether the elements of this <code>IntersectionMatrix</code>
   *  satisfies the required dimension symbols.
   *
   *@param  requiredDimensionSymbols  nine dimension symbols with which to
   *      compare the elements of this <code>IntersectionMatrix</code>. Possible
   *      values are <code>{T, F, * , 0, 1, 2}</code>.
   *@return                           <code>true</code> if this <code>IntersectionMatrix</code>
   *      matches the required dimension symbols
   */
  public boolean matches(final String requiredDimensionSymbols) {
    if (requiredDimensionSymbols.length() != 9) {
      throw new IllegalArgumentException("Should be length 9: "
        + requiredDimensionSymbols);
    }
    for (int ai = 0; ai < 3; ai++) {
      for (int bi = 0; bi < 3; bi++) {
        if (!matches(matrix[ai][bi],
          requiredDimensionSymbols.charAt(3 * ai + bi))) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   *  Changes the value of one of this <code>IntersectionMatrix</code>s
   *  elements.
   *
   *@param  row             the row of this <code>IntersectionMatrix</code>,
   *      indicating the interior, boundary or exterior of the first <code>Geometry</code>
   *@param  column          the column of this <code>IntersectionMatrix</code>,
   *      indicating the interior, boundary or exterior of the second <code>Geometry</code>
   *@param  dimensionValue  the new value of the element
   */
  public void set(final int row, final int column, final int dimensionValue) {
    matrix[row][column] = dimensionValue;
  }

  public void set(final Location row, final Location column,
    final int dimensionValue) {
    set(row.getIndex(), column.getIndex(), dimensionValue);
  }

  /**
   *  Changes the elements of this <code>IntersectionMatrix</code> to the
   *  dimension symbols in <code>dimensionSymbols</code>.
   *
   *@param  dimensionSymbols  nine dimension symbols to which to set this <code>IntersectionMatrix</code>
   *      s elements. Possible values are <code>{T, F, * , 0, 1, 2}</code>
   */
  public void set(final String dimensionSymbols) {
    for (int i = 0; i < dimensionSymbols.length(); i++) {
      final int row = i / 3;
      final int col = i % 3;
      matrix[row][col] = Dimension.toDimensionValue(dimensionSymbols.charAt(i));
    }
  }

  /**
   *  Changes the elements of this <code>IntersectionMatrix</code> to <code>dimensionValue</code>
   *  .
   *
   *@param  dimensionValue  the dimension value to which to set this <code>IntersectionMatrix</code>
   *      s elements. Possible values <code>{TRUE, FALSE, DONTCARE, 0, 1, 2}</code>
   *      .
   */
  public void setAll(final int dimensionValue) {
    for (int ai = 0; ai < 3; ai++) {
      for (int bi = 0; bi < 3; bi++) {
        matrix[ai][bi] = dimensionValue;
      }
    }
  }

  /**
   *  Changes the specified element to <code>minimumDimensionValue</code> if the
   *  element is less.
   *
   *@param  row                    the row of this <code>IntersectionMatrix</code>
   *      , indicating the interior, boundary or exterior of the first <code>Geometry</code>
   *@param  column                 the column of this <code>IntersectionMatrix</code>
   *      , indicating the interior, boundary or exterior of the second <code>Geometry</code>
   *@param  minimumDimensionValue  the dimension value with which to compare the
   *      element. The order of dimension values from least to greatest is
   *      <code>{DONTCARE, TRUE, FALSE, 0, 1, 2}</code>.
   */
  public void setAtLeast(final int row, final int column,
    final int minimumDimensionValue) {
    if (matrix[row][column] < minimumDimensionValue) {
      matrix[row][column] = minimumDimensionValue;
    }
  }

  /**
   *  For each element in this <code>IntersectionMatrix</code>, changes the
   *  element to the corresponding minimum dimension symbol if the element is
   *  less.
   *
   *@param  minimumDimensionSymbols  nine dimension symbols with which to
   *      compare the elements of this <code>IntersectionMatrix</code>. The
   *      order of dimension values from least to greatest is <code>{DONTCARE, TRUE, FALSE, 0, 1, 2}</code>
   *      .
   */
  public void setAtLeast(final String minimumDimensionSymbols) {
    for (int i = 0; i < minimumDimensionSymbols.length(); i++) {
      final int row = i / 3;
      final int col = i % 3;
      setAtLeast(row, col,
        Dimension.toDimensionValue(minimumDimensionSymbols.charAt(i)));
    }
  }

  /**
   *  If row >= 0 and column >= 0, changes the specified element to <code>minimumDimensionValue</code>
   *  if the element is less. Does nothing if row <0 or column < 0.
   *
   *@param  row                    the row of this <code>IntersectionMatrix</code>
   *      , indicating the interior, boundary or exterior of the first <code>Geometry</code>
   *@param  column                 the column of this <code>IntersectionMatrix</code>
   *      , indicating the interior, boundary or exterior of the second <code>Geometry</code>
   *@param  minimumDimensionValue  the dimension value with which to compare the
   *      element. The order of dimension values from least to greatest is
   *      <code>{DONTCARE, TRUE, FALSE, 0, 1, 2}</code>.
   */
  public void setAtLeastIfValid(final int row, final int column,
    final int minimumDimensionValue) {
    if (row >= 0 && column >= 0) {
      setAtLeast(row, column, minimumDimensionValue);
    }
  }

  public void setAtLeastIfValid(final Location row, final Location column,
    final int minimumDimensionValue) {
    setAtLeastIfValid(row.getIndex(), column.getIndex(), minimumDimensionValue);
  }

  /**
   *  Returns a nine-character <code>String</code> representation of this <code>IntersectionMatrix</code>
   *  .
   *
   *@return    the nine dimension symbols of this <code>IntersectionMatrix</code>
   *      in row-major order.
   */
  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder("123456789");
    for (int ai = 0; ai < 3; ai++) {
      for (int bi = 0; bi < 3; bi++) {
        buf.setCharAt(3 * ai + bi, Dimension.toDimensionSymbol(matrix[ai][bi]));
      }
    }
    return buf.toString();
  }

  /**
   *  Transposes this IntersectionMatrix.
   *
   *@return    this <code>IntersectionMatrix</code> as a convenience
   */
  public IntersectionMatrix transpose() {
    int temp = matrix[1][0];
    matrix[1][0] = matrix[0][1];
    matrix[0][1] = temp;
    temp = matrix[2][0];
    matrix[2][0] = matrix[0][2];
    matrix[0][2] = temp;
    temp = matrix[2][1];
    matrix[2][1] = matrix[1][2];
    matrix[1][2] = temp;
    return this;
  }
}
