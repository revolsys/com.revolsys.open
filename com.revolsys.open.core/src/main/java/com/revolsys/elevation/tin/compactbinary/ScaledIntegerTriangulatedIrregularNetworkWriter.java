package com.revolsys.elevation.tin.compactbinary;

import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class ScaledIntegerTriangulatedIrregularNetworkWriter extends BaseObjectWithProperties
  implements TriangulatedIrregularNetworkWriter {

  private final Resource resource;

  public ScaledIntegerTriangulatedIrregularNetworkWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    super.close();
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
      ChannelWriter out = this.resource.newChannelWriter()) {
      final BoundingBox tinBoundingBox = tin.getBoundingBox();

      final GeometryFactory geometryFactory = tin.getGeometryFactory();

      final int coordinateSystemId = geometryFactory.getCoordinateSystemId();
      double scaleX;
      {
        double scale = geometryFactory.getScaleX();
        if (scale <= 0) {
          scale = 1000;
        }
        scaleX = scale;
      }
      double scaleY;
      {
        double scale = geometryFactory.getScaleY();
        if (scale <= 0) {
          scale = 1000;
        }
        scaleY = scale;
      }
      double scaleZ;
      {
        double scale = geometryFactory.getScaleZ();
        if (scale <= 0) {
          scale = 1000;
        }
        scaleZ = scale;
      }

      out.putBytes(ScaledIntegerTriangulatedIrregularNetwork.FILE_TYPE_BYTES);
      out.putShort(ScaledIntegerTriangulatedIrregularNetwork.VERSION);
      out.putInt(coordinateSystemId); // Coordinate System ID
      out.putDouble(scaleX); // Scale X
      out.putDouble(scaleY); // Scale Y
      out.putDouble(scaleZ); // Scale Z
      out.putDouble(tinBoundingBox.getMinX()); // minX
      out.putDouble(tinBoundingBox.getMinY()); // minY
      out.putDouble(tinBoundingBox.getMaxX()); // maxX
      out.putDouble(tinBoundingBox.getMaxY()); // maxY

      tin.forEachTriangle(triangle -> {
        for (int i = 0; i < 3; i++) {
          final double x = triangle.getX(i);
          final double y = triangle.getY(i);
          final double z = triangle.getZ(i);
          if (Double.isFinite(x)) {
            final int intX = (int)Math.round(x * scaleX);
            out.putInt(intX);
          } else {
            out.putInt(Integer.MIN_VALUE);
          }
          if (Double.isFinite(y)) {
            final int intY = (int)Math.round(y * scaleY);
            out.putInt(intY);
          } else {
            out.putInt(Integer.MIN_VALUE);
          }
          if (Double.isFinite(z)) {
            final int intZ = (int)Math.round(z * scaleZ);
            out.putInt(intZ);
          } else {
            out.putInt(Integer.MIN_VALUE);
          }
        }
      });
    } catch (final Exception e) {
      throw Exceptions.wrap("Unable to write: " + this.resource, e);
    }
  }
}
