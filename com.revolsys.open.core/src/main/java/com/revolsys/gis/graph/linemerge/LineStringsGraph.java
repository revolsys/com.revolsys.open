package com.revolsys.gis.graph.linemerge;

import com.revolsys.collection.list.Lists;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.util.CleanDuplicatePoints;

public class LineStringsGraph extends Graph<LineString> {

  public void addEdge(LineString line) {
    if (!line.isEmpty()) {
      line = CleanDuplicatePoints.clean(line);
      final int vertexCount = line.getVertexCount();
      if (vertexCount > 1) {
        addEdge(line, line);
      }
    }
  }

  public void removeEdge(final LineString line) {
    final Point point = line.getFromPoint();
    final Node<LineString> node = findNode(point);
    if (node != null) {
      for (final Edge<LineString> edge : Lists.array(node.getOutEdges())) {
        final LineString edgeLine = edge.getLine();
        if (line.equals(edgeLine)) {
          remove(edge);
        }
      }
    }
  }

  public void removeEdges(final Iterable<LineString> lines) {
    for (final LineString line : lines) {
      removeEdge(line);
    }
  }
}
