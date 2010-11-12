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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class LineStringLessThanDistanceFilter implements Filter<LineString> {
  private Envelope envelope;

  /** The geometry to compare the data objects to to. */
  private LineString geometry;

  /** The maximum distance the object can be from the source geometry. */
  private double distance;

  public LineStringLessThanDistanceFilter() {
  }

  /**
   * Construct a new LineStringLessThanDistanceFilter.
   * 
   * @param geometry The geometry to compare the data objects to to.
   * @param distance
   */
  public LineStringLessThanDistanceFilter(
    final LineString geometry,
    final double distance) {
    this.distance = distance;
    setGeometry(geometry);
  }

  public boolean accept(
    final LineString line) {
    if (line.getEnvelopeInternal().intersects(envelope)) {
      double distance = LineStringUtil.distance(line, this.geometry,
        this.distance);
      if (distance < this.distance) {
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
   * Get the maximum distance the object can be from the source geometry.
   * 
   * @return The maximum distance the object can be from the source geometry.
   */
  public double getDistance() {
    return distance;
  }

  public void setGeometry(
    final LineString geometry) {
    this.geometry = geometry;
    this.envelope = new Envelope(geometry.getEnvelopeInternal());
    this.envelope.expandBy(distance);
  }

  public void setDistance(
    final double distance) {
    this.distance = distance;
  }
}
