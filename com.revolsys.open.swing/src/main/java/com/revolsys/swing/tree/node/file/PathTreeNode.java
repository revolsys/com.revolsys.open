package com.revolsys.swing.tree.node.file;

import java.awt.TextField;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.slf4j.LoggerFactory;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.record.io.RecordIo;
import com.revolsys.data.record.io.RecordReaderFactory;
import com.revolsys.data.record.io.RecordStoreFactoryRegistry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Paths;
import com.revolsys.io.file.FolderConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.raster.GeoreferencedImageFactory;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.AndEnableCheck;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodePropertyEnableCheck;
import com.revolsys.swing.tree.TreeNodeRunnable;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;
import com.revolsys.swing.tree.node.record.PathRecordStoreTreeNode;
import com.revolsys.util.Property;
import com.revolsys.util.UrlProxy;
import com.revolsys.util.UrlUtil;
import com.revolsys.util.WrappedException;

public class PathTreeNode extends LazyLoadTreeNode implements UrlProxy {

  private static final JFileChooser CHOOSER = new JFileChooser();

  private static final UIDefaults DEFAULTS = UIManager.getDefaults();

  public static final Icon ICON_FILE = CHOOSER.getIcon(FileUtil.createTempFile("xxxx", "6z4gsdj"));

  public static final Icon ICON_FILE_DATABASE = Icons.getIconWithBadge(ICON_FILE, "database");

  public static final Icon ICON_FILE_IMAGE = Icons.getIconWithBadge(ICON_FILE, "picture");

  public static final Icon ICON_FILE_TABLE = Icons.getIconWithBadge(ICON_FILE, "table");

  public static final Icon ICON_FILE_VECTOR = Icons.getIconWithBadge(ICON_FILE, "table");

  public static final Icon ICON_FOLDER = DEFAULTS.getIcon("Tree.closedIcon");

  public static final Icon ICON_FOLDER_DRIVE = Icons.getIconWithBadge(ICON_FOLDER, "drive");

  public static final Icon ICON_FOLDER_LINK = Icons.getIconWithBadge(ICON_FOLDER, "link");

  public static final Icon ICON_FOLDER_MISSING = Icons.getIcon("folder_error");

  private static final MenuFactory MENU = new MenuFactory("File");

  static {
    final EnableCheck isDirectory = new TreeNodePropertyEnableCheck("directory");
    final EnableCheck isFileLayer = new TreeNodePropertyEnableCheck("fileLayer");

    final InvokeMethodAction refresh = TreeNodeRunnable.createAction("Refresh", "arrow_refresh",
      NODE_EXISTS, "refresh");
    MENU.addMenuItem("default", refresh);

    MENU.addMenuItem("default",
      TreeNodeRunnable.createAction("Add Layer", "map_add", isFileLayer, "addLayer"));

    MENU.addMenuItem("default", TreeNodeRunnable.createAction("Add Folder Connection", "link_add",
      new AndEnableCheck(isDirectory, NODE_EXISTS), "addFolderConnection"));
  }

  public static void addPathNode(final List<BaseTreeNode> children, final Path path) {
    if (!Paths.isHidden(path)) {
      if (PathTreeNode.isRecordStore(path)) {
        final PathRecordStoreTreeNode recordStoreNode = new PathRecordStoreTreeNode(path);
        children.add(recordStoreNode);
      } else {
        BaseTreeNode child;
        final List<String> fileNameExtensions = Paths.getFileNameExtensions(path);
        if (fileNameExtensions.contains("zip") || fileNameExtensions.contains("jar")) {
          try {
            child = new SingleFileSystemTreeNode(path);
          } catch (final Throwable e) {
            child = new PathTreeNode(path);
          }
        } else {
          child = new PathTreeNode(path);
        }
        children.add(child);
      }
    }
  }

