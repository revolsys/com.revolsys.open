package com.revolsys.gis.graph.geometry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListIndexLineSegmentIterator;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryGraph extends Graph<LineSegment> {

  private final List<Point> points = new ArrayList<Point>();

  private final List<Geometry> geometries = new ArrayList<Geometry>();

  public GeometryGraph(final Geometry geometry) {
    this(GeometryFactory.getFactory(geometry));
    addGeometry(geometry);
  }

  public GeometryGraph(final GeometryFactory geometryFactory) {
    setGeometryFactory(geometryFactory);
  }

  private void addEdges(final CoordinatesList points,
    final Map<String, Object> attributes) {
    final CoordinatesListIndexLineSegmentIterator iterator = new CoordinatesListIndexLineSegmentIterator(
      getGeometryFactory(), points);
    int index = 0;
    for (final LineSegment lineSegment : iterator) {
      final Edge<LineSegment> edge = add(lineSegment, lineSegment.getLine());
      attributes.put("segmentIndex", index++);
      edge.addAttributes(attributes);
    }

  }

  public void addGeometry(Geometry geometry) {
    geometry = getGeometryFactory().createGeometry(geometry);
    final Map<String, Object> properties = new LinkedHashMap<String, Object>();

    final int geometryIndex = geometries.size();
    properties.put("geometryIndex", geometryIndex);
    geometries.add(geometry);
    for (int partIndex = 0; partIndex < geometry.getNumGeometries(); partIndex++) {
      properties.put("partIndex", partIndex);
      final Geometry part = geometry.getGeometryN(partIndex);
      if (part instanceof Point) {
        final Point point = (Point)part;
        points.add(point);
      } else if (part instanceof LineString) {
        final LineString line = (LineString)part;
        final CoordinatesList points = CoordinatesListUtil.get(line);
        addEdges(points, properties);
      } else if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
        int ringIndex = 0;
        for (final CoordinatesList ring : rings) {
          properties.put("ringIndex", ringIndex++);
          addEdges(ring, properties);
        }
        properties.remove("ringIndex");
      }
    }
  }

  @Override
  protected LineSegment clone(final LineSegment segment, final LineString line) {
    return new LineSegment(getGeometryFactory(), segment);
  }
}
