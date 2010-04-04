package com.revolsys.jump.ui.task;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.tree.TreeCellRenderer;

import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewMap;

import org.openjump.swing.factory.component.ComponentFactory;

import com.revolsys.jump.ui.model.LayerTreeModel;
import com.revolsys.jump.ui.model.TreeLayerNamePanel;
import com.revolsys.jump.ui.swing.view.FactoryView;
import com.revolsys.jump.ui.swing.view.LayerPanelComponentFactory;
import com.revolsys.jump.ui.swing.view.MapViewComponentFactory;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.ui.InfoFrame;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerNameRenderer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.DummyTool;
import com.vividsolutions.jump.workbench.ui.renderer.Renderer;

@SuppressWarnings("serial")
public class DockingTaskFrame extends TaskFrame {

  private RootWindow rootWindow;

  private WorkbenchContext workbenchContext;

  private DockingInfoFrame infoFrame;

  private LayerNamePanel layerNamePanel;

  private ComponentFactory<Component> infoComponentFactory;

  private MapViewComponentFactory mapComponentFactory;

  private LayerPanelComponentFactory layersComponentFactory;

  private LayerViewPanel layerViewPanel;

  private Timer timer;

  public DockingTaskFrame(final WorkbenchContext workbenchContext) {
    this.workbenchContext = workbenchContext;
  }

  public void setTask(final Task task) {
    super.setTask(task);
    addInternalFrameListener(new InternalFrameAdapter() {
      public void internalFrameDeactivated(final InternalFrameEvent e) {
        getLayerViewPanel().setCurrentCursorTool(new DummyTool());
      }

      public void internalFrameOpened(final InternalFrameEvent e) {
        try {
          layersComponentFactory.setTaskFrame(DockingTaskFrame.this);
          mapComponentFactory.setTaskFrame(DockingTaskFrame.this);
          createInfoNode();
          // createJavaDocking();
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              try {
                while (getParent() == null) {
                  synchronized (this) {
                    try {
                      wait(100);
                    } catch (InterruptedException e) {
                    }
                  }
                }
                setMaximum(true);
              } catch (PropertyVetoException e1) {
              }
            }
          });
        } finally {
          layersComponentFactory.setTaskFrame(null);
          mapComponentFactory.setTaskFrame(null);
        }
      }

      private void createInfoNode() {
        ViewMap viewMap = new ViewMap();
        View layerNameView = new FactoryView(layersComponentFactory);
        layerNamePanel = (LayerNamePanel)layerNameView.getComponent();
        viewMap.addView(0, layerNameView);

        View layersView = new FactoryView(mapComponentFactory);
        viewMap.addView(1, layersView);

        rootWindow = DockingUtil.createRootWindow(viewMap, true);
        rootWindow.setWindow(new SplitWindow(true, 0.2f, layerNameView,
          layersView));

        getContentPane().add(rootWindow, BorderLayout.CENTER);
      }
    });
    WorkbenchFrame frame = workbenchContext.getWorkbench().getFrame();
    layerViewPanel = new LayerViewPanel(task.getLayerManager(), frame);
    layerViewPanel.addListener(frame.getLayerViewPanelListener());
    layerViewPanel.getViewport().addListener(frame);

    setResizable(true);
    setClosable(true);
    setMaximizable(true);
    setIconifiable(true);

    setSize(800, 600);
    getContentPane().setLayout(new BorderLayout());
    updateTitle();

    task.add(this);
    installAnimator();
  }

  @SuppressWarnings("unchecked")
  protected LayerNamePanel createLayerNamePanel() {
    TreeLayerNamePanel treeLayerNamePanel = new TreeLayerNamePanel(this,
      new LayerTreeModel(this), getLayerViewPanel().getRenderingManager(),
      new HashMap<Class<?>, TreeCellRenderer>());
    Map<Class<?>, JPopupMenu> nodeClassToPopupMenuMap = this.workbenchContext.getWorkbench()
      .getFrame()
      .getNodeClassToPopupMenuMap();
    for (Entry<Class<?>, JPopupMenu> entry : nodeClassToPopupMenuMap.entrySet()) {
      Class<?> nodeClass = entry.getKey();
      JPopupMenu menu = entry.getValue();
      treeLayerNamePanel.addPopupMenu(nodeClass, menu);
    }
    return treeLayerNamePanel;
  }

  public InfoFrame getInfoFrame() {
    if (infoFrame == null) {
      infoFrame = new DockingInfoFrame(workbenchContext, this, this);
    }
    return infoFrame;
  }

  public LayerNamePanel getLayerNamePanel() {
    return layerNamePanel;
  }

  protected View createViewWithToolbar(final String name,
    final JComponent toolBar, final JComponent component) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(component, BorderLayout.CENTER);
    panel.add(toolBar, BorderLayout.NORTH);
    return new View(name, null, panel);

  }

  /**
   * @return the layerViewPanel
   */
  public LayerViewPanel getLayerViewPanel() {
    return layerViewPanel;
  }

  /**
   * @return the root
   */
  public RootWindow getRootWindow() {
    return rootWindow;
  }

  /**
   * @return the infoComponentFactory
   */
  public ComponentFactory<Component> getInfoComponentFactory() {
    return infoComponentFactory;
  }

  public void setInfoComponentFactory(
    final ComponentFactory<Component> infoComponentFactory) {
    this.infoComponentFactory = infoComponentFactory;
  }

  public MapViewComponentFactory getMapComponentFactory() {
    return mapComponentFactory;
  }

  public void setMapComponentFactory(
    final MapViewComponentFactory mapComponentFactory) {
    this.mapComponentFactory = mapComponentFactory;
  }

  public LayerPanelComponentFactory getLayersComponentFactory() {
    return layersComponentFactory;
  }

  public void setLayersComponentFactory(
    final LayerPanelComponentFactory layersComponentFactory) {
    this.layersComponentFactory = layersComponentFactory;
  }

  protected void installAnimator() {
    timer = new Timer(500, new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if (clockedRenderingInProgress()) {
          repaint();
        } else if (clocksShown()) {
          repaint();
        }
      }

      @SuppressWarnings("unchecked")
      private boolean clockedRenderingInProgress() {
        LayerManager layerManager = getLayerManager();
        List<Layerable> layerables = layerManager.getLayerables(Layerable.class);
        for (Layerable layerable : layerables) {
          if (!layerable.getBlackboard().get(
            LayerNameRenderer.USE_CLOCK_ANIMATION_KEY, false)) {
            continue;
          }
          Renderer renderer = layerViewPanel.getRenderingManager().getRenderer(
            layerable);
          if (renderer != null && renderer.isRendering()) {
            return true;
          }
        }
        return false;
      }

      // Previously we had a flag to keep track of whether
      // clocks were displayed. However that was not sufficient,
      // as quick-rendering layers were missed by the timer,
      // and thus the clock icon, if painted (e.g. by #zoomChanged
      // in TreeLayerNamePanel), would not be cleared. So here
      // we do a more thorough check for whether any clocks are
      // displayed. [Jon Aquino 2005-03-14]
      @SuppressWarnings("unchecked")
      private boolean clocksShown() {
        LayerManager layerManager = getLayerManager();
        List<Layerable> layerables = layerManager.getLayerables(Layerable.class);
        for (Layerable layerable : layerables) {
          Blackboard blackboard = layerable.getBlackboard();
          if (blackboard.get(LayerNameRenderer.PROGRESS_ICON_KEY) != null) {
            return true;
          }
        }
        return false;
      }
    });
    timer.setCoalesce(true);
    timer.start();
  }
}
