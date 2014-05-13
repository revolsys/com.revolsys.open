/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.revolsys.jts.triangulate;


import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Point;

/**
 * A simple split point finder which returns the midpoint of the split segment. This is a default
 * strategy only. Usually a more sophisticated strategy is required to prevent repeated splitting.
 * Other points which could be used are:
 * <ul>
 * <li>The projection of the encroaching point on the segment
 * <li>A point on the segment which will produce two segments which will not be further encroached
 * <li>The point on the segment which is the same distance from an endpoint as the encroaching
 * point
 * </ul>
 * 
 * @author Martin Davis
 */
public class MidpointSplitPointFinder implements ConstraintSplitPointFinder {
    /**
     * Gets the midpoint of the split segment
     */
    public Point findSplitPoint(Segment seg, Point encroachPt) {
        Point p0 = seg.getStart();
        Point p1 = seg.getEnd();
        return new Coordinate((double)(p0.getX() + p1.getX()) / 2, (p0.getY() + p1.getY()) / 2, Point.NULL_ORDINATE);
    }

}
