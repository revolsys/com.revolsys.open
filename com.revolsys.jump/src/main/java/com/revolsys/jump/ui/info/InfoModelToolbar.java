package com.revolsys.jump.ui.info;

import java.awt.Dimension;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.openjump.core.ui.enablecheck.BooleanPropertyEnableCheck;
import org.openjump.swing.listener.InvokeMethodActionListener;

import com.revolsys.jump.ui.enablecheck.PropertyEnableCheck;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.EnableableToolBar;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.InfoModelListener;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;

@SuppressWarnings("serial")
public class InfoModelToolbar extends EnableableToolBar implements
  CurrentFeatureListener, InfoModelListener {
  private JTextField featureIndexField;

  private JTextPane featureCountPane;

  private CurrentFeatureInfoModel infoModel;

  private ZoomToSelectedItemsPlugIn zoomToSelectedItemsPlugIn = new ZoomToSelectedItemsPlugIn();

  private ErrorHandler errorHandler;

  private LayerViewPanel layerViewPanel;

  public InfoModelToolbar(final CurrentFeatureInfoModel infoModel,
    final WorkbenchContext context, final LayerViewPanel layerViewPanel) {
    this.infoModel = infoModel;
    this.errorHandler = context.getErrorHandler();
    this.layerViewPanel = layerViewPanel;

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

    // Record navigation buttons
    add(new JButton(), I18N.get("ui.GeometryInfoTab.attributes"),
      IconLoader.icon("Start.gif"), new InvokeMethodActionListener(infoModel,
        "firstFeature"), new BooleanPropertyEnableCheck(infoModel, "isAtFirst",
        false));
    add(new JButton(), I18N.get("ui.GeometryInfoTab.attributes"),
      IconLoader.icon("Prev.gif"), new InvokeMethodActionListener(infoModel,
        "previousFeature"), new BooleanPropertyEnableCheck(infoModel,
        "isAtFirst", false));
    add(new JButton(), I18N.get("ui.GeometryInfoTab.attributes"),
      IconLoader.icon("Next.gif"), new InvokeMethodActionListener(infoModel,
        "nextFeature"), new BooleanPropertyEnableCheck(infoModel, "isAtLast",
        false));
    add(new JButton(), I18N.get("ui.GeometryInfoTab.attributes"),
      IconLoader.icon("End.gif"), new InvokeMethodActionListener(infoModel,
        "lastFeature"), new BooleanPropertyEnableCheck(infoModel, "isAtLast",
        false));

    addSeparator();

    EnableCheck currentFeatureEnableCheck = new PropertyEnableCheck(infoModel,
      "getCurrentFeature", null, true, errorHandler);
    add(new JButton(), I18N.get("ui.AttributeTab.zoom-to-selected-rows"),
      IconLoader.icon("SmallMagnify.gif"), new InvokeMethodActionListener(this,
        "zoom"), currentFeatureEnableCheck);

    add(new JButton(), I18N.get("ui.AttributeTab.select-in-task-window"),
      IconLoader.icon("SmallSelect.gif"), new InvokeMethodActionListener(this,
        "selectInLayerViewPanel"), currentFeatureEnableCheck);

    add(new JButton(), I18N.get("ui.AttributeTab.flash-selected-rows"),
      IconLoader.icon("Flashlight.gif"), new InvokeMethodActionListener(this,
        "flashSelectedFeatures"), currentFeatureEnableCheck);

    infoModel.addListener(this);
    infoModel.addCurrentFeatureListener(this);
  }

  public void selectInLayerViewPanel() {
    SelectionManager selectionManager = layerViewPanel.getSelectionManager();
    selectionManager.clear();
    selectionManager.getFeatureSelection().selectItems(
      infoModel.getCurrentLayer(),
      Collections.singletonList(infoModel.getCurrentFeature()));
  }

  public void flashSelectedFeatures() throws NoninvertibleTransformException {
    Feature currentFeature = infoModel.getCurrentFeature();
    Geometry geometry = currentFeature.getGeometry();
    zoomToSelectedItemsPlugIn.flash(Collections.singletonList(geometry),
      layerViewPanel);
  }

  public void zoom() throws NoninvertibleTransformException {
    Feature currentFeature = infoModel.getCurrentFeature();
    Geometry geometry = currentFeature.getGeometry();
    zoomToSelectedItemsPlugIn.zoom(Collections.singletonList(geometry),
      layerViewPanel);
  }

  public void layerAdded(final LayerTableModel layerTableModel) {
    update();
  }

  public void layerRemoved(final LayerTableModel layerTableModel) {
    update();
  }

  public void featureSelected(final Layer layer, final Feature feature) {
    update();
  }

  private void update() {
    Feature currentFeature = infoModel.getCurrentFeature();
    int featureCount = infoModel.getFeatureCount();
    if (currentFeature != null) {
      int featureIndex = infoModel.getFeatureIndex() + 1;
      featureIndexField.setText(String.valueOf(featureIndex));
      featureCountPane.setText(" of " + featureCount);
    } else {
      featureCount = 0;
      featureIndexField.setText("0");
      featureCountPane.setText(" of 0");
    }
    updateEnabledState();
  }
}
