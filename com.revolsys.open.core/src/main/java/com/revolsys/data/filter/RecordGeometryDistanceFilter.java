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

import java.util.Comparator;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Geometry;

public class RecordGeometryDistanceFilter implements Filter<Record>,
Comparator<Record> {
  /** The geometry to compare the data objects to to. */
  private final Geometry geometry;

  /** The maximum maxDistance the object can be from the source geometry. */
  private final double maxDistance;

  /**
   * Construct a new LineStringLessThanDistanceFilter.
   *
   * @param geometry The geometry to compare the data objects to to.
   * @param maxDistance
   */
  public RecordGeometryDistanceFilter(final Geometry geometry,
    final double maxDistance) {
    this.geometry = geometry;
    this.maxDistance = maxDistance;
  }

  @Override
  public boolean accept(final Record record) {
    final double distance = getDistance(record);
    if (distance <= this.maxDistance) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int compare(final Record record1, final Record record2) {
    final double distance1 = getDistance(record1);
    final double distance2 = getDistance(record2);
    return Double.compare(distance1, distance2);
  }

  public double getDistance(final Record record) {
    final Geometry matchGeometry = record.getGeometryValue();
    final double distance = matchGeometry.distance(this.geometry);
    return distance;
  }

  /**
   * Get the geometry to compare the data objects to to.
   *
   * @return The geometry to compare the data objects to to.
   */
  public Geometry getGeometry() {
    return this.geometry;
  }

  /**
   * Get the maximum maxDistance the object can be from the source geometry.
   *
   * @return The maximum maxDistance the object can be from the source geometry.
   */
  public double getMaxDistance() {
    return this.maxDistance;
  }

}
