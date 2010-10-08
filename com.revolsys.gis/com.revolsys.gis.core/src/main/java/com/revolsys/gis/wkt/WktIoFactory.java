package com.revolsys.gis.wkt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryIoFactory;
import com.revolsys.gis.data.io.DataObjectIteratorReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Writer;

public class WktIoFactory extends AbstractDataObjectAndGeometryIoFactory
  implements WktConstants {

  /** The factory instance. */
  public static final WktIoFactory INSTANCE = new WktIoFactory();

  public WktIoFactory() {
    super(WktConstants.DESCRIPTION, false);
    addMediaTypeAndFileExtension(MEDIA_TYPE, FILE_EXTENSION);
  }

  public Writer<DataObject> createDataObjectWriter(
    String baseName,
    DataObjectMetaData metaData,
    OutputStream outputStream,
    Charset charset) {
    return new WktDataObjectWriter(metaData, new OutputStreamWriter(
      outputStream, charset));
  }

  public DataObjectReader createDataObjectReader(
    Resource resource,
    DataObjectFactory factory) {
    try {
      final WktDataObjectIterator iterator = new WktDataObjectIterator(factory, resource);

      return new DataObjectIteratorReader(iterator);
    } catch (IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

}
