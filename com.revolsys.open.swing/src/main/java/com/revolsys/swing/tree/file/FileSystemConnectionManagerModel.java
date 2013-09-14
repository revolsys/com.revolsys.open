package com.revolsys.swing.tree.file;

import java.io.File;
import java.util.List;

import javax.swing.tree.DefaultTreeCellRenderer;

import com.revolsys.io.file.FileSystemConnectionManager;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class FileSystemConnectionManagerModel extends
  AbstractObjectTreeNodeModel<FileSystemConnectionManager, File> {

  public FileSystemConnectionManagerModel() {
    setSupportedClasses(FileSystemConnectionManager.class);
    setSupportedChildClasses(File.class);
    setObjectTreeNodeModels(new FileModel());
    final DefaultTreeCellRenderer renderer = getRenderer();
    renderer.setOpenIcon(FileModel.ICON_FOLDER_DRIVE);
    renderer.setClosedIcon(FileModel.ICON_FOLDER_DRIVE);
  }

  @Override
  protected List<File> getChildren(
    final FileSystemConnectionManager connectionManager) {
    final List<File> files = connectionManager.getFileSystems();
    return files;
  }
}
