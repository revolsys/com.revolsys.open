package com.revolsys.gis.grid.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * The MapGridGeometrySheetFilter will compare the centroid of the Geometry for
 * a data object to check that it is within the specified map sheet.
 * 
 * @author Paul Austin
 */
public class MapGridGeometrySheetFilter implements Filter<DataObject> {
  /** Set the grid to check the mapsheet for. */
  private RectangularMapGrid grid;

  private boolean inverse;

  /** The map sheet name. */
  private String sheet;

  public boolean accept(
    final DataObject object) {
    if (sheet != null && grid != null) {
      final Geometry geometry = object.getGeometryValue();
      if (geometry != null) {
        Geometry geographicsGeometry = GeometryProjectionUtil.perform(geometry, 4326);
        final Coordinate centroid = geographicsGeometry.getCentroid().getCoordinate();
        final String geometrySheet = grid.getMapTileName(centroid.x, centroid.y);
        if (geometrySheet != null) {
          if (sheet.equals(geometrySheet) == !inverse) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * @return the grid
   */
  public RectangularMapGrid getGrid() {
    return grid;
  }

  /**
   * @return the sheet
   */
  public String getSheet() {
    return sheet;
  }

  /**
   * @return the inverse
   */
  public boolean isInverse() {
    return inverse;
  }

  /**
   * @param grid the grid to set
   */
  public void setGrid(
    final RectangularMapGrid grid) {
    this.grid = grid;
  }

  /**
   * @param inverse the inverse to set
   */
  public void setInverse(
    final boolean inverse) {
    this.inverse = inverse;
  }

  /**
   * @param sheet the sheet to set
   */
  public void setSheet(
    final String sheet) {
    this.sheet = sheet;
  }

  @Override
  public String toString() {
    if (inverse) {
      return "map sheet != " + sheet;
    } else {
      return "map sheet != " + sheet;
    }
  }

}
