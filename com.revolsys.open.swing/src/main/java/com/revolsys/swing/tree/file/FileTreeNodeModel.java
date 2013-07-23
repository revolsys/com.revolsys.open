package com.revolsys.swing.tree.file;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

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
        return "/ (ROOT)";
      }
    }
  }

  @Override
  public TreeCellRenderer getRenderer(final File file) {
    final DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)super.getRenderer(file);
    if (!file.exists()) {
      renderer.setOpenIcon(ICON_FOLDER_MISSING);
      renderer.setClosedIcon(ICON_FOLDER_MISSING);
    } else if (file.getParentFile() == null) {
      renderer.setOpenIcon(ICON_FOLDER_DRIVE);
      renderer.setClosedIcon(ICON_FOLDER_DRIVE);
    } else if (file.isDirectory()) {
      renderer.setOpenIcon(ICON_FOLDER);
      renderer.setClosedIcon(ICON_FOLDER);
    } else {
    }
    return renderer;
  }

  @Override
  public boolean isLeaf(final File file) {
    return !file.isDirectory();
  }

}
