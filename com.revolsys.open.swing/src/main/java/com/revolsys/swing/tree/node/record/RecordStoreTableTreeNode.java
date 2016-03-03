package com.revolsys.swing.tree.node.record;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.io.PathName;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.RecordStoreLayer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.util.CaseConverter;

public class RecordStoreTableTreeNode extends BaseTreeNode {

  public static final ImageIcon ICON_TABLE = Icons.getIcon("table");

  public static Map<String, Icon> ICONS_GEOMETRY = new HashMap<String, Icon>();

  private static final MenuFactory MENU = new MenuFactory("Record Store Table");

  static {
    for (final String geometryType : Arrays.asList("Geometry", "Point", "MultiPoint", "LineString",
      "MultiLineString", "Polygon", "MultiPolygon")) {
      ICONS_GEOMETRY.put(geometryType, Icons.getIcon("table_" + geometryType.toLowerCase()));
    }
    ICONS_GEOMETRY.put("GeometryCollection", Icons.getIcon("table_geometry"));

    TreeNodes.addMenuItem(MENU, "default", "Add Layer", "map_add",
      RecordStoreTableTreeNode::addLayer);
  }

  public static Icon getIcon(final String geometryType) {
    Icon icon = ICONS_GEOMETRY.get(geometryType);
    if (icon == null) {
      icon = ICON_TABLE;
    }
    return icon;
  }

  private final Map<String, Object> connectionMap;

  public RecordStoreTableTreeNode(final Map<String, Object> connectionMap, final PathName typePath,
    final String geometryType) {
    super(typePath);
    this.connectionMap = connectionMap;
    if (geometryType == null) {
      setType("Data Table");
    } else {
      setType("Data Table (" + CaseConverter.toCapitalizedWords(geometryType) + ")");
    }

    final String name = typePath.getName();
    setName(name);

    final Icon icon = getIcon(geometryType);
    setIcon(icon);
  }

  private void addLayer() {
    final PathName typePath = getTypePath();
    final Map<String, Object> connection = getConnectionMap();
    final Map<String, Object> layerConfig = new LinkedHashMap<>();
    MapObjectFactory.setType(layerConfig, "recordStoreLayer");
    layerConfig.put("name", getName());
    layerConfig.put("connection", connection);
    layerConfig.put("typePath", typePath);
    final AbstractLayer layer = RecordStoreLayer.newLayer(layerConfig);
    final LinkedList<String> layerGroupPath = new LinkedList<>();
    BaseTreeNode parentNode = getParent();
    while (parentNode instanceof RecordStoreSchemaTreeNode) {
      final String parentName = parentNode.getName();
      layerGroupPath.addFirst(parentName);
      parentNode = parentNode.getParent();
    }
    final String parentName = parentNode.getName();
    layerGroupPath.addFirst(parentName);
    final Project project = Project.get();
    LayerGroup layerGroup = project;
    for (final String groupName : layerGroupPath) {
      layerGroup = layerGroup.addLayerGroup(groupName);
    }
    layerGroup.addLayer(layer);
    layer.showTableView(null);
  }

  public Map<String, Object> getConnectionMap() {
    return this.connectionMap;
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  public PathName getTypePath() {
    return (PathName)getUserObject();
  }
}
