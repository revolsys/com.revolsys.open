/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/format/shape/io/geometry/JtsGeometryConverter.java $
 * $Author:paul.austin@revolsys.com $
 * $Date:2007-06-09 09:28:28 -0700 (Sat, 09 Jun 2007) $
 * $Revision:265 $

 * Copyright 2004-2005 Revolution Systems Inc.
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
package com.revolsys.io.shp.geometry;

import java.io.IOException;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.io.EndianInput;
import com.revolsys.io.shp.ShapefileConstants;
import com.vividsolutions.jts.geom.Geometry;

public class JtsGeometryConverter {
  private final GeometryFactory geometryFactory;

  public JtsGeometryConverter() {
    this(GeometryFactory.getFactory());
  }

  public JtsGeometryConverter(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public Geometry readGeometry(final EndianInput in) throws IOException {
    final int x = in.readInt();
    final int recordLength = in.readInt();
    final int shapeType = in.readLEInt();
    return readGeometry(in, shapeType, recordLength);
  }

  public Geometry readGeometry(final EndianInput in, final int shapeType,
    final int recordLength) throws IOException {
    switch (shapeType) {
      case ShapefileConstants.NULL_SHAPE:
        return null;
      case ShapefileConstants.POINT_SHAPE:
        return new Point2DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.POINT_M_SHAPE:
        return new Point2DMConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.POINT_ZM_SHAPE:
        return new Point3DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.MULTI_POINT_SHAPE:
        return new Point2DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.MULTI_POINT_M_SHAPE:
        return new Point2DMConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.MULTI_POINT_ZM_SHAPE:
        return new Point3DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.POLYLINE_SHAPE:
        return new LineString2DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.POLYLINE_M_SHAPE:
        return new LineString2DMConverter(geometryFactory).read(in,
          recordLength);
      case ShapefileConstants.POLYLINE_ZM_SHAPE:
        return new LineString3DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.POLYGON_SHAPE:
        return new Polygon2DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.POLYGON_M_SHAPE:
        return new Polygon2DMConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.POLYGON_ZM_SHAPE:
        return new Polygon3DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.MULTI_PATCH_SHAPE:
        return new MultiPolygonConverter(geometryFactory).read(in, recordLength);
      default:
      break;
    }
    return null;
  }
}
