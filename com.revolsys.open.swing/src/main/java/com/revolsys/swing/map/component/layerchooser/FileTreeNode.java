package com.revolsys.swing.map.component.layerchooser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.springframework.util.StringUtils;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.filter.DirectoryFilenameFilter;
import com.revolsys.swing.tree.file.FileModel;
import com.revolsys.swing.tree.model.node.AbstractTreeNode;

public class FileTreeNode extends AbstractTreeNode {
  public static List<TreeNode> getFileNodes(final TreeNode parent,
    final File[] files) {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    if (files != null) {
      for (final File childFile : files) {
        if (!childFile.isHidden()) {
          if (childFile.isDirectory() || FileModel.isDataStore(childFile)) {
            final FileTreeNode child = new FileTreeNode(parent, childFile);
            children.add(child);
          }
        }
      }
    }
    return children;
  }

  public static boolean isAllowsChildren(final File file) {
    if (file == null) {
      return true;
    } else if (!file.exists()) {
      return false;
    } else if (file.isDirectory()) {
      return true;
    } else {
      return FileModel.isDataStore(file);
    }
  }

  public FileTreeNode(final TreeNode parent) {
    this(parent, null);
  }

  public FileTreeNode(final TreeNode parent, final File file) {
    super(parent, file);
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof FileTreeNode) {
      final FileTreeNode fileNode = (FileTreeNode)other;
      final File file = getUserObject();
      final File otherFile = fileNode.getUserObject();
      return EqualsRegistry.equal(file, otherFile);
    }
    return false;
  }

  @Override
  public int getChildCount() {
    int count = 0;
    final File file = getUserObject();
    if (file == null) {
      final File[] roots = File.listRoots();
      if (roots != null) {
        count += roots.length;
      }
    } else if (FileModel.isDataStore(file)) {
      // TODO connect to store, cache it
    } else if (!file.exists()) {
    } else if (file.isDirectory()) {

      final File[] files = file.listFiles(new DirectoryFilenameFilter());
      for (final File childFile : files) {
        if (!childFile.isHidden()) {
          if (childFile.isDirectory() || FileModel.isDataStore(childFile)) {
            count++;
          }
        }
      }
    }
    return count;
  }

  @Override
  protected List<TreeNode> getChildren() {
    List<TreeNode> children;
    final File file = getUserObject();
    if (file == null) {
      final File[] roots = File.listRoots();
      children = getFileNodes(this, roots);
    } else if (FileModel.isDataStore(file)) {
      // TODO connect to store, cache it
      children = Collections.emptyList();
    } else if (!file.exists()) {
      children = Collections.emptyList();
    } else if (file.isDirectory()) {
      final File[] files = file.listFiles();
      children = getFileNodes(this, files);
    } else {
      children = Collections.emptyList();
    }
    return children;
  }

  public File getFile() {
    return (File)getUserObject();
  }

  @Override
  public Icon getIcon() {
    final File file = getUserObject();
    return FileModel.getIcon(file);
  }

  @Override
  public int hashCode() {
    final File file = getUserObject();
    if (file == null) {
      return 0;
    } else {
      return file.hashCode();
    }
  }

  @Override
  public boolean isAllowsChildren() {
    final File file = getUserObject();
    return isAllowsChildren(file);
  }

  @Override
  public String toString() {
    final File file = getUserObject();
    if (file == null) {
      return "File Systems";
    } else {
      final String name = file.getName();
      if (StringUtils.hasText(name)) {
        return name;
      } else {
        return "/";
      }
    }
  }
}
