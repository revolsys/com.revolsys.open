package com.revolsys.jump.ui.swing.view;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;

import org.openjump.core.ui.enablecheck.BooleanPropertyEnableCheck;
import org.openjump.swing.listener.InvokeMethodActionListener;
import org.openjump.swing.listener.InvokeMethodListSelectionListener;

import com.revolsys.jump.ui.info.FeatureCollectionTablePanel;
import com.revolsys.jump.ui.swing.listener.InvokeMethodTableModelListener;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.EnableableToolBar;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;

@SuppressWarnings("serial")
public class LayerTableToolBar extends EnableableToolBar {

  private JTextField featureIndexField;

  private JTextPane featureCountPane;

  private JTable table;

  public LayerTableToolBar(final FeatureCollectionTablePanel attributePanel) {
    this.table = attributePanel.getTable();

    EnableCheck rowEnableCheck = new BooleanPropertyEnableCheck(attributePanel,
      "isRowSelected", true);

    featureIndexField = new JTextField();
    Dimension textSize = new Dimension(60,
      featureIndexField.getPreferredSize().height);
    featureIndexField.setMinimumSize(textSize);
    featureIndexField.setMaximumSize(textSize);
    featureIndexField.setSize(textSize);
    featureIndexField.setPreferredSize(textSize);
    add(featureIndexField);

    featureCountPane = new JTextPane();
    featureCountPane.setMinimumSize(textSize);
    featureCountPane.setMaximumSize(textSize);
    featureCountPane.setSize(textSize);
    featureCountPane.setPreferredSize(textSize);
    featureCountPane.setOpaque(false);
    add(featureCountPane);

    add(new JButton(), I18N.get("ui.AttributeTab.zoom-to-first-row"),
      IconLoader.icon("Start.gif"), new InvokeMethodActionListener(
        attributePanel, "zoomToFirst"), new MultiEnableCheck().add(
        new BooleanPropertyEnableCheck(attributePanel, "isAtFirst", false))
        .add(rowEnableCheck));

    add(new JButton(), I18N.get("ui.AttributeTab.zoom-to-previous-row"),
      IconLoader.icon("Prev.gif"), new InvokeMethodActionListener(
        attributePanel, "zoomToPrevious"), new MultiEnableCheck().add(
        new BooleanPropertyEnableCheck(attributePanel, "isAtFirst", false))
        .add(rowEnableCheck));

    add(new JButton(), I18N.get("ui.AttributeTab.zoom-to-next-row"),
      IconLoader.icon("Next.gif"), new InvokeMethodActionListener(
        attributePanel, "zoomToNext"), new MultiEnableCheck().add(
        new BooleanPropertyEnableCheck(attributePanel, "isAtLast", false)).add(
        rowEnableCheck));

    add(new JButton(), I18N.get("ui.AttributeTab.zoom-to-last-row"),
      IconLoader.icon("End.gif"), new InvokeMethodActionListener(
        attributePanel, "zoomToLast"), new MultiEnableCheck().add(
        new BooleanPropertyEnableCheck(attributePanel, "isAtLast", false)).add(
        rowEnableCheck));

    addSeparator();

    add(new JButton(), I18N.get("ui.AttributeTab.zoom-to-selected-rows"),
      IconLoader.icon("SmallMagnify.gif"), new InvokeMethodActionListener(
        attributePanel, "zoomToSelected"), rowEnableCheck);

    add(new JButton(), I18N.get("ui.AttributeTab.select-in-task-window"),
      IconLoader.icon("SmallSelect.gif"), new InvokeMethodActionListener(
        attributePanel, "selectInLayerViewPanel"), rowEnableCheck);

    add(new JButton(), I18N.get("ui.AttributeTab.flash-selected-rows"),
      IconLoader.icon("Flashlight.gif"), new InvokeMethodActionListener(
        attributePanel, "flashSelectedFeatures"), rowEnableCheck);

    // FeatureInfoPlugIn featureInfoPlugIn = new FeatureInfoPlugIn();
    // add(new JButton(), featureInfoPlugIn.getName(),
    // GUIUtil.toSmallIcon(FeatureInfoTool.ICON),
    // FeatureInfoPlugIn.toActionListener(featureInfoPlugIn, workbenchContext,
    // null), FeatureInfoPlugIn.createEnableCheck(workbenchContext));
    updateEnabledState();
    table.getModel().addTableModelListener(
      new InvokeMethodTableModelListener(this, "updateEnabledState"));
    ListSelectionModel selectionModel = table.getSelectionModel();
    selectionModel.addListSelectionListener(new InvokeMethodListSelectionListener(
      this, "updateEnabledState"));
  }

  @Override
  public void updateEnabledState() {
    super.updateEnabledState();
    int selectedRow = table.getSelectedRow();
    if (selectedRow >= 0) {
      featureIndexField.setText(String.valueOf(selectedRow + 1));
    } else {
      featureIndexField.setText("");
    }
    int numRows = table.getRowCount();
    if (numRows > 0) {
      featureCountPane.setText(" of " + numRows);
    } else {
      featureCountPane.setText(" of 0");
    }
  }

}
