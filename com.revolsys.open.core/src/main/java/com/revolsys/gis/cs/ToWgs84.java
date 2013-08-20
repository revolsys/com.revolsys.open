package com.revolsys.gis.cs;

import java.util.List;

public class ToWgs84 {

  private final double dx;

  private final double dy;

  private final double dz;

  private final double ex;

  private final double ey;

  private final double ez;

  private final double ppm;

  public ToWgs84(final List<Object> values) {
    dx = ((Number)values.get(0)).doubleValue();
    dy = ((Number)values.get(1)).doubleValue();
    dz = ((Number)values.get(2)).doubleValue();
    ex = ((Number)values.get(3)).doubleValue();
    ey = ((Number)values.get(4)).doubleValue();
    ez = ((Number)values.get(5)).doubleValue();
    ppm = ((Number)values.get(6)).doubleValue();
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof ToWgs84) {
      final ToWgs84 toWgs84 = (ToWgs84)object;
      if (dx != toWgs84.dx) {
        return false;
      } else if (dy != toWgs84.dy) {
        return false;
      } else if (dz != toWgs84.dz) {
        return false;
      } else if (ex != toWgs84.ex) {
        return false;
      } else if (ey != toWgs84.ey) {
        return false;
      } else if (ez != toWgs84.ez) {
        return false;
      } else if (ppm != toWgs84.ppm) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public double getDx() {
    return dx;
  }

  public double getDy() {
    return dy;
  }

  public double getDz() {
    return dz;
  }

  public double getEx() {
    return ex;
  }

  public double getEy() {
    return ey;
  }

  public double getEz() {
    return ez;
  }

  public double getPpm() {
    return ppm;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp = Double.doubleToLongBits(dx);
    result = prime * result + (int)(temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(dy);
    result = prime * result + (int)(temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(dz);
    result = prime * result + (int)(temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(ex);
    result = prime * result + (int)(temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(ey);
    result = prime * result + (int)(temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(ez);
    result = prime * result + (int)(temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(ppm);
    result = prime * result + (int)(temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return dx + ", " + dy + ", " + dz + ", " + ex + ", " + ey + ", " + ez
      + ", " + ppm;
  }
}
