package com.revolsys.swing.tree.record;

import java.awt.TextField;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.tree.TreeNode;

import com.revolsys.data.io.RecordStoreConnectionMapProxy;
import com.revolsys.data.io.RecordStoreProxy;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.io.FileUtil;
import com.revolsys.io.datastore.RecordStoreConnectionManager;
import com.revolsys.io.datastore.RecordStoreConnectionRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.file.FileTreeNode;
import com.revolsys.util.Property;

public class FileRecordStoreTreeNode extends FileTreeNode implements
RecordStoreProxy, RecordStoreConnectionMapProxy {
  public static void addRecordStoreConnection() {
    final FileRecordStoreTreeNode node = BaseTree.getMouseClickItem();
    final File file = node.getUserData();
    final String fileName = FileUtil.getBaseName(file);

    final ValueField panel = new ValueField();
    panel.setTitle("Add Data Store Connection");
    SwingUtil.setTitledBorder(panel, "Data Store Connection");

    SwingUtil.addLabel(panel, "File");
    final JLabel fileLabel = new JLabel(file.getAbsolutePath());
    panel.add(fileLabel);

    SwingUtil.addLabel(panel, "Name");
    final TextField nameField = new TextField(20);
    panel.add(nameField);
    nameField.setText(fileName);

    SwingUtil.addLabel(panel, "Folder Connections");
    final List<RecordStoreConnectionRegistry> registries = RecordStoreConnectionManager.get()
        .getVisibleConnectionRegistries();
    final JComboBox registryField = new JComboBox(
      new Vector<RecordStoreConnectionRegistry>(registries));

    panel.add(registryField);

    GroupLayoutUtil.makeColumns(panel, 2, true);
    panel.showDialog();
    if (panel.isSaved()) {
      final RecordStoreConnectionRegistry registry = (RecordStoreConnectionRegistry)registryField.getSelectedItem();
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
      final Map<String, Object> connection = node.getRecordStoreConnectionMap();
      final Map<String, Object> config = new HashMap<String, Object>();
      config.put("name", connectionName);
      config.put("connection", connection);
      registry.createConnection(config);
    }
  }

  private static final MenuFactory MENU = new MenuFactory();

  static {
    final InvokeMethodAction refresh = TreeItemRunnable.createAction("Refresh",
      "arrow_refresh", "refresh");
    MENU.addMenuItem("default", refresh);

    MENU.addMenuItemTitleIcon("default", "Add Data Store Connection",
      "link_add", null, FileRecordStoreTreeNode.class,
        "addRecordStoreConnection");
  }

  public FileRecordStoreTreeNode(final TreeNode parent, final File file) {
    super(parent, file);
    setType("Data Store");
    setName(FileUtil.getFileName(file));
    setIcon(FileTreeNode.ICON_FILE_DATABASE);
    setAllowsChildren(true);
  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final RecordStore recordStore = getRecordStore();
    return RecordStoreConnectionTreeNode.getChildren(this,
      getRecordStoreConnectionMap(), recordStore);
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends RecordStore> V getRecordStore() {
    final File file = getUserData();
    return (V)RecordStoreConnectionManager.getRecordStore(file);
  }

  @Override
  public Map<String, Object> getRecordStoreConnectionMap() {
    final TreeNode parent = getParent();
    final File file = getUserData();
    final URL url = FileTreeNode.getUrl(parent, file);

    return Collections.<String, Object> singletonMap("url", url.toString());
  }

}
