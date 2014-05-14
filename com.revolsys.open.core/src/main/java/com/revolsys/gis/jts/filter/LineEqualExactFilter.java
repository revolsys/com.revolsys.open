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
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.LineString;

public class LineEqualExactFilter implements Filter<LineString> {
  private final PointList points;

  int axisCount = -1;

  public LineEqualExactFilter(final LineString line) {
    this.points = CoordinatesListUtil.get(line);
  }

  public LineEqualExactFilter(final LineString line, final int axisCount) {
    this.points = CoordinatesListUtil.get(line);
    this.axisCount = axisCount;
  }

  @Override
  public boolean accept(final LineString line) {
    final PointList points = CoordinatesListUtil.get(line);

    final boolean equal;
    if (axisCount >= 2) {
      equal = this.points.equals(points, axisCount);
    } else {
      equal = this.points.equals(points);
    }
    if (equal) {
      return true;
    } else {
      return false;
    }
  }

}
