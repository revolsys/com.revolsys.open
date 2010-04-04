package com.revolsys.jump.ui.info;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;

@SuppressWarnings("serial")
public class LayerTitlePanel extends JPanel {
  private JLabel categoryPath = new JLabel();

  private JLabel layerName = new JLabel();

  public LayerTitlePanel() {
    super(new BorderLayout());
    Font baseFont = layerName.getFont();

    categoryPath.setBorder(new EmptyBorder(new Insets(1, 2, 1, 2)));
    categoryPath.setFont(baseFont.deriveFont(baseFont.getSize() * 0.9f));
    add(categoryPath, BorderLayout.NORTH);

    layerName.setBorder(new EmptyBorder(new Insets(1, 2, 1, 2)));
    layerName.setFont(baseFont.deriveFont(baseFont.getSize() * 1.4f)
      .deriveFont(Font.BOLD));
    add(layerName, BorderLayout.SOUTH);
  }

  public void setLayer(final Layer layer) {
    Category category = null;
    if (layer != null) {
      LayerManager layerManager = layer.getLayerManager();
      if (layerManager != null) {
        category = layerManager.getCategory(layer);
      }

      setBorder(BorderFactory.createLineBorder(InfoModelUtil.getColor(layer), 3));
      layerName.setText(layer.getName());
    } else {
      setBorder(null);
      layerName.setText("");
    }
    if (category != null) {
      categoryPath.setText(category.getName());
    } else {
      categoryPath.setText("");
    }
  }
}
