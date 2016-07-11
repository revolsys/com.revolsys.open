package com.revolsys.geometry.graph.visitor;

import java.util.List;
import java.util.function.Predicate;

import com.revolsys.geometry.filter.EqualFilter;
import com.revolsys.geometry.filter.LinearIntersectionFilter;
import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.RecordGraph;
import com.revolsys.geometry.graph.filter.EdgeObjectFilter;
import com.revolsys.geometry.graph.filter.EdgeTypeNameFilter;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.util.GeometryProperties;
import com.revolsys.record.Record;
import com.revolsys.record.RecordLog;
import com.revolsys.record.filter.RecordGeometryFilter;
import com.revolsys.util.ObjectProcessor;
import com.revolsys.visitor.AbstractVisitor;

public class LinearIntersectionNotEqualEdgeLogVisitor extends AbstractVisitor<Edge<Record>>
  implements ObjectProcessor<RecordGraph> {
  private static final String PROCESSED = "_"
    + LinearIntersectionNotEqualLineEdgeCleanupVisitor.class.getName() + ".PROCESSED";

  @Override
  public void accept(final Edge<Record> edge) {
    final Record object = edge.getObject();
    final LineString line = edge.getLine();
    if (GeometryProperties.getGeometryProperty(line, PROCESSED) != Boolean.TRUE) {
      final String typePath = edge.getTypeName();

      final Graph<Record> graph = edge.getGraph();

      Predicate<Edge<Record>> attributeAndGeometryFilter = new EdgeTypeNameFilter<>(typePath);

      final Predicate<Edge<Record>> filter = getPredicate();
      if (filter != null) {
        attributeAndGeometryFilter = attributeAndGeometryFilter.and(filter);
      }

      final Predicate<Record> notEqualLineFilter = new RecordGeometryFilter<>(
        new EqualFilter<>(line)).negate();

      final RecordGeometryFilter<LineString> linearIntersectionFilter = new RecordGeometryFilter<>(
        new LinearIntersectionFilter(line));

      attributeAndGeometryFilter = attributeAndGeometryFilter
        .and(new EdgeObjectFilter<>(notEqualLineFilter.and(linearIntersectionFilter)));

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
  }

  @Override
  public void process(final RecordGraph graph) {
    graph.forEachEdge(this);
  }
}
