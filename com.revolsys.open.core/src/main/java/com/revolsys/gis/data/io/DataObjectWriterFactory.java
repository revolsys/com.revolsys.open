package com.revolsys.gis.data.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;

public interface DataObjectWriterFactory extends IoFactory {

  Writer<DataObject> createDataObjectWriter(
    DataObjectMetaData metaData,
    Resource resource);

  Writer<DataObject> createDataObjectWriter(
    String baseName,
    DataObjectMetaData metaData,
    OutputStream outputStream);

  Writer<DataObject> createDataObjectWriter(
    String baseName,
    DataObjectMetaData metaData,
    OutputStream outputStream,
    Charset charset);

  Set<CoordinateSystem> getCoordinateSystems();

  boolean isCoordinateSystemSupported(CoordinateSystem coordinateSystem);

  boolean isCustomAttributionSupported();

  boolean isGeometrySupported();

  boolean isSingleFile();
}
