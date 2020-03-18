package com.revolsys.swing.tree.node.file;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.logging.Logs;
import org.jeometry.common.net.UrlProxy;

import com.revolsys.connection.file.FileConnectionManager;
import com.revolsys.connection.file.FolderConnectionRegistry;
import com.revolsys.elevation.cloud.PointCloudReadFactory;
import com.revolsys.elevation.gridded.GriddedElevationModelReaderFactory;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkReaderFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.file.Paths;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageReadFactory;
import com.revolsys.record.io.RecordIo;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordReaderFactory;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.swing.Borders;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.swing.tree.node.FunctionChildrenTreeNode;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;
import com.revolsys.swing.tree.node.record.PathRecordStoreTreeNode;
import com.revolsys.util.Property;

public class PathTreeNode extends LazyLoadTreeNode implements UrlProxy {
  private static JFileChooser chooser;

  public static final MenuFactory MENU = new MenuFactory("File");

  static {
    addRefreshMenuItem(MENU);

    TreeNodes
      .addMenuItem(MENU, "record", "Export Records", "table:save",
        PathTreeNode::actionExportRecords) //
      .setVisibleCheck(TreeNodes.enableCheck(PathTreeNode::isRecordFileLayer));

    TreeNodes
      .addMenuItem(MENU, "folder", "Add Folder Connection", "link:add",
        PathTreeNode::actionAddFolderConnection)//
      .setVisibleCheck(TreeNodes.enableCheck(PathTreeNode::isDirectory));
  }

  private static Icon iconFile;

  private static final Map<String, Icon> ICON_FILE_MAP = new HashMap<>();

  private static Icon iconFolder;

  private static final Map<String, Icon> ICON_FOLDER_MAP = new HashMap<>();

  public static void addPathNode(final List<BaseTreeNode> children, final Path path,
    final boolean showHidden) {
    if (showHidden || !Paths.isHidden(path) && Files.exists(path)) {
      final BaseTreeNode child = newTreeNode(path);
      children.add(child);
    }
  }

  private synchronized static JFileChooser getChooser() {
    if (chooser == null) {
      Invoke.andWait(() -> chooser = new JFileChooser());
    }
    return chooser;
  }

  private static Icon getIcon(final File file) {
    final JFileChooser chooser = getChooser();
    return chooser.getIcon(file);
  }

  public static Icon getIcon(final Path path) {
    if (path == null) {
      return getIconFolder("error");
    } else {
      if (GeoreferencedImage.isReadable(path)) {
        return getIconFile("picture");
      } else if (RecordReader.isReadable(path)) {
        return getIconFile("table");
      }
      File file;
      try {
        file = path.toFile();
        final Icon icon = getIcon(file);
        return icon;
      } catch (final UnsupportedOperationException e) {
        if (Files.isDirectory(path)) {
          return getIconFolder();
        }
        final String fileNameExtension = Paths.getFileNameExtension(path);
        file = FileUtil.newTempFile("1234567890", fileNameExtension);
        final Icon icon = getIcon(file);
        file.delete();
        return icon;
      }
    }
  }

  public static Icon getIconFile() {
    if (iconFile == null) {
      final JFileChooser chooser = getChooser();
      iconFile = chooser.getIcon(FileUtil.newTempFile("xxxx", "6z4gsdj"));
    }
    return iconFile;
  }

  public static Icon getIconFile(final String badgeName) {
    Icon icon = ICON_FILE_MAP.get(badgeName);
    if (icon == null) {
      final Icon iconFile = getIconFile();
      icon = Icons.getIconWithBadge(iconFile, badgeName);
      ICON_FILE_MAP.put(badgeName, icon);
    }
    return icon;
  }

  public static Icon getIconFolder() {
    if (iconFolder == null) {
      iconFolder = Icons.getIcon("folder");
    }
    return iconFolder;
  }

  public static Icon getIconFolder(final String badgeName) {
    Icon icon = ICON_FOLDER_MAP.get(badgeName);
    if (icon == null) {
      final Icon iconFolder = getIconFolder();
      icon = Icons.getIconWithBadge(iconFolder, badgeName);
      ICON_FOLDER_MAP.put(badgeName, icon);
    }
    return icon;
  }

