package com.revolsys.elevation.tin;

public interface TriangleConsumer {
  void accept(double x1, double y1, double z1, double x2, double y2, double z2, double x3,
    double y3, double z3);
}
