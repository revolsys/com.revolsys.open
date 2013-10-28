package com.revolsys.io.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.FileUtil;

public class FolderConnectionFile {
  private final FolderConnection folderConnection;

  private final File file;

  public FolderConnectionFile(final FolderConnection folderConnection,
    final File file) {
    this.folderConnection = folderConnection;
    this.file = file;
  }

  @Override
  public boolean equals(final Object obj) {
    if (EqualsRegistry.equal(folderConnection, folderConnection)) {
      if (EqualsRegistry.equal(file, file)) {
        return true;
      }
    }
    return false;
  }

  public boolean exists() {
    return file.exists();
  }

  public File getFile() {
    return file;
  }

  public List<FolderConnectionFile> getFiles() {
    final List<FolderConnectionFile> fileConnections = new ArrayList<FolderConnectionFile>();
    final List<File> files = FileUtil.listVisibleFiles(file);
    for (final File childFile : files) {
      final FolderConnectionFile fileConnection = new FolderConnectionFile(
        folderConnection, childFile);
      fileConnections.add(fileConnection);
    }
    return fileConnections;
  }

  public FolderConnection getFolderConnection() {
    return folderConnection;
  }

  public String getName() {
    return file.getName();
  }

  public String getPath() {
    return file.getPath();
  }

  @Override
  public int hashCode() {
    return file.hashCode();
  }

  public boolean isDirectory() {
    return file.isDirectory();
  }

  @Override
  public String toString() {
    return file.getName();
  }
}