  public static List<BaseTreeNode> getPathNodes(final Iterable<Path> paths,
    final boolean showHidden) {
    final List<BaseTreeNode> children = new ArrayList<>();
    if (paths != null) {
      for (final Path path : paths) {
        addPathNode(children, path, showHidden);
      }
    }
    Collections.sort(children);
    return children;
  }

  public static List<BaseTreeNode> getPathNodes(final Path path) {
    if (Files.isDirectory(path)) {
      try (
        final DirectoryStream<Path> children = Files.newDirectoryStream(path)) {
        return getPathNodes(children, false);
      } catch (final AccessDeniedException e) {
      } catch (final IOException e) {
        Logs.debug(PathTreeNode.class, "Unable to get children " + path);
      }
    }
    return Collections.emptyList();
  }

  public static URL getUrl(final BaseTreeNode parent, final Path path) {
    if (parent instanceof UrlProxy) {
      final UrlProxy parentProxy = (UrlProxy)parent;
      return parentProxy.getUrl(path);
    } else {
      try {
        final URL url = path.toUri().toURL();
        return url;
      } catch (final MalformedURLException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  public static boolean isAllowsChildren(final Path path) {
    if (path == null) {
      return true;
    } else if (!Paths.exists(path)) {
      return false;
    } else if (Files.isDirectory(path)) {
      return true;
    } else {
      return false;
    }
  }

  public static BaseTreeNode newFileSystemsTreeNode() {
    final BaseTreeNode fileSystems = new FunctionChildrenTreeNode(FileSystems.getDefault(),
      "File Systems", getIconFolder("drive"), (fileSystem) -> {
        final Iterable<Path> roots = ((FileSystem)fileSystem).getRootDirectories();
        return getPathNodes(roots, true);
      });
    fileSystems.setOpen(true);
    return fileSystems;
  }

  public static BaseTreeNode newTreeNode(final Path path) {
    if (RecordStore.isRecordStore(path)) {
      return new PathRecordStoreTreeNode(path);
    } else {
      final List<String> fileNameExtensions = Paths.getFileNameExtensions(path);
      if (fileNameExtensions.contains("zip") || fileNameExtensions.contains("jar")) {
        try {
          return new SingleFileSystemTreeNode(path);
        } catch (final Throwable e) {
          return new PathTreeNode(path);
        }
      } else {
        return new PathTreeNode(path);
      }
    }
  }

  private boolean exists;

  private boolean hasFile;

  public PathTreeNode(final Path path) {
    super(path);
    final String fileName = Paths.getFileName(path);
    setName(fileName);
    refreshFields();
  }

  private void actionAddFolderConnection() {
    if (isDirectory()) {
      final Path path = getPath();
      final String fileName = getName();

      final ValueField panel = new ValueField();
      panel.setTitle("Add Folder Connection");
      Borders.titled(panel, "Folder Connection");

      SwingUtil.addLabel(panel, "Folder");
      final JLabel fileLabel = new JLabel(path.toString());
      panel.add(fileLabel);

      SwingUtil.addLabel(panel, "Name");
      final TextField nameField = new TextField(20);
      panel.add(nameField);
      nameField.setText(fileName);

      SwingUtil.addLabel(panel, "Folder Connections");
      final List<FolderConnectionRegistry> registries = new ArrayList<>();
      for (final FolderConnectionRegistry registry : FileConnectionManager.get()
        .getVisibleConnectionRegistries()) {
        if (!registry.isReadOnly()) {
          registries.add(registry);
        }
      }
      final ComboBox<FolderConnectionRegistry> registryField = ComboBox.newComboBox("registry",
        new Vector<>(registries));

      panel.add(registryField);

      GroupLayouts.makeColumns(panel, 2, true);
      panel.showDialog();
      if (panel.isSaved()) {
        final FolderConnectionRegistry registry = registryField.getSelectedItem();
        String connectionName = nameField.getText();
        if (!Property.hasValue(connectionName)) {
          connectionName = fileName;
        }

        registry.addConnection(connectionName, path);
      }
    }
  }

  private void actionExportRecords() {
    final Path path = getPath();
    boolean hasGeometryField;
    try (
      RecordReader reader = RecordReader.newRecordReader(path)) {
      if (reader == null) {
        return;
      } else {
        final RecordDefinition recordDefinition = reader.getRecordDefinition();
        if (recordDefinition == null) {
          return;
        } else {
          hasGeometryField = recordDefinition.hasGeometryField();
        }
      }
    }
    final String title = Paths.getBaseName(path);
    AbstractRecordLayer.exportRecords(title, hasGeometryField,
      targetFile -> RecordIo.copyRecords(path, targetFile));
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof PathTreeNode) {
      if (getClass() == other.getClass()) {
        final PathTreeNode fileNode = (PathTreeNode)other;
        if (isExists() == fileNode.isExists()) {
          final Path path = getPath();
          final Path otherPath = fileNode.getPath();
          final boolean equal = DataType.equal(path, otherPath);
          return equal;
        }
      }
    }
    return false;
  }

  // @Override
  // public String getType() {
  // final Path path = getPath();
  // if (Files.isDirectory(path)) {
  // return "Folder";
  // } else if (Files.exists(path)) {
  // final String extension = Paths.getFileNameExtension(path);
  // if (Property.hasValue(extension)) {
  // final IoFactory factory = IoFactory.factoryByFileExtension(IoFactory.class,
  // extension);
  // if (factory != null) {
  // return factory.getName();
  // }
  // }
  // return "File";
  // } else {
  // return "Missing File/Folder";
  // }
  // }

  @Override
  public Icon getIcon() {
    Icon icon = super.getIcon();
    if (icon == null) {
      if (isExists()) {
        final Path path = getPath();
        icon = getIcon(path);
        setIcon(icon);
      }
    }
    return icon;
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  public Path getPath() {
    return getUserData();
  }

  @Override
  public URL getUrl() {
    final Path path = getPath();
    final BaseTreeNode parent = getParent();
    return getUrl(parent, path);
  }

  @Override
  public int hashCode() {
    final Path path = getPath();
    if (path == null) {
      return 0;
    } else {
      return path.hashCode();
    }
  }

  @Override
  public boolean isAllowsChildren() {
    final Path path = getPath();
    return isAllowsChildren(path);
  }

  public boolean isDirectory() {
    if (isExists()) {
      final Path path = getPath();
      return Files.isDirectory(path);
    } else {
      return false;
    }
  }

  @Override
  public boolean isExists() {
    return this.exists;
  }

  public boolean isFileLayer() {
    if (isExists()) {
      final Path path = getPath();
      if (!this.hasFile) {
        return false;
      } else if (IoFactory.hasFactory(GeoreferencedImageReadFactory.class, path)) {
        return true;
      } else if (IoFactory.hasFactory(PointCloudReadFactory.class, path)) {
        return true;
      } else if (IoFactory.hasFactory(RecordReaderFactory.class, path)) {
        return true;
      } else if (IoFactory.hasFactory(GriddedElevationModelReaderFactory.class, path)) {
        return true;
      } else if (IoFactory.hasFactory(TriangulatedIrregularNetworkReaderFactory.class, path)) {
        return true;
      }
    }
    return false;
  }

  public boolean isHasFile() {
    return this.hasFile;
  }

  public <F extends IoFactory> boolean isReadable(final Class<F> factoryClass) {
    if (isExists()) {
      final Path path = getPath();
      if (!this.hasFile) {
        return false;
      } else {
        final F factory = IoFactory.factory(factoryClass, path);
        if (factory != null) {
          if (factory.isReadFromDirectorySupported() || !Files.isDirectory(path)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean isRecordFileLayer() {
    return isReadable(RecordReaderFactory.class);
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    refreshFields();
    final Path path = getPath();
    return getPathNodes(path);
  }

  private void refreshFields() {
    final Path path = getPath();
    this.exists = Paths.exists(path);
    final Icon icon = getIcon(path);
    setIcon(icon);
    try {
      path.toFile();
      this.hasFile = true;
    } catch (final UnsupportedOperationException e) {
    }
  }
}
