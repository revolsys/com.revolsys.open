package com.revolsys.swing.map.component.layerchooser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.io.PathUtil;
import com.revolsys.swing.tree.model.node.LazyLoadTreeNode;

public class DataObjectStoreTableTreeNode extends LazyLoadTreeNode {

  public static final ImageIcon ICON_TABLE = SilkIconLoader.getIcon("table");

  public static Map<String, Icon> ICONS_GEOMETRY = new HashMap<String, Icon>();

  static {
    for (final String geometryType : Arrays.asList("Point", "MultiPoint",
      "LineString", "MultiLineString", "Polygon", "MultiPolygon")) {
      ICONS_GEOMETRY.put(geometryType,
        SilkIconLoader.getIcon("table_" + geometryType.toLowerCase()));
    }
  }

  public DataObjectStoreTableTreeNode(
    final DataObjectStoreSchemaTreeNode parent, final String typePath,
    final String geometryType) {
    super(typePath);
    setAllowsChildren(false);
    setParent(parent);

    final String title = PathUtil.getName(typePath);
    setTitle(title);

    Icon icon = ICONS_GEOMETRY.get(geometryType);
    if (icon == null) {
      icon = ICON_TABLE;
    }
    setIcon(icon);
  }

}
