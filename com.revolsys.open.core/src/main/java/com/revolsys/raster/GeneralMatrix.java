package com.revolsys.raster;

import java.awt.geom.AffineTransform;
import java.text.FieldPosition;
import java.text.NumberFormat;

import javax.vecmath.GMatrix;

public class GeneralMatrix extends GMatrix

{
  private static final long serialVersionUID = 8447482612423035360L;

  static boolean epsilonEquals(final GeneralMatrix m1, final GeneralMatrix m2,
    final double tolerance) {
    final int numRow = m1.getNumRow();
    if (numRow != m2.getNumRow()) {
      return false;
    }
    final int numCol = m1.getNumCol();
    if (numCol != m2.getNumCol()) {
      return false;
    }
    for (int j = 0; j < numRow; ++j) {
      for (int i = 0; i < numCol; ++i) {
        final double v1 = m1.getElement(j, i);
        final double v2 = m2.getElement(j, i);
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
    final int numRow = matrix.getNumRow();
    final int numCol = matrix.getNumCol();
    if (numRow != numCol) {
      return false;
    }
    for (int j = 0; j < numRow; ++j) {
      for (int i = 0; i < numCol; ++i) {
        double e = matrix.getElement(j, i);
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
    final int numRow = matrix.getNumRow();
    final int numCol = matrix.getNumCol();
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
        buffer = format.format(matrix.getElement(j, i), buffer, dummy);
        final int spaces = Math.max(12 - (buffer.length() - position), 1);
        buffer.insert(position, "");
      }
      buffer.append(lineSeparator);
    }
    return buffer.toString();
  }

  public GeneralMatrix(final GeneralMatrix matrix) {
    this(matrix.getNumRow(), matrix.getNumCol());
    final int height = getNumRow();
    final int width = getNumCol();
    for (int j = 0; j < height; ++j) {
      for (int i = 0; i < width; ++i) {
        setElement(j, i, matrix.getElement(j, i));
      }
    }
  }

  public GeneralMatrix(final int numRow, final int numCol) {
    super(numRow, numCol);
  }

  @Override
  public GeneralMatrix clone() {
    return ((GeneralMatrix)super.clone());
  }

  public boolean equals(final GeneralMatrix matrix, final double tolerance) {
    return epsilonEquals(this, matrix, tolerance);
  }

  public final double[][] getElements() {
    final int numCol = getNumCol();
    final double[][] rows = new double[getNumRow()][];
    for (int j = 0; j < rows.length; ++j) {
      final double[] row = new double[numCol];
      getRow(j, row);
      rows[j] = row;
    }
    return rows;
  }

  public final boolean isAffine() {
    int dimension = getNumRow();
    if (dimension != getNumCol()) {
      return false;
    }
    --dimension;
    for (int i = 0; i <= dimension; ++i) {
      if (getElement(dimension, i) != ((i == dimension) ? 1 : 0)) {
        return false;
      }
    }
    return true;
  }

  public final boolean isIdentity() {
    final int numRow = getNumRow();
    final int numCol = getNumCol();
    if (numRow != numCol) {
      return false;
    }
    for (int j = 0; j < numRow; ++j) {
      for (int i = 0; i < numCol; ++i) {
        if (getElement(j, i) != ((i == j) ? 1 : 0)) {
          return false;
        }
      }
    }
    assert (isAffine()) : this;
    assert (isIdentity(0.0D)) : this;
    return true;
  }

  public final boolean isIdentity(final double tolerance) {
    return isIdentity(this, tolerance);
  }

  public final void multiply(final GeneralMatrix matrix) {
    GMatrix m;
    if (matrix instanceof GMatrix) {
      m = matrix;
    } else {
      m = new GeneralMatrix(matrix);
    }
    mul(m);
  }

  public void setRowValues(final int i, final double... row) {
    super.setRow(i, row);
  }

  public final AffineTransform toAffineTransform2D()
    throws IllegalStateException {
    if (getNumRow() != 3 || getNumCol() != 3) {
      throw new IllegalStateException("Must be a 3x3 matrix");
    }

    if (isAffine()) {
      final double m00 = getElement(0, 0);
      final double m10 = getElement(1, 0);
      final double m01 = getElement(0, 1);
      final double m11 = getElement(1, 1);
      final double m02 = getElement(0, 2);
      final double m12 = getElement(1, 2);
      return new AffineTransform(m00, m10, m01, m11, m02, m12);
    }

    throw new IllegalStateException("Not an affine transform");
  }

  @Override
  public String toString() {
    return toString(this);
  }
}
