package com.revolsys.data.io;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.ZipUtil;
import com.revolsys.io.filter.ExtensionFilenameFilter;

public class ZipDataObjectReader extends DelegatingReader<Record> implements
  DataObjectReader {
  private DataObjectReader reader;

  private File directory;

  public ZipDataObjectReader(final Resource resource,
    final String fileExtension, final RecordFactory factory) {
    try {
      final String baseName = FileUtil.getBaseName(resource.getFilename());
      final String zipEntryName = baseName + "." + fileExtension;
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
    } catch (final IOException e) {
      throw new RuntimeException("Error reading resource " + resource, e);
    }
  }

  @Override
  protected void doClose() {
    FileUtil.deleteDirectory(directory);
  }

  @Override
  public RecordDefinition getMetaData() {
    return reader.getMetaData();
  }

  protected boolean openFile(final Resource resource,
    final RecordFactory factory, final String zipEntryName) {
    final File file = new File(directory, zipEntryName);
    if (file.exists()) {
      final FileSystemResource fileResource = new FileSystemResource(file);
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
}
