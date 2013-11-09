package com.revolsys.swing.tree.file;

import java.awt.TextField;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

import com.revolsys.io.file.FolderConnection;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.DirectoryNameField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;
import com.revolsys.util.Property;

public class FolderConnectionRegistryTreeNode extends LazyLoadTreeNode
  implements PropertyChangeListener {

  private static final MenuFactory MENU = new MenuFactory();

  public FolderConnectionRegistryTreeNode(
    final FolderConnectionsTreeNode parent,
    final FolderConnectionRegistry registry) {
    super(parent, registry);
    setType("Folder Connections");
    setName(registry.getName());
    setIcon(FileTreeUtil.ICON_FOLDER_LINK);
    setAllowsChildren(true);
    Property.addListener(registry, this);
  }

  public void addConnection() {
    final FolderConnectionRegistryTreeNode node = BaseTree.getMouseClickItem();
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
  public void doDelete() {
    final FolderConnectionRegistry registry = getRegistry();
    Property.removeListener(registry, this);
    super.doDelete();
  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    final FolderConnectionRegistry registry = getRegistry();
    final List<FolderConnection> conections = registry.getConections();
    for (final FolderConnection connection : conections) {
      final FolderConnectionTreeNode child = new FolderConnectionTreeNode(this,
        connection);
      children.add(child);
    }
    return children;
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  public FolderConnectionRegistry getRegistry() {
    final FolderConnectionRegistry registry = getUserObject();
    return registry;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (event instanceof IndexedPropertyChangeEvent) {
      final IndexedPropertyChangeEvent indexEvent = (IndexedPropertyChangeEvent)event;
      final String propertyName = indexEvent.getPropertyName();
      if (propertyName.equals("connections")) {
        final int index = indexEvent.getIndex();
        final Object newValue = indexEvent.getNewValue();
        final Object oldValue = indexEvent.getOldValue();
        if (newValue == null) {
          if (oldValue != null) {
            removeNode(index);
            nodeRemoved(index, oldValue);
          }
        } else if (oldValue == null) {
          FolderConnectionTreeNode node = new FolderConnectionTreeNode(this,
            (FolderConnection)newValue);
          addNode(index, node);

          nodesInserted(index);
        } else {
          nodesChanged(index);
        }
      }
    }
  }
}
