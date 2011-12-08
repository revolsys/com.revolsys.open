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

  public static void write(Resource resource, TriangulatedIrregularNetwork tin) {
    TinWriter tinWriter = new TinWriter(resource);
    try {
      tinWriter.write(tin);
    } finally {
      tinWriter.close();
    }
  }

  private PrintWriter out;

  private int tinIndex = 0;

  public TinWriter(Resource resource) {
    this.out = SpringUtil.getPrintWriter(resource);
    out.println("TIN");
  }

  public void write(TriangulatedIrregularNetwork tin) {
    out.println("BEGT");

    out.print("TNAM tin-");
    out.println(++tinIndex);

    out.println("TCOL 255 255 255");

    int nodeIndex = 0;
    Map<Coordinates, Integer> nodeMap = new HashMap<Coordinates, Integer>();
    Set<Coordinates> nodes = tin.getNodes();
    out.print("VERT ");
    out.println(nodes.size());
    for (Coordinates point : nodes) {
      nodeMap.put(point, ++nodeIndex);
      out.print(point.getX());
      out.print(' ');
      out.print(point.getY());
      out.print(' ');
      out.println(point.getZ());
    }

    List<Triangle> triangles = tin.getTriangles();
    out.print("TRI ");
    out.println(triangles.size());
    for (Triangle triangle : triangles) {
      for (int i = 0; i < 3; i++) {
        if (i > 0) {
          out.print(' ');
        }
        Coordinates point = triangle.get(i);
        Integer index = nodeMap.get(point);
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

  public void close() {
    out.close();
  }
}
