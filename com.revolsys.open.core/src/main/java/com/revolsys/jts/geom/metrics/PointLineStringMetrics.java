package com.revolsys.jts.geom.metrics;

import com.revolsys.jts.geom.Side;
import com.revolsys.util.MathUtil;

public class PointLineStringMetrics {

  public static PointLineStringMetrics EMPTY = new PointLineStringMetrics(0, Double.MAX_VALUE,
    Double.MAX_VALUE, null);

  private final double distance;

  private final double distanceAlong;

  private final Side side;

  private final double lineLength;

  public PointLineStringMetrics(final double lineLength, final double distanceAlong,
    final double distance, final Side side) {
    this.lineLength = lineLength;
    this.distance = distance;
    this.distanceAlong = distanceAlong;
    this.side = side;
  }

  public double getDistance() {
    return this.distance;
  }

  public double getDistanceAlong() {
    return this.distanceAlong;
  }

  public double getLineLength() {
    return this.lineLength;
  }

  public Side getSide() {
    return this.side;
  }

  public boolean isAfterLine() {
    return this.distanceAlong > this.lineLength;
  }

  public boolean isBeforeLine() {
    return this.distanceAlong < 0;
  }

  public boolean isBesideLine() {
    return 0 <= this.distanceAlong && this.distanceAlong <= this.lineLength;
  }

  public boolean isOnLine() {
    return this.side == null && isBesideLine();
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder("{");
    if (this.lineLength > 0) {
      string.append("L:");
      string.append(MathUtil.toString(this.lineLength));
    }
    if (this.distanceAlong != Double.MAX_VALUE) {
      if (string.length() > 0) {
        string.append(',');
      }
      string.append("A:");
      string.append(MathUtil.toString(this.distanceAlong));
    }
    if (this.distance != Double.MAX_VALUE) {
      if (string.length() > 0) {
        string.append(',');
      }
      string.append("D:");
      string.append(MathUtil.toString(this.distance));
    }
    if (this.side != null) {
      if (string.length() > 0) {
        string.append(',');
      }
      string.append("S=\"");
      string.append(this.side);
      string.append('"');
    }
    string.append("}");
    return string.toString();
  }

  public boolean withinDistanceFromEnds(final double distance) {
    if (Math.abs(this.distanceAlong) <= distance) {
      return true;
    } else if (Math.abs(this.lineLength - this.distanceAlong) <= distance) {
      return true;
    } else {
      return false;
    }
  }
}
