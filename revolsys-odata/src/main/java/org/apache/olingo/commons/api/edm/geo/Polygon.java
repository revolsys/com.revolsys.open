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

import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;

/**
 * Polygon.
 */
public class Polygon extends Geospatial {

  final ComposedGeospatial<LineString> interiorRings;

  final ComposedGeospatial<Point> exterior;

  /**
   * Creates a new polygon.
   *
   * @param dimension   Dimension of the polygon
   * @param srid        SRID values
   * @param interiors    List of interior rings
   * @param exterior    Ring of exterior point
   */
  public Polygon(final Dimension dimension, final SRID srid, final List<LineString> interiors,
    final LineString exterior) {

    super(dimension, Type.POLYGON, srid);
    if (interiors != null) {
      this.interiorRings = new MultiLineString(dimension, srid, interiors);
    } else {
      this.interiorRings = null;
    }
    this.exterior = exterior;
  }

  /**
   * Creates a new polygon.
   *
   * @param dimension   Dimension of the polygon
   * @param srid        SRID values
   * @param interior    List of interior points
   * @param exterior    List of exterior point
   * @deprecated
   */
  @Deprecated
  public Polygon(final Dimension dimension, final SRID srid, final List<Point> interior,
    final List<Point> exterior) {

    super(dimension, Type.POLYGON, srid);
    if (interior != null) {
      final LineString lineString = new LineString(dimension, srid, interior);
      this.interiorRings = new MultiLineString(dimension, srid, Arrays.asList(lineString));
    } else {
      this.interiorRings = null;
    }
    this.exterior = new LineString(dimension, srid, exterior);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Polygon polygon = (Polygon)o;
    return this.dimension == polygon.dimension
      && (this.srid == null ? polygon.srid == null : this.srid.equals(polygon.srid))
      && (this.interiorRings == null ? polygon.interiorRings == null
        : this.interiorRings.equals(polygon.interiorRings))
      && (this.exterior == null ? polygon.exterior == null
        : this.exterior.equals(polygon.exterior));
  }

  @Override
  public EdmPrimitiveTypeKind getEdmPrimitiveTypeKind() {
    return this.dimension == Dimension.GEOGRAPHY ? EdmPrimitiveTypeKind.GeographyPolygon
      : EdmPrimitiveTypeKind.GeometryPolygon;
  }

  /**
   * Gets exterior points.
   *
   * @return exterior points.
   */
  public ComposedGeospatial<Point> getExterior() {
    return this.exterior;
  }

  /**
   * Gets interior points.
   *
   * @return interior points.
   * @deprecated
   * @see #getInterior(int)
   */
  @Deprecated
  public ComposedGeospatial<Point> getInterior() {
    if (this.interiorRings == null || this.interiorRings.geospatials.isEmpty()) {
      return null;
    }
    return getInterior(0);
  }

  /**
   * Gets the nth interior ring
   * @param n
   * @return the ring or an exception if no such ring exists
   */
  public ComposedGeospatial<Point> getInterior(final int n) {
    return this.interiorRings.geospatials.get(n);
  }

  /**
   * Get the number of interior rings
   * @return number of interior rings
   */
  public int getNumberOfInteriorRings() {
    if (this.interiorRings == null) {
      return 0;
    }
    return this.interiorRings.geospatials.size();
  }

  @Override
  public int hashCode() {
    int result = this.dimension == null ? 0 : this.dimension.hashCode();
    result = 31 * result + (this.srid == null ? 0 : this.srid.hashCode());
    result = 31 * result + (this.interiorRings == null ? 0 : this.interiorRings.hashCode());
    result = 31 * result + (this.exterior == null ? 0 : this.exterior.hashCode());
    return result;
  }
}
