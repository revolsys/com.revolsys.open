package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.revolsys.gis.data.visitor.NestedVisitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgeQuadTree;
import com.revolsys.gis.graph.Graph;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

public class BoundingBoxIntersectsEdgeVisitor<T> extends NestedVisitor<Edge<T>> {
  public static <T> List<Edge<T>> getEdges(final Graph<T> graph,
    final Edge<T> edge, final double maxDistance) {
    final CreateListVisitor<Edge<T>> results = new CreateListVisitor<Edge<T>>();

    final LineString line = edge.getLine();
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    final BoundingBox boundingBox = new BoundingBox(geometryFactory,
      line.getEnvelopeInternal());
    boundingBox.expandBy(maxDistance);
    final BoundingBoxIntersectsEdgeVisitor<T> visitor = new BoundingBoxIntersectsEdgeVisitor<T>(
      boundingBox, results);
    final EdgeQuadTree<T> index = graph.getEdgeIndex();
    index.query(boundingBox, visitor);
    List<Edge<T>> list = results.getList();
    list.remove(edge);
    return list;

  }

  private final BoundingBox boundingBox;

  public BoundingBoxIntersectsEdgeVisitor(final BoundingBox boundingBox,
    final Visitor<Edge<T>> matchVisitor) {
    super(matchVisitor);
    this.boundingBox = boundingBox;
  }

  @Override
  public boolean visit(final Edge<T> edge) {
    final Envelope envelope = edge.getEnvelope();
    if (this.boundingBox.intersects(envelope)) {
      super.visit(edge);
    }
    return true;
  }
}
