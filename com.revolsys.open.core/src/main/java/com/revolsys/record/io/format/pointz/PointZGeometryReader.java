package com.revolsys.record.io.format.pointz;

import java.io.EOFException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.WrappedException;

public class PointZGeometryReader extends AbstractIterator<Geometry> implements GeometryReader {
  private final Resource resource;

  private ChannelReader reader;

  private double scaleXy;

  private double scaleZ;

  private GeometryFactory geometryFactory;

  public PointZGeometryReader(final Resource resource) {
    this.resource = resource;
  }

  @Override
  protected void closeDo() {
    super.closeDo();
    final ChannelReader in = this.reader;
    this.reader = null;
    if (in != null) {
      in.close();
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  protected Geometry getNext() {
    try {
      final int xInt = this.reader.getInt();
      final int yInt = this.reader.getInt();
      final int zInt = this.reader.getInt();
      final double x = xInt / this.scaleXy;
      final double y = yInt / this.scaleXy;
      final double z = zInt / this.scaleZ;
      return this.geometryFactory.point(x, y, z);
    } catch (final WrappedException e) {
      if (e.getCause() instanceof EOFException) {
        throw new NoSuchElementException();
      } else {
        throw e;
      }
    }
  }

  @Override
  protected void initDo() {
    super.initDo();
    final ChannelReader reader = this.resource.newChannelReader();
    this.reader = reader;
    final byte[] fileTypeBytes = new byte[6];
    reader.getBytes(fileTypeBytes);
    final String fileType = new String(fileTypeBytes, StandardCharsets.UTF_8); // File
                                                                               // type
    if (!PointZIoFactory.FILE_TYPE_POINTZ.equals(fileType)) {
      throw new IllegalArgumentException(
        "File must start with the text: " + PointZIoFactory.FILE_TYPE_POINTZ + " not " + fileType);
    }
    @SuppressWarnings("unused")
    final short version = reader.getShort();
    final int coordinateSystemId = reader.getInt();
    this.scaleXy = reader.getDouble();

    this.scaleZ = reader.getDouble();
    this.geometryFactory = GeometryFactory.fixed(coordinateSystemId, 3, this.scaleXy, this.scaleXy,
      this.scaleZ);
  }

  @Override
  public RecordDefinition newRecordDefinition(final String name) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final RecordDefinition recordDefinition = new RecordDefinitionBuilder(name) //
      .addField("POINT", DataTypes.POINT) //
      .setGeometryFactory(geometryFactory) //
      .getRecordDefinition();
    return recordDefinition;
  }

}
