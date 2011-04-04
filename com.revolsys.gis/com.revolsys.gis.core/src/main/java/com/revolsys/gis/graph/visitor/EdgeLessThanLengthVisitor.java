package com.revolsys.gis.graph.visitor;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.visitor.Visitor;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.util.ObjectProcessor;

public class EdgeLessThanLengthVisitor extends
  AbstractEdgeListenerVisitor<DataObject> implements
  ObjectProcessor<DataObjectGraph> {

  private double minLength;

  private Visitor<Edge<DataObject>> visitor;

  public EdgeLessThanLengthVisitor() {
  }

  public EdgeLessThanLengthVisitor(final double minLength) {
    this.minLength = minLength;
  }

  public EdgeLessThanLengthVisitor(final double minLength,
    final Visitor<Edge<DataObject>> visitor) {
    this.minLength = minLength;
    this.visitor = visitor;
  }

  public double getMinLength() {
    return minLength;
  }

  public void process(final DataObjectGraph graph) {
    graph.visitEdges(this);
  }

  public void setMinLength(final double minLength) {
    this.minLength = minLength;
  }

  public boolean visit(final Edge<DataObject> edge) {
    final double length = edge.getLength();
    if (length < minLength) {
      edgeEvent(edge, "Edge less than length", "Review", length + " < "
        + minLength);
      if (visitor != null) {
        visitor.visit(edge);
      }
    }
    return true;
  }
}
