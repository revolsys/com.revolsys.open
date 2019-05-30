package com.revolsys.geometry.model;

import java.util.function.BiFunction;

import com.revolsys.geometry.model.editor.BoundingBoxEditor;

public interface BoundingBoxProxy extends GeometryFactoryProxy {
  default BoundingBoxEditor bboxEditor() {
    return new BoundingBoxEditor(this);
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
