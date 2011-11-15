package com.revolsys.gis.data.io;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.FileUtil;
import com.revolsys.io.ZipUtil;
import com.revolsys.io.filter.ExtensionFilenameFilter;

public class ZipDataObjectReader extends DelegatingReader<DataObject> implements
  DataObjectReader {
  private DataObjectReader reader;

  private File directory;

  public ZipDataObjectReader(Resource resource, String fileExtension,
    DataObjectFactory factory) {
    try {
      final String baseName = FileUtil.getBaseName(resource.getFilename());
      String zipEntryName = baseName + "." + fileExtension;
      directory = ZipUtil.unzipFile(resource);
      if (!openFile(resource, factory, zipEntryName)) {
        final String[] files = directory.list(new ExtensionFilenameFilter(
          fileExtension));
        if (files != null && files.length == 1) {
          openFile(resource, factory, files[0]);
        }
      }
      if (reader == null) {
        close();
        throw new IllegalArgumentException("No *." + fileExtension
          + " exists in zip file " + resource);
      } else {
        setReader(reader);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error reading resource " + resource, e);
    }
  }

  protected boolean openFile(Resource resource, DataObjectFactory factory,
    final String zipEntryName) {
    File file = new File(directory, zipEntryName);
    if (file.exists()) {
      FileSystemResource fileResource = new FileSystemResource(file);
      reader = AbstractDataObjectAndGeometryReaderFactory.dataObjectReader(
        fileResource, factory);
      if (reader == null) {
        close();
        throw new IllegalArgumentException("Cannot create reader for file "
          + zipEntryName + " in zip file " + resource);
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  protected void doClose() {
    FileUtil.deleteDirectory(directory);
  }

  public DataObjectMetaData getMetaData() {
    return reader.getMetaData();
  }
}
