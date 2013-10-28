package com.revolsys.swing.tree.file;

import java.awt.Component;
import java.io.File;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;

import org.springframework.util.StringUtils;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectReaderFactory;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageFactory;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class FileModel extends AbstractObjectTreeNodeModel<File, File> {

  public static final Icon ICON_FILE_DATABASE = SilkIconLoader.getIcon("file_database");

  public static final Icon ICON_FILE = SilkIconLoader.getIcon("file");

  public static final Icon ICON_FILE_VECTOR = SilkIconLoader.getIcon("file_table");

  public static final Icon ICON_FILE_TABLE = SilkIconLoader.getIcon("file_table");

  public static final Icon ICON_FILE_IMAGE = SilkIconLoader.getIcon("file_image");

  public static final Icon ICON_FOLDER_DRIVE = SilkIconLoader.getIcon("folder_drive");

  public static final Icon ICON_FOLDER_MISSING = SilkIconLoader.getIcon("folder_error");

  public static final ImageIcon ICON_FOLDER_LINK = SilkIconLoader.getIcon("folder_link");

  public static Icon getIcon(final File file) {
    if (file == null) {
      return ICON_FOLDER_DRIVE;
    } else if (file.exists()) {
      final String name = file.getName();
      if (isDataStore(file)) {
        return ICON_FILE_DATABASE;
      } else if (isImage(file)) {
        return ICON_FILE_IMAGE;
      } else if (isVector(file)) {
        return ICON_FILE_TABLE;
      } else if (file.isDirectory()) {
        return ICON_FOLDER;
      } else if (StringUtils.hasText(name)) {
        return ICON_FILE;
      } else {
        return ICON_FOLDER_DRIVE;
      }
    } else {
      return ICON_FOLDER_MISSING;
    }
  }

  public static boolean isDataStore(final File file) {
    final Set<String> fileExtensions = DataObjectStoreFactoryRegistry.getFileExtensions();
    final String extension = FileUtil.getFileNameExtension(file).toLowerCase();
    return fileExtensions.contains(extension);
  }

  public static boolean isImage(final File file) {
    final String fileNameExtension = FileUtil.getFileNameExtension(file);
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    return ioFactoryRegistry.isFileExtensionSupported(
      GeoReferencedImageFactory.class, fileNameExtension);
  }

  public static boolean isVector(final File file) {
    final String fileNameExtension = FileUtil.getFileNameExtension(file);
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    return ioFactoryRegistry.isFileExtensionSupported(
      DataObjectReaderFactory.class, fileNameExtension);
  }

  public FileModel() {
    setSupportedClasses(File.class);
    setSupportedChildClasses(File.class);
    setObjectTreeNodeModels(this);
  }

  @Override
  protected List<File> getChildren(final File file) {
    return FileUtil.listVisibleFiles(file);
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

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getParent(final File node) {
    if (node == null) {
      return null;
    } else {
      return (T)node.getParentFile();
    }
  }

  @Override
  public Component getRenderer(final File file, final JTree tree,
    final boolean selected, final boolean expanded, final boolean leaf,
    final int row, final boolean hasFocus) {
    final JLabel renderer = (JLabel)super.getRenderer(file, tree, selected,
      expanded, leaf, row, hasFocus);
    final Icon icon = getIcon(file);
    renderer.setIcon(icon);
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
