package com.revolsys.io.cogojson;

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
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;
import com.revolsys.io.geojson.GeoJsonConstants;
import com.revolsys.io.geojson.GeoJsonDataObjectWriter;
import com.revolsys.io.geojson.GeoJsonGeometryIterator;

public class CogoJsonIoFactory extends
  AbstractDataObjectAndGeometryWriterFactory implements GeometryReaderFactory {

  public CogoJsonIoFactory() {
    super(GeoJsonConstants.COGO_DESCRIPTION, true, true);
    addMediaTypeAndFileExtension(GeoJsonConstants.COGO_MEDIA_TYPE,
      GeoJsonConstants.COGO_FILE_EXTENSION);
  }

  @Override
  public Writer<DataObject> createDataObjectWriter(final String baseName,
    final DataObjectMetaData metaData, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);
    return new GeoJsonDataObjectWriter(writer, true);
  }

  @Override
  public GeometryReader createGeometryReader(final Resource resource) {
    try {
      final GeoJsonGeometryIterator iterator = new GeoJsonGeometryIterator(
        resource);
      return new GeometryReader(iterator);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  @Override
  public boolean isBinary() {
    return false;
  }
}
