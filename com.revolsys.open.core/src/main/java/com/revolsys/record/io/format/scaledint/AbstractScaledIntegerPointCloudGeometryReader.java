package com.revolsys.record.io.format.scaledint;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.MapEx;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.WrappedException;

public class AbstractScaledIntegerPointCloudGeometryReader<G extends Geometry>
  extends AbstractIterator<G> {
  private final Resource resource;

  private ChannelReader reader;

  private double scaleXy;

  private double scaleZ;

  private GeometryFactory geometryFactory;

  private boolean exists = false;

  private ByteBuffer byteBuffer;

  public AbstractScaledIntegerPointCloudGeometryReader(final Resource resource,
    final MapEx properties) {
    this.resource = resource;
    setProperties(properties);
  }

  @Override
  protected void closeDo() {
    super.closeDo();
    final ChannelReader reader = this.reader;
    this.reader = null;
    if (reader != null) {
      reader.close();
    }
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected G getNext() {
    final ChannelReader reader = this.reader;
    if (reader == null) {
      throw new NoSuchElementException();
    } else {
      try {
        final int xInt = reader.getInt();
        final int yInt = reader.getInt();
        final int zInt = reader.getInt();
        final double x = xInt / this.scaleXy;
        final double y = yInt / this.scaleXy;
        final double z = zInt / this.scaleZ;
        return (G)this.geometryFactory.point(x, y, z);
      } catch (final WrappedException e) {
        if (e.getCause() instanceof EOFException) {
          throw new NoSuchElementException();
        } else {
          throw e;
        }
      }
    }
  }

  @Override
  protected void initDo() {
    super.initDo();
    final ChannelReader reader = this.resource.newChannelReader(this.byteBuffer);
    this.reader = reader;
    if (reader == null) {
      this.exists = false;
    } else {
      this.exists = true;
      final byte[] fileTypeBytes = new byte[6];
      reader.getBytes(fileTypeBytes);
      final String fileType = new String(fileTypeBytes, StandardCharsets.UTF_8); // File
                                                                                 // type
      if (!ScaledIntegerPointCloud.FILE_TYPE_HEADER.equals(fileType)
        && !"POINTZ".equals(fileType)) {
        throw new IllegalArgumentException("File must start with the text: "
          + ScaledIntegerPointCloud.FILE_TYPE_HEADER + " not " + fileType);
      }
      @SuppressWarnings("unused")
      final short version = reader.getShort();
      final int coordinateSystemId = reader.getInt();
      this.scaleXy = reader.getDouble();

      this.scaleZ = reader.getDouble();
      this.geometryFactory = GeometryFactory.fixed3d(coordinateSystemId, this.scaleXy, this.scaleXy,
        this.scaleZ);
    }
  }

  public boolean isExists() {
    return this.exists;
  }

  public RecordDefinition newRecordDefinition(final String name) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final RecordDefinition recordDefinition = new RecordDefinitionBuilder(name) //
      .addField("POINT", DataTypes.POINT) //
      .setGeometryFactory(geometryFactory) //
      .getRecordDefinition();
    return recordDefinition;
  }

  public void setByteBuffer(final ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
    if (byteBuffer != null) {
      byteBuffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

}
