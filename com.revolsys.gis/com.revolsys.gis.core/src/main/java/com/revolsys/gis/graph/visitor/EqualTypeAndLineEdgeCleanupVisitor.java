package com.revolsys.gis.graph.visitor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.filter.GeometryFilter;
import com.revolsys.gis.data.visitor.AbstractVisitor;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.filter.EdgeObjectFilter;
import com.revolsys.gis.graph.filter.EdgeTypeNameFilter;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.jts.filter.LineEqualIgnoreDirectionFilter;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.data.equals.DataObjectEquals;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.ObjectProcessor;
import com.vividsolutions.jts.geom.LineString;

public class EqualTypeAndLineEdgeCleanupVisitor extends
  AbstractVisitor<Edge<DataObject>> implements ObjectProcessor<DataObjectGraph> {

  /** Flag indicating that the edge has been processed. */
  private static final String EDGE_PROCESSED = EqualTypeAndLineEdgeCleanupVisitor.class.getName()
    + ".processed";

  private static final Logger LOG = LoggerFactory.getLogger(EqualTypeAndLineEdgeCleanupVisitor.class);

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

  public Set<String> getEqualExcludeAttributes() {
    return equalExcludeAttributes;
  }

  @PostConstruct
  public void init() {
    duplicateStatistics = new Statistics("Duplicate equal lines");
    duplicateStatistics.connect();
  }

  public void process(final DataObjectGraph graph) {
    graph.visitEdges(this);
  }

  private void processEqualEdge(final Edge<DataObject> edge1,
    final Edge<DataObject> edge2) {
    final DataObject object1 = edge1.getObject();
    final DataObject object2 = edge2.getObject();

    final boolean equalAttributes = EqualsRegistry.INSTANCE.equals(object1,
      object2, equalExcludeAttributes);

    final LineString line1 = edge1.getLine();
    if (equalAttributes) {
      boolean equalExcludedAttributes = true;
      for (final String name : equalExcludeAttributes) {
        if (!DataObjectEquals.equals(object1, object2, name)) {
          equalExcludedAttributes = false;
        }
      }
      final LineString line2 = edge2.getLine();

      final boolean equalZ = fixMissingZValues(line1, line2);
      if (equalExcludedAttributes || getComparator() != null) {
        if (equalZ) {
          edge2.remove();
          if (duplicateStatistics != null) {
            duplicateStatistics.add(object2);
          }
        } else {
          LOG.error("Equal geometry with different coordinates or Z-values: "
            + line1);
        }
      } else {
        LOG.error("Equal geometry with different attributes: " + line1);
      }
    } else {
      LOG.error("Equal geometry with different attributes: " + line1);
    }
  }

  public boolean fixMissingZValues(final LineString line1,
    final LineString line2) {
    CoordinatesList points1 = CoordinatesListUtil.get(line1);
    CoordinatesList points2 = CoordinatesListUtil.get(line2);
    if (points1.getNumAxis() > 2) {
      final int numPoints = points1.size();
      boolean reverse = isReverse(points1, points2);
      if (reverse) {
        int j = numPoints - 1;
        for (int i = 0; i < numPoints; i++) {
          if (!fixZValues(points1, j, points2, i)) {
            return false;
          }
          j--;
        }
      } else {
        for (int i = 0; i < numPoints; i++) {
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

  public boolean fixZValues(CoordinatesList points1, int index1,
    CoordinatesList points2, int index2) {
    double z1 = points1.getZ(index2);
    double z2 = points2.getZ(index1);
    if (Double.isNaN(z1) || z1 == 0) {
      if (!Double.isNaN(z2)) {
        points1.setZ(index2, z2);
      }
      return true;
    } else if (Double.isNaN(z2) || z2 == 0) {
      if (!Double.isNaN(z1)) {
        points2.setZ(index1, z1);
      }
      return true;
    } else {
      return z1 == z2;
    }
  }

  public boolean isReverse(CoordinatesList points1, CoordinatesList points2) {
    final int numPoints = points1.size();
    if (points1.equal(0, points2, numPoints - 1, 2)) {
      if (points1.equal(0, points1, numPoints - 1, 2)) {
        int j = numPoints - 1;
        for (int i = 1; i < numPoints; i++) {
          if (!points1.equal(i, points2, j, 2)) {
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

  public void setEqualExcludeAttributes(
    final Collection<String> equalExcludeAttributes) {
    setEqualExcludeAttributes(new HashSet<String>(equalExcludeAttributes));
  }

  public void setEqualExcludeAttributes(final Set<String> equalExcludeAttributes) {
    this.equalExcludeAttributes = new HashSet<String>(equalExcludeAttributes);
    this.equalExcludeAttributes.add(DataObjectEquals.EXCLUDE_ID);
    this.equalExcludeAttributes.add(DataObjectEquals.EXCLUDE_GEOMETRY);
  }

  public boolean visit(final Edge<DataObject> edge) {
    if (edge.getAttribute(EDGE_PROCESSED) == null) {
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

      final Filter<DataObject> equalLineFilter = new GeometryFilter<LineString>(
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
