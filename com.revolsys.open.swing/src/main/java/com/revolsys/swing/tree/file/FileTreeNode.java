package com.revolsys.swing.tree.file;

import java.awt.TextField;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.tree.TreeNode;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.file.FolderConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.raster.AbstractGeoReferencedImageFactory;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.datastore.FileDataObjectStoreTreeNode;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;
import com.revolsys.util.UrlProxy;
import com.revolsys.util.UrlUtil;

public class FileTreeNode extends LazyLoadTreeNode implements UrlProxy {

  private static final MenuFactory MENU = new MenuFactory();
  static {
    final EnableCheck isDirectory = new TreeItemPropertyEnableCheck("directory");
    final EnableCheck isFileLayer = new TreeItemPropertyEnableCheck("fileLayer");

    MENU.addMenuItemTitleIcon("default", "Add Layer", "map_add", isFileLayer,
      FileTreeNode.class, "addLayer");

    MENU.addMenuItemTitleIcon("default", "Add Folder Connection", "link_add",
      isDirectory, FileTreeNode.class, "addFolderConnection");
  }

  public static void addFolderConnection() {
    final FileTreeNode node = BaseTree.getMouseClickItem();
    if (node.isDirectory()) {
      final File directory = node.getUserObject();
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
          if (FileTreeUtil.isDataStore(childFile)) {
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
  protected List<TreeNode> doLoadChildren() {
    final File file = getUserObject();
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
    return FileTreeUtil.getIcon(file);
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
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

  public boolean isDirectory() {
    final File file = getFile();
    return file.isDirectory();
  }

  public boolean isFileLayer() {
    final File file = getFile();
    final String fileName = file.getName();
    if (AbstractGeoReferencedImageFactory.hasGeoReferencedImageFactory(fileName)) {
      return true;
    } else if (AbstractDataObjectReaderFactory.hasDataObjectReaderFactory(fileName)) {
      return true;
    } else {
      return false;
    }
  }
}
