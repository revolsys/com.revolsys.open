package com.revolsys.gis.graph.visitor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.data.equals.RecordEquals;
import com.revolsys.data.filter.RecordGeometryFilter;
import com.revolsys.data.record.Record;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.RecordGraph;
import com.revolsys.gis.graph.comparator.EdgeLengthComparator;
import com.revolsys.gis.graph.filter.EdgeObjectFilter;
import com.revolsys.gis.graph.filter.EdgeTypeNameFilter;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jts.filter.EqualFilter;
import com.revolsys.gis.jts.filter.LinearIntersectionFilter;
import com.revolsys.jts.geom.LineString;
import com.revolsys.predicate.AndPredicate;
import com.revolsys.util.ObjectProcessor;
import com.revolsys.visitor.AbstractVisitor;

public class LinearIntersectionNotEqualLineEdgeCleanupVisitor extends AbstractVisitor<Edge<Record>>
  implements ObjectProcessor<RecordGraph> {

  private static final Logger LOG = LoggerFactory
    .getLogger(LinearIntersectionNotEqualLineEdgeCleanupVisitor.class);

  private Set<String> equalExcludeAttributes = new HashSet<String>(
    Arrays.asList(RecordEquals.EXCLUDE_ID, RecordEquals.EXCLUDE_GEOMETRY));

  private Statistics duplicateStatistics;

  private Comparator<Record> newerComparator;

  public LinearIntersectionNotEqualLineEdgeCleanupVisitor() {
    super.setComparator(new EdgeLengthComparator<Record>(true));
  }

  @PreDestroy
  public void destroy() {
    if (this.duplicateStatistics != null) {
      this.duplicateStatistics.disconnect();
    }
    this.duplicateStatistics = null;
  }

  public Set<String> getEqualExcludeAttributes() {
    return this.equalExcludeAttributes;
  }

  public Comparator<Record> getNewerComparator() {
    return this.newerComparator;
  }

  @PostConstruct
  public void init() {
    this.duplicateStatistics = new Statistics("Duplicate intersecting lines");
    this.duplicateStatistics.connect();
  }

  private boolean middleCoordinatesEqual(final LineString points1, final LineString points2) {
    if (points1.getVertexCount() == points2.getVertexCount()) {
      for (int i = 1; i < points2.getVertexCount(); i++) {
        if (!points1.equalsVertex(2, i, points1, i)) {
          return false;
        }
      }
      return true;

    } else {
      return false;
    }
  }

  @Override
  public void process(final RecordGraph graph) {
    graph.visitEdges(this);
  }

  @Override
  public void setComparator(final Comparator<Edge<Record>> comparator) {
    throw new IllegalArgumentException("Cannot override comparator");
  }

  public void setEqualExcludeAttributes(final Collection<String> equalExcludeAttributes) {
    setEqualExcludeAttributes(new HashSet<String>(equalExcludeAttributes));
  }

  public void setEqualExcludeAttributes(final Set<String> equalExcludeAttributes) {
    this.equalExcludeAttributes = new HashSet<String>(equalExcludeAttributes);
    this.equalExcludeAttributes.add(RecordEquals.EXCLUDE_ID);
    this.equalExcludeAttributes.add(RecordEquals.EXCLUDE_GEOMETRY);
  }

  public void setNewerComparator(final Comparator<Record> newerComparator) {
    this.newerComparator = newerComparator;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean visit(final Edge<Record> edge) {
    final String typePath = edge.getTypeName();

    final Graph<Record> graph = edge.getGraph();
    final LineString line = edge.getLine();

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
      if (intersectingEdges.size() == 1 && line.getLength() > 10) {
        if (line.getVertexCount() > 2) {
          final Edge<Record> edge2 = intersectingEdges.get(0);
          final LineString line2 = edge2.getLine();

          if (middleCoordinatesEqual(line, line2)) {
            final boolean firstEqual = line.equalsVertex(2, 0, line2, 0);
            if (!firstEqual) {
              final Node<Record> fromNode1 = edge.getFromNode();
              final Node<Record> fromNode2 = edge2.getFromNode();
              if (fromNode1.distance(fromNode2) < 2) {
                graph.moveNodesToMidpoint(typePath, fromNode1, fromNode2);
                return true;
              }
            }
            final boolean lastEqual = line.equalsVertex(2, line.getVertexCount() - 1, line2,
              line.getVertexCount() - 1);
            if (!lastEqual) {
              final Node<Record> toNode1 = edge.getToNode();
              final Node<Record> toNode2 = edge2.getToNode();
              if (toNode1.distance(toNode2) < 2) {
                graph.moveNodesToMidpoint(typePath, toNode1, toNode2);
                return true;
              }
            }
          }
        }
      }
      LOG.error("Has intersecting edges " + line);
    }
    return true;
  }

}
