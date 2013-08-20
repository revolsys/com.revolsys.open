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
import com.vividsolutions.jts.geom.LineString;

public class LineStartsSharesStartOrEndFilter implements Filter<LineString> {
  private final CoordinatesList points;

  private final CoordinatesList reversePoints;

  public LineStartsSharesStartOrEndFilter(final LineString line) {
    this.points = CoordinatesListUtil.get(line);
    this.reversePoints = points.reverse();
  }

  @Override
  public boolean accept(final LineString line) {
    final CoordinatesList points = CoordinatesListUtil.get(line);

    if (this.points.startsWith(points, this.points.getNumAxis())) {
      return true;
    } else if (this.reversePoints.startsWith(points.reverse(),
      this.points.getNumAxis())) {
      return true;
    } else {
      return false;
    }
  }

}
