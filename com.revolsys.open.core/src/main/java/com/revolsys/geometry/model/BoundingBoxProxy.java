package com.revolsys.geometry.model;

import java.util.function.BiFunction;

public interface BoundingBoxProxy extends GeometryFactoryProxy {
  default boolean bboxCoveredBy(final BoundingBoxProxy boundingBox) {
    return bboxWith(boundingBox, BoundingBox::coveredBy);
  }

  default boolean bboxCovers(final BoundingBoxProxy boundingBox) {
    return bboxWith(boundingBox, BoundingBox::covers);
  }

  default double bboxDistance(final BoundingBoxProxy boundingBox) {
    return bboxWith(boundingBox, BoundingBox::distance);
  }

  default boolean bboxEquals(final BoundingBoxProxy boundingBox) {
    return bboxWith(boundingBox, BoundingBox::equals);
  }

  default boolean bboxIntersects(final BoundingBoxProxy boundingBox) {
    return bboxWith(boundingBox, BoundingBox::intersects);
  }

  default boolean bboxIntersects(final double x1, final double y1, final double x2,
    final double y2) {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.intersects(x1, y1, x2, y2);
  }

  default boolean bboxIntersectsFast(final BoundingBoxProxy boundingBox) {
    return bboxWith(boundingBox, BoundingBox::intersectsFast);
  }

  default <R> R bboxWith(final BoundingBoxProxy boundingBox,
    final BiFunction<BoundingBox, BoundingBox, R> action) {
    final BoundingBox boundingBox1 = getBoundingBox();
    BoundingBox boundingBox2;
    if (boundingBox == null) {
      boundingBox2 = BoundingBox.empty();
    } else {
      boundingBox2 = boundingBox.getBoundingBox();
    }
    return action.apply(boundingBox1, boundingBox2);
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
}
