package com.revolsys.swing.tree.file;

import java.io.File;
import java.util.List;

import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.io.FileSystemConnectionManager;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class FileSystemConnectionManagerModel extends
  AbstractObjectTreeNodeModel<FileSystemConnectionManager, File> {

  public FileSystemConnectionManagerModel() {
    setSupportedClasses(FileSystemConnectionManager.class);
    setSupportedChildClasses(File.class);
    setObjectTreeNodeModels(new FileTreeNodeModel());
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setOpenIcon(FileTreeNodeModel.ICON_FOLDER_DRIVE);
    renderer.setClosedIcon(FileTreeNodeModel.ICON_FOLDER_DRIVE);
  }

  @Override
  protected List<File> getChildren(
    final FileSystemConnectionManager connectionManager) {
    final List<File> files = connectionManager.getFileSystems();
    return files;
  }
}
