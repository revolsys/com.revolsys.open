package com.revolsys.elevation.tin.compactbinary;

import java.io.DataInputStream;
import java.io.IOException;

import com.revolsys.elevation.tin.CompactTriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class CompactBinaryTinReader implements BaseCloseable {

  public static TriangulatedIrregularNetwork read(final Resource resource) {
    try (
      final CompactBinaryTinReader compactBinaryTinReader = new CompactBinaryTinReader(resource)) {
      final TriangulatedIrregularNetwork tin = compactBinaryTinReader.read();
      return tin;
    }
  }

  private final Resource resource;

  public CompactBinaryTinReader(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
  }

  public TriangulatedIrregularNetwork read() {
    try (
      DataInputStream in = this.resource.newBufferedInputStream(DataInputStream::new)) {
      @SuppressWarnings("unused")
      final String fileType = FileUtil.readString(in, 5); // File type
      @SuppressWarnings("unused")
      final String version = FileUtil.readString(in, 8); // version
      final int coordinateSystemId = in.readInt(); // Coordinate System ID
      final double minX = in.readDouble();
      final double minY = in.readDouble();
      final double maxX = in.readDouble();
      final double maxY = in.readDouble();
      final GeometryFactory geometryFactory = GeometryFactory.floating3(coordinateSystemId);

      final int vertexCount = in.readInt();
      final double[] vertexXCoordinates = new double[vertexCount];
      final double[] vertexYCoordinates = new double[vertexCount];
      final double[] vertexZCoordinates = new double[vertexCount];
      for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
        vertexXCoordinates[vertexIndex] = in.readDouble();
        vertexYCoordinates[vertexIndex] = in.readDouble();
        vertexZCoordinates[vertexIndex] = in.readFloat();
      }

      final int triangleCount = in.readInt();
      final int[] triangleVertex0Indices = new int[triangleCount];
      final int[] triangleVertex1Indices = new int[triangleCount];
      final int[] triangleVertex2Indices = new int[triangleCount];
      for (int triangleIndex = 0; triangleIndex < triangleCount; triangleIndex++) {
        triangleVertex0Indices[triangleIndex] = in.readInt();
        triangleVertex1Indices[triangleIndex] = in.readInt();
        triangleVertex2Indices[triangleIndex] = in.readInt();
      }
      return new CompactTriangulatedIrregularNetwork(geometryFactory, vertexCount,
        vertexXCoordinates, vertexYCoordinates, vertexZCoordinates, triangleCount,
        triangleVertex0Indices, triangleVertex1Indices, triangleVertex2Indices);
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.resource, e);
    }
  }
}
