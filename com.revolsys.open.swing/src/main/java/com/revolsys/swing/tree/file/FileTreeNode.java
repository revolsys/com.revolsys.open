package com.revolsys.swing.tree.file;

import java.awt.TextField;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.tree.TreeNode;

import org.springframework.util.StringUtils;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.DataObjectReaderFactory;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.file.FolderConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.raster.AbstractGeoReferencedImageFactory;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageFactory;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.datastore.FileDataObjectStoreTreeNode;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;
import com.revolsys.util.UrlProxy;
import com.revolsys.util.UrlUtil;

public class FileTreeNode extends LazyLoadTreeNode implements UrlProxy {

  public static final Icon ICON_FILE = SilkIconLoader.getIcon("file");

  public static final Icon ICON_FILE_DATABASE = SilkIconLoader.getIcon("file_database");

  public static final Icon ICON_FILE_IMAGE = SilkIconLoader.getIcon("file_image");

  public static final Icon ICON_FILE_TABLE = SilkIconLoader.getIcon("file_table");

  public static final Icon ICON_FILE_VECTOR = SilkIconLoader.getIcon("file_table");

  public static final Icon ICON_FOLDER = SilkIconLoader.getIcon("folder");

  public static final Icon ICON_DRIVE = SilkIconLoader.getIcon("drive");

  public static final Icon ICON_DRIVE_MISSING = SilkIconLoader.getIcon("drive_error");

  public static final ImageIcon ICON_FOLDER_LINK = SilkIconLoader.getIcon("folder_link");

  public static final Icon ICON_FOLDER_MISSING = SilkIconLoader.getIcon("folder_error");

  private static final MenuFactory MENU = new MenuFactory();

  static {
    final EnableCheck isDirectory = new TreeItemPropertyEnableCheck("directory");
    final EnableCheck isFileLayer = new TreeItemPropertyEnableCheck("fileLayer");

    final InvokeMethodAction refresh = TreeItemRunnable.createAction("Refresh",
      "arrow_refresh", "refresh");
    MENU.addMenuItem("default", refresh);

    MENU.addMenuItemTitleIcon("default", "Add Layer", "map_add", isFileLayer,
      FileTreeNode.class, "addLayer");

    MENU.addMenuItemTitleIcon("default", "Add Folder Connection", "link_add",
      isDirectory, FileTreeNode.class, "addFolderConnection");
  }

  public static void addFolderConnection() {
    final FileTreeNode node = BaseTree.getMouseClickItem();
    if (node.isDirectory()) {
      final File directory = node.getUserData();
      final String fileName = node.getName();

      final ValueField panel = new ValueField();
      panel.setTitle("Add Folder Connection");
      SwingUtil.setTitledBorder(panel, "Folder Connection");

      SwingUtil.addLabel(panel, "Folder");
      final JLabel fileLabel = new JLabel(directory.getAbsolutePath());
      panel.add(fileLabel);

      SwingUtil.addLabel(panel, "Name");
      final TextField nameField = new TextField(20);
      panel.add(nameField);
      nameField.setText(fileName);

      SwingUtil.addLabel(panel, "Folder Connections");
      final List<FolderConnectionRegistry> registries = FolderConnectionManager.get()
        .getVisibleConnectionRegistries();
      final JComboBox registryField = new JComboBox(
        new Vector<FolderConnectionRegistry>(registries));

      panel.add(registryField);

      GroupLayoutUtil.makeColumns(panel, 2, true);
      panel.showDialog();
      if (panel.isSaved()) {
        final FolderConnectionRegistry registry = (FolderConnectionRegistry)registryField.getSelectedItem();
        String connectionName = nameField.getText();
        if (!StringUtils.hasText(connectionName)) {
          connectionName = fileName;
        }
        final String baseConnectionName = connectionName;
        int i = 0;
        while (registry.getConnection(connectionName) != null) {
          connectionName = baseConnectionName + i;
          i++;
        }
        registry.addConnection(connectionName, directory);
      }
    }
  }

