package com.revolsys.swing.tree.file;

import java.io.File;
import java.util.List;

import com.revolsys.io.FileSystemConnectionManager;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class FileSystemConnectionManagerModel extends
  AbstractObjectTreeNodeModel<FileSystemConnectionManager, File> {

  public FileSystemConnectionManagerModel() {
    setSupportedClasses(FileSystemConnectionManager.class);
    setSupportedChildClasses(File.class);
    setObjectTreeNodeModels(new FileTreeNodeModel());
  }

  @Override
  protected List<File> getChildren(
    final FileSystemConnectionManager connectionManager) {
    final List<File> files = connectionManager.getFileSystems();
    return files;
  }
}
