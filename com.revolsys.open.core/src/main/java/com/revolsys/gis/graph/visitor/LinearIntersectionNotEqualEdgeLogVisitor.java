package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Filter;
import com.revolsys.filter.NotFilter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectLog;
import com.revolsys.gis.data.model.filter.DataObjectGeometryFilter;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.filter.EdgeObjectFilter;
import com.revolsys.gis.graph.filter.EdgeTypeNameFilter;
import com.revolsys.gis.jts.GeometryProperties;
import com.revolsys.gis.jts.filter.EqualFilter;
import com.revolsys.gis.jts.filter.LinearIntersectionFilter;
import com.revolsys.gis.model.data.equals.GeometryEqualsExact3d;
import com.revolsys.jts.geom.LineString;
import com.revolsys.util.ObjectProcessor;
import com.revolsys.visitor.AbstractVisitor;

public class LinearIntersectionNotEqualEdgeLogVisitor extends
  AbstractVisitor<Edge<DataObject>> implements ObjectProcessor<DataObjectGraph> {
  private static final String PROCESSED = LinearIntersectionNotEqualLineEdgeCleanupVisitor.class.getName()
    + ".PROCESSED";
  static {
    GeometryEqualsExact3d.addExclude(PROCESSED);
  }

  @Override
  public void process(final DataObjectGraph graph) {
    graph.visitEdges(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean visit(final Edge<DataObject> edge) {
    final DataObject object = edge.getObject();
    final LineString line = edge.getLine();
    if (GeometryProperties.getGeometryProperty(line, PROCESSED) != Boolean.TRUE) {
      final String typePath = edge.getTypeName();

      final Graph<DataObject> graph = edge.getGraph();

      final AndFilter<Edge<DataObject>> attributeAndGeometryFilter = new AndFilter<Edge<DataObject>>();

      attributeAndGeometryFilter.addFilter(new EdgeTypeNameFilter<DataObject>(
        typePath));

      final Filter<Edge<DataObject>> filter = getFilter();
      if (filter != null) {
        attributeAndGeometryFilter.addFilter(filter);
      }

      final Filter<DataObject> notEqualLineFilter = new NotFilter<DataObject>(
        new DataObjectGeometryFilter<LineString>(new EqualFilter<LineString>(
          line)));

      final DataObjectGeometryFilter<LineString> linearIntersectionFilter = new DataObjectGeometryFilter<LineString>(
        new LinearIntersectionFilter(line));

      attributeAndGeometryFilter.addFilter(new EdgeObjectFilter<DataObject>(
        new AndFilter<DataObject>(notEqualLineFilter, linearIntersectionFilter)));

      final List<Edge<DataObject>> intersectingEdges = graph.getEdges(
        attributeAndGeometryFilter, line);

      if (!intersectingEdges.isEmpty()) {
        DataObjectLog.error(getClass(), "Overlapping edge", object);
        GeometryProperties.setGeometryProperty(line, PROCESSED, Boolean.TRUE);
        for (final Edge<DataObject> intersectingEdge : intersectingEdges) {
          final DataObject intersectingObject = intersectingEdge.getObject();
          final LineString intersectingLine = intersectingObject.getGeometryValue();
          if (GeometryProperties.getGeometryProperty(intersectingLine, PROCESSED) != Boolean.TRUE) {
            GeometryProperties.setGeometryProperty(intersectingLine, PROCESSED,
              Boolean.TRUE);
          }
        }
      }
    }
    return true;
  }
}
