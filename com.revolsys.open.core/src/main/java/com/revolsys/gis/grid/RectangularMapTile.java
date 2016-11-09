package com.revolsys.gis.grid;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

public interface RectangularMapTile extends BoundingBoxProxy, Record {
  @Override
  BoundingBox getBoundingBox();

  String getFormattedName();

  RectangularMapGrid getGrid();

  String getName();

  Polygon getPolygon(GeometryFactory factory, int numPoints);

  Polygon getPolygon(GeometryFactory factory, final int numXPoints, final int numYPoints);

  Polygon getPolygon(int numPoints);

  Polygon getPolygon(final int numXPoints, final int numYPoints);

  @Override
  default RecordDefinition getRecordDefinition() {
    final RectangularMapGrid grid = getGrid();
    return grid.getRecordDefinition();
  }
}
