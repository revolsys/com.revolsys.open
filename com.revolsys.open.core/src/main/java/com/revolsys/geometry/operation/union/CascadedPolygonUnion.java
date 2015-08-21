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
package com.revolsys.geometry.operation.union;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.index.strtree.STRtree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.util.GeometryCombiner;

/**
 * Provides an efficient method of unioning a collection of
 * {@link Polygonal} geometrys.
 * The geometries are indexed using a spatial index,
 * and unioned recursively in index order.
 * For geometries with a high degree of overlap,
 * this has the effect of reducing the number of vertices
 * early in the process, which increases speed
 * and robustness.
 * <p>
 * This algorithm is faster and more robust than
 * the simple iterated approach of
 * repeatedly unioning each polygon to a result geometry.
 * <p>
 * The <tt>buffer(0)</tt> trick is sometimes faster, but can be less robust and
 * can sometimes take a long time to complete.
 * This is particularly the case where there is a high degree of overlap
 * between the polygons.  In this case, <tt>buffer(0)</tt> is forced to compute
 * with <i>all</i> line segments from the outset,
 * whereas cascading can eliminate many segments
 * at each stage of processing.
 * The best situation for using <tt>buffer(0)</tt> is the trivial case
 * where there is <i>no</i> overlap between the input geometries.
 * However, this case is likely rare in practice.
 *
 * @author Martin Davis
 *
 */
public class CascadedPolygonUnion {
  /**
   * The effectiveness of the index is somewhat sensitive
   * to the node capacity.
   * Testing indicates that a smaller capacity is better.
   * For an STRtree, 4 is probably a good number (since
   * this produces 2x2 "squares").
   */
  private static final int STRTREE_NODE_CAPACITY = 4;

  /**
   * Gets the element at a given list index, or
   * null if the index is out of range.
   *
   * @param list
   * @param index
   * @return the geometry at the given index
   * or null if the index is out of range
   */
  private static Geometry getGeometry(final List list, final int index) {
    if (index >= list.size()) {
      return null;
    }
    return (Geometry)list.get(index);
  }

  /**
   * Computes a {@link Geometry} containing only {@link Polygonal} components.
   * Extracts the {@link Polygon}s from the input
   * and returns them as an appropriate {@link Polygonal} geometry.
   * <p>
   * If the input is already <tt>Polygonal</tt>, it is returned unchanged.
   * <p>
   * A particular use case is to filter out non-polygonal components
   * returned from an overlay operation.
   *
   * @param geometry the geometry to filter
   * @return a Polygonal geometry
   */
  private static Geometry restrictToPolygons(final Geometry geometry) {
    if (geometry instanceof Polygonal) {
      return geometry;
    } else {
      final List<Polygon> polygons = geometry.getGeometries(Polygon.class);
      if (polygons.size() == 1) {
        return polygons.get(0);
      }
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();
      return geometryFactory.multiPolygon(polygons);
    }
  }

  /**
   * Computes the union of
   * a collection of {@link Polygonal} {@link Geometry}s.
   *
   * @param polys a collection of {@link Polygonal} {@link Geometry}s
   */
  public static Geometry union(final Collection polys) {
    final CascadedPolygonUnion op = new CascadedPolygonUnion(polys);
    return op.union();
  }

  private Collection inputPolys;

  private GeometryFactory geomFactory = null;

  /**
   * Creates a new instance to union
   * the given collection of {@link Geometry}s.
   *
   * @param polys a collection of {@link Polygonal} {@link Geometry}s
   */
  public CascadedPolygonUnion(final Collection polys) {
    this.inputPolys = polys;
    // guard against null input
    if (this.inputPolys == null) {
      this.inputPolys = new ArrayList();
    }
  }

  // ========================================================
  /*
   * The following methods are for experimentation only
   */

  /**
   * Unions a list of geometries
   * by treating the list as a flattened binary tree,
   * and performing a cascaded union on the tree.
   */
  private Geometry binaryUnion(final List geoms) {
    return binaryUnion(geoms, 0, geoms.size());
  }

  /**
   * Unions a section of a list using a recursive binary union on each half
   * of the section.
   *
   * @param geoms the list of geometries containing the section to union
   * @param start the start index of the section
   * @param end the index after the end of the section
   * @return the union of the list section
   */
  private Geometry binaryUnion(final List geoms, final int start, final int end) {
    if (end - start <= 1) {
      final Geometry g0 = getGeometry(geoms, start);
      return unionSafe(g0, null);
    } else if (end - start == 2) {
      return unionSafe(getGeometry(geoms, start), getGeometry(geoms, start + 1));
    } else {
      // recurse on both halves of the list
      final int mid = (end + start) / 2;
      final Geometry g0 = binaryUnion(geoms, start, mid);
      final Geometry g1 = binaryUnion(geoms, mid, end);
      return unionSafe(g0, g1);
    }
  }

  // =======================================

  private Geometry extractByEnvelope(final BoundingBox env, final Geometry geom,
    final List disjointGeoms) {
    final List intersectingGeoms = new ArrayList();
    for (int i = 0; i < geom.getGeometryCount(); i++) {
      final Geometry elem = geom.getGeometry(i);
      if (elem.getBoundingBox().intersects(env)) {
        intersectingGeoms.add(elem);
      } else {
        disjointGeoms.add(elem);
      }
    }
    return this.geomFactory.buildGeometry(intersectingGeoms);
  }

