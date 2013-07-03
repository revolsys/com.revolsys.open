package com.revolsys.swing.tree.file;

import java.io.File;
import java.util.List;

import com.revolsys.io.FolderConnectionManager;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class FileSystemConnectionManagerModel extends
  AbstractObjectTreeNodeModel<FolderConnectionManager, File> {

  public FileSystemConnectionManagerModel() {
    setSupportedClasses(FolderConnectionManager.class);
    setSupportedChildClasses(File.class);
    setObjectTreeNodeModels(new FileTreeNodeModel());
  }

  @Override
  protected List<File> getChildren(
    final FolderConnectionManager connectionManager) {
    final List<File> files = connectionManager.getFileSystems();
    return files;
  }
}
