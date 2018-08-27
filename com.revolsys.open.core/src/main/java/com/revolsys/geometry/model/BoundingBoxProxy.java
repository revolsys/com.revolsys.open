package com.revolsys.geometry.model;

import java.util.function.BiFunction;

public interface BoundingBoxProxy extends GeometryFactoryProxy {
  default boolean bboxCoveredBy(final BoundingBoxProxy boundingBox) {
    final BoundingBoxFunction<Boolean> action = BoundingBox::bboxCoveredBy;
    return bboxWith(boundingBox, action, false);
  }

  default boolean bboxCovers(final BoundingBoxProxy boundingBox) {
    final BoundingBoxFunction<Boolean> action = BoundingBox::bbxCovers;
    return bboxWith(boundingBox, action, false);
  }

  /**
   * Tests if the given point lies in or on the envelope.
   *
   *@param  p  the point which this <code>BoundingBox</code> is
   *      being checked for containing
   *@return    <code>true</code> if the point lies in the interior or
   *      on the boundary of this <code>BoundingBox</code>.
   */
  default boolean bboxCovers(final Point point) {
    final BoundingBoxPointFunction<Boolean> action = BoundingBox::bboxCovers;
    return bboxWith(point, action, false);
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

  /**
   * Computes the distance between this and another
   * <code>BoundingBox</code>.
   * The distance between overlapping Envelopes is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  default double bboxDistance(final Point point) {
    final BoundingBoxPointFunction<Double> action = BoundingBox::bboxDistance;
    return bboxWith(point, action, Double.POSITIVE_INFINITY);
  }

  default boolean bboxEquals(final BoundingBoxProxy boundingBox) {
    final BiFunction<BoundingBox, BoundingBox, Boolean> action = BoundingBox::equals;
    return bboxWith(boundingBox, action, false);
  }

  /**
   *  Check if the region defined by <code>other</code>
   *  overlaps (intersects) the region of this <code>BoundingBox</code>.
   *
   *@param  other  the <code>BoundingBox</code> which this <code>BoundingBox</code> is
   *          being checked for overlapping
   *@return        <code>true</code> if the <code>BoundingBox</code>s overlap
   */
  default boolean bboxIntersects(final BoundingBoxProxy boundingBox) {
    final BoundingBoxFunction<Boolean> action = BoundingBox::bboxIntersects;
    return bboxWith(boundingBox, action, false);
  }

  default boolean bboxIntersects(final double x1, final double y1, final double x2,
    final double y2) {
    final BoundingBox boundingBox = getBoundingBox();
    return boundingBox.bboxIntersects(x1, y1, x2, y2);
  }

  default boolean bboxIntersects(final Point point) {
    final BoundingBoxPointFunction<Boolean> action = BoundingBox::bboxIntersects;
    return bboxWith(point, action, false);
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

  default <R> R bboxWith(final Point point, final BoundingBoxPointFunction<R> action,
    final R emptyResult) {
    final BoundingBox boundingBox1 = getBoundingBox();
    return boundingBox1.bboxWith(point, action, emptyResult);
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
