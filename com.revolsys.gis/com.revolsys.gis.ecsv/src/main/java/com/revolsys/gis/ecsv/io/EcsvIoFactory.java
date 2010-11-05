package com.revolsys.gis.ecsv.io;

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

public class EcsvIoFactory extends
  AbstractDataObjectAndGeometryIoFactory {

  /** The factory instance. */
  public static final EcsvIoFactory INSTANCE = new EcsvIoFactory();

  public EcsvIoFactory() {
    super(EcsvConstants.DESCRIPTION, false);
    addMediaTypeAndFileExtension(EcsvConstants.MEDIA_TYPE,
      EcsvConstants.FILE_EXTENSION);
  }

  /**
   * Create a reader for the file using the specified data object factory.
   * 
   * @param file The file to read.
   * @param factory The factory used to create data objects.
   * @return The reader for the file.
   */
  public DataObjectReader createDataObjectReader(
    Resource resource,
    DataObjectFactory dataObjectFactory) {
    try {
      final EcsvDataObjectIterator iterator = new EcsvDataObjectIterator(resource, dataObjectFactory);
      return new DataObjectIteratorReader(iterator);
    } catch (IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
 }

  public Writer<DataObject> createDataObjectWriter(
    String baseName,
    DataObjectMetaData metaData,
    OutputStream outputStream,
    Charset charset) {
    return new EcsvDataObjectWriter(metaData, new OutputStreamWriter(outputStream,
      charset));
  }
}
