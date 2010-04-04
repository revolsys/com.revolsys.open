package com.revolsys.jump.util;

import java.awt.Component;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.util.Direction;

import org.openjump.swing.factory.component.ComponentFactory;

import com.revolsys.jump.ui.swing.view.FactoryView;

public final class DockingUtil {
  private DockingUtil() {
  }

  public static View findView(final DockingWindow parent,
    final ComponentFactory<Component> viewFactory) {
    for (int i = 0; i < parent.getChildWindowCount(); i++) {
      DockingWindow child = parent.getChildWindow(i);
      if (child instanceof FactoryView) {
        FactoryView factoryView = (FactoryView)child;
        if (factoryView.getFactory() == viewFactory) {
          return factoryView;
        }
      }
      View view = findView(child, viewFactory);
      if (view != null) {
        return view;
      }
    }
    return null;
  }

  public static void addToRootWindow(final DockingWindow newWindow,
    final RootWindow root, final Direction direction) {
    if (root != null) {
      DockingWindow topWindow = root.getWindow();
      if (topWindow == null) {
        root.setWindow(newWindow);
      } else {
        if (direction == Direction.RIGHT) {
          insertWindow(topWindow, newWindow, 1, true, direction);
        } else if (direction == Direction.LEFT) {
          insertWindow(topWindow, newWindow, 0, true, direction);
        } else if (direction == Direction.UP) {
          insertWindow(topWindow, newWindow, 0, false, direction);
        } else if (direction == Direction.DOWN) {
          insertWindow(topWindow, newWindow, 1, false, direction);
        }

      }
    }
  }

  public static void insertWindow(final DockingWindow topWindow,
    final DockingWindow newWindow, final int index, final boolean horizontal,
    final Direction direction) {
    if (topWindow instanceof SplitWindow) {
      SplitWindow splitWindow = (SplitWindow)topWindow;
      if (splitWindow.isHorizontal() == horizontal) {
        DockingWindow childWindow = splitWindow.getChildWindow(index);
        addTab(childWindow, newWindow);
      } else {
        topWindow.split(newWindow, direction, 0.8f);
      }
    } else {
      topWindow.split(newWindow, direction, 0.8f);
    }
  }

  public static void addTab(final DockingWindow oldWindow,
    final DockingWindow newWindow) {
    if (oldWindow instanceof TabWindow) {
      TabWindow tabWindow = (TabWindow)oldWindow;
      tabWindow.addTab(newWindow);
    } else {
      TabWindow tabWindow = new TabWindow(new DockingWindow[] {
        oldWindow, newWindow
      });
      DockingWindow parentWindow = oldWindow.getWindowParent();
      parentWindow.replaceChildWindow(oldWindow, tabWindow);
    }

  }

}
