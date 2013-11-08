package com.revolsys.swing.tree.file;

import java.io.File;
import java.util.List;

import javax.swing.tree.TreeNode;

import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class FileSystemsTreeNode extends LazyLoadTreeNode {
  public FileSystemsTreeNode(final TreeNode parent) {
    super(parent, null);
    setType("File Systems");
    setName("File Systems");
    setIcon(FileTreeUtil.ICON_FOLDER_DRIVE);
    setAllowsChildren(true);
  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final File[] roots = File.listRoots();
    return FileTreeNode.getFileNodes(this, roots);
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
}
