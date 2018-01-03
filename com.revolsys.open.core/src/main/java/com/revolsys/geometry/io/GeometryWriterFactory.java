package com.revolsys.geometry.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;

import com.revolsys.collection.map.MapEx;
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

  GeometryWriter newGeometryWriter(final Resource resource, MapEx properties);

  GeometryWriter newGeometryWriter(String baseName, OutputStream out, Charset charset);

}
