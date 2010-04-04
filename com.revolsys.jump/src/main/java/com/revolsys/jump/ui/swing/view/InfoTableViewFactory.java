package com.revolsys.jump.ui.swing.view;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.openjump.swing.factory.component.ComponentFactory;

import com.revolsys.jump.ui.info.CurrentFeatureInfoModel;
import com.revolsys.jump.ui.info.FeatureTablePanel;
import com.revolsys.jump.ui.info.InfoModelToolbar;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.AttributeTab;
import com.vividsolutions.jump.workbench.ui.InfoFrame;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.TaskFrameProxy;

public class InfoTableViewFactory implements ComponentFactory<Component> {
  public static final String KEY = InfoTableViewFactory.class.getName();

  public static final String NAME = "Info Table"; // I18N.get(KEY +".name");

  public static final String TOOL_TIP = I18N.get(KEY + ".toolTip");

  private WorkbenchContext workbenchContext;

  public InfoTableViewFactory(final WorkbenchContext workbenchContext) {
    this.workbenchContext = workbenchContext;
  }

  public Component createComponent() {
    JPanel panel = new JPanel(new BorderLayout());

    TaskFrame taskFrame = ((TaskFrameProxy)workbenchContext.getWorkbench()
      .getFrame()
      .getActiveInternalFrame()).getTaskFrame();
    InfoFrame infoFrame = taskFrame.getInfoFrame();
    CurrentFeatureInfoModel infoModel = (CurrentFeatureInfoModel)infoFrame.getModel();
    AttributeTab featureTablePanel = new AttributeTab(infoModel,
      workbenchContext, taskFrame, taskFrame, false);
    LayerViewPanel layerViewPanel = workbenchContext.getLayerViewPanel();

    panel.add(
      new InfoModelToolbar(infoModel, workbenchContext, layerViewPanel),
      BorderLayout.NORTH);
    panel.add(featureTablePanel, BorderLayout.CENTER);
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
    final Component component) {
   
   }
}
