package com.revolsys.elevation.tin.tin;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkImpl;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.impl.TriangleDouble;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.MathUtil;

public class TinReader implements BaseCloseable {

  public static TriangulatedIrregularNetwork read(final BoundingBox boundingBox,
    final Resource resource) {
    final TinReader tinReader = new TinReader(boundingBox, resource);
    try {
      final TriangulatedIrregularNetwork tin = tinReader.read();
      return tin;
    } finally {
      tinReader.close();
    }
  }

  public static TriangulatedIrregularNetwork read(final GeometryFactory geometryFactory,
    final Resource resource) {
    final TinReader tinReader = new TinReader(geometryFactory, resource);
    try {
      final TriangulatedIrregularNetwork tin = tinReader.read();
      return tin;
    } finally {
      tinReader.close();
    }
  }

  private BoundingBox boundingBox;

  private final GeometryFactory geometryFactory;

  private final BufferedReader in;

  public TinReader(final BoundingBox boundingBox, final Resource resource) {
    this.boundingBox = boundingBox;
    this.geometryFactory = boundingBox.getGeometryFactory();
    this.in = resource.newBufferedReader();
    final String line = readLine();
    if (!"TIN".equals(line)) {
      throw new IllegalArgumentException("File does not contain a tin");
    }
  }

  public TinReader(final GeometryFactory geometryFactory, final Resource resource) {
    this.geometryFactory = geometryFactory;
    this.in = resource.newBufferedReader();
    final String line = readLine();
    if (!"TIN".equals(line)) {
      throw new IllegalArgumentException("File does not contain a tin");
    }
  }

  @Override
  public void close() {
    FileUtil.closeSilent(this.in);
  }

  public TriangulatedIrregularNetwork read() {
    final IntHashMap<Point> nodeIdMap = new IntHashMap<>();
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
    BoundingBox boundingBox = new BoundingBoxDoubleGf(this.geometryFactory);

    final int numNodes = Integer.parseInt(line.substring(5));
    for (int i = 1; i <= numNodes; i++) {
      line = readLine();
      final double[] coordinates = MathUtil.toDoubleArraySplit(line, " ");
      final Point point = new PointDouble(3, coordinates);
      boundingBox = boundingBox.expand(point);
      nodeIdMap.put(i, point);
    }
    line = readLine();

    if (this.boundingBox != null) {
      boundingBox = this.boundingBox;
    }
    final List<Triangle> triangles = new ArrayList<>();
    if (line.startsWith("ENDT")) {
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
        final Triangle triangle = TriangleDouble.newTriangle(points);
        triangles.add(triangle);
      }
    }
    return new TriangulatedIrregularNetworkImpl(boundingBox, triangles);
  }

  private String readLine() {
    try {
      return this.in.readLine();
    } catch (final IOException e) {
      throw new RuntimeException("Unable to read line", e);
    }
  }
}
