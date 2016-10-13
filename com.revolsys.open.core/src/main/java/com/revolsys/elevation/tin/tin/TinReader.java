package com.revolsys.elevation.tin.tin;

import java.io.BufferedReader;
import java.io.IOException;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.elevation.tin.CompactTriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.MathUtil;

public class TinReader implements BaseCloseable {

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

  private final GeometryFactory geometryFactory;

  private final BufferedReader in;

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

    final int vertexCount = Integer.parseInt(line.substring(5));
    final double[] vertexCoordinates = new double[vertexCount * 3];
    int coordinateOffset = 0;
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      line = readLine();
      final double[] coordinates = MathUtil.toDoubleArraySplit(line, " ");
      System.arraycopy(coordinates, 0, vertexCoordinates, coordinateOffset, 3);
      coordinateOffset += 3;
    }
    line = readLine();

    final int[] triangleVertexIndices;
    if (line.startsWith("ENDT")) {
      triangleVertexIndices = null;
    } else {
      if (!line.startsWith("TRI ")) {
        throw new IllegalArgumentException("Expecting TRI not " + line);
      }

      final int triangleCount = Integer.parseInt(line.substring(4));
      triangleVertexIndices = new int[triangleCount * 3];
      int triangleVertexOffset = 0;
      for (int triangleIndex = 0; triangleIndex < triangleCount; triangleIndex++) {
        line = readLine();
        final int[] indexes = MathUtil.toIntArraySplit(line, " ");
        for (int i = 0; i < indexes.length; i++) {
          final int vertexIndex = indexes[i] - 1;
          triangleVertexIndices[triangleVertexOffset++] = vertexIndex;
        }
      }
    }
    if (triangleVertexIndices == null) {
      throw new IllegalArgumentException("Not implemented");
    } else {
      return new CompactTriangulatedIrregularNetwork(this.geometryFactory, vertexCoordinates,
        triangleVertexIndices);
    }
  }

  private String readLine() {
    try {
      return this.in.readLine();
    } catch (final IOException e) {
      throw new RuntimeException("Unable to read line", e);
    }
  }
}
