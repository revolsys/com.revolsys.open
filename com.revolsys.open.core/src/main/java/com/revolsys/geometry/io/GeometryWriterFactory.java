package com.revolsys.geometry.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.revolsys.collection.set.Sets;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.io.FileIoFactory;
import com.revolsys.io.IoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;

public interface GeometryWriterFactory extends FileIoFactory, IoFactoryWithCoordinateSystem {
  static Set<DataType> ALL_GEOMETRY_TYPES = Sets.newLinkedHash(//
    DataTypes.GEOMETRY, //
    DataTypes.GEOMETRY_COLLECTION, //
    DataTypes.POINT, //
    DataTypes.MULTI_POINT, //
    DataTypes.LINEAR_RING, //
    DataTypes.LINE_STRING, //
    DataTypes.MULTI_LINE_STRING, //
    DataTypes.POLYGON, //
    DataTypes.MULTI_POLYGON);

  default GeometryWriter newGeometryWriter(final Object source) {
    final Resource resource = Resource.getResource(source);
    return newGeometryWriter(resource);
  }

  /**
   * Construct a new writer to write to the specified resource.
   *
   * @param resource The resource to write to.
   * @return The writer.
   */
  default GeometryWriter newGeometryWriter(final Resource resource) {
    final OutputStream out = resource.newBufferedOutputStream();
    final String baseName = resource.getBaseName();
    return newGeometryWriter(baseName, out);
  }

  default GeometryWriter newGeometryWriter(final String baseName, final OutputStream out) {
    return newGeometryWriter(baseName, out, StandardCharsets.UTF_8);

  }

  GeometryWriter newGeometryWriter(String baseName, OutputStream out, Charset charset);

}
