package com.revolsys.gis.parallel;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class ValidateGeometryRange extends
  BaseInOutProcess<DataObject, DataObject> {
  private static final Logger LOG = Logger.getLogger(ValidateGeometryRange.class);

  private double maxX = Double.MAX_VALUE;

  private double maxY = Double.MAX_VALUE;

  private double maxZ = Double.MAX_VALUE;

  private double minX = -Double.MAX_VALUE;

  private double minY = -Double.MAX_VALUE;

  private double minZ = -Double.MAX_VALUE;

  /**
   * @return the maxX
   */
  public double getMaxX() {
    return maxX;
  }

  /**
   * @return the maxY
   */
  public double getMaxY() {
    return maxY;
  }

  /**
   * @return the maxZ
   */
  public double getMaxZ() {
    return maxZ;
  }

  /**
   * @return the minX
   */
  public double getMinX() {
    return minX;
  }

  /**
   * @return the minY
   */
  public double getMinY() {
    return minY;
  }

  /**
   * @return the minZ
   */
  public double getMinZ() {
    return minZ;
  }

  private boolean isValid(final double min, final double max, final double value) {
    return (value >= min && value <= max);
  }

  private boolean isValid(final String type, final Geometry geometry) {
    boolean valid = true;
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      final Geometry subGeometry = geometry.getGeometry(i);
      if (subGeometry instanceof Point) {
        final Point coordinate = geometry.getPoint();
        if (!isValid(type, coordinate)) {
          valid = false;
        }
      } else if (subGeometry instanceof LineString) {
        final LineString line = (LineString)subGeometry;
        valid = isValid(type, line);

      } else if (subGeometry instanceof Polygon) {
        final Polygon polygon = (Polygon)subGeometry;

        if (!isValid(type, polygon.getExteriorRing())) {
          valid = false;
        }
        for (int k = 0; k < polygon.getNumInteriorRing(); k++) {
          final LineString ring = polygon.getInteriorRing(k);
          if (!isValid(type, ring)) {
            valid = false;
          }
        }
      }
    }
    return valid;

  }

  private boolean isValid(final String type, final LineString line) {
    boolean valid = true;
    for (final Vertex point : line.vertices()) {
      if (!isValid(type, point)) {
        valid = false;
      }
    }
    return valid;
  }

  private boolean isValid(final String type, final Point coordinate) {
    if (!isValid(minX, maxY, coordinate.getX())
      || !isValid(minY, maxY, coordinate.getY())
      || !isValid(minZ, maxZ, coordinate.getZ())) {
      LOG.warn(type + " has invalid coordinate at " + coordinate);
      return false;
    } else {
      return true;
    }

  }

  @Override
  protected void process(final Channel<DataObject> in,
    final Channel<DataObject> out, final DataObject object) {
    // TODO Auto-generated method stub
    final Geometry geometry = object.getGeometryValue();
    isValid(object.getMetaData().getPath().toString(), geometry);
    out.write(object);
  }

  /**
   * @param maxX the maxX to set
   */
  public void setMaxX(final double maxX) {
    this.maxX = maxX;
  }

  /**
   * @param maxY the maxY to set
   */
  public void setMaxY(final double maxY) {
    this.maxY = maxY;
  }

  /**
   * @param maxZ the maxZ to set
   */
  public void setMaxZ(final double maxZ) {
    this.maxZ = maxZ;
  }

  /**
   * @param minX the minX to set
   */
  public void setMinX(final double minX) {
    this.minX = minX;
  }

  /**
   * @param minY the minY to set
   */
  public void setMinY(final double minY) {
    this.minY = minY;
  }

  /**
   * @param minZ the minZ to set
   */
  public void setMinZ(final double minZ) {
    this.minZ = minZ;
  }

}
