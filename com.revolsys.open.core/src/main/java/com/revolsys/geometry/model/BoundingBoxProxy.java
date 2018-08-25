package com.revolsys.geometry.model;

import java.util.function.BiFunction;

public interface BoundingBoxProxy extends GeometryFactoryProxy {
  default boolean bboxCoveredBy(final BoundingBoxProxy boundingBox) {
    final BoundingBoxFunction<Boolean> action = BoundingBox::coveredBy;
    return bboxWith(boundingBox, action, false);
  }

  default boolean bboxCovers(final BoundingBoxProxy boundingBox) {
    final BoundingBoxFunction<Boolean> action = BoundingBox::covers;
    return bboxWith(boundingBox, action, false);
  }

  default double bboxDistance(final BoundingBoxProxy boundingBox) {
    final BoundingBoxFunction<Double> action = BoundingBox::bboxDistance;
    return bboxWith(boundingBox, action, Double.POSITIVE_INFINITY);
  }

  default double bboxDistance(final double minX, final double minY, final double maxX,
    final double maxY) {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.bboxDistance(minX, minY, maxX, maxY);
  }

  default boolean bboxEquals(final BoundingBoxProxy boundingBox) {
    final BiFunction<BoundingBox, BoundingBox, Boolean> action = BoundingBox::equals;
    return bboxWith(boundingBox, action, false);
  }

  default boolean bboxIntersects(final BoundingBoxProxy boundingBox) {
    final BiFunction<BoundingBox, BoundingBox, Boolean> action = BoundingBox::intersects;
    return bboxWith(boundingBox, action, false);
  }

  default boolean bboxIntersects(final double x1, final double y1, final double x2,
    final double y2) {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.intersects(x1, y1, x2, y2);
  }

  default boolean bboxIntersectsFast(final BoundingBoxProxy boundingBox) {
    return bboxWith(boundingBox, BoundingBox::intersectsFast, false);
  }

  default <R> R bboxWith(final BoundingBoxProxy boundingBox,
    final BiFunction<BoundingBox, BoundingBox, R> action, final R emptyValue) {
    final BoundingBox boundingBox1 = getBoundingBox();
    return boundingBox1.bboxWith(boundingBox, action, emptyValue);
  }

  default <R> R bboxWith(final BoundingBoxProxy boundingBox, final BoundingBoxFunction<R> action,
    final R emptyResult) {
    final BoundingBox boundingBox1 = getBoundingBox();
    return boundingBox1.bboxWith(boundingBox, action, emptyResult);
  }

  default boolean bboxWithinDistance(final BoundingBoxProxy boundingBox, final double maxDistance) {
    final double distance = bboxDistance(boundingBox);
    if (distance < maxDistance) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Return the {@link BoundingBox} encompassing this object. The return value must never
   * be null an {@link BoundingBox#isEmpty()} object must be returned instead.
   *
   * @return The boundingBox
   */
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

  default boolean isBboxEmpty() {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox == null || boundingBox.isEmpty();
  }
}
