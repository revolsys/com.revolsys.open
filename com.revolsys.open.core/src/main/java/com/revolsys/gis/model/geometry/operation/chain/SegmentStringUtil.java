package com.revolsys.gis.model.geometry.operation.chain;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.LineString;

/**
 * Utility methods for processing {@link SegmentString}s.
 * 
 * @author Martin Davis
 */
public class SegmentStringUtil {
  /**
   * Extracts all linear components from a given {@link Geometry} to
   * {@link SegmentString}s. The SegmentString data item is set to be the source
   * Geometry.
   * 
   * @param geom the geometry to extract from
   * @return a List of SegmentStrings
   */
  public static List<SegmentString> extractSegmentStrings(Geometry geom) {
    List<SegmentString> segments = new ArrayList<SegmentString>();
    for (CoordinatesList points : geom.getCoordinatesLists()) {
      if (points.size() > 1) {
        segments.add(new NodedSegmentString(points, geom));
      }
    }
    return segments;
  }

}
