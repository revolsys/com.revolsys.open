package com.revolsys.gis.data.io;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;

public class DataObjectDirectoryReader extends
  AbstractDirectoryReader<DataObject> {

  public DataObjectDirectoryReader() {
  }

  protected Reader<DataObject> createReader(
    Resource resource) {
    System.out.println(resource);
    final IoFactoryRegistry registry = IoFactoryRegistry.INSTANCE;
    final String filename = resource.getFilename();
    final String extension = FileUtil.getFileNameExtension(filename);
    final DataObjectReaderFactory factory = registry.getFactoryByFileExtension(
      DataObjectReaderFactory.class, extension);
    final Reader<DataObject> reader = factory.createDataObjectReader(resource);
    return reader;
  }

}
