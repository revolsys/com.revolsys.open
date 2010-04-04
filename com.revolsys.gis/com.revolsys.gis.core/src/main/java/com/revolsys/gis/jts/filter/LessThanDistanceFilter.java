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
package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.jts.LineStringUtil;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

public class LessThanDistanceFilter implements Filter<LineString> {
  private final Envelope envelope;

  /** The geometry to compare the data objects to to. */
  private final LineString geometry;

  /** The maximum maxDistance the object can be from the source geometry. */
  private final double maxDistance;

  /**
   * Construct a new LessThanDistanceFilter.
   * 
   * @param geometry The geometry to compare the data objects to to.
   * @param maxDistance
   */
  public LessThanDistanceFilter(
    final LineString geometry,
    final double maxDistance) {
    this.geometry = geometry;
    this.maxDistance = maxDistance;
    this.envelope = new Envelope(geometry.getEnvelopeInternal());
    this.envelope.expandBy(maxDistance);
  }

  public boolean accept(
    final LineString geometry) {
    if (geometry.getEnvelopeInternal().intersects(envelope)) {
      final double distance = LineStringUtil.distance(geometry, this.geometry,
        maxDistance);
      if (distance < maxDistance) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public Envelope getEnvelope() {
    return envelope;
  }

  /**
   * Get the geometry to compare the data objects to to.
   * 
   * @return The geometry to compare the data objects to to.
   */
  public LineString getGeometry() {
    return geometry;
  }

  /**
   * Get the maximum maxDistance the object can be from the source geometry.
   * 
   * @return The maximum maxDistance the object can be from the source geometry.
   */
  public double getMaxDistance() {
    return maxDistance;
  }
}
