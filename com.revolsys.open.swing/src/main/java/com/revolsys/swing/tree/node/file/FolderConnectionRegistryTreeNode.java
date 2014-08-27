package com.revolsys.swing.tree.node.file;

import java.awt.TextField;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.io.file.FolderConnection;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.DirectoryNameField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;
import com.revolsys.util.Property;

public class FolderConnectionRegistryTreeNode extends LazyLoadTreeNode
implements PropertyChangeListener {

  private static final MenuFactory MENU = new MenuFactory();

  public FolderConnectionRegistryTreeNode(
    final FolderConnectionRegistry registry) {
    super(registry);
    setType("Folder Connections");
    setName(registry.getName());
    setIcon(FileTreeNode.ICON_FOLDER_LINK);
    Property.addListener(registry, this);
  }

  public void addConnection() {
    final FolderConnectionRegistryTreeNode node = MenuFactory.getMenuSource();
    final FolderConnectionRegistry registry = node.getRegistry();
    final ValueField panel = new ValueField();
    panel.setTitle("Add Folder Connection");
    SwingUtil.setTitledBorder(panel, "Folder Connection");
    SwingUtil.addLabel(panel, "Name");
    final TextField nameField = new TextField(20);
    panel.add(nameField);

    SwingUtil.addLabel(panel, "Folder");
    final DirectoryNameField folderField = new DirectoryNameField();
    panel.add(folderField);

    GroupLayoutUtil.makeColumns(panel, 2, true);
    panel.showDialog();
    if (panel.isSaved()) {
      final File file = folderField.getDirectoryFile();
      if (file != null && file.exists()) {
        registry.addConnection(nameField.getText(), file);
      }
    }
  }

  @Override
  public void doClose() {
    final FolderConnectionRegistry registry = getRegistry();
    Property.removeListener(registry, this);
    super.doClose();
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final List<BaseTreeNode> children = new ArrayList<>();
    final FolderConnectionRegistry registry = getRegistry();
    final List<FolderConnection> conections = registry.getConections();
    for (final FolderConnection connection : conections) {
      final FolderConnectionTreeNode child = new FolderConnectionTreeNode(
        connection);
      children.add(child);
    }
    return children;
  }

  @Override
  public void doPropertyChange(final PropertyChangeEvent event) {
    if (event instanceof IndexedPropertyChangeEvent) {
      final IndexedPropertyChangeEvent indexEvent = (IndexedPropertyChangeEvent)event;
      final String propertyName = indexEvent.getPropertyName();
      if (propertyName.equals("connections")) {
        refresh();
      }
    }
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  public FolderConnectionRegistry getRegistry() {
    final FolderConnectionRegistry registry = getUserData();
    return registry;
  }
}
