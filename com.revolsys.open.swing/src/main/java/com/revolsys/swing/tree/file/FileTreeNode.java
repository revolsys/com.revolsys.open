package com.revolsys.swing.tree.file;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.springframework.util.StringUtils;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.swing.tree.datastore.FileDataObjectStoreTreeNode;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;
import com.revolsys.util.UrlProxy;
import com.revolsys.util.UrlUtil;

public class FileTreeNode extends LazyLoadTreeNode implements UrlProxy {
  public static List<TreeNode> getFileNodes(final TreeNode parent,
    final File file) {
    final File[] files = file.listFiles();
    return getFileNodes(parent, files);
  }

  public static List<TreeNode> getFileNodes(final TreeNode parent,
    final File[] files) {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    if (files != null) {
      for (final File childFile : files) {
        if (!childFile.isHidden()) {
          if (FileModel.isDataStore(childFile)) {
            final FileDataObjectStoreTreeNode dataStoreNode = new FileDataObjectStoreTreeNode(
              parent, childFile);
            children.add(dataStoreNode);
          } else {
            final FileTreeNode child = new FileTreeNode(parent, childFile);
            children.add(child);
          }
        }
      }
    }
    return children;
  }

  public static URL getUrl(final TreeNode parent, final File file) {
    if (parent instanceof UrlProxy) {
      final UrlProxy parentProxy = (UrlProxy)parent;
      String childPath = file.getName();
      if (file.isDirectory()) {
        childPath += "/";
      }
      return UrlUtil.getUrl(parentProxy, childPath);
    } else {
      return FileUtil.toUrl(file);
    }
  }

  public static boolean isAllowsChildren(final File file) {
    if (file == null) {
      return true;
    } else if (!file.exists()) {
      return false;
    } else if (file.isDirectory()) {
      return true;
    } else {
      return false;
    }
  }

  public FileTreeNode(final TreeNode parent, final File file) {
    super(parent, file);
    String fileName = file.getName();
    if (!StringUtils.hasText(fileName)) {
      fileName = "/";
    }
    setName(fileName);
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

  public File getFile() {
    return getUserObject();
  }

  @Override
  public Icon getIcon() {
    final File file = getUserObject();
    return FileModel.getIcon(file);
  }

  @Override
  public String getType() {
    final File file = getUserObject();
    if (file.isDirectory()) {
      return "Folder";
    } else if (file.exists()) {
      final String extension = FileUtil.getFileNameExtension(file);
      if (StringUtils.hasText(extension)) {
        final IoFactory factory = IoFactoryRegistry.getInstance()
          .getFactoryByFileExtension(IoFactory.class, extension);
        if (factory != null) {
          return factory.getName();
        }
      }
      return "File";
    } else {
      return "Missing File/Folder";
    }
  }

  @Override
  public URL getUrl() {
    final File file = getFile();
    final TreeNode parent = getParent();
    return getUrl(parent, file);
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
  protected List<TreeNode> doLoadChildren() {
    final File file = getUserObject();
    if (file.isDirectory()) {
      return getFileNodes(this, file);
    } else {
      return Collections.emptyList();
    }
  }
}
