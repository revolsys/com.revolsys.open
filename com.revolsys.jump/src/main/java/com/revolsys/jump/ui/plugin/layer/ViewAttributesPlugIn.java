package com.revolsys.jump.ui.plugin.layer;

import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.util.Direction;

import org.openjump.core.ui.plugin.AbstractUiPlugIn;

import com.revolsys.jump.ui.swing.view.FactoryView;
import com.revolsys.jump.ui.swing.view.LayerTableComponentFactory;
import com.revolsys.jump.ui.task.DockingTaskFrame;
import com.revolsys.jump.util.DockingUtil;
import com.revolsys.jump.util.WorkbenchUtil;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;

public class ViewAttributesPlugIn extends AbstractUiPlugIn {
  private static final String NAME = I18N.get("ui.plugin.ViewAttributesPlugIn.view-edit-attributes");

  private Map<Layer, LayerTableComponentFactory> factories = new WeakHashMap<Layer, LayerTableComponentFactory>();

  private WorkbenchContext workbenchContext;

  public void initialize(
    final PlugInContext context)
    throws Exception {
    workbenchContext = context.getWorkbenchContext();

    JUMPWorkbench workbench = workbenchContext.getWorkbench();
    WorkbenchFrame frame = workbench.getFrame();
    JPopupMenu layerNamePopupMenu = frame.getLayerNamePopupMenu();

    MultiEnableCheck enableCheck = createEnableCheck(workbenchContext);
    ImageIcon icon = getIcon();
    WorkbenchUtil.replaceMenuItem(workbenchContext, layerNamePopupMenu, NAME,
      NAME, this, icon, enableCheck);

  }

  public boolean execute(
    final PlugInContext context)
    throws Exception {
    reportNothingToUndoYet(context);
    WorkbenchFrame workbenchFrame = context.getWorkbenchFrame();
    DockingTaskFrame taskFrame = (DockingTaskFrame)workbenchFrame.getActiveInternalFrame();
    final Layer layer = context.getSelectedLayer(0);
    final LayerManager layerManager = taskFrame.getLayerManager();
    Category category = layerManager.getCategory(layer);
    LayerTableComponentFactory factory = factories.get(layer);
    if (factory == null) {
      factory = new LayerTableComponentFactory(workbenchContext, category,
        layer);
      factories.put(layer, factory);
    }
    RootWindow root = taskFrame.getRootWindow();
    View view = DockingUtil.findView(root, factory);
    if (view == null) {
      final View newView = new FactoryView(factory);
      newView.setFocusable(true);
      DockingUtil.addToRootWindow(newView, root, Direction.DOWN);
      layerManager.addLayerListener(new LayerListener() {
        public void categoryChanged(
          final CategoryEvent e) {
        }

        public void featuresChanged(
          final FeatureEvent e) {
        }

        public void layerChanged(
          final LayerEvent e) {
          if (e.getLayerable() == layer) {
            if (e.getType() == LayerEventType.REMOVED) {
              newView.close();
              layerManager.removeLayerListener(this);
            }
          }
        }
      });
    }

    return true;
  }

  public MultiEnableCheck createEnableCheck(
    final WorkbenchContext workbenchContext) {
    EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
    return new MultiEnableCheck().add(
      checkFactory.createTaskWindowMustBeActiveCheck()).add(
      checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
  }

  public ImageIcon getIcon() {
    return IconLoader.icon("Row.gif");
  }

  public String getName() {
    return NAME;
  }
}
