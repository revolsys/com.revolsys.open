package com.revolsys.gis.graph.visitor;

import com.revolsys.collection.Visitor;
import com.revolsys.data.record.Record;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.RecordGraph;
import com.revolsys.util.ObjectProcessor;

public class EdgeLessThanLengthVisitor extends
AbstractEdgeListenerVisitor<Record> implements
ObjectProcessor<RecordGraph> {

  private double minLength;

  private Visitor<Edge<Record>> visitor;

  public EdgeLessThanLengthVisitor() {
  }

  public EdgeLessThanLengthVisitor(final double minLength) {
    this.minLength = minLength;
  }

  public EdgeLessThanLengthVisitor(final double minLength,
    final Visitor<Edge<Record>> visitor) {
    this.minLength = minLength;
    this.visitor = visitor;
  }

  public double getMinLength() {
    return this.minLength;
  }

  @Override
  public void process(final RecordGraph graph) {
    graph.visitEdges(this);
  }

  public void setMinLength(final double minLength) {
    this.minLength = minLength;
  }

  @Override
  public boolean visit(final Edge<Record> edge) {
    final double length = edge.getLength();
    if (length < this.minLength) {
      edgeEvent(edge, "Edge less than length", "Review", length + " < "
          + this.minLength);
      if (this.visitor != null) {
        this.visitor.visit(edge);
      }
    }
    return true;
  }
}
