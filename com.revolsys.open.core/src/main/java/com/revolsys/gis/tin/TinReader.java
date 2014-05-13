package com.revolsys.gis.tin;

import java.io.BufferedReader;
import java.io.IOException;

import org.springframework.core.io.Resource;

import com.revolsys.collection.IntHashMap;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.io.FileUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.MathUtil;

public class TinReader {

  public static TriangulatedIrregularNetwork read(
    final BoundingBox boundingBox, final Resource resource) {
    final TinReader tinReader = new TinReader(boundingBox, resource);
    try {
      final TriangulatedIrregularNetwork tin = tinReader.read();
      return tin;
    } finally {
      tinReader.close();
    }
  }

  public static TriangulatedIrregularNetwork read(
    final GeometryFactory geometryFactory, final Resource resource) {
    final TinReader tinReader = new TinReader(geometryFactory, resource);
    try {
      final TriangulatedIrregularNetwork tin = tinReader.read();
      return tin;
    } finally {
      tinReader.close();
    }
  }

  private final BufferedReader in;

  private final GeometryFactory geometryFactory;

  private BoundingBox boundingBox;

  public TinReader(final BoundingBox boundingBox, final Resource resource) {
    this.boundingBox = boundingBox;
    this.geometryFactory = boundingBox.getGeometryFactory();
    this.in = SpringUtil.getBufferedReader(resource);
    final String line = readLine();
    if (!"TIN".equals(line)) {
      throw new IllegalArgumentException("File does not contain a tin");
    }
  }

  public TinReader(final GeometryFactory geometryFactory,
    final Resource resource) {
    this.geometryFactory = geometryFactory;
    this.in = SpringUtil.getBufferedReader(resource);
    final String line = readLine();
    if (!"TIN".equals(line)) {
      throw new IllegalArgumentException("File does not contain a tin");
    }
  }

  public void close() {
    FileUtil.closeSilent(in);
  }

  public TriangulatedIrregularNetwork read() {
    final IntHashMap<Point> nodeIdMap = new IntHashMap<Point>();
    String line = readLine();
    if (!"BEGT".equals(line)) {
      throw new IllegalArgumentException("Expecting BEGT not " + line);
    }
    line = readLine();
    if (line.startsWith("TNAM")) {
      line = readLine();
    }
    if (line.startsWith("TCOL")) {
      line = readLine();
    }
    if (!line.startsWith("VERT ")) {
      throw new IllegalArgumentException("Expecting VERT not " + line);
    }
    BoundingBox boundingBox = new Envelope(geometryFactory);

    final int numNodes = Integer.parseInt(line.substring(5));
    for (int i = 1; i <= numNodes; i++) {
      line = readLine();
      final double[] coordinates = MathUtil.toDoubleArraySplit(line, " ");
      final Point point = new DoubleCoordinates(3, coordinates);
      boundingBox = boundingBox.expand(point);
      nodeIdMap.put(i, point);
    }
    line = readLine();

    if (this.boundingBox != null) {
      boundingBox = this.boundingBox;
    }
    final TriangulatedIrregularNetwork tin = new TriangulatedIrregularNetwork(
      boundingBox, true);

    if (line.startsWith("ENDT")) {
      tin.insertNodes(nodeIdMap.values());
    } else {
      if (!line.startsWith("TRI ")) {
        throw new IllegalArgumentException("Expecting TRI not " + line);
      }
      final int numTriangles = Integer.parseInt(line.substring(4));
      for (int i = 1; i <= numTriangles; i++) {
        line = readLine();
        final double[] indexes = MathUtil.toDoubleArraySplit(line, " ");

        final Point[] points = new Point[3];
        for (int j = 0; j < 3; j++) {
          final int index = (int)indexes[j];
          points[j] = nodeIdMap.get(index);
          if (points[j] == null) {
            throw new IllegalArgumentException(
              "Unable to get coordinates for triangle " + i + " vert " + index);
          }
        }
        final Triangle triangle = new Triangle(points);
        tin.addTriangle(triangle);
      }
    }
    return tin;
  }

  private String readLine() {
    try {
      return in.readLine();
    } catch (final IOException e) {
      throw new RuntimeException("Unable to read line", e);
    }
  }
}
