package com.revolsys.raster;

import java.awt.geom.AffineTransform;
import java.text.FieldPosition;
import java.text.NumberFormat;

import com.revolsys.math.matrix.Matrix;

public class GeneralMatrix extends Matrix

{
  private static final long serialVersionUID = 8447482612423035360L;

  static boolean epsilonEquals(final GeneralMatrix m1, final GeneralMatrix m2,
    final double tolerance) {
    final int numRow = m1.getRowCount();
    if (numRow != m2.getRowCount()) {
      return false;
    }
    final int numCol = m1.getColumnCount();
    if (numCol != m2.getColumnCount()) {
      return false;
    }
    for (int j = 0; j < numRow; ++j) {
      for (int i = 0; i < numCol; ++i) {
        final double v1 = m1.get(j, i);
        final double v2 = m2.get(j, i);
        if (Math.abs(v1 - v2) > tolerance) {
          if (Double.doubleToLongBits(v1) != Double.doubleToLongBits(v2)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  static boolean isIdentity(final GeneralMatrix matrix, double tolerance) {
    tolerance = Math.abs(tolerance);
    final int numRow = matrix.getRowCount();
    final int numCol = matrix.getColumnCount();
    if (numRow != numCol) {
      return false;
    }
    for (int j = 0; j < numRow; ++j) {
      for (int i = 0; i < numCol; ++i) {
        double e = matrix.get(j, i);
        if (i == j) {
          e -= 1.0D;
        }
        if (Math.abs(e) > tolerance) {
          return false;
        }
      }
    }

    return true;
  }

  static String toString(final GeneralMatrix matrix) {
    final int numRow = matrix.getRowCount();
    final int numCol = matrix.getColumnCount();
    StringBuffer buffer = new StringBuffer();
    final int columnWidth = 12;
    final String lineSeparator = "\n";
    final FieldPosition dummy = new FieldPosition(0);
    final NumberFormat format = NumberFormat.getNumberInstance();
    format.setGroupingUsed(false);
    format.setMinimumFractionDigits(6);
    format.setMaximumFractionDigits(6);
    for (int j = 0; j < numRow; ++j) {
      for (int i = 0; i < numCol; ++i) {
        final int position = buffer.length();
        buffer = format.format(matrix.get(j, i), buffer, dummy);
        final int spaces = Math.max(12 - (buffer.length() - position), 1);
        buffer.insert(position, "");
      }
      buffer.append(lineSeparator);
    }
    return buffer.toString();
  }

  public GeneralMatrix(final GeneralMatrix matrix) {
    this(matrix.getRowCount(), matrix.getColumnCount());
    final int height = getRowCount();
    final int width = getColumnCount();
    for (int j = 0; j < height; ++j) {
      for (int i = 0; i < width; ++i) {
        set(j, i, matrix.get(j, i));
      }
    }
  }

  public GeneralMatrix(final int numRow, final int numCol) {
    super(numRow, numCol);
  }

  @Override
  public GeneralMatrix clone() {
    return (GeneralMatrix)super.clone();
  }

  public boolean equals(final GeneralMatrix matrix, final double tolerance) {
    return epsilonEquals(this, matrix, tolerance);
  }

  public final double[][] gets() {
    final double[][] rows = new double[getRowCount()][];
    for (int rowIndex = 0; rowIndex < rows.length; ++rowIndex) {
      final double[] row = getRow(rowIndex);
      rows[rowIndex] = row;
    }
    return rows;
  }

  public final boolean isAffine() {
    int dimension = getRowCount();
    if (dimension != getColumnCount()) {
      return false;
    }
    --dimension;
    for (int i = 0; i <= dimension; ++i) {
      if (get(dimension, i) != (i == dimension ? 1 : 0)) {
        return false;
      }
    }
    return true;
  }

  public final boolean isIdentity() {
    final int numRow = getRowCount();
    final int numCol = getColumnCount();
    if (numRow != numCol) {
      return false;
    }
    for (int j = 0; j < numRow; ++j) {
      for (int i = 0; i < numCol; ++i) {
        if (get(j, i) != (i == j ? 1 : 0)) {
          return false;
        }
      }
    }
    assert isAffine() : this;
    assert isIdentity(0.0D) : this;
    return true;
  }

  public final boolean isIdentity(final double tolerance) {
    return isIdentity(this, tolerance);
  }

  public final void multiply(final GeneralMatrix matrix) {
    Matrix m;
    if (matrix instanceof Matrix) {
      m = matrix;
    } else {
      m = new GeneralMatrix(matrix);
    }
    times(m);
  }

  public void setRowValues(final int i, final double... row) {
    super.setRow(i, row);
  }

  public final AffineTransform toAffineTransform2D() throws IllegalStateException {
    if (getRowCount() != 3 || getColumnCount() != 3) {
      throw new IllegalStateException("Must be a 3x3 matrix");
    }

    if (isAffine()) {
      final double m00 = get(0, 0);
      final double m10 = get(1, 0);
      final double m01 = get(0, 1);
      final double m11 = get(1, 1);
      final double m02 = get(0, 2);
      final double m12 = get(1, 2);
      return new AffineTransform(m00, m10, m01, m11, m02, m12);
    }

    throw new IllegalStateException("Not an affine transform");
  }

  @Override
  public String toString() {
    return toString(this);
  }

}
