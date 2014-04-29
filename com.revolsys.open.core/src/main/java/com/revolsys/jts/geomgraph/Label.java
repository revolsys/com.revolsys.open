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
package com.revolsys.jts.geomgraph;

import com.revolsys.jts.geom.Location;

/**
* A <code>Label</code> indicates the topological relationship of a component
* of a topology graph to a given <code>Geometry</code>.
* This class supports labels for relationships to two <code>Geometry</code>s,
* which is sufficient for algorithms for binary operations.
* <P>
* Topology graphs support the concept of labeling nodes and edges in the graph.
* The label of a node or edge specifies its topological relationship to one or
* more geometries.  (In fact, since JTS operations have only two arguments labels
* are required for only two geometries).  A label for a node or edge has one or
* two elements, depending on whether the node or edge occurs in one or both of the
* input <code>Geometry</code>s.  Elements contain attributes which categorize the
* topological location of the node or edge relative to the parent
* <code>Geometry</code>; that is, whether the node or edge is in the interior,
* boundary or exterior of the <code>Geometry</code>.  Attributes have a value
* from the set <code>{Interior, Boundary, Exterior}</code>.  In a node each
* element has  a single attribute <code>&lt;On&gt;</code>.  For an edge each element has a
* triplet of attributes <code>&lt;Left, On, Right&gt;</code>.
* <P>
* It is up to the client code to associate the 0 and 1 <code>TopologyLocation</code>s
* with specific geometries.
* @version 1.7
*
*/
public class Label {

  // converts a Label to a Line label (that is, one with no side Locations)
  public static Label toLineLabel(final Label label) {
    final Label lineLabel = new Label(Location.NONE);
    for (int i = 0; i < 2; i++) {
      lineLabel.setLocation(i, label.getLocation(i));
    }
    return lineLabel;
  }

  TopologyLocation elt[] = new TopologyLocation[2];

  /**
   * Construct a Label with a single location for both Geometries.
   * Initialize the location for the Geometry index.
   */
  public Label(final int geomIndex, final Location onLoc) {
    elt[0] = new TopologyLocation(Location.NONE);
    elt[1] = new TopologyLocation(Location.NONE);
    elt[geomIndex].setLocation(onLoc);
  }

  /**
   * Construct a Label with On, Left and Right locations for both Geometries.
   * Initialize the locations for the given Geometry index.
   */
  public Label(final int geomIndex, final Location onLoc,
    final Location leftLoc, final Location rightLoc) {
    elt[0] = new TopologyLocation(Location.NONE, Location.NONE, Location.NONE);
    elt[1] = new TopologyLocation(Location.NONE, Location.NONE, Location.NONE);
    elt[geomIndex].setLocations(onLoc, leftLoc, rightLoc);
  }

  /**
   * Construct a Label with the same values as the argument Label.
   */
  public Label(final Label lbl) {
    elt[0] = new TopologyLocation(lbl.elt[0]);
    elt[1] = new TopologyLocation(lbl.elt[1]);
  }

  /**
   * Construct a Label with a single location for both Geometries.
   * Initialize the locations to Null
   */
  public Label(final Location onLoc) {
    elt[0] = new TopologyLocation(onLoc);
    elt[1] = new TopologyLocation(onLoc);
  }

  /**
   * Construct a Label with On, Left and Right locations for both Geometries.
   * Initialize the locations for both Geometries to the given values.
   */
  public Label(final Location onLoc, final Location leftLoc,
    final Location rightLoc) {
    elt[0] = new TopologyLocation(onLoc, leftLoc, rightLoc);
    elt[1] = new TopologyLocation(onLoc, leftLoc, rightLoc);
  }

  public boolean allPositionsEqual(final int geomIndex, final Location loc) {
    return elt[geomIndex].allPositionsEqual(loc);
  }

  public void flip() {
    elt[0].flip();
    elt[1].flip();
  }

  public int getGeometryCount() {
    int count = 0;
    if (!elt[0].isNull()) {
      count++;
    }
    if (!elt[1].isNull()) {
      count++;
    }
    return count;
  }

  public Location getLocation(final int geomIndex) {
    return elt[geomIndex].get(Position.ON);
  }

  public Location getLocation(final int geomIndex, final int posIndex) {
    return elt[geomIndex].get(posIndex);
  }

  public boolean isAnyNull(final int geomIndex) {
    return elt[geomIndex].isAnyNull();
  }

  public boolean isArea() {
    return elt[0].isArea() || elt[1].isArea();
  }

  public boolean isArea(final int geomIndex) {
    /*
     * Testing if (elt[0].getLocations().length != elt[1].getLocations().length)
     * { System.out.println(this); }
     */
    return elt[geomIndex].isArea();
  }

  public boolean isEqualOnSide(final Label lbl, final int side) {
    return this.elt[0].isEqualOnSide(lbl.elt[0], side)
      && this.elt[1].isEqualOnSide(lbl.elt[1], side);
  }

  public boolean isLine(final int geomIndex) {
    return elt[geomIndex].isLine();
  }

  public boolean isNull(final int geomIndex) {
    return elt[geomIndex].isNull();
  }

  /**
   * Merge this label with another one.
   * Merging updates any null attributes of this label with the attributes from lbl
   */
  public void merge(final Label lbl) {
    for (int i = 0; i < 2; i++) {
      if (elt[i] == null && lbl.elt[i] != null) {
        elt[i] = new TopologyLocation(lbl.elt[i]);
      } else {
        elt[i].merge(lbl.elt[i]);
      }
    }
  }

  public void setAllLocations(final int geomIndex, final Location location) {
    elt[geomIndex].setAllLocations(location);
  }

  public void setAllLocationsIfNull(final int geomIndex, final Location location) {
    elt[geomIndex].setAllLocationsIfNull(location);
  }

  public void setAllLocationsIfNull(final Location location) {
    setAllLocationsIfNull(0, location);
    setAllLocationsIfNull(1, location);
  }

  public void setLocation(final int geomIndex, final int posIndex,
    final Location location) {
    elt[geomIndex].setLocation(posIndex, location);
  }

  public void setLocation(final int geomIndex, final Location location) {
    elt[geomIndex].setLocation(Position.ON, location);
  }

  /**
   * Converts one GeometryLocation to a Line location
   */
  public void toLine(final int geomIndex) {
    if (elt[geomIndex].isArea()) {
      elt[geomIndex] = new TopologyLocation(elt[geomIndex].location[0]);
    }
  }

  @Override
  public String toString() {
    final StringBuffer buf = new StringBuffer();
    if (elt[0] != null) {
      buf.append("A:");
      buf.append(elt[0].toString());
    }
    if (elt[1] != null) {
      buf.append(" B:");
      buf.append(elt[1].toString());
    }
    return buf.toString();
  }
}
