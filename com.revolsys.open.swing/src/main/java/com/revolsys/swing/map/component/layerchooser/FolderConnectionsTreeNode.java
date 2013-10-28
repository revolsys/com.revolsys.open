package com.revolsys.swing.map.component.layerchooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import com.revolsys.io.FileUtil;
import com.revolsys.io.file.FolderConnection;
import com.revolsys.io.file.FolderConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.tree.file.FileModel;
import com.revolsys.swing.tree.model.node.AbstractTreeNode;

public class FolderConnectionsTreeNode extends AbstractTreeNode {

  public FolderConnectionsTreeNode(final TreeNode parent) {
    super(parent, null);
  }

  public FolderConnectionsTreeNode(final TreeNode parent, final File file) {
    super(parent, file);
  }

  public FolderConnectionsTreeNode(final TreeNode parent,
    final FolderConnection connection) {
    super(parent, connection);
  }

  public FolderConnectionsTreeNode(final TreeNode parent,
    final FolderConnectionRegistry registry) {
    super(parent, registry);
  }

  @Override
  protected List<TreeNode> getChildren() {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    final Object object = getUserObject();
    if (object == null) {
      final List<FolderConnectionRegistry> registries = FolderConnectionManager.get()
        .getVisibleConnectionRegistries();
      for (final FolderConnectionRegistry childRegistry : registries) {
        final FolderConnectionsTreeNode child = new FolderConnectionsTreeNode(
          this, childRegistry);
        children.add(child);
      }
    } else if (object instanceof FolderConnectionRegistry) {
      final FolderConnectionRegistry registry = (FolderConnectionRegistry)object;
      final List<FolderConnection> conections = registry.getConections();
      for (final FolderConnection connection : conections) {
        final FolderConnectionsTreeNode child = new FolderConnectionsTreeNode(
          this, connection);
        children.add(child);
      }
    } else if (object instanceof FolderConnection) {
      final FolderConnection connection = (FolderConnection)object;
      return getFileNodes(connection.getFile());
    } else {
      final File file = (File)object;
      return getFileNodes(file);
    }
    return children;
  }

  public List<TreeNode> getFileNodes(final File file) {
    final List<File> files = FileUtil.listVisibleFiles(file);
    final List<TreeNode> children = new ArrayList<TreeNode>();
    if (files != null) {
      for (final File childFile : files) {
        if (!childFile.isHidden()) {
          if (childFile.isDirectory() || FileModel.isDataStore(childFile)) {
            final TreeNode child = new FolderConnectionsTreeNode(this,
              childFile);
            children.add(child);
          }
        }
      }
    }
    return children;
  }

  @Override
  public Icon getIcon() {
    Icon icon;
    final Object object = getUserObject();
    if (object == null) {
      icon = FileModel.ICON_FOLDER_LINK;
    } else if (object instanceof FolderConnectionRegistry) {
      icon = FileModel.ICON_FOLDER_LINK;
    } else if (object instanceof FolderConnection) {
      final FolderConnection connection = (FolderConnection)object;
      final File file = connection.getFile();
      if (file == null || !file.exists()) {
        icon = FileModel.ICON_FOLDER_MISSING;
      } else {
        icon = FileModel.ICON_FOLDER_LINK;
      }
    } else {
      final File file = (File)object;
      icon = FileModel.getIcon(file);
    }
    return icon;
  }

  @Override
  public boolean isAllowsChildren() {
    final Object object = getUserObject();
    if (object == null) {
      return true;
    } else if (object instanceof FolderConnectionRegistry) {
      return true;
    } else if (object instanceof FolderConnection) {
      return true;
    } else {
      final File file = (File)object;
      return FileTreeNode.isAllowsChildren(file);
    }
  }

  @Override
  public String toString() {
    String string;
    final Object object = getUserObject();
    if (object == null) {
      string = "Folder Connections";
    } else if (object instanceof FolderConnectionRegistry) {
      final FolderConnectionRegistry registry = (FolderConnectionRegistry)object;
      string = registry.getName();
    } else if (object instanceof FolderConnection) {
      final FolderConnection connection = (FolderConnection)object;
      final File file = connection.getFile();
      string = FileUtil.getFileName(file);
    } else {
      final File file = (File)object;
      string = FileUtil.getFileName(file);
    }
    return string;
  }
}
