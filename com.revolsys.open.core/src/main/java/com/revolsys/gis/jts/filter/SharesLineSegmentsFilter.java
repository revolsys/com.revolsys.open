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
import com.revolsys.gis.jts.LineSegmentDoubleGF;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;

public class SharesLineSegmentsFilter implements Filter<LineString> {
  private final List<LineSegment> segments = new ArrayList<LineSegment>();

  public SharesLineSegmentsFilter(final LineString line) {

    final PointList points = CoordinatesListUtil.get(line);
    final Iterator<Point> pointIterator = points.iterator();
    Point previousPoint = pointIterator.next();
    while (pointIterator.hasNext()) {
      final Point nextPoint = pointIterator.next();
      segments.add(new LineSegmentDoubleGF(previousPoint, nextPoint));
      previousPoint = nextPoint;
    }
  }

  @Override
  public boolean accept(final LineString line) {

    final PointList points = CoordinatesListUtil.get(line);
    final Iterator<Point> pointIterator = points.iterator();
    Point previousPoint = pointIterator.next();
    while (pointIterator.hasNext()) {
      final Point nextPoint = pointIterator.next();
      final LineSegment segment = new LineSegmentDoubleGF(previousPoint, nextPoint);
      if (segments.contains(segment)) {
        return true;
      }
      previousPoint = nextPoint;
    }
    return false;
  }
}
