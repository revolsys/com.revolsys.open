package com.revolsys.jump.ui.info;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

import org.openjump.swing.util.SpringUtilities;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.InfoModelListener;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;

public class InfoModelTablePanel extends JPanel implements InfoModelListener {
  /**
   * 
   */
  private static final long serialVersionUID = -6437471483359309122L;

  private Map<LayerTableModel, JPanel> panels = new HashMap<LayerTableModel, JPanel>();

  private WorkbenchContext workbenchContext;

  private JPanel container = new JPanel(new SpringLayout());

  @SuppressWarnings("unchecked")
  public InfoModelTablePanel(final WorkbenchContext workbenchContext,
    final CurrentFeatureInfoModel infoModel) {
    JScrollPane scrollPane = new JScrollPane(
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    add(scrollPane, BorderLayout.CENTER);
    scrollPane.getViewport().add(container);

    this.workbenchContext = workbenchContext;
    for (LayerTableModel layerTableModel : (Collection<LayerTableModel>)infoModel.getLayerTableModels()) {
      layerAdded(layerTableModel);
    }
    infoModel.addListener(this);
  }

  public void layerAdded(final LayerTableModel layerTableModel) {
    if (!panels.containsKey(layerTableModel)) {
      FeatureCollectionTablePanel tablePanel = new FeatureCollectionTablePanel(
        workbenchContext);
      tablePanel.setLayerTableModel(layerTableModel);
      panels.put(layerTableModel, tablePanel);
      container.add(tablePanel);
      System.out.println("Comp add");
    }
    SpringUtilities.makeCompactGrid(container, panels.size(), 1, 5, 5, 5, 5);
  }

  public void layerRemoved(final LayerTableModel layerTableModel) {
    JPanel tablePanel = panels.remove(layerTableModel);
    if (tablePanel != null) {
      container.remove(tablePanel);
      
      System.out.println("Comp remove");
    }
    SpringUtilities.makeCompactGrid(container, panels.size(), 1, 5, 5, 5, 5);
  }

}
