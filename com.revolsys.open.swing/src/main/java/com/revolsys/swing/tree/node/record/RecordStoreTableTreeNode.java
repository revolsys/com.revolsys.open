package com.revolsys.swing.tree.node.record;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.io.Path;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.RecordStoreLayer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeNodeRunnable;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.util.CaseConverter;

public class RecordStoreTableTreeNode extends BaseTreeNode {

  public static Icon getIcon(final String geometryType) {
    Icon icon = ICONS_GEOMETRY.get(geometryType);
    if (icon == null) {
      icon = ICON_TABLE;
    }
    return icon;
  }

  public static final ImageIcon ICON_TABLE = SilkIconLoader.getIcon("table");

  public static Map<String, Icon> ICONS_GEOMETRY = new HashMap<String, Icon>();

  private static final MenuFactory MENU = new MenuFactory();

  static {
    for (final String geometryType : Arrays.asList("Point", "MultiPoint",
      "LineString", "MultiLineString", "Polygon", "MultiPolygon")) {
      ICONS_GEOMETRY.put(geometryType,
        SilkIconLoader.getIcon("table_" + geometryType.toLowerCase()));
    }

    MENU.addMenuItem("default",
      TreeNodeRunnable.createAction("Add Layer", "map_add", "addLayer"));
  }

  private final Map<String, Object> connectionMap;

  public RecordStoreTableTreeNode(final Map<String, Object> connectionMap,
    final String typePath, final String geometryType) {
    super(typePath);
    this.connectionMap = connectionMap;
    if (geometryType == null) {
      setType("Data Table");
    } else {
      setType("Data Table (" + CaseConverter.toCapitalizedWords(geometryType)
        + ")");
    }

    final String name = Path.getName(typePath);
    setName(name);

    final Icon icon = getIcon(geometryType);
    setIcon(icon);
  }

  public void addLayer() {
    final String typePath = getTypePath();
    final Map<String, Object> connection = getConnectionMap();
    final Map<String, Object> layerConfig = new LinkedHashMap<String, Object>();
    layerConfig.put("type", "recordStore");
    layerConfig.put("name", getName());
    layerConfig.put("connection", connection);
    layerConfig.put("typePath", typePath);
    final AbstractLayer layer = RecordStoreLayer.create(layerConfig);
    Project.get().addLayer(layer);
    // TODO different layer groups?
  }

  public Map<String, Object> getConnectionMap() {
    return this.connectionMap;
  }

  @Override
  public MenuFactory getMenu() {
    return MENU;
  }

  public String getTypePath() {
    return (String)getUserObject();
  }
}
