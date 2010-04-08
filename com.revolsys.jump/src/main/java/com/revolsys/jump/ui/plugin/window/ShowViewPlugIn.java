package com.revolsys.jump.ui.plugin.window;

import java.awt.Component;

import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.util.Direction;

import org.openjump.swing.factory.component.ComponentFactory;

import com.revolsys.jump.ui.swing.view.FactoryView;
import com.revolsys.jump.ui.task.DockingTaskFrame;
import com.revolsys.jump.util.DockingUtil;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.plugin.FeatureInstaller;

public class ShowViewPlugIn extends AbstractPlugIn {
  public static final String SHOW_VIEW_MENU = "Show View";

  private ComponentFactory<Component> viewFactory;

  private Direction direction;

  public ShowViewPlugIn(final ComponentFactory<Component> viewFactory,
    final Direction direction) {
    this.viewFactory = viewFactory;
    this.direction = direction;
  }

  public void initialize(final PlugInContext context) throws Exception {

    FeatureInstaller featureInstaller = context.getFeatureInstaller();
    JMenu windowMenu = featureInstaller.menuBarMenu(MenuNames.WINDOW);
    JMenu showViewMenu = menuBarMenu(windowMenu.getPopupMenu(), SHOW_VIEW_MENU);
    if (showViewMenu == null) {
      windowMenu.insert(new JMenu(SHOW_VIEW_MENU), 0);
    }
    featureInstaller.addMainMenuItem(this, new String[] {
      MenuNames.WINDOW, SHOW_VIEW_MENU
    }, viewFactory.getName(), false, null, new EnableCheckFactory(
      context.getWorkbenchContext()).createTaskWindowMustBeActiveCheck());
  }

  public JMenu menuBarMenu(final JPopupMenu menu, final String childName) {
    MenuElement[] subElements = menu.getSubElements();
    for (int i = 0; i < subElements.length; i++) {
      if (!(subElements[i] instanceof JMenuItem)) {
        continue;
      }
      JMenuItem menuItem = (JMenuItem)subElements[i];
      if (menuItem.getText().equals(childName)) {
        return (JMenu)menuItem;
      }
    }
    return null;
  }

  public boolean execute(final PlugInContext context) throws Exception {
    JInternalFrame frame = context.getWorkbenchFrame().getActiveInternalFrame();
    if (frame instanceof DockingTaskFrame) {
      DockingTaskFrame taskFrame = (DockingTaskFrame)frame;
      RootWindow root = taskFrame.getRootWindow();
      if (root != null) {
        View view = DockingUtil.findView(root, viewFactory);
        if (view == null) {
          view = new FactoryView(viewFactory);
          view.addListener(new DockingWindowAdapter() {
            @Override
            public void windowClosed(
              final DockingWindow view) {
              if (view instanceof FactoryView) {
                FactoryView factoryView = (FactoryView)view;
                ComponentFactory<Component> factory = factoryView.getFactory();
                Component component = factoryView.getComponent();
                factory.close(component);
              }
            }
          });
          view.setFocusable(true);
          DockingUtil.addToRootWindow(view, root, direction);
        }
      }
    }
    return true;
  }

}
