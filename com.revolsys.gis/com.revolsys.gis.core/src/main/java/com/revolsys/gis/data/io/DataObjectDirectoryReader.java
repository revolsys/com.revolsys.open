package com.revolsys.gis.data.io;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;

public class DataObjectDirectoryReader extends
  AbstractDirectoryReader<DataObject> implements DataObjectMetaDataFactory {

  private Map<QName, DataObjectMetaData> typeNameMetaDataMap = new HashMap<QName, DataObjectMetaData>();

  public DataObjectDirectoryReader() {
  }

  protected Reader<DataObject> createReader(Resource resource) {
    final IoFactoryRegistry registry = IoFactoryRegistry.INSTANCE;
    final String filename = resource.getFilename();
    final String extension = FileUtil.getFileNameExtension(filename);
    final DataObjectReaderFactory factory = registry.getFactoryByFileExtension(
      DataObjectReaderFactory.class, extension);
    final DataObjectReader reader = factory.createDataObjectReader(resource);
    addMetaData(reader);
    return reader;
  }

  protected void addMetaData(final DataObjectReader reader) {
    final DataObjectMetaData metaData = reader.getMetaData();
    if (metaData != null) {
      QName typeName = metaData.getName();
      typeNameMetaDataMap.put(typeName, metaData);
    }
  }

  public DataObjectMetaData getMetaData(QName typeName) {
    DataObjectMetaData metaData = typeNameMetaDataMap.get(typeName);
    return metaData;
  }

}
