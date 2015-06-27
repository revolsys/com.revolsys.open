package com.revolsys.gis.graph.visitor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.equals.RecordEquals;
import com.revolsys.data.filter.RecordGeometryFilter;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordLog;
import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.RecordGraph;
import com.revolsys.gis.graph.filter.EdgeObjectFilter;
import com.revolsys.gis.graph.filter.EdgeTypeNameFilter;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jts.filter.LineEqualIgnoreDirectionFilter;
import com.revolsys.jts.geom.LineString;
import com.revolsys.util.ObjectProcessor;
import com.revolsys.visitor.AbstractVisitor;

public class EqualTypeAndLineEdgeCleanupVisitor extends AbstractVisitor<Edge<Record>>
  implements ObjectProcessor<RecordGraph> {

  /** Flag indicating that the edge has been processed. */
  private static final String EDGE_PROCESSED = EqualTypeAndLineEdgeCleanupVisitor.class.getName()
    + ".processed";

  private Statistics duplicateStatistics;

  private Set<String> equalExcludeAttributes = new HashSet<String>(
    Arrays.asList(RecordEquals.EXCLUDE_ID, RecordEquals.EXCLUDE_GEOMETRY));

  @PreDestroy
  public void destroy() {
    if (this.duplicateStatistics != null) {
      this.duplicateStatistics.disconnect();
    }
    this.duplicateStatistics = null;
  }

  public boolean fixMissingZValues(final LineString line1, final LineString line2) {
    final LineString points1 = line1;
    final LineString points2 = line2;
    final int axisCount = points1.getAxisCount();
    if (axisCount > 2) {
      final int vertexCount = points1.getVertexCount();
      final boolean reverse = isReverse(line1, line2);
      if (reverse) {
        int j = vertexCount - 1;
        for (int i = 0; i < vertexCount; i++) {
          if (!fixZValues(points1, j, points2, i)) {
            return false;
          }
          j--;
        }
      } else {
        for (int i = 0; i < vertexCount; i++) {
          if (!fixZValues(points1, i, points2, i)) {
            return false;
          }
        }
      }
      return true;
    } else {
      return true;
    }
  }

  public boolean fixZValues(final LineString points1, final int index1, final LineString points2,
    final int index2) {
    // TODO
    // final double z1 = points1.getZ(index2);
    // final double z2 = points2.getZ(index1);
    // if (Double.isNaN(z1) || z1 == 0) {
    // if (!Double.isNaN(z2)) {
    // points1.setValue(index2, 2, z2);
    // }
    // return true;
    // } else if (Double.isNaN(z2) || z2 == 0) {
    // if (!Double.isNaN(z1)) {
    // points2.setValue(index1, 2, z1);
    // }
    return true;
    // } else {
    // return z1 == z2;
    // }
  }

  public Set<String> getEqualExcludeAttributes() {
    return this.equalExcludeAttributes;
  }

  @PostConstruct
  public void init() {
    this.duplicateStatistics = new Statistics("Duplicate equal lines");
    this.duplicateStatistics.connect();
  }

  public boolean isReverse(final LineString points1, final LineString points2) {
    final int numPoints = points1.getVertexCount();
    if (points1.equalsVertex(2, 0, points2, numPoints - 1)) {
      if (points1.equalsVertex(2, 0, points1, numPoints - 1)) {
        int j = numPoints - 1;
        for (int i = 1; i < numPoints; i++) {
          if (!points1.equalsVertex(2, i, points2, j)) {
            return false;
          }
          j++;
        }
        return true;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public void process(final RecordGraph graph) {
    graph.visitEdges(this);
  }

  private void processEqualEdge(final Edge<Record> edge1, final Edge<Record> edge2) {
    final Record object1 = edge1.getObject();
    final Record object2 = edge2.getObject();

    final boolean equalAttributes = EqualsInstance.INSTANCE.equals(object1, object2,
      this.equalExcludeAttributes);

    final LineString line1 = edge1.getLine();
    int compare = 0;
    final Comparator<Edge<Record>> comparator = getComparator();
    if (comparator != null) {
      compare = comparator.compare(edge1, edge2);
    }
    if (compare == 0) {
      if (equalAttributes) {
        boolean equalExcludedAttributes = true;
        for (final String name : this.equalExcludeAttributes) {
          if (!RecordEquals.equals(object1, object2, name)) {
            equalExcludedAttributes = false;
          }
        }
        final LineString line2 = edge2.getLine();

        final boolean equalZ = fixMissingZValues(line1, line2);
        if (equalExcludedAttributes) {
          if (equalZ) {
            removeDuplicate(edge2, edge1);
          } else {
            RecordLog.error(getClass(), "Equal geometry with different coordinates or Z-values",
              object1);
          }
        } else {
          RecordLog.error(getClass(), "Equal geometry with different attributes: ", object1);
        }
      } else {
        RecordLog.error(getClass(), "Equal geometry with different attributes: ", object1);
      }
    } else {
      removeDuplicate(edge2, edge1);
    }
  }

  private void processEqualEdges(final List<Edge<Record>> equalEdges) {
    final Iterator<Edge<Record>> edgeIter = equalEdges.iterator();
    final Edge<Record> edge1 = edgeIter.next();
    edge1.setProperty(EDGE_PROCESSED, Boolean.TRUE);

    while (edgeIter.hasNext()) {
      final Edge<Record> edge2 = edgeIter.next();

      edge2.setProperty(EDGE_PROCESSED, Boolean.TRUE);
      processEqualEdge(edge1, edge2);
      if (edge1.isRemoved()) {
        return;
      }
    }
  }

  protected void removeDuplicate(final Edge<Record> removeEdge, final Edge<Record> keepEdge) {
    removeEdge.remove();
    if (this.duplicateStatistics != null) {
      this.duplicateStatistics.add(removeEdge.getObject());
    }
  }

  public void setEqualExcludeAttributes(final Collection<String> equalExcludeAttributes) {
    setEqualExcludeAttributes(new HashSet<String>(equalExcludeAttributes));
  }

  public void setEqualExcludeAttributes(final Set<String> equalExcludeAttributes) {
    this.equalExcludeAttributes = new HashSet<String>(equalExcludeAttributes);
    this.equalExcludeAttributes.add(RecordEquals.EXCLUDE_ID);
    this.equalExcludeAttributes.add(RecordEquals.EXCLUDE_GEOMETRY);
  }

  @Override
  public boolean visit(final Edge<Record> edge) {
    if (edge.getProperty(EDGE_PROCESSED) == null) {
      final String typePath = edge.getTypeName();
      final Graph<Record> graph = edge.getGraph();
      final LineString line = edge.getLine();

      final AndFilter<Edge<Record>> attributeAndGeometryFilter = new AndFilter<Edge<Record>>();

      attributeAndGeometryFilter.addFilter(new EdgeTypeNameFilter<Record>(typePath));

      final Filter<Edge<Record>> filter = getFilter();
      if (filter != null) {
        attributeAndGeometryFilter.addFilter(filter);
      }

      final Filter<Record> equalLineFilter = new RecordGeometryFilter<LineString>(
        new LineEqualIgnoreDirectionFilter(line, 2));
      final EdgeObjectFilter<Record> edgeFilter = new EdgeObjectFilter<Record>(equalLineFilter);
      attributeAndGeometryFilter.addFilter(edgeFilter);

      final List<Edge<Record>> equalEdges;
      if (getComparator() == null) {
        equalEdges = graph.getEdges(attributeAndGeometryFilter, line);
      } else {
        equalEdges = graph.getEdges(attributeAndGeometryFilter, getComparator(), line);
      }
      processEqualEdges(equalEdges);
    }
    return true;
  }
}
