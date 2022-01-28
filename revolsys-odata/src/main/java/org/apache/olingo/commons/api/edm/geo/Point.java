/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.commons.api.edm.geo;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;

/**
 * Represents a point, either Edm.GeographyPoint or Edm.GeometryPoint.
 *
 */
public class Point extends Geospatial {

  /**
   * The X coordinate of the point. In most long/lat systems, this is the longitude.
   */
  private double x;

  /**
   * The Y coordinate of the point. In most long/lat systems, this is the latitude.
   */
  private double y;

  /**
   * The Z coordinate of the point. In most long/lat systems, this is a radius from the center of the earth, or the
   * height / elevation over the ground.
   */
  private double z;

  /**
   * Creates a new point.
   * @param dimension   Dimension of the point
   * @param srid        SRID value
   */
  public Point(final Dimension dimension, final SRID srid) {
    super(dimension, Type.POINT, srid);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Point point = (Point)o;
    return this.dimension == point.dimension
      && (this.srid == null ? point.srid == null : this.srid.equals(point.srid))
      && this.x == point.x && this.y == point.y && this.z == point.z;
  }

  @Override
  public EdmPrimitiveTypeKind getEdmPrimitiveTypeKind() {
    return this.dimension == Dimension.GEOGRAPHY ? EdmPrimitiveTypeKind.GeographyPoint
      : EdmPrimitiveTypeKind.GeometryPoint;
  }

  /**
   * Returns the x coordinate.
   * @return x coordinate
   */
  public double getX() {
    return this.x;
  }

  /**
   * Returns the y coordinate.
   * @return y coordinate
   */
  public double getY() {
    return this.y;
  }

  /**
   * Returns the z coordinate.
   * @return z coordinate
   */
  public double getZ() {
    return this.z;
  }

  @Override
  public int hashCode() {
    int result = this.dimension == null ? 0 : this.dimension.hashCode();
    result = 31 * result + (this.srid == null ? 0 : this.srid.hashCode());
    result = 31 * result + Double.valueOf(this.x).hashCode();
    result = 31 * result + Double.valueOf(this.y).hashCode();
    result = 31 * result + Double.valueOf(this.z).hashCode();
    return result;
  }

  /**
   * Sets the x coordinate.
   * @param x x coordinate
   */
  public void setX(final double x) {
    this.x = x;
  }

  /**
   * Sets the y coordinate.
   * @param y y coordinate
   */
  public void setY(final double y) {
    this.y = y;
  }

  /**
   * Sets the z coordinate.
   * @param z z coordinate
   */
  public void setZ(final double z) {
    this.z = z;
  }

  @Override
  public String toString() {
    return (this.dimension == null ? "" : this.dimension.name()) + '\''
      + (this.srid == null ? "" : "SRID=" + this.srid.toString() + ';') + "Point(" + this.x + ' '
      + this.y + ")'";
  }
}