  /**
   * Reduces a tree of geometries to a list of geometries
   * by recursively unioning the subtrees in the list.
   *
   * @param geomTree a tree-structured list of geometries
   * @return a list of Geometrys
   */
  private List reduceToGeometries(final List geomTree) {
    final List geoms = new ArrayList();
    for (final Iterator i = geomTree.iterator(); i.hasNext();) {
      final Object o = i.next();
      Geometry geom = null;
      if (o instanceof List) {
        geom = unionTree((List)o);
      } else if (o instanceof Geometry) {
        geom = (Geometry)o;
      }
      geoms.add(geom);
    }
    return geoms;
  }

  /**
   * Computes the union of the input geometries.
   * <p>
   * This method discards the input geometries as they are processed.
   * In many input cases this reduces the memory retained
   * as the operation proceeds.
   * Optimal memory usage is achieved
   * by disposing of the original input collection
   * before calling this method.
   *
   * @return the union of the input geometries
   * or null if no input geometries were provided
   * @throws IllegalStateException if this method is called more than once
   */
  public Geometry union() {
    if (this.inputPolys == null) {
      throw new IllegalStateException("union() method cannot be called twice");
    }
    if (this.inputPolys.isEmpty()) {
      return null;
    }
    this.geomFactory = ((Geometry)this.inputPolys.iterator().next()).getGeometryFactory();

    /**
     * A spatial index to organize the collection
     * into groups of close geometries.
     * This makes unioning more efficient, since vertices are more likely
     * to be eliminated on each round.
     */
    // STRtree index = new STRtree();
    final STRtree index = new STRtree(STRTREE_NODE_CAPACITY);
    for (final Iterator i = this.inputPolys.iterator(); i.hasNext();) {
      final Geometry item = (Geometry)i.next();
      index.insert(item.getBoundingBox(), item);
    }
    // To avoiding holding memory remove references to the input geometries,
    this.inputPolys = null;

    final List itemTree = index.itemsTree();
    // printItemEnvelopes(itemTree);
    final Geometry unionAll = unionTree(itemTree);
    return unionAll;
  }

  /**
   * Encapsulates the actual unioning of two polygonal geometries.
   *
   * @param g0
   * @param g1
   * @return
   */
  private Geometry unionActual(final Geometry g0, final Geometry g1) {
    /*
     * System.out.println(g0.getNumGeometries() + ", " + g1.getNumGeometries());
     * if (g0.getNumGeometries() > 5) { System.out.println(g0);
     * System.out.println(g1); }
     */

    // return bufferUnion(g0, g1);
    return restrictToPolygons(g0.union(g1));
  }

  private Geometry unionOptimized(final Geometry g0, final Geometry g1) {
    final BoundingBox g0Env = g0.getBoundingBox();
    final BoundingBox g1Env = g1.getBoundingBox();
    // *
    if (!g0Env.intersects(g1Env)) {
      final Geometry combo = GeometryCombiner.combine(g0, g1);
      // System.out.println("Combined");
      // System.out.println(combo);
      return combo;
    }
    // */
    // System.out.println(g0.getNumGeometries() + ", " + g1.getNumGeometries());

    if (g0.getGeometryCount() <= 1 && g1.getGeometryCount() <= 1) {
      return unionActual(g0, g1);
    }

    // for testing...
    // if (true) return g0.union(g1);

    final BoundingBox commonEnv = g0Env.intersection(g1Env);
    return unionUsingEnvelopeIntersection(g0, g1, commonEnv);

    // return UnionInteracting.union(g0, g1);
  }

  /**
   * Computes the union of two geometries,
   * either or both of which may be null.
   *
   * @param g0 a Geometry
   * @param g1 a Geometry
   * @return the union of the input(s)
   * or null if both inputs are null
   */
  private Geometry unionSafe(final Geometry g0, final Geometry g1) {
    if (g0 == null && g1 == null) {
      return null;
    }

    if (g0 == null) {
      return g1.clone();
    }
    if (g1 == null) {
      return g0.clone();
    }

    return unionOptimized(g0, g1);
  }

  private Geometry unionTree(final List geomTree) {
    /**
     * Recursively unions all subtrees in the list into single geometries.
     * The result is a list of Geometrys only
     */
    final List geoms = reduceToGeometries(geomTree);
    // Geometry union = bufferUnion(geoms);
    final Geometry union = binaryUnion(geoms);

    // print out union (allows visualizing hierarchy)
    // System.out.println(union);

    return union;
    // return repeatedUnion(geoms);
    // return buffer0Union(geoms);

  }

  /**
   * Unions two polygonal geometries, restricting computation
   * to the envelope intersection where possible.
   * The case of MultiPolygons is optimized to union only
   * the polygons which lie in the intersection of the two geometry's envelopes.
   * Polygons outside this region can simply be combined with the union result,
   * which is potentially much faster.
   * This case is likely to occur often during cascaded union, and may also
   * occur in real world data (such as unioning data for parcels on different street blocks).
   *
   * @param g0 a polygonal geometry
   * @param g1 a polygonal geometry
   * @param common the intersection of the envelopes of the inputs
   * @return the union of the inputs
   */
  private Geometry unionUsingEnvelopeIntersection(final Geometry g0, final Geometry g1,
    final BoundingBox common) {
    final List disjointPolys = new ArrayList();

    final Geometry g0Int = extractByEnvelope(common, g0, disjointPolys);
    final Geometry g1Int = extractByEnvelope(common, g1, disjointPolys);

    // System.out.println("# geoms in common: " + intersectingPolys.size());
    final Geometry union = unionActual(g0Int, g1Int);

    disjointPolys.add(union);
    final Geometry overallUnion = GeometryCombiner.combine(disjointPolys);

    return overallUnion;
  }
}
