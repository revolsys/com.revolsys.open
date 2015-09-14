package com.revolsys.gis.parallel;

import org.apache.log4j.Logger;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.record.Record;

public class ValidateGeometryRange extends BaseInOutProcess<Record, Record> {
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
    return this.maxX;
  }

  /**
   * @return the maxY
   */
  public double getMaxY() {
    return this.maxY;
  }

  /**
   * @return the maxZ
   */
  public double getMaxZ() {
    return this.maxZ;
  }

  /**
   * @return the minX
   */
  public double getMinX() {
    return this.minX;
  }

  /**
   * @return the minY
   */
  public double getMinY() {
    return this.minY;
  }

  /**
   * @return the minZ
   */
  public double getMinZ() {
    return this.minZ;
  }

  private boolean isValid(final double min, final double max, final double value) {
    return value >= min && value <= max;
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

        if (!isValid(type, polygon.getShell())) {
          valid = false;
        }
        for (int k = 0; k < polygon.getHoleCount(); k++) {
          final LineString ring = polygon.getHole(k);
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
    if (!isValid(this.minX, this.maxY, coordinate.getX())
      || !isValid(this.minY, this.maxY, coordinate.getY())
      || !isValid(this.minZ, this.maxZ, coordinate.getZ())) {
      LOG.warn(type + " has invalid coordinate at " + coordinate);
      return false;
    } else {
      return true;
    }

  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    // TODO Auto-generated method stub
    final Geometry geometry = object.getGeometry();
    isValid(object.getRecordDefinition().getPath().toString(), geometry);
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
