package com.revolsys.jump.ui.task;

import java.awt.Component;

import javax.swing.event.InternalFrameListener;

import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.util.Direction;

import org.openjump.swing.factory.component.ComponentFactory;

import com.revolsys.jump.ui.info.CurrentFeatureInfoModel;
import com.revolsys.jump.ui.swing.view.FactoryView;
import com.revolsys.jump.util.DockingUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.ui.InfoModel;
import com.vividsolutions.jump.workbench.ui.PrimaryInfoFrame;

@SuppressWarnings("serial")
public class DockingInfoFrame extends PrimaryInfoFrame {
  private InfoModel model;

  private DockingTaskFrame taskFrame;

  public DockingInfoFrame(final WorkbenchContext workbenchContext,
    final LayerManagerProxy layerManagerProxy, final DockingTaskFrame taskFrame) {
    super(workbenchContext, layerManagerProxy, taskFrame);
    model = new CurrentFeatureInfoModel(layerManagerProxy.getLayerManager());
    this.taskFrame = taskFrame;
    InternalFrameListener[] listeners = getInternalFrameListeners();
    for (int i = 0; i < listeners.length; i++) {
      InternalFrameListener listener = listeners[i];
      removeInternalFrameListener(listener);
    }
  }

  public InfoModel getModel() {
    return model;
  }

  public void surface() {
    ComponentFactory<Component> infoComponentFactory = taskFrame.getInfoComponentFactory();
    RootWindow root = taskFrame.getRootWindow();
    View view = DockingUtil.findView(root, infoComponentFactory);
    if (view == null) {
      view = new FactoryView(infoComponentFactory);
      view.setFocusable(true);
      DockingUtil.addToRootWindow(view, root, Direction.DOWN);
    }
  }
}
