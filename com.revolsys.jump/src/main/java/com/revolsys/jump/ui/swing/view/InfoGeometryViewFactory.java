package com.revolsys.jump.ui.swing.view;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.openjump.swing.factory.component.ComponentFactory;

import com.revolsys.jump.ui.info.CurrentFeatureInfoModel;
import com.revolsys.jump.ui.info.CurrentGeometryViewPanel;
import com.revolsys.jump.ui.info.InfoModelToolbar;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.InfoFrame;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.TaskFrameProxy;

public class InfoGeometryViewFactory implements ComponentFactory<Component> {
  public static final String KEY = InfoGeometryViewFactory.class.getName();

  public static final String NAME = "Info Geometry"; // I18N.get(KEY +".name");

  public static final String TOOL_TIP = I18N.get(KEY + ".toolTip");

  private WorkbenchContext workbenchContext;

  public InfoGeometryViewFactory(
    final WorkbenchContext workbenchContext) {
    this.workbenchContext = workbenchContext;
  }

  public Component createComponent() {
    JPanel panel = new JPanel(new BorderLayout());

    CurrentGeometryViewPanel geometryPanel = new CurrentGeometryViewPanel(
      workbenchContext);
    LayerViewPanel layerViewPanel = workbenchContext.getLayerViewPanel();
    InfoFrame infoFrame = ((TaskFrameProxy)workbenchContext.getWorkbench()
      .getFrame()
      .getActiveInternalFrame()).getTaskFrame().getInfoFrame();
    CurrentFeatureInfoModel infoModel = (CurrentFeatureInfoModel)infoFrame.getModel();
    infoModel.addCurrentFeatureListener(geometryPanel);

    panel.add(
      new InfoModelToolbar(infoModel, workbenchContext, layerViewPanel),
      BorderLayout.NORTH);
    panel.add(geometryPanel, BorderLayout.CENTER);
    return panel;
  }

  public Icon getIcon() {
    return null;
  }

  public String getName() {
    return NAME;
  }

  public String getToolTip() {
    return TOOL_TIP;
  }

  public void close(
    Component component) {
    // TODO Auto-generated method stub

  }
}
