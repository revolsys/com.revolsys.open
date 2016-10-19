/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.index;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.predicate.Predicates;
import com.revolsys.visitor.CreateListVisitor;

/**
 * The basic operations supported by classes
 * implementing spatial index algorithms.
 * <p>
 * A spatial index typically provides a primary filter for range rectangle queries.
 * A secondary filter is required to test for exact intersection.
 * The secondary filter may consist of other kinds of tests,
 * such as testing other spatial relationships.
 *
 * @version 1.7
 */
public interface SpatialIndex<V> extends GeometryFactoryProxy {

  default void forEach(BoundingBox boundingBox, final Consumer<? super V> action) {
    boundingBox = convertBoundingBox(boundingBox);
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    forEach(minX, minY, maxX, maxY, action);
  }

  default void forEach(BoundingBox boundingBox, final Predicate<? super V> filter,
    final Consumer<? super V> action) {
    boundingBox = convertBoundingBox(boundingBox);
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    forEach(minX, minY, maxX, maxY, filter, action);
  }

  void forEach(final Consumer<? super V> action);

  void forEach(double x, double y, Consumer<? super V> action);

  void forEach(double minX, double minY, double maxX, double maxY, Consumer<? super V> action);

  default void forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Predicate<? super V> filter, final Consumer<? super V> action) {
    final Consumer<? super V> filteredAction = Predicates.newConsumer(filter, action);
    forEach(minX, minY, maxX, maxY, filteredAction);
    ;
  }

  default void forEach(final double x, final double y, final Predicate<? super V> filter,
    final Consumer<? super V> action) {
    final Consumer<? super V> filteredAction = Predicates.newConsumer(filter, action);
    forEach(x, y, filteredAction);
  }

  default void forEach(final Predicate<? super V> filter, final Consumer<? super V> action) {
    final Consumer<? super V> filteredAction = Predicates.newConsumer(filter, action);
    forEach(filteredAction);
  }

  default List<V> getItems() {
    final CreateListVisitor<V> visitor = new CreateListVisitor<>();
    forEach(visitor);
    return visitor.getList();
  }

  /**
   * Queries the index for all items whose extents intersect the given search {@link BoundingBox}
   * Note that some kinds of indexes may also return objects which do not in fact
   * intersect the query envelope.
   *
   * @param boundingBox the envelope to query for
   * @return a list of the items found by the query
   */
  default List<V> getItems(final BoundingBox boundingBox) {
    final CreateListVisitor<V> visitor = new CreateListVisitor<>();
    forEach(boundingBox, visitor);
    return visitor.getList();
  }

  default List<V> getItems(final BoundingBox boundingBox, final Predicate<? super V> filter) {
    final CreateListVisitor<V> visitor = new CreateListVisitor<>();
    forEach(boundingBox, filter, visitor);
    return visitor.getList();
  }

  default List<V> getItems(final double x, final double y) {
    final CreateListVisitor<V> visitor = new CreateListVisitor<>();
    forEach(x, y, visitor);
    return visitor.getList();
  }

  default List<V> getItems(final double minX, final double minY, final double maxX,
    final double maxY, final Predicate<? super V> filter) {
    final CreateListVisitor<V> visitor = new CreateListVisitor<>();
    forEach(minX, minY, maxX, maxY, filter, visitor);
    return visitor.getList();
  }

  default List<V> getItems(final double x, final double y, final Predicate<? super V> filter) {
    final CreateListVisitor<V> visitor = new CreateListVisitor<>();
    forEach(x, y, filter, visitor);
    return visitor.getList();
  }

  int getSize();

  /**
   * Adds a spatial item with an extent specified by the given {@link BoundingBox} to the index
   */
  void insertItem(BoundingBox boundingBox, V item);

  /**
   * Removes a single item from the tree.
   *
   * @param boundingBox the BoundingBox of the item to remove
   * @param item the item to remove
   * @return <code>true</code> if the item was found
   */
  boolean removeItem(BoundingBox getItems, V item);

}