  public static Icon getIcon(final Path path) {
    if (path == null) {
      return ICON_FOLDER_MISSING;
    } else {
      if (isImage(path)) {
        return ICON_FILE_IMAGE;
      } else if (RecordIo.canReadRecords(path)) {
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
        file = FileUtil.createTempFile("1234567890", fileNameExtension);
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
      final File file = FileUtil.createTempFile("1234567890", fileNameExtension);
      file.delete();
      return file;
    }
  }

  public static List<BaseTreeNode> getPathNodes(final BaseTreeNode parent,
    final Iterable<Path> paths) {
    final List<BaseTreeNode> children = new ArrayList<>();
    if (paths != null) {
      for (final Path path : paths) {
        addPathNode(children, path);
      }
    }
    return children;
  }

  public static List<BaseTreeNode> getPathNodes(final BaseTreeNode parent, final Path path) {
    if (Files.isDirectory(path)) {
      try (
        final DirectoryStream<Path> children = Files.newDirectoryStream(path)) {
        return getPathNodes(parent, children);
      } catch (final IOException e) {
        LoggerFactory.getLogger(PathTreeNode.class).debug("Unable to get children " + path);
        return Collections.emptyList();
      }
    } else {
      return Collections.emptyList();
    }
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
        return path.toUri().toURL();
      } catch (final MalformedURLException e) {
        throw new WrappedException(e);
      }
    }
  }

  public static boolean isAllowsChildren(final Path path) {
    if (path == null) {
      return true;
    } else if (!Files.exists(path)) {
      return false;
    } else if (Files.isDirectory(path)) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean isImage(final Path path) {
    final String fileNameExtension = Paths.getFileNameExtension(path);
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    return ioFactoryRegistry.isFileExtensionSupported(GeoreferencedImageFactory.class,
      fileNameExtension);
  }

  public static boolean isRecordStore(final Path path) {
    final Set<String> fileExtensions = RecordStoreFactoryRegistry.getFileExtensions();
    final String extension = Paths.getFileNameExtension(path).toLowerCase();
    return fileExtensions.contains(extension);
  }

  private boolean hasFile = false;

  public PathTreeNode(final Path path) {
    super(path);
    final String fileName = Paths.getFileName(path);
    setName(fileName);
    final Icon icon = getIcon(path);
    setIcon(icon);
    try {
      path.toFile();
      this.hasFile = true;
    } catch (final UnsupportedOperationException e) {
    }
  }

  public void addFolderConnection() {
    if (isDirectory()) {
      final Path path = getPath();
      final String fileName = getName();

      final ValueField panel = new ValueField();
      panel.setTitle("Add Folder Connection");
      SwingUtil.setTitledBorder(panel, "Folder Connection");

      SwingUtil.addLabel(panel, "Folder");
      final JLabel fileLabel = new JLabel(getIconFile(path).getAbsolutePath());
      panel.add(fileLabel);

      SwingUtil.addLabel(panel, "Name");
      final TextField nameField = new TextField(20);
      panel.add(nameField);
      nameField.setText(fileName);

      SwingUtil.addLabel(panel, "Folder Connections");
      final List<FolderConnectionRegistry> registries = new ArrayList<>();
      for (final FolderConnectionRegistry registry : FolderConnectionManager.get()
        .getVisibleConnectionRegistries()) {
        if (!registry.isReadOnly()) {
          registries.add(registry);
        }
      }
      final JComboBox registryField = new JComboBox(
        new Vector<FolderConnectionRegistry>(registries));

      panel.add(registryField);

      GroupLayoutUtil.makeColumns(panel, 2, true);
      panel.showDialog();
      if (panel.isSaved()) {
        final FolderConnectionRegistry registry = (FolderConnectionRegistry)registryField
          .getSelectedItem();
        String connectionName = nameField.getText();
        if (!Property.hasValue(connectionName)) {
          connectionName = fileName;
        }
        final String baseConnectionName = connectionName;
        int i = 0;
        while (registry.getConnection(connectionName) != null) {
          connectionName = baseConnectionName + i;
          i++;
        }
        registry.addConnection(connectionName, getIconFile(path));
      }
    }
  }

  public void addLayer() {
    final URL url = getUrl();
    final Project project = Project.get();
    project.openFile(url);
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final Path path = getPath();
    return getPathNodes(this, path);
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof PathTreeNode) {
      final PathTreeNode fileNode = (PathTreeNode)other;
      final Path path = getPath();
      final Path otherPath = fileNode.getPath();
      final boolean equal = EqualsRegistry.equal(path, otherPath);
      return equal;
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

  public Path getPath() {
    return getUserData();
  }

  @Override
  public String getType() {
    final Path path = getPath();
    if (Files.isDirectory(path)) {
      return "Folder";
    } else if (Files.exists(path)) {
      final String extension = Paths.getFileNameExtension(path);
      if (Property.hasValue(extension)) {
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
    final Path path = getPath();
    return Files.isDirectory(path);
  }

  @Override
  public boolean isExists() {
    final Path path = getPath();
    if (path == null) {
      return false;
    } else {
      return Files.exists(path) && super.isExists();
    }
  }

  public boolean isFileLayer() {
    final Path path = getPath();
    if (!this.hasFile) {
      return false;
    } else if (IoFactory.hasFactory(GeoreferencedImageFactory.class, path)) {
      return true;
    } else if (IoFactory.hasFactory(RecordReaderFactory.class, path)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isHasFile() {
    return this.hasFile;
  }

  @Override
  public void refresh() {
    setIcon(null);
    super.refresh();
  }
}
