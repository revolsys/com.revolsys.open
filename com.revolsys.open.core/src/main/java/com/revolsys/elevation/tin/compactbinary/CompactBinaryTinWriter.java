package com.revolsys.elevation.tin.compactbinary;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.elevation.tin.BaseCompactTriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class CompactBinaryTinWriter extends BaseObjectWithProperties
  implements TriangulatedIrregularNetworkWriter {
  private final String version = "   0.0.1";

  private final Resource resource;

  public CompactBinaryTinWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void flush() {
  }

  @Override
  public void open() {
  }

  @Override
  public void write(final TriangulatedIrregularNetwork tin) {
    try (
      DataOutputStream out = this.resource.newBufferedOutputStream(DataOutputStream::new)) {
      out.writeChars("DEMC");
      out.writeChar('-');
      out.writeChars(this.version);
      out.writeInt(tin.getCoordinateSystemId());
      final BoundingBox tinBoundingBox = tin.getBoundingBox();
      out.writeDouble(tinBoundingBox.getMinX());
      out.writeDouble(tinBoundingBox.getMinY());
      out.writeDouble(tinBoundingBox.getMaxX());
      out.writeDouble(tinBoundingBox.getMaxY());

      final Map<Point, Integer> nodeMap = new HashMap<>();
      final int vertexCount = tin.getVertexCount();
      out.writeInt(vertexCount);
      final boolean isCompactTin = nodeMap instanceof BaseCompactTriangulatedIrregularNetwork;
      if (isCompactTin) {
        tin.forEachVertex((point) -> {
          try {
            out.writeDouble(point.getX());
            out.writeDouble(point.getY());
            out.writeFloat((float)point.getZ());
          } catch (final IOException e) {
            throw Exceptions.wrap("Unable to write: " + this.resource, e);
          }
        });
      } else {
        tin.forEachVertex((point) -> {
          try {
            final int vertexIndex = nodeMap.size();
            nodeMap.put(point, vertexIndex);
            out.writeDouble(point.getX());
            out.writeInt(' ');
            out.writeDouble(point.getY());
            out.writeFloat((float)point.getZ());
          } catch (final IOException e) {
            throw Exceptions.wrap("Unable to write: " + this.resource, e);
          }

        });
      }

      final int triangleCount = tin.getTriangleCount();
      out.writeInt(triangleCount);
      if (tin instanceof BaseCompactTriangulatedIrregularNetwork) {
        final BaseCompactTriangulatedIrregularNetwork compactTin = (BaseCompactTriangulatedIrregularNetwork)tin;
        for (int triangleIndex = 0; triangleIndex < triangleCount; triangleIndex++) {
          for (int j = 0; j < 3; j++) {
            final int index = compactTin.getTriangleVertexIndex(triangleIndex, j);
            out.writeInt(index);
          }
        }
      } else {
        tin.forEachTriangle((triangle) -> {
          for (int i = 0; i < 3; i++) {
            final Point point = triangle.getPoint(i);
            final Integer index = nodeMap.get(point);
            if (index == null) {
              throw new NullPointerException();
            }
            try {
              out.writeInt(index);
            } catch (final IOException e) {
              throw Exceptions.wrap("Unable to write: " + this.resource, e);
            }
          }
        });
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to write: " + this.resource, e);
    }
  }
}
