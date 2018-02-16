package com.revolsys.geometry.util;

public interface OutCode {
  /** Bit flag indicating a point is to the left of a rectangle. */
  public static final int OUT_LEFT = 1; // 0001 Wikipedia value, others have different bit orders

  /** Bit flag indicating a point is to the right of a rectangle. */
  public static final int OUT_RIGHT = 2; // 0010 Wikipedia value, others have different bit orders

  /** Bit flag indicating a point is below a rectangle. */
  public static final int OUT_BOTTOM = 4; // 0100 Wikipedia value, others have different bit orders

  /** Bit flag indicating a point is above a rectangle. */
  public static final int OUT_TOP = 8; // 1000 Wikipedia value, others have different bit orders

  static boolean isBottom(final int outCode) {
    return (outCode & OUT_BOTTOM) != 0;
  }

  static boolean isInside(final int outCode) {
    return outCode == 0;
  }

  static boolean isLeft(final int outCode) {
    return (outCode & OUT_LEFT) != 0;
  }

  static boolean isRight(final int outCode) {
    return (outCode & OUT_RIGHT) != 0;
  }

  static boolean isTop(final int outCode) {
    return (outCode & OUT_TOP) != 0;
  }

  static int getOutcode(final double minX, final double minY, final double maxX,
    final double maxY, final double x, final double y) {
    int out = 0;
    if (x < minX) {
      out = OUT_LEFT;
    } else if (x > maxX) {
      out = OUT_RIGHT;
    }
    if (y < minY) {
      out |= OUT_BOTTOM;
    } else if (y > maxY) {
      out |= OUT_TOP;
    }
    return out;
  }
}
