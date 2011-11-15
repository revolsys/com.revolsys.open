package com.revolsys.gis.graph.visitor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Filter;
import com.revolsys.filter.NotFilter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.filter.DataObjectGeometryFilter;
import com.revolsys.gis.data.visitor.AbstractVisitor;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.comparator.EdgeLengthComparator;
import com.revolsys.gis.graph.filter.EdgeObjectFilter;
import com.revolsys.gis.graph.filter.EdgeTypeNameFilter;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jts.filter.EqualFilter;
import com.revolsys.gis.jts.filter.LinearIntersectionFilter;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.data.equals.DataObjectEquals;
import com.revolsys.util.ObjectProcessor;
import com.vividsolutions.jts.geom.LineString;

public class LinearIntersectionNotEqualLineEdgeCleanupVisitor extends
  AbstractVisitor<Edge<DataObject>> implements ObjectProcessor<DataObjectGraph> {

  private static final Logger LOG = LoggerFactory.getLogger(EqualTypeAndLineEdgeCleanupVisitor.class);

  private Set<String> equalExcludeAttributes = new HashSet<String>(
    Arrays.asList(DataObjectEquals.EXCLUDE_ID,
      DataObjectEquals.EXCLUDE_GEOMETRY));

  private Statistics duplicateStatistics;

  private Comparator<DataObject> newerComparator;

  public LinearIntersectionNotEqualLineEdgeCleanupVisitor() {
    super.setComparator(new EdgeLengthComparator<DataObject>(true));
  }

  public Set<String> getEqualExcludeAttributes() {
    return equalExcludeAttributes;
  }

  public Comparator<DataObject> getNewerComparator() {
    return newerComparator;
  }

  public void process(final DataObjectGraph graph) {
    graph.visitEdges(this);
  }

  @Override
  public void setComparator(final Comparator<Edge<DataObject>> comparator) {
    throw new IllegalArgumentException("Cannot override comparator");
  }

  public void setEqualExcludeAttributes(
    final Collection<String> equalExcludeAttributes) {
    setEqualExcludeAttributes(new HashSet<String>(equalExcludeAttributes));
  }

  public void setEqualExcludeAttributes(final Set<String> equalExcludeAttributes) {
    this.equalExcludeAttributes = new HashSet<String>(equalExcludeAttributes);
    this.equalExcludeAttributes.add(DataObjectEquals.EXCLUDE_ID);
    this.equalExcludeAttributes.add(DataObjectEquals.EXCLUDE_GEOMETRY);
  }

  public void setNewerComparator(final Comparator<DataObject> newerComparator) {
    this.newerComparator = newerComparator;
  }

  @SuppressWarnings("unchecked")
  public boolean visit(final Edge<DataObject> edge) {
    final QName typeName = edge.getTypeName();

    final Graph<DataObject> graph = edge.getGraph();
    final LineString line = edge.getLine();

    final AndFilter<Edge<DataObject>> attributeAndGeometryFilter = new AndFilter<Edge<DataObject>>();

    attributeAndGeometryFilter.addFilter(new EdgeTypeNameFilter<DataObject>(
      typeName));

    final Filter<Edge<DataObject>> filter = getFilter();
    if (filter != null) {
      attributeAndGeometryFilter.addFilter(filter);
    }

    final Filter<DataObject> notEqualLineFilter = new NotFilter<DataObject>(
      new DataObjectGeometryFilter<LineString>(new EqualFilter<LineString>(line)));

    final DataObjectGeometryFilter<LineString> linearIntersectionFilter = new DataObjectGeometryFilter<LineString>(
      new LinearIntersectionFilter(line));

    attributeAndGeometryFilter.addFilter(new EdgeObjectFilter<DataObject>(
      new AndFilter<DataObject>(notEqualLineFilter, linearIntersectionFilter)));

    final List<Edge<DataObject>> intersectingEdges = graph.getEdges(
      attributeAndGeometryFilter, line);

    if (!intersectingEdges.isEmpty()) {
      if (intersectingEdges.size() == 1 && line.getLength() > 10) {
        CoordinatesList points = CoordinatesListUtil.get(line);
        if (points.size() > 2) {
          Edge<DataObject> edge2 = intersectingEdges.get(0);
          LineString line2 = edge2.getLine();
          CoordinatesList points2 = CoordinatesListUtil.get(line2);

          if (middleCoordinatesEqual(points, points2)) {
            boolean firstEqual = points.equal(0, points2, 0, 2);
            if (!firstEqual) {
              final Node<DataObject> fromNode1 = edge.getFromNode();
              final Node<DataObject> fromNode2 = edge2.getFromNode();
              if (fromNode1.distance(fromNode2) < 2) {
                graph.moveNodesToMidpoint(typeName, fromNode1, fromNode2);
                return true;
              }
            }
            boolean lastEqual = points.equal(points.size() - 1, points2,
              points.size() - 1, 2);
            if (!lastEqual) {
              final Node<DataObject> toNode1 = edge.getToNode();
              final Node<DataObject> toNode2 = edge2.getToNode();
              if (toNode1.distance(toNode2) < 2) {
                graph.moveNodesToMidpoint(typeName, toNode1, toNode2);
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

  private boolean middleCoordinatesEqual(CoordinatesList points1,
    CoordinatesList points2) {
    if (points1.size() == points2.size()) {
      for (int i = 1; i < points2.size(); i++) {
        if (!points1.equal(i, points1, i, 2)) {
          return false;
        }
      }
      return true;

    } else {
      return false;
    }
  }

  @PostConstruct
  public void init() {
    duplicateStatistics = new Statistics("Duplicate intersecting lines");
    duplicateStatistics.connect();
  }

  @PreDestroy
  public void destroy() {
    if (duplicateStatistics != null) {
      duplicateStatistics.disconnect();
    }
    duplicateStatistics = null;
  }

}