  public static void addLayer() {
    final FileTreeNode node = BaseTree.getMouseClickItem();
    final URL url = node.getUrl();
    Project.get().openFile(url);
  }

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
          if (FileTreeNode.isDataStore(childFile)) {
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

  public static Icon getIcon(final File file) {
    if (file == null) {
      return ICON_FOLDER_MISSING;
    } else {
      final boolean exists = file.exists();
      if (FileUtil.isRoot(file)) {
        if (exists) {
          return ICON_DRIVE;
        } else {
          return ICON_DRIVE_MISSING;
        }
      } else if (exists) {
        if (isDataStore(file)) {
          return ICON_FILE_DATABASE;
        } else if (isImage(file)) {
          return ICON_FILE_IMAGE;
        } else if (isVector(file)) {
          return ICON_FILE_TABLE;
        } else if (file.isDirectory()) {
          return ICON_FOLDER;
        } else {
          return ICON_FILE;
        }
      } else {
        return ICON_FOLDER_MISSING;
      }
    }
  }

  public static URL getUrl(final TreeNode parent, final File file) {
    if (parent instanceof UrlProxy) {
      final UrlProxy parentProxy = (UrlProxy)parent;
      String childPath = FileUtil.getFileName(file);

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

  public FileTreeNode(final TreeNode parent, final File file) {
    super(parent, file);
    final String fileName = FileUtil.getFileName(file);
    setName(fileName);
  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final File file = getUserData();
    if (file.isDirectory()) {
      return getFileNodes(this, file);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof FileTreeNode) {
      final FileTreeNode fileNode = (FileTreeNode)other;
      final File file = getFile();
      final File otherFile = fileNode.getFile();
      final boolean equal = EqualsRegistry.equal(file, otherFile);
      return equal;
    }
    return false;
  }

  public File getFile() {
    return getUserData();
  }

  @Override
  public Icon getIcon() {
    final File file = getUserData();
    return FileTreeNode.getIcon(file);
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  @Override
  public String getType() {
    final File file = getUserData();
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
    final File file = getUserData();
    if (file == null) {
      return 0;
    } else {
      return file.hashCode();
    }
  }

  @Override
  public boolean isAllowsChildren() {
    final File file = getUserData();
    return isAllowsChildren(file);
  }

  public boolean isDirectory() {
    final File file = getFile();
    return file.isDirectory();
  }

  public boolean isFileLayer() {
    final File file = getFile();
    final String fileName = FileUtil.getFileName(file);
    if (AbstractGeoReferencedImageFactory.hasGeoReferencedImageFactory(fileName)) {
      return true;
    } else if (AbstractDataObjectReaderFactory.hasDataObjectReaderFactory(fileName)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public void refresh() {
    final File file = getFile();
    if (file.exists()) {
      if (file.isDirectory()) {
        final List<FileTreeNode> oldNodes = (List)getChildren();
        final ListIterator<FileTreeNode> oldIterator = oldNodes.listIterator();

        final List<FileTreeNode> newNodes = (List)getFileNodes(this, file);
        final ListIterator<FileTreeNode> newIterator = newNodes.listIterator();
        int i = 0;

        while (oldIterator.hasNext() && newIterator.hasNext()) {
          FileTreeNode oldNode = oldIterator.next();
          File oldFile = oldNode.getFile();

          FileTreeNode newNode = newIterator.next();
          File newFile = newNode.getFile();
          while (oldFile != null && oldFile.compareTo(newFile) < 0) {
            oldIterator.remove();
            nodeRemoved(i, oldNode);
            if (oldIterator.hasNext()) {
              oldNode = oldIterator.next();
              oldFile = oldNode.getFile();
            } else {
              oldFile = null;
            }
          }
          if (oldFile != null) {
            while (newFile != null && newFile.compareTo(oldFile) < 0) {
              oldIterator.previous();
              oldIterator.add(newNode);
              oldIterator.next();
              nodesInserted(i);
              i++;
              if (newIterator.hasNext()) {
                newNode = newIterator.next();
                newFile = newNode.getFile();
              } else {
                newFile = null;
              }
            }
            if (newFile != null) {
              i++;
            }
          }
        }
        while (oldIterator.hasNext()) {
          oldIterator.next();
          oldIterator.remove();

        }

        while (newIterator.hasNext()) {
          final FileTreeNode newNode = newIterator.next();
          oldIterator.add(newNode);
          nodesInserted(i);
          i++;
        }
      }
    } else {
      final TreeNode parent = getParent();
      if (parent instanceof LazyLoadTreeNode) {
        final LazyLoadTreeNode parentNode = (LazyLoadTreeNode)parent;
        parentNode.removeNode(this);
      }
    }
  }
}
