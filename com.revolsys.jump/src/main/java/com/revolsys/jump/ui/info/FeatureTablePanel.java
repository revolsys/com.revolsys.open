package com.revolsys.jump.ui.info;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.revolsys.jump.ui.swing.FeatureTypeUiBuilderRegistry;
import com.revolsys.jump.ui.swing.table.FeatureTableFactory;
import com.revolsys.jump.ui.swing.table.FeatureTableModel;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;

@SuppressWarnings("serial")
public class FeatureTablePanel extends JPanel implements CurrentFeatureListener {
  private FeatureTableModel tableModel;

  private JScrollPane scrollPane;

  private LayerTitlePanel title;

  public FeatureTablePanel(final WorkbenchContext context) {
    super(new BorderLayout());
    tableModel = new FeatureTableModel();

    title = new LayerTitlePanel();
    this.add(title, BorderLayout.NORTH);
    title.setBackground(Color.WHITE);

    FeatureTypeUiBuilderRegistry uiBuilderRegistry = FeatureTypeUiBuilderRegistry.getInstance(context);
    JTable featureTable = FeatureTableFactory.createTable(tableModel,
      uiBuilderRegistry);
    scrollPane = new JScrollPane(featureTable);
    this.add(scrollPane, BorderLayout.CENTER);
  }

  public void featureSelected(final Layer layer, final Feature feature) {
    tableModel.setFeature(feature);
    title.setLayer(layer);
  }

}
