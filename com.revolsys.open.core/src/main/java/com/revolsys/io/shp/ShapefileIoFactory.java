package com.revolsys.io.shp;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryIoFactory;
import com.revolsys.gis.data.io.DataObjectIteratorReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Writer;
import com.revolsys.spring.OutputStreamResource;

public class ShapefileIoFactory extends AbstractDataObjectAndGeometryIoFactory {
  public ShapefileIoFactory() {
    super(ShapefileConstants.DESCRIPTION, true, true);
    addMediaTypeAndFileExtension(ShapefileConstants.MIME_TYPE,
      ShapefileConstants.FILE_EXTENSION);
    setSingleFile(false);
  }

  public DataObjectReader createDataObjectReader(
    final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    try {
      final ShapefileIterator iterator = new ShapefileIterator(resource,
        dataObjectFactory);
      return new DataObjectIteratorReader(iterator);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  @Override
  public Writer<DataObject> createDataObjectWriter(
    final DataObjectMetaData metaData,
    final Resource resource) {
    try {
      return new ShapefileDataObjectWriter(metaData, resource);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create writer for " + resource, e);
    }
  }

  public Writer<DataObject> createDataObjectWriter(
    final String baseName,
    final DataObjectMetaData metaData,
    final OutputStream outputStream,
    final Charset charset) {
    return createDataObjectWriter(metaData, new OutputStreamResource(baseName,
      outputStream));
  }
}
