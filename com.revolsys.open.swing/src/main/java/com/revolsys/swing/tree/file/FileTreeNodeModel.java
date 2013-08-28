package com.revolsys.swing.tree.file;

import java.awt.Component;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;

import org.springframework.util.StringUtils;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class FileTreeNodeModel extends AbstractObjectTreeNodeModel<File, File> {
  public static final Icon ICON_FOLDER_DRIVE = SilkIconLoader.getIconWithBadge(
    "folder", "drive");

  public static final Icon ICON_FOLDER_MISSING = SilkIconLoader.getIconWithBadge(
    "folder", "error");

  public static final ImageIcon ICON_FOLDER_LINK = SilkIconLoader.getIconWithBadge(
    "folder", "link");

  public FileTreeNodeModel() {
    setSupportedClasses(File.class);
    setSupportedChildClasses(File.class);
    setObjectTreeNodeModels(this);
  }

  @Override
  protected List<File> getChildren(final File file) {
    if (file.isDirectory()) {
      final File[] files = file.listFiles();
      if (files != null) {
        return Arrays.asList(files);
      }
    }
    return Collections.emptyList();
  }

  @Override
  public Object getLabel(final File file) {
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
  public Component getRenderer(final File file, final JTree tree,
    final boolean selected, final boolean expanded, final boolean leaf,
    final int row, final boolean hasFocus) {
    final JLabel renderer = (JLabel)super.getRenderer(file, tree, selected,
      expanded, leaf, row, hasFocus);
    if (file.exists()) {
      final String name = file.getName();
      if (file.isDirectory()) {
        renderer.setIcon(ICON_FOLDER);
      } else if (StringUtils.hasText(name)) {
      } else {
        renderer.setIcon(ICON_FOLDER_DRIVE);
      }
    } else {
      renderer.setIcon(ICON_FOLDER_MISSING);
    }
    return renderer;
  }

  @Override
  public boolean isLeaf(final File file) {
    if (file.exists()) {
      return !file.isDirectory();
    } else {
      return StringUtils.hasText(file.getName());
    }
  }

}
