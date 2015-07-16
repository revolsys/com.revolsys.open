/*
 * $URL:$
 * $Author:$
 * $Date:$
 * $Revision:$

 * Copyright 2004-2007 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.data.filter;

import com.revolsys.data.record.Record;
import java.util.function.Predicate;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.TopologyException;

public class RecordGeometryIntersectsFilter implements Predicate<Record> {
  /** The geometry to compare the data objects to to. */
  private final Geometry geometry;

  private final GeometryFactory geometryFactory;

  /**
   * Construct a new RecordGeometryIntersectsFilter.
   *
   * @param geometry The geometry to compare the data objects to to.
   */
  public RecordGeometryIntersectsFilter(final Geometry geometry) {
    this.geometry = geometry;
    this.geometryFactory = geometry.getGeometryFactory();
  }

  @Override
  public boolean test(final Record object) {
    try {
      final Geometry matchGeometry = object.getGeometry();
      final Geometry convertedGeometry = matchGeometry.convert(this.geometryFactory);
      try {
        if (convertedGeometry != null && this.geometry != null
          && convertedGeometry.intersects(this.geometry)) {
          return true;
        } else {
          return false;
        }
      } catch (final TopologyException e) {
        e.printStackTrace();
        return true;
      }
    } catch (final Throwable t) {
      t.printStackTrace();
      return false;
    }
  }

  /**
   * Get the geometry to compare the data objects to to.
   *
   * @return The geometry to compare the data objects to to.
   */
  public Geometry getGeometry() {
    return this.geometry;
  }
}
