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
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

public class LineContainsWithinToleranceFilter implements Filter<LineString> {
  private final CoordinatesList points;

  private Envelope envelope;

  private double tolerance;

  private boolean flip = false;

  public LineContainsWithinToleranceFilter(final LineString line) {
    this.points = CoordinatesListUtil.get(line);
    this.envelope = line.getEnvelopeInternal();
  }

  public LineContainsWithinToleranceFilter(final LineString line,
    double tolerance) {
    this.points = CoordinatesListUtil.get(line);
    this.tolerance = tolerance;
    this.envelope = line.getEnvelopeInternal();
    this.envelope.expandBy(tolerance);
  }

  public LineContainsWithinToleranceFilter(final LineString line,
    double tolerance, boolean flip) {
    this(line, tolerance);
    this.flip = flip;
  }

  public boolean accept(final LineString line) {
    if (this.envelope.intersects(line.getEnvelopeInternal())) {
      CoordinatesList points = CoordinatesListUtil.get(line);

      final boolean contains;
      if (flip) {
        contains = CoordinatesListUtil.containsWithinTolerance(points,
          this.points, tolerance);
      } else {
        contains = CoordinatesListUtil.containsWithinTolerance(this.points,
          points, tolerance);
      }
      if (contains) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

}
