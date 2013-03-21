package com.revolsys.swing.map.layer.grid;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.gis.grid.RectangularMapTile;
import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.tree.TreeUtil;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.PreferencesUtil;

@SuppressWarnings("serial")
public class ZoomToMapSheet extends AbstractAction {

  public ZoomToMapSheet() {
    putValue(NAME, "Zoom to Map Sheet");
    putValue(SMALL_ICON, SilkIconLoader.getIcon("zoom"));
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final Object source = e.getSource();
    GridLayer layer = TreeUtil.getFirstSelectedNode(source, GridLayer.class);
    Project project = layer.getProject();
    if (project != null) {
      final RectangularMapGrid grid = layer.getGrid();
      String gridName = grid.getName();
      String preferenceName = CaseConverter.toCapitalizedWords(gridName)
        + "MapSheet";
      String mapSheet = PreferencesUtil.getString(getClass(), preferenceName);
      mapSheet = JOptionPane.showInputDialog("Enter name of the" + gridName
        + " map sheet to zoom to", mapSheet);
      if (StringUtils.hasText(mapSheet)) {
        try {
          RectangularMapTile mapTile = grid.getTileByName(mapSheet);
          final BoundingBox boundingBox = mapTile.getBoundingBox();
          project.setViewBoundingBox(boundingBox);
        } catch (Throwable t) {
          String message = "Invalid map sheet " + mapSheet +" for " + gridName;
          LoggerFactory.getLogger(getClass()).error(message,e);
          JOptionPane.showMessageDialog((Component)e.getSource(), message);
        } finally {
          PreferencesUtil.setString(getClass(), preferenceName, mapSheet);
        }
      }
    }
  }
}
