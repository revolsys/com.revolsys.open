package com.revolsys.swing.tree.file;

import java.io.File;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.tree.model.node.AbstractTreeNode;

public class FileSystemsTreeNode extends AbstractTreeNode {
  private List<TreeNode> children;
  public static final Icon ICON_FOLDER_DRIVE = SilkIconLoader.getIcon("folder_drive");

  public FileSystemsTreeNode(final TreeNode parent) {
    super(parent, null);
    setType("File Systems");
    setName("File Systems");
    setIcon(FileSystemsTreeNode.ICON_FOLDER_DRIVE);
    setAllowsChildren(true);
    init();
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof FileSystemsTreeNode) {
      return true;
    }
    return false;
  }

  @Override
  public List<TreeNode> getChildren() {
    return children;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  protected void init() {
    final File[] roots = File.listRoots();
    children = FileTreeNode.getFileNodes(this, roots);
  }
}
