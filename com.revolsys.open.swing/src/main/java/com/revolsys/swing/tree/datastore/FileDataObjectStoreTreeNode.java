package com.revolsys.swing.tree.datastore;

import java.awt.TextField;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeNode;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreConnectionMapProxy;
import com.revolsys.gis.data.io.DataObjectStoreProxy;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.io.datastore.DataObjectStoreConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.DirectoryNameField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.file.FileModel;
import com.revolsys.swing.tree.file.FileTreeNode;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class FileDataObjectStoreTreeNode extends LazyLoadTreeNode implements
  DataObjectStoreProxy, DataObjectStoreConnectionMapProxy {

  public FileDataObjectStoreTreeNode(final TreeNode parent, final File file) {
    super(parent, file);
    setType("Data Store");
    setName(file.getName());
    setIcon(FileModel.ICON_FILE_DATABASE);
    setAllowsChildren(true);
  }

  // TODo
  public void addConnection() {
    final Object object = BaseTree.getMouseClickItem();
    if (object instanceof FolderConnectionRegistry) {
      final FolderConnectionRegistry registry = (FolderConnectionRegistry)object;
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

  }

  @Override
  protected List<TreeNode> doLoadChildren() {
    final List<TreeNode> children = new ArrayList<TreeNode>();
    final DataObjectStore dataStore = getDataStore();
    for (final DataObjectStoreSchema schema : dataStore.getSchemas()) {
      final String schemaPath = schema.getPath();

      final DataObjectStoreSchemaTreeNode schemaNode = new DataObjectStoreSchemaTreeNode(
        this, schemaPath);
      children.add(schemaNode);
    }
    return children;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends DataObjectStore> V getDataStore() {
    final File file = getUserObject();
    return (V)DataObjectStoreConnectionManager.getDataStore(file);
  }

  @Override
  public Map<String, Object> getDataStoreConnection() {
    final TreeNode parent = getParent();
    final File file = getUserObject();
    final URL url = FileTreeNode.getUrl(parent, file);

    return Collections.<String, Object> singletonMap("url", url.toString());
  }

}
