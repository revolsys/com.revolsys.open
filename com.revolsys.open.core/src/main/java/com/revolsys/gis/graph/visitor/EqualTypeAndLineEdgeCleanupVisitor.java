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

import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectLog;
import com.revolsys.gis.data.model.filter.DataObjectGeometryFilter;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.filter.EdgeObjectFilter;
import com.revolsys.gis.graph.filter.EdgeTypeNameFilter;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jts.filter.LineEqualIgnoreDirectionFilter;
import com.revolsys.gis.model.data.equals.DataObjectEquals;
import com.revolsys.gis.model.data.equals.EqualsInstance;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.PointList;
import com.revolsys.util.ObjectProcessor;
import com.revolsys.visitor.AbstractVisitor;

public class EqualTypeAndLineEdgeCleanupVisitor extends
  AbstractVisitor<Edge<DataObject>> implements ObjectProcessor<DataObjectGraph> {

  /** Flag indicating that the edge has been processed. */
  private static final String EDGE_PROCESSED = EqualTypeAndLineEdgeCleanupVisitor.class.getName()
    + ".processed";

  private Statistics duplicateStatistics;

  private Set<String> equalExcludeAttributes = new HashSet<String>(
    Arrays.asList(DataObjectEquals.EXCLUDE_ID,
      DataObjectEquals.EXCLUDE_GEOMETRY));

  @PreDestroy
  public void destroy() {
    if (duplicateStatistics != null) {
      duplicateStatistics.disconnect();
    }
    duplicateStatistics = null;
  }

  public boolean fixMissingZValues(final LineString line1,
    final LineString line2) {
    final PointList points1 = line1;
    final PointList points2 = line2;
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

  public boolean fixZValues(final PointList points1, final int index1,
    final PointList points2, final int index2) {
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
    return equalExcludeAttributes;
  }

  @PostConstruct
  public void init() {
    duplicateStatistics = new Statistics("Duplicate equal lines");
    duplicateStatistics.connect();
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
  public void process(final DataObjectGraph graph) {
    graph.visitEdges(this);
  }

  private void processEqualEdge(final Edge<DataObject> edge1,
    final Edge<DataObject> edge2) {
    final DataObject object1 = edge1.getObject();
    final DataObject object2 = edge2.getObject();

    final boolean equalAttributes = EqualsInstance.INSTANCE.equals(object1,
      object2, equalExcludeAttributes);

    final LineString line1 = edge1.getLine();
    int compare = 0;
    final Comparator<Edge<DataObject>> comparator = getComparator();
    if (comparator != null) {
      compare = comparator.compare(edge1, edge2);
    }
    if (compare == 0) {
      if (equalAttributes) {
        boolean equalExcludedAttributes = true;
        for (final String name : equalExcludeAttributes) {
          if (!DataObjectEquals.equals(object1, object2, name)) {
            equalExcludedAttributes = false;
          }
        }
        final LineString line2 = edge2.getLine();

        final boolean equalZ = fixMissingZValues(line1, line2);
        if (equalExcludedAttributes) {
          if (equalZ) {
            removeDuplicate(edge2, edge1);
          } else {
            DataObjectLog.error(getClass(),
              "Equal geometry with different coordinates or Z-values", object1);
          }
        } else {
          DataObjectLog.error(getClass(),
            "Equal geometry with different attributes: ", object1);
        }
      } else {
        DataObjectLog.error(getClass(),
          "Equal geometry with different attributes: ", object1);
      }
    } else {
      removeDuplicate(edge2, edge1);
    }
  }

  private void processEqualEdges(final List<Edge<DataObject>> equalEdges) {
    final Iterator<Edge<DataObject>> edgeIter = equalEdges.iterator();
    final Edge<DataObject> edge1 = edgeIter.next();
    edge1.setAttribute(EDGE_PROCESSED, Boolean.TRUE);

    while (edgeIter.hasNext()) {
      final Edge<DataObject> edge2 = edgeIter.next();

      edge2.setAttribute(EDGE_PROCESSED, Boolean.TRUE);
      processEqualEdge(edge1, edge2);
      if (edge1.isRemoved()) {
        return;
      }
    }
  }

  protected void removeDuplicate(final Edge<DataObject> removeEdge,
    final Edge<DataObject> keepEdge) {
    removeEdge.remove();
    if (duplicateStatistics != null) {
      duplicateStatistics.add(removeEdge.getObject());
    }
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

  @Override
  public boolean visit(final Edge<DataObject> edge) {
    if (edge.getAttribute(EDGE_PROCESSED) == null) {
      final String typePath = edge.getTypeName();
      final Graph<DataObject> graph = edge.getGraph();
      final LineString line = edge.getLine();

      final AndFilter<Edge<DataObject>> attributeAndGeometryFilter = new AndFilter<Edge<DataObject>>();

      attributeAndGeometryFilter.addFilter(new EdgeTypeNameFilter<DataObject>(
        typePath));

      final Filter<Edge<DataObject>> filter = getFilter();
      if (filter != null) {
        attributeAndGeometryFilter.addFilter(filter);
      }

      final Filter<DataObject> equalLineFilter = new DataObjectGeometryFilter<LineString>(
        new LineEqualIgnoreDirectionFilter(line, 2));
      final EdgeObjectFilter<DataObject> edgeFilter = new EdgeObjectFilter<DataObject>(
        equalLineFilter);
      attributeAndGeometryFilter.addFilter(edgeFilter);

      final List<Edge<DataObject>> equalEdges;
      if (getComparator() == null) {
        equalEdges = graph.getEdges(attributeAndGeometryFilter, line);
      } else {
        equalEdges = graph.getEdges(attributeAndGeometryFilter,
          getComparator(), line);
      }
      processEqualEdges(equalEdges);
    }
    return true;
  }
}
