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
      final boolean equalExact = LineStringUtil.equalsExact(line1, line2, 3);
      if (equalExcludedAttributes || getComparator() != null) {
        if (equalExact) {
          edge1.remove();
          if (duplicateStatistics != null) {
            duplicateStatistics.add(object1);
          }
        } else {
          // TODO check for 0 z and not null z-s
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
