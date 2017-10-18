package com.revolsys.geometry.index.strtree;

public interface Bounds<B> {

  boolean intersectsBounds(B bounds);
}
