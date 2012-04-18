package com.revolsys.io.csv;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectIoFactory;
import com.revolsys.gis.data.io.DataObjectIteratorReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Writer;
import com.revolsys.spring.SpringUtil;

public class CsvDataObjectIoFactory extends AbstractDataObjectIoFactory
  implements DataObjectStoreFactory {
  public CsvDataObjectIoFactory() {
    super(CsvConstants.DESCRIPTION, false, true, true);
    addMediaTypeAndFileExtension(CsvConstants.MEDIA_TYPE,
      CsvConstants.FILE_EXTENSION);
  }

  public DataObjectReader createDataObjectReader(
    final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    final CsvDataObjectIterator iterator = new CsvDataObjectIterator(resource,
      dataObjectFactory);
    return new DataObjectIteratorReader(iterator);
  }

  public Writer<DataObject> createDataObjectWriter(
    final String baseName,
    final DataObjectMetaData metaData,
    final OutputStream outputStream,
    final Charset charset) {
    return new CsvDataObjectWriter(metaData, new OutputStreamWriter(
      outputStream, charset));
  }

  public DataObjectStore createDataObjectStore(
    Map<String, ? extends Object> connectionProperties) {
    String url = (String)connectionProperties.get("url");
    Resource resource = SpringUtil.getResource(url);
    File directory = SpringUtil.getFile(resource);
    return new CsvDataObjectStore(directory);
  }

  public Class<? extends DataObjectStore> getDataObjectStoreInterfaceClass(
    Map<String, ? extends Object> connectionProperties) {
    return DataObjectStore.class;
  }

  public List<String> getUrlPatterns() {
    // TODO Auto-generated method stub
    return null;
  }
}
