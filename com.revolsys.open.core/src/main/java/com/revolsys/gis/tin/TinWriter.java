package com.revolsys.gis.tin;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.Triangle;
import com.revolsys.spring.SpringUtil;

public class TinWriter {

  public static void write(
    final Resource resource,
    final TriangulatedIrregularNetwork tin) {
    final TinWriter tinWriter = new TinWriter(resource);
    try {
      tinWriter.write(tin);
    } finally {
      tinWriter.close();
    }
  }

  private final PrintWriter out;

  private int tinIndex = 0;

  public TinWriter(final Resource resource) {
    this.out = SpringUtil.getPrintWriter(resource);
    out.println("TIN");
  }

  public void close() {
    out.close();
  }

  public void write(final TriangulatedIrregularNetwork tin) {
    out.println("BEGT");

    out.print("TNAM tin-");
    out.println(++tinIndex);

    out.println("TCOL 255 255 255");

    int nodeIndex = 0;
    final Map<Coordinates, Integer> nodeMap = new HashMap<Coordinates, Integer>();
    final Set<Coordinates> nodes = tin.getNodes();
    out.print("VERT ");
    out.println(nodes.size());
    for (final Coordinates point : nodes) {
      nodeMap.put(point, ++nodeIndex);
      out.print(point.getX());
      out.print(' ');
      out.print(point.getY());
      out.print(' ');
      out.println(point.getZ());
    }

    final List<Triangle> triangles = tin.getTriangles();
    out.print("TRI ");
    out.println(triangles.size());
    for (final Triangle triangle : triangles) {
      for (int i = 0; i < 3; i++) {
        if (i > 0) {
          out.print(' ');
        }
        final Coordinates point = triangle.get(i);
        final Integer index = nodeMap.get(point);
        if (index == null) {
          System.out.println(point);
          System.out.println(triangle);
          System.out.println(tin.getBoundingBox().toPolygon());
          throw new NullPointerException();
        }
        out.print(index);
      }
      out.println();
    }

    out.println("ENDT");
  }
}
