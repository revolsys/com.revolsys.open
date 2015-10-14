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
package com.revolsys.geometry.test.function;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineCap;
import com.revolsys.geometry.model.LineJoin;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.util.GeometryMapper;
import com.revolsys.geometry.model.util.GeometryMapper.MapOp;
import com.revolsys.geometry.noding.SegmentString;
import com.revolsys.geometry.operation.buffer.BufferInputLineSimplifier;
import com.revolsys.geometry.operation.buffer.BufferParameters;
import com.revolsys.geometry.operation.buffer.OffsetCurveSetBuilder;
import com.revolsys.geometry.operation.buffer.validate.BufferResultValidator;

public class BufferFunctions {

  public static String bufferDescription = "Buffers a geometry by a distance";

  public static Geometry buffer(final Geometry g, final double distance) {
    return g.buffer(distance);
  }

  public static Geometry bufferCurve(final Geometry g, final double distance) {
    return buildCurveSet(g, distance, new BufferParameters());
  }

  public static Geometry bufferCurveWithParams(final Geometry g, final Double distance,
    final Integer quadrantSegments, final Integer capStyle, final Integer joinStyle,
    final Double mitreLimit) {
    double dist = 0;
    if (distance != null) {
      dist = distance.doubleValue();
    }

    final BufferParameters bufParams = new BufferParameters();
    if (quadrantSegments != null) {
      bufParams.setQuadrantSegments(quadrantSegments.intValue());
    }
    if (capStyle != null) {
      bufParams.setEndCapStyle(LineCap.fromGeometryValue(capStyle));
    }
    if (joinStyle != null) {
      bufParams.setJoinStyle(LineJoin.fromGeometryValue(joinStyle));
    }
    if (mitreLimit != null) {
      bufParams.setMitreLimit(mitreLimit.doubleValue());
    }

    return buildCurveSet(g, dist, bufParams);
  }

  public static Geometry bufferEach(final Geometry g, final double distance) {
    return GeometryMapper.map(g, new MapOp() {

      @Override
      public Geometry map(final Geometry g) {
        return g.buffer(distance);
      }

    });
  }

  public static Geometry bufferLineSimplifier(final Geometry g, final double distance) {
    return buildBufferLineSimplifiedSet(g, distance);
  }

  public static Geometry bufferValidated(final Geometry g, final double distance) {
    final Geometry buf = g.buffer(distance);
    final String errMsg = BufferResultValidator.isValidMsg(g, distance, buf);
    if (errMsg != null) {
      throw new IllegalStateException("Buffer Validation error: " + errMsg);
    }
    return buf;
  }

  public static Geometry bufferValidatedGeom(final Geometry g, final double distance) {
    final Geometry buf = g.buffer(distance);
    final BufferResultValidator validator = new BufferResultValidator(g, distance, buf);
    final boolean isValid = validator.isValid();
    return validator.getErrorIndicator();
  }

  public static Geometry bufferWithParams(final Geometry g, final Double distance,
    final Integer quadrantSegments, final Integer capStyle, final Integer joinStyle,
    final Double mitreLimit) {
    double dist = 0;
    if (distance != null) {
      dist = distance.doubleValue();
    }

    final BufferParameters bufParams = new BufferParameters();
    if (quadrantSegments != null) {
      bufParams.setQuadrantSegments(quadrantSegments.intValue());
    }
    if (capStyle != null) {
      bufParams.setEndCapStyle(LineCap.fromGeometryValue(capStyle));
    }
    if (joinStyle != null) {
      bufParams.setJoinStyle(LineJoin.fromGeometryValue(joinStyle));
    }
    if (mitreLimit != null) {
      bufParams.setMitreLimit(mitreLimit.doubleValue());
    }

    return g.buffer(dist, bufParams);
  }

  private static Geometry buildBufferLineSimplifiedSet(final Geometry geometry,
    final double distance) {
    final List<LineString> simpLines = new ArrayList<>();

    final List<LineString> lines = geometry.getGeometryComponents(LineString.class);
    for (final LineString line : lines) {
      final Point[] pts = CoordinatesListUtil.getCoordinateArray(line);
      simpLines.add(geometry.getGeometryFactory()
        .lineString(BufferInputLineSimplifier.simplify(line, distance)));
    }
    final Geometry simpGeom = geometry.getGeometryFactory().buildGeometry(simpLines);
    return simpGeom;
  }

  private static Geometry buildCurveSet(final Geometry geometry, final double dist,
    final BufferParameters bufParams) {
    // --- now construct curve
    final GeometryFactory precisionModel = geometry.getGeometryFactory();
    final OffsetCurveSetBuilder curveBuilder = new OffsetCurveSetBuilder(geometry, dist,
      precisionModel, bufParams);
    final List curves = curveBuilder.getCurves();

    final List<LineString> lines = new ArrayList<LineString>();
    for (final Iterator i = curves.iterator(); i.hasNext();) {
      final SegmentString ss = (SegmentString)i.next();
      final LineString points = ss.getPoints();
      lines.add(geometry.getGeometryFactory().lineString(points));
    }
    final Geometry curve = geometry.getGeometryFactory().geometry(lines);
    return curve;
  }

  public static Geometry singleSidedBuffer(final Geometry geom, final double distance) {
    final BufferParameters bufParams = new BufferParameters();
    bufParams.setSingleSided(true);
    return geom.buffer(distance, bufParams);
  }

}
