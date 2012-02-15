package com.revolsys.io.geojson;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryWriterFactory;
import com.revolsys.gis.data.io.GeometryReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.revolsys.io.Writer;

public class GeoJsonIoFactory extends
  AbstractDataObjectAndGeometryWriterFactory implements GeometryReaderFactory {

  public GeoJsonIoFactory() {
    super(GeoJsonConstants.DESCRIPTION, true, true);
    addMediaTypeAndFileExtension(GeoJsonConstants.MEDIA_TYPE,
      GeoJsonConstants.FILE_EXTENSION);
  }

  public Writer<DataObject> createDataObjectWriter(
    final String baseName,
    final DataObjectMetaData metaData,
    final OutputStream outputStream,
    final Charset charset) {
    return new GeoJsonDataObjectWriter(new OutputStreamWriter(outputStream,
      charset));
  }

  public GeometryReader createGeometryReader(final Resource resource) {
    try {
      final GeoJsonGeometryIterator iterator = new GeoJsonGeometryIterator(
        resource);
      return new GeometryReader(iterator);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  public boolean isBinary() {
    return false;
  }
}
