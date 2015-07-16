package com.revolsys.gis.graph.visitor;

import java.util.List;
import java.util.function.Predicate;

import com.revolsys.data.equals.GeometryEqualsExact3d;
import com.revolsys.data.filter.RecordGeometryFilter;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordLog;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.RecordGraph;
import com.revolsys.gis.graph.filter.EdgeObjectFilter;
import com.revolsys.gis.graph.filter.EdgeTypeNameFilter;
import com.revolsys.gis.jts.GeometryProperties;
import com.revolsys.gis.jts.filter.EqualFilter;
import com.revolsys.gis.jts.filter.LinearIntersectionFilter;
import com.revolsys.jts.geom.LineString;
import com.revolsys.predicate.AndPredicate;
import com.revolsys.util.ObjectProcessor;
import com.revolsys.visitor.AbstractVisitor;

public class LinearIntersectionNotEqualEdgeLogVisitor extends AbstractVisitor<Edge<Record>>
  implements ObjectProcessor<RecordGraph> {
  private static final String PROCESSED = LinearIntersectionNotEqualLineEdgeCleanupVisitor.class
    .getName() + ".PROCESSED";

  static {
    GeometryEqualsExact3d.addExclude(PROCESSED);
  }

  @Override
  public void process(final RecordGraph graph) {
    graph.visitEdges(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean visit(final Edge<Record> edge) {
    final Record object = edge.getObject();
    final LineString line = edge.getLine();
    if (GeometryProperties.getGeometryProperty(line, PROCESSED) != Boolean.TRUE) {
      final String typePath = edge.getTypeName();

      final Graph<Record> graph = edge.getGraph();

      final AndPredicate<Edge<Record>> attributeAndGeometryFilter = new AndPredicate<Edge<Record>>();

      attributeAndGeometryFilter.addFilter(new EdgeTypeNameFilter<Record>(typePath));

      final Predicate<Edge<Record>> filter = getPredicate();
      if (filter != null) {
        attributeAndGeometryFilter.addFilter(filter);
      }

      final Predicate<Record> notEqualLineFilter = new RecordGeometryFilter<LineString>(
        new EqualFilter<LineString>(line)).negate();

      final RecordGeometryFilter<LineString> linearIntersectionFilter = new RecordGeometryFilter<LineString>(
        new LinearIntersectionFilter(line));

      attributeAndGeometryFilter.addFilter(new EdgeObjectFilter<Record>(
        new AndPredicate<Record>(notEqualLineFilter, linearIntersectionFilter)));

      final List<Edge<Record>> intersectingEdges = graph.getEdges(attributeAndGeometryFilter, line);

      if (!intersectingEdges.isEmpty()) {
        RecordLog.error(getClass(), "Overlapping edge", object);
        GeometryProperties.setGeometryProperty(line, PROCESSED, Boolean.TRUE);
        for (final Edge<Record> intersectingEdge : intersectingEdges) {
          final Record intersectingObject = intersectingEdge.getObject();
          final LineString intersectingLine = intersectingObject.getGeometry();
          if (GeometryProperties.getGeometryProperty(intersectingLine, PROCESSED) != Boolean.TRUE) {
            GeometryProperties.setGeometryProperty(intersectingLine, PROCESSED, Boolean.TRUE);
          }
        }
      }
    }
    return true;
  }
}
