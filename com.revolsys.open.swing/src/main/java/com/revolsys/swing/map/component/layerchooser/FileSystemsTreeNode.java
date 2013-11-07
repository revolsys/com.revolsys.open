package com.revolsys.swing.map.component.layerchooser;

import java.io.File;
import java.util.List;

import javax.swing.tree.TreeNode;

import com.revolsys.swing.tree.file.FileModel;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class FileSystemsTreeNode extends LazyLoadTreeNode {
  public FileSystemsTreeNode(final TreeNode parent) {
    super(null);
    setTitle("File Systems");
    setIcon(FileModel.ICON_FOLDER_DRIVE);
    setAllowsChildren(true);
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof FileSystemsTreeNode) {
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  protected List<TreeNode> loadChildren() {
    final File[] roots = File.listRoots();
    return FileTreeNode.getFileNodes(this, roots);
  }
}
