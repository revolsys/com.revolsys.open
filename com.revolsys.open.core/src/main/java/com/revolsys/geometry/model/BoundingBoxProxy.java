package com.revolsys.geometry.model;

public interface BoundingBoxProxy extends GeometryFactoryProxy {
  BoundingBox getBoundingBox();

  @Override
  default GeometryFactory getGeometryFactory() {
    final BoundingBox boundingBox = getBoundingBox();
    if (boundingBox == null) {
      return GeometryFactory.DEFAULT_2D;
    } else {
      return GeometryFactoryProxy.super.getGeometryFactory();
    }
  }
}
