package com.revolsys.gis.graph.visitor;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.gis.data.visitor.FilterVisitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.filter.EdgeObjectFilter;
import com.revolsys.parallel.channel.Channel;

public class ChannelOutEdgeVisitor<T> implements Visitor<Edge<T>> {
  public static <T> void write(
    final Graph<T> graph,
    final Channel<T> out) {
    final Visitor<Edge<T>> visitor = new ChannelOutEdgeVisitor<T>(out);
    graph.visitEdges(visitor);
  }

  public static <T> void write(
    final Graph<T> graph,
    final Filter<T> filter,
    final Channel<T> out) {
    final Visitor<Edge<T>> visitor = new ChannelOutEdgeVisitor<T>(out);
    final EdgeObjectFilter<T> edgeFilter = new EdgeObjectFilter<T>(filter);
    final Visitor<Edge<T>> filterVisitor = new FilterVisitor<Edge<T>>(
      edgeFilter, visitor);
    graph.visitEdges(filterVisitor);
  }

  private final Channel<T> out;

  public ChannelOutEdgeVisitor(
    final Channel<T> out) {
    this.out = out;
  }

  public boolean visit(
    final Edge<T> edge) {
    if (out == null) {
      return false;
    } else {
      final T object = edge.getObject();
      out.write(object);
      return true;
    }
  }
}
