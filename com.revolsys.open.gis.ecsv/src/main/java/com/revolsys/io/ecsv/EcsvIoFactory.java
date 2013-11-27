package com.revolsys.io.ecsv;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryIoFactory;
import com.revolsys.gis.data.io.DataObjectIteratorReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.DirectoryDataObjectStore;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;
import com.revolsys.spring.SpringUtil;

public class EcsvIoFactory extends AbstractDataObjectAndGeometryIoFactory
  implements DataObjectStoreFactory {
  public static DataObjectMetaData readSchema(final Resource resource) {
    final EcsvDataObjectIterator iterator = new EcsvDataObjectIterator(resource);
    try {
      iterator.init();
      final DataObjectMetaData metaData = iterator.getMetaData();
      return metaData;
    } finally {
      iterator.close();
    }
  }

  public static void writeSchema(final DataObjectMetaData metaData,
    final Resource resource) {
    final java.io.Writer out = SpringUtil.getWriter(resource);
    final EcsvDataObjectWriter writer = new EcsvDataObjectWriter(metaData, out);
    writer.open();
    writer.close();
  }

  public EcsvIoFactory() {
    super(EcsvConstants.DESCRIPTION, false, true);
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
  @Override
  public DataObjectReader createDataObjectReader(final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    final EcsvDataObjectIterator iterator = new EcsvDataObjectIterator(
      resource, dataObjectFactory);
    return new DataObjectIteratorReader(iterator);
  }

  @Override
  public DataObjectStore createDataObjectStore(
    final Map<String, ? extends Object> connectionProperties) {
    final String url = (String)connectionProperties.get("url");
    final Resource resource = SpringUtil.getResource(url);
    final File directory = SpringUtil.getFile(resource);
    return new DirectoryDataObjectStore(directory, EcsvConstants.FILE_EXTENSION);
  }

  @Override
  public Writer<DataObject> createDataObjectWriter(final String baseName,
    final DataObjectMetaData metaData, final OutputStream outputStream,
    final Charset charset) {

    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);

    return new EcsvDataObjectWriter(metaData, writer);
  }

  @Override
  public Class<? extends DataObjectStore> getDataObjectStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return DataObjectStore.class;
  }

  @Override
  public List<String> getUrlPatterns() {
    return null;
  }
}
