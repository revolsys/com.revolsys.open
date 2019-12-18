package com.revolsys.swing.tree.node.record;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.jeometry.common.io.PathName;

import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.record.io.RecordStoreConnection;
import com.revolsys.record.io.RecordStoreConnectionManager;
import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;
import com.revolsys.swing.map.form.RecordStoreConnectionForm;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.RecordStoreLayer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.swing.tree.node.ConnectionManagerTrees;
import com.revolsys.swing.tree.node.LazyLoadTreeNode;
import com.revolsys.swing.tree.node.file.PathTreeNode;

public class RecordStoreConnectionTrees extends ConnectionManagerTrees {

  static {
    // RecordStoreConnectionRegistry
    MenuFactory.addMenuInitializer(RecordStoreConnectionRegistry.class, (menu) -> {
      TreeNodes.addMenuItemNodeValue(menu, "default", 0, "Add Connection", "database:add",
        RecordStoreConnectionRegistry::isEditable, RecordStoreConnectionTrees::addConnection);

      TreeNodes.addMenuItemNodeValue(menu, "default", 1, "Import Connection...", "database:import",
        RecordStoreConnectionRegistry::isEditable, RecordStoreConnectionTrees::importConnection);
    });

    // RecordStoreConnection
    MenuFactory.addMenuInitializer(RecordStoreConnection.class, (menu) -> {
      TreeNodes.addMenuItemNodeValue(menu, "default", 0, "Edit Connection", "database:edit",
        RecordStoreConnection::isEditable, RecordStoreConnectionTrees::editConnection);

      TreeNodes.<RecordStoreConnection> addMenuItemNodeValue(menu, "default", 1,
        "Export Connection", "database:export", ConnectionManagerTrees::exportConnection);
    });

    MenuFactory.addMenuInitializer(RecordStoreSchema.class, (menu) -> {
      LazyLoadTreeNode.addRefreshMenuItem(menu);
    });

    MenuFactory.addMenuInitializer(RecordDefinitionImpl.class, (menu) -> {
      TreeNodes.addMenuItemNodeValue(menu, "default", "Add Record Layer", "map:add",
        RecordStoreConnectionTrees::addLayer);
      // LazyLoadTreeNode.addRefreshMenuItem(menu);
    });
  }

  private static void addConnection(final RecordStoreConnectionRegistry registry) {
    final RecordStoreConnectionForm form = new RecordStoreConnectionForm(registry);
    form.showDialog();
  }

  private static void addLayer(final RecordDefinitionImpl recordDefinition) {
    final PathName typePath = recordDefinition.getPathName();
    final RecordStore recordStore = recordDefinition.getRecordStore();
    final Map<String, Object> connection = recordStore.getConnectionProperties();
    final Map<String, Object> layerConfig = new LinkedHashMap<>();
    MapObjectFactory.setType(layerConfig, "recordStoreLayer");
    layerConfig.put("name", recordDefinition.getName());
    layerConfig.put("connection", connection);
    layerConfig.put("typePath", typePath);
    layerConfig.put("showTableView", AbstractLayer.isShowNewLayerTableView());

    final LinkedList<String> path = new LinkedList<>();
    {
      BaseTreeNode node = BaseTree.getMenuNode();
      node = node.getParent();

      while (node != null) {
        final Object nodeValue = node.getUserObject();
        String nodeName = node.getName();
        if (node instanceof PathTreeNode) {
          nodeName = FileUtil.getBaseName(nodeName);
        }
        if (nodeValue instanceof RecordStoreSchemaElement) {
          path.addFirst(nodeName);
          node = node.getParent();
        } else {
          path.addFirst(nodeName);
          node = null;
        }
      }
    }
    final AbstractLayer layer = new RecordStoreLayer(layerConfig);
    LayerGroup layerGroup = Project.get();
    for (final String name : path) {
      try {
        layerGroup = layerGroup.addLayerGroup(name);
      } catch (final IllegalArgumentException e) {
        int i = 1;
        while (layerGroup.hasLayerWithSameName(null, name + i)) {
          i++;
        }
        layerGroup = layerGroup.addLayerGroup(name + i);
      }
    }
    layerGroup.addLayer(layer);
  }

  private static void editConnection(final RecordStoreConnection connection) {
    final RecordStoreConnectionRegistry registry = connection.getRegistry();
    final RecordStoreConnectionForm form = new RecordStoreConnectionForm(registry, connection);
    form.showDialog();
  }

  public static BaseTreeNode newRecordStoreConnectionsTreeNode() {
    final RecordStoreConnectionManager connectionManager = RecordStoreConnectionManager.get();
    final BaseTreeNode node = BaseTreeNode.newTreeNode(connectionManager);
    node.setOpen(true);
    return node;
  }

}
