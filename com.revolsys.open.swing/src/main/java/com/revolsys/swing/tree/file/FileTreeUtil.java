package com.revolsys.swing.tree.file;

import java.io.File;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.springframework.util.StringUtils;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectReaderFactory;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageFactory;

public class FileTreeUtil {

  public static final Icon ICON_FILE_DATABASE = SilkIconLoader.getIcon("file_database");

  public static final Icon ICON_FILE = SilkIconLoader.getIcon("file");

  public static final Icon ICON_FOLDER = SilkIconLoader.getIcon("folder");

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

}
