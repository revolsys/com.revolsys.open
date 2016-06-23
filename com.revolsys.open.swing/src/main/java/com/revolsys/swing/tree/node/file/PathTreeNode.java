package com.revolsys.swing.tree.node.file;

import java.awt.TextField;
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
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

import org.slf4j.LoggerFactory;

import com.revolsys.datatype.DataType;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.file.FileConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.io.file.Paths;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageFactory;
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
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.swing.tree.node.FunctionChildrenTreeNode;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;
import com.revolsys.swing.tree.node.record.PathRecordStoreTreeNode;
import com.revolsys.util.Property;
import com.revolsys.util.UrlProxy;
import com.revolsys.util.UrlUtil;
import com.revolsys.util.WrappedException;

public class PathTreeNode extends LazyLoadTreeNode implements UrlProxy {
  private static final JFileChooser CHOOSER = new JFileChooser();

  public static final Icon ICON_FILE = CHOOSER.getIcon(FileUtil.newTempFile("xxxx", "6z4gsdj"));

  public static final Icon ICON_FILE_DATABASE = Icons.getIconWithBadge(ICON_FILE, "database");

  public static final Icon ICON_FILE_IMAGE = Icons.getIconWithBadge(ICON_FILE, "picture");

  public static final Icon ICON_FILE_TABLE = Icons.getIconWithBadge(ICON_FILE, "table");

  public static final Icon ICON_FILE_VECTOR = Icons.getIconWithBadge(ICON_FILE, "table");

  public static final Icon ICON_FOLDER = Icons.getIcon("folder");

  public static final Icon ICON_FOLDER_DRIVE = Icons.getIconWithBadge(ICON_FOLDER, "drive");

  public static final Icon ICON_FOLDER_LINK = Icons.getIconWithBadge(ICON_FOLDER, "link");

  public static final Icon ICON_FOLDER_MISSING = Icons.getIconWithBadge(ICON_FOLDER, "error");

  private static final MenuFactory MENU = new MenuFactory("File");

  static {
    addRefreshMenuItem(MENU);

    TreeNodes.addMenuItem(MENU, "default", "Add Layer", "map_add", PathTreeNode::isFileLayer,
      PathTreeNode::actionAddLayer);

    TreeNodes.addMenuItem(MENU, "default", "Export Records", "table_save",
      PathTreeNode::isRecordFileLayer, PathTreeNode::actionExportRecords);

    TreeNodes.addMenuItem(MENU, "default", "Add Folder Connection", "link_add",
      PathTreeNode::isDirectory, PathTreeNode::actionAddFolderConnection);
  }

  public static void addPathNode(final List<BaseTreeNode> children, final Path path,
    final boolean showHidden) {
    if (showHidden || !Paths.isHidden(path) && Files.exists(path)) {
      final BaseTreeNode child = newTreeNode(path);
      children.add(child);
    }
  }

  public static Icon getIcon(final Path path) {
    if (path == null) {
      return ICON_FOLDER_MISSING;
    } else {
      if (GeoreferencedImage.isReadable(path)) {
        return ICON_FILE_IMAGE;
      } else if (RecordReader.isReadable(path)) {
        return ICON_FILE_TABLE;
      }
      File file;
      try {
        file = path.toFile();
        final Icon icon = CHOOSER.getIcon(file);
        return icon;
      } catch (final UnsupportedOperationException e) {
        if (Files.isDirectory(path)) {
          return ICON_FOLDER;
        }
        final String fileNameExtension = Paths.getFileNameExtension(path);
        file = FileUtil.newTempFile("1234567890", fileNameExtension);
        final Icon icon = CHOOSER.getIcon(file);
        file.delete();
        return icon;
      }
    }
  }

  private static File getIconFile(final Path path) {
    try {
      return path.toFile();
    } catch (final UnsupportedOperationException e) {
      final String fileNameExtension = Paths.getFileNameExtension(path);
      final File file = FileUtil.newTempFile("1234567890", fileNameExtension);
      file.delete();
      return file;
    }
  }

  public static List<BaseTreeNode> getPathNodes(final Iterable<Path> paths,
    final boolean showHidden) {
    final List<BaseTreeNode> children = new ArrayList<>();
    if (paths != null) {
      for (final Path path : paths) {
        addPathNode(children, path, showHidden);
      }
    }
    return children;
  }

  public static List<BaseTreeNode> getPathNodes(final Path path) {
    if (Files.isDirectory(path)) {
      try (
        final DirectoryStream<Path> children = Files.newDirectoryStream(path)) {
        return getPathNodes(children, false);
      } catch (final AccessDeniedException e) {
      } catch (final IOException e) {
        LoggerFactory.getLogger(PathTreeNode.class).debug("Unable to get children " + path);
      }
    }
    return Collections.emptyList();
  }

  public static URL getUrl(final BaseTreeNode parent, final Path path) {
    if (parent instanceof UrlProxy) {
      final UrlProxy parentProxy = (UrlProxy)parent;
      String childPath = Paths.getFileName(path);

      if (Files.isDirectory(path)) {
        childPath += "/";
      }
      return UrlUtil.getUrl(parentProxy, childPath);
    } else {
      try {
        final URL url = path.toUri().toURL();
        return url;
      } catch (final MalformedURLException e) {
        throw new WrappedException(e);
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
      "File Systems", ICON_FOLDER_DRIVE, (fileSystem) -> {
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
      final JLabel fileLabel = new JLabel(getIconFile(path).getAbsolutePath());
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

        registry.addConnection(connectionName, getIconFile(path));
      }
    }
  }

  private void actionAddLayer() {
    final URL url = getUrl();
    final Project project = Project.get();
    project.openFile(url);
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
    AbstractRecordLayer.exportRecords(title, hasGeometryField, (targetFile) -> {
      RecordIo.copyRecords(path, targetFile);
    });
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
      } else if (IoFactory.hasFactory(GeoreferencedImageFactory.class, path)) {
        return true;
      } else if (IoFactory.hasFactory(RecordReaderFactory.class, path)) {
        return true;
      }
    }
    return false;
  }

  public boolean isHasFile() {
    return this.hasFile;
  }

  public boolean isRecordFileLayer() {
    if (isExists()) {
      final Path path = getPath();
      if (!this.hasFile) {
        return false;
      } else if (IoFactory.hasFactory(RecordReaderFactory.class, path)) {
        return true;
      }
    }
    return false;
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
