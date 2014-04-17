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
package com.revolsys.jts.geom.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryComponentFilter;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;

/**
 * Extracts all the 1-dimensional ({@link LineString}) components from a {@link Geometry}.
 * For polygonal geometries, this will extract all the component {@link LinearRing}s.
 * If desired, <code>LinearRing</code>s can be forced to be returned as <code>LineString</code>s.
 *
 * @version 1.7
 */
public class LinearComponentExtracter implements GeometryComponentFilter {
  /**
   * Extracts the linear components from a single {@link Geometry}
   * and adds them to the provided {@link Collection}.
   *
   * @param geoms the collection of geometries from which to extract linear components
   * @param lines the collection to add the extracted linear components to
   * @return the collection of linear components (LineStrings or LinearRings)
   */
  public static Collection getLines(final Collection geoms,
    final Collection lines) {
    for (final Iterator i = geoms.iterator(); i.hasNext();) {
      final Geometry g = (Geometry)i.next();
      getLines(g, lines);
    }
    return lines;
  }

  /**
   * Extracts the linear components from a single {@link Geometry}
   * and adds them to the provided {@link Collection}.
   *
   * @param geoms the Collection of geometries from which to extract linear components
   * @param lines the collection to add the extracted linear components to
   * @param forceToLineString true if LinearRings should be converted to LineStrings
   * @return the collection of linear components (LineStrings or LinearRings)
   */
  public static Collection getLines(final Collection geoms,
    final Collection lines, final boolean forceToLineString) {
    for (final Iterator i = geoms.iterator(); i.hasNext();) {
      final Geometry g = (Geometry)i.next();
      getLines(g, lines, forceToLineString);
    }
    return lines;
  }

  /**
   * Extracts the linear components from a single geometry.
   * If more than one geometry is to be processed, it is more
   * efficient to create a single {@link LinearComponentExtracter} instance
   * and pass it to multiple geometries.
   *
   * @param geom the geometry from which to extract linear components
   * @return the list of linear components
   */
  public static List<LineString> getLines(final Geometry geom) {
    return getLines(geom, false);
  }

  /**
   * Extracts the linear components from a single geometry.
   * If more than one geometry is to be processed, it is more
   * efficient to create a single {@link LinearComponentExtracter} instance
   * and pass it to multiple geometries.
   *
   * @param geom the geometry from which to extract linear components
   * @param forceToLineString true if LinearRings should be converted to LineStrings
   * @return the list of linear components
   */
  public static List getLines(final Geometry geom,
    final boolean forceToLineString) {
    final List lines = new ArrayList();
    geom.apply(new LinearComponentExtracter(lines, forceToLineString));
    return lines;
  }

  /**
   * Extracts the linear components from a single {@link Geometry}
   * and adds them to the provided {@link Collection}.
   *
   * @param geom the geometry from which to extract linear components
   * @param lines the Collection to add the extracted linear components to
   * @return the Collection of linear components (LineStrings or LinearRings)
   */
  public static Collection getLines(final Geometry geom, final Collection lines) {
    if (geom instanceof LineString) {
      lines.add(geom);
    } else {
      geom.apply(new LinearComponentExtracter(lines));
    }
    return lines;
  }

  /**
   * Extracts the linear components from a single {@link Geometry}
   * and adds them to the provided {@link Collection}.
   *
   * @param geom the geometry from which to extract linear components
   * @param lines the Collection to add the extracted linear components to
   * @param forceToLineString true if LinearRings should be converted to LineStrings
   * @return the Collection of linear components (LineStrings or LinearRings)
   */
  public static Collection getLines(final Geometry geom,
    final Collection lines, final boolean forceToLineString) {
    geom.apply(new LinearComponentExtracter(lines, forceToLineString));
    return lines;
  }

  private final Collection lines;

  private boolean isForcedToLineString = false;

  /**
   * Constructs a LineExtracterFilter with a list in which to store LineStrings found.
   */
  public LinearComponentExtracter(final Collection lines) {
    this.lines = lines;
  }

  /**
   * Constructs a LineExtracterFilter with a list in which to store LineStrings found.
   */
  public LinearComponentExtracter(final Collection lines,
    final boolean isForcedToLineString) {
    this.lines = lines;
    this.isForcedToLineString = isForcedToLineString;
  }

  @Override
  public void filter(final Geometry geom) {
    if (isForcedToLineString && geom instanceof LinearRing) {
      final LineString line = geom.getGeometryFactory().lineString(
        ((LinearRing)geom).getCoordinatesList());
      lines.add(line);
      return;
    }
    // if not being forced, and this is a linear component
    if (geom instanceof LineString) {
      lines.add(geom);
    }

    // else this is not a linear component, so skip it
  }

  /**
   * Indicates that LinearRing components should be 
   * converted to pure LineStrings.
   * 
   * @param isForcedToLineString true if LinearRings should be converted to LineStrings
   */
  public void setForceToLineString(final boolean isForcedToLineString) {
    this.isForcedToLineString = isForcedToLineString;
  }

}
