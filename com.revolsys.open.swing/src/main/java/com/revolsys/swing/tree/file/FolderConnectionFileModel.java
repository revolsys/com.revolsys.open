package com.revolsys.swing.tree.file;

import java.awt.Component;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;

import org.springframework.util.StringUtils;

import com.revolsys.io.file.FolderConnectionFile;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class FolderConnectionFileModel extends
  AbstractObjectTreeNodeModel<FolderConnectionFile, FolderConnectionFile> {

  public FolderConnectionFileModel() {
    setSupportedClasses(FolderConnectionFile.class);
    setSupportedChildClasses(FolderConnectionFile.class);
    setObjectTreeNodeModels(this);
  }

  @Override
  protected List<FolderConnectionFile> getChildren(
    final FolderConnectionFile file) {
    final List<FolderConnectionFile> files = file.getFiles();
    return files;
  }

  @Override
  public Object getLabel(final FolderConnectionFile file) {
    if (file == null) {
      return file;
    } else {
      final String name = file.getName();
      if (StringUtils.hasText(name)) {
        return name;
      } else {
        final String path = file.getPath();
        if (path.equals("/")) {
          return "/";
        } else {
          return path.replaceAll("//", "");
        }
      }
    }
  }

  @Override
  public Component getRenderer(final FolderConnectionFile file,
    final JTree tree, final boolean selected, final boolean expanded,
    final boolean leaf, final int row, final boolean hasFocus) {
    final JLabel renderer = (JLabel)super.getRenderer(file, tree, selected,
      expanded, leaf, row, hasFocus);
    final Icon icon = FileModel.getIcon(file.getFile());
    renderer.setIcon(icon);
    return renderer;
  }

  @Override
  public boolean isLeaf(final FolderConnectionFile file) {
    if (file.exists()) {
      return !file.isDirectory();
    } else {
      return StringUtils.hasText(file.getName());
    }
  }

}
