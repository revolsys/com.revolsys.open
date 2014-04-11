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
package com.revolsys.jts.precision;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateList;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Polygonal;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.geom.util.GeometryEditor;

/**
 * Reduces the precision of the coordinates of a {@link Geometry}
 * according to the supplied {@link PrecisionModel}, without
 * attempting to preserve valid topology.
 * <p>
 * In the case of {@link Polygonal} geometries, 
 * the topology of the resulting geometry may be invalid if
 * topological collapse occurs due to coordinates being shifted.
 * It is up to the client to check this and handle it if necessary.
 * Collapses may not matter for some uses.  An example
 * is simplifying the input to the buffer algorithm.
 * The buffer algorithm does not depend on the validity of the input geometry.
 *
 * @version 1.7
 * 
 * @deprecated use GeometryPrecisionReducer
 */
@Deprecated
public class SimpleGeometryPrecisionReducer {
  private class PrecisionReducerCoordinateOperation extends
    GeometryEditor.CoordinateOperation {
    @Override
    public Coordinates[] edit(final Coordinates[] coordinates,
      final Geometry geom) {
      if (coordinates.length == 0) {
        return null;
      }

      final Coordinates[] reducedCoords = new Coordinates[coordinates.length];
      // copy coordinates and reduce
      for (int i = 0; i < coordinates.length; i++) {
        final Coordinates coord = new Coordinate(coordinates[i]);
        newPrecisionModel.makePrecise(coord);
        reducedCoords[i] = coord;
      }
      // remove repeated points, to simplify returned geometry as much as
      // possible
      final CoordinateList noRepeatedCoordList = new CoordinateList(
        reducedCoords, false);
      final Coordinates[] noRepeatedCoords = noRepeatedCoordList.toCoordinateArray();

      /**
       * Check to see if the removal of repeated points
       * collapsed the coordinate List to an invalid length
       * for the type of the parent geometry.
       * It is not necessary to check for Point collapses, since the coordinate list can
       * never collapse to less than one point.
       * If the length is invalid, return the full-length coordinate array
       * first computed, or null if collapses are being removed.
       * (This may create an invalid geometry - the client must handle this.)
       */
      int minLength = 0;
      if (geom instanceof LineString) {
        minLength = 2;
      }
      if (geom instanceof LinearRing) {
        minLength = 4;
      }

      Coordinates[] collapsedCoords = reducedCoords;
      if (removeCollapsed) {
        collapsedCoords = null;
      }

      // return null or orginal length coordinate array
      if (noRepeatedCoords.length < minLength) {
        return collapsedCoords;
      }

      // ok to return shorter coordinate array
      return noRepeatedCoords;
    }
  }

  /**
   * Convenience method for doing precision reduction on a single geometry,
   * with collapses removed and keeping the geometry precision model the same.
   * 
   * @param g
   * @param precModel
   * @return the reduced geometry
   */
  public static Geometry reduce(final Geometry g, final PrecisionModel precModel) {
    final SimpleGeometryPrecisionReducer reducer = new SimpleGeometryPrecisionReducer(
      precModel);
    return reducer.reduce(g);
  }

  private final PrecisionModel newPrecisionModel;

  private boolean removeCollapsed = true;

  private boolean changePrecisionModel = false;

  public SimpleGeometryPrecisionReducer(final PrecisionModel pm) {
    newPrecisionModel = pm;
  }

  public Geometry reduce(final Geometry geom) {
    GeometryEditor geomEdit;
    if (changePrecisionModel) {
      final GeometryFactory newFactory = new GeometryFactory(newPrecisionModel,
        geom.getGeometryFactory().getSrid());
      geomEdit = new GeometryEditor(newFactory);
    } else {
      // don't change geometry factory
      geomEdit = new GeometryEditor();
    }

    return geomEdit.edit(geom, new PrecisionReducerCoordinateOperation());
  }

  /**
   * Sets whether the {@link PrecisionModel} of the new reduced Geometry
   * will be changed to be the {@link PrecisionModel} supplied to
   * specify the precision reduction.
   * <p>  
   * The default is to <b>not</b> change the precision model
   *
   * @param changePrecisionModel if <code>true</code> the precision model of the created Geometry will be the
   * the precisionModel supplied in the constructor.
   */
  public void setChangePrecisionModel(final boolean changePrecisionModel) {
    this.changePrecisionModel = changePrecisionModel;
  }

  /**
   * Sets whether the reduction will result in collapsed components
   * being removed completely, or simply being collapsed to an (invalid)
   * Geometry of the same type.
   * The default is to remove collapsed components.
   *
   * @param removeCollapsed if <code>true</code> collapsed components will be removed
   */
  public void setRemoveCollapsedComponents(final boolean removeCollapsed) {
    this.removeCollapsed = removeCollapsed;
  }
}
