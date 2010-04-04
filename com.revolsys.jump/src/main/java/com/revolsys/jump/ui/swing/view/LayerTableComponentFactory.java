package com.revolsys.jump.ui.swing.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.openjump.swing.factory.component.ComponentFactory;

import com.revolsys.jump.ui.info.FeatureCollectionTablePanel;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.EnableableToolBar;

public class LayerTableComponentFactory implements ComponentFactory<Component> {
  public static final String KEY = LayerTableComponentFactory.class.getName();

  private Category category;

  private Layer layer;

  private WorkbenchContext workbenchContext;

  public LayerTableComponentFactory(
    final WorkbenchContext workbenchContext,
    final Category category,
    final Layer layer) {
    this.workbenchContext = workbenchContext;
    this.category = category;
    this.layer = layer;
  }

  public Component createComponent() {
    JPanel panel = new JPanel(new BorderLayout());

    FeatureCollectionTablePanel attributePanel = new FeatureCollectionTablePanel(
      workbenchContext);

    attributePanel.setLayer(layer);
    panel.add(attributePanel, BorderLayout.CENTER);

    EnableableToolBar toolbar = new LayerTableToolBar(attributePanel);
    panel.add(toolbar, BorderLayout.NORTH);

    return panel;
  }

  public Icon getIcon() {
    return null;
  }

  public String getName() {
    return layer.getName() + " (" + category.getName() + ")";
  }

  public String getToolTip() {
    return getName();
  }

  public void close(
    final Component component) {

  }
}
