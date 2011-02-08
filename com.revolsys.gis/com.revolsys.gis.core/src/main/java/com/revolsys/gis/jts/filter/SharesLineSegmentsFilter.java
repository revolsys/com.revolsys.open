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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.filter.Filter;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.geom.LineString;

public class SharesLineSegmentsFilter implements Filter<LineString> {
  private final List<LineSegment> segments = new ArrayList<LineSegment>();

  public SharesLineSegmentsFilter(final LineString line) {

    CoordinatesList points = CoordinatesListUtil.get(line);
    Iterator<Coordinates> pointIterator = points.iterator();
    Coordinates previousPoint = pointIterator.next();
    while (pointIterator.hasNext()) {
      Coordinates nextPoint = pointIterator.next();
      segments.add(new LineSegment(previousPoint, nextPoint));
      previousPoint = nextPoint;
    }
  }

  public boolean accept(final LineString line) {

    CoordinatesList points = CoordinatesListUtil.get(line);
    Iterator<Coordinates> pointIterator = points.iterator();
    Coordinates previousPoint = pointIterator.next();
    while (pointIterator.hasNext()) {
      Coordinates nextPoint = pointIterator.next();
      LineSegment segment = new LineSegment(previousPoint, nextPoint);
      if (segments.contains(segment)) {
        return true;
      }
      previousPoint = nextPoint;
    }
    return false;
  }
}
