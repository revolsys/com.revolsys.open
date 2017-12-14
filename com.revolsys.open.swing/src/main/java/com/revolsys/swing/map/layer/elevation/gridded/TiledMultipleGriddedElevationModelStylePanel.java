package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.swing.map.layer.elevation.gridded.renderer.TiledMultipleGriddedElevationModelLayerRenderer;
import com.revolsys.swing.map.layer.record.style.panel.BaseStylePanel;

public class TiledMultipleGriddedElevationModelStylePanel extends BaseStylePanel
  implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public TiledMultipleGriddedElevationModelStylePanel(
    final TiledMultipleGriddedElevationModelLayerRenderer renderer) {
    super(renderer, false);
    final JPanel panel = new JPanel(new BorderLayout());
    add(panel, 1);
    final JPanel stylePanels = new JPanel(new VerticalLayout(5));
    panel.add(stylePanels, BorderLayout.CENTER);
    addPanel(this, "Style", renderer, "minZ", "maxZ");
  }
}
