package com.revolsys.swing.tree.node.file;

import java.awt.TextField;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import com.revolsys.io.file.FolderConnection;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.Borders;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.FileField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.LazyLoadTreeNode;

public class FolderConnectionRegistryTreeNode extends LazyLoadTreeNode
  implements PropertyChangeListener {

  private static final MenuFactory MENU = new MenuFactory("Folder Connection Registry");

  static {
    addRefreshMenuItem(MENU);
  }

  public FolderConnectionRegistryTreeNode(final FolderConnectionRegistry registry) {
    super(registry);
    setType("Folder Connections");
    setName(registry.getName());
    setIcon(PathTreeNode.ICON_FOLDER_LINK);
  }

  public void addConnection() {
    final FolderConnectionRegistry registry = getRegistry();
    final ValueField panel = new ValueField();
    panel.setTitle("Add Folder Connection");
    Borders.titled(panel, "Folder Connection");
    SwingUtil.addLabel(panel, "Name");
    final TextField nameField = new TextField(20);
    panel.add(nameField);

    SwingUtil.addLabel(panel, "Folder");
    final FileField folderField = new FileField(JFileChooser.DIRECTORIES_ONLY);
    panel.add(folderField);

    GroupLayouts.makeColumns(panel, 2, true);
    panel.showDialog();
    if (panel.isSaved()) {
      final File file = folderField.getFile();
      if (file != null && file.exists()) {
        registry.addConnection(nameField.getText(), file);
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

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final List<BaseTreeNode> children = new ArrayList<>();
    final FolderConnectionRegistry registry = getRegistry();
    final List<FolderConnection> conections = registry.getConections();
    for (final FolderConnection connection : conections) {
      final FolderConnectionTreeNode child = new FolderConnectionTreeNode(connection);
      children.add(child);
    }
    return children;
  }

  @Override
  public void propertyChangeDo(final PropertyChangeEvent event) {
    if (event instanceof IndexedPropertyChangeEvent) {
      final IndexedPropertyChangeEvent indexEvent = (IndexedPropertyChangeEvent)event;
      final String propertyName = indexEvent.getPropertyName();
      if (propertyName.equals("connections")) {
        refresh();
      }
    }
  }
}
