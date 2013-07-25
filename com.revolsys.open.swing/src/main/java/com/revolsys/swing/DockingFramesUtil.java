package com.revolsys.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.event.CDockableLocationEvent;
import bibliothek.gui.dock.common.event.CDockableLocationListener;
import bibliothek.gui.dock.common.location.CExternalizedLocation;
import bibliothek.gui.dock.common.mode.ExtendedMode;

public class DockingFramesUtil {

  private static final Map<Object, CControl> OBJECT_CONTROLS = new WeakHashMap<Object, CControl>();

  private static final Map<Object, Map<String, CWorkingArea>> OBJECT_WORKING_AREAS = new WeakHashMap<Object, Map<String, CWorkingArea>>();

  public static DefaultSingleCDockable addDockable(
    final CWorkingArea workingArea, final String id, final String title,
    final Component component) {
    if (component == null) {
      return null;
    } else {
      final DefaultSingleCDockable dockable = new DefaultSingleCDockable(id,
        title, component);
      workingArea.add(dockable);
      dockable.setMinimizable(true);
      dockable.setDefaultLocation(ExtendedMode.MINIMIZED, CLocation.base()
        .minimalWest());
      dockable.setVisible(true);
      dockable.addCDockableLocationListener(new CDockableLocationListener() {

        @Override
        public void changed(final CDockableLocationEvent event) {
          final CLocation newLocation = event.getNewLocation();
          if (newLocation != null
            && newLocation.getClass() == CExternalizedLocation.class) {
            final CExternalizedLocation externalLocation = (CExternalizedLocation)newLocation;
            final CLocation oldLocation = event.getOldLocation();
            if (oldLocation == null
              || oldLocation.getClass() != CExternalizedLocation.class) {

              final Dimension size = component.getPreferredSize();
              final int x = externalLocation.getX();
              final int y = externalLocation.getY();
              dockable.setLocation(CLocation.external(x, y, size.width + 20,
                size.height + 60));
            }
          }
        }
      });
      return dockable;
    }
  }

  public static DefaultSingleCDockable addDockable(
    final Object workingAreaObject, final String workingAreaName,
    final String id, final String title, final Component component) {
    final CWorkingArea workingArea = getCWorkingArea(workingAreaObject,
      workingAreaName);
    if (workingArea == null) {
      return null;
    } else {
      return addDockable(workingArea, id, title, component);
    }
  }

  public static CWorkingArea createCWorkingArea(final CControl control,
    final Object object, final String name) {
    final CWorkingArea workingArea = control.createWorkingArea(name);
    control.addDockable(workingArea);
    setCWorkingArea(object, name, workingArea);
    return workingArea;
  }

  public static CWorkingArea createCWorkingArea(final CControl control,
    final Object object, final String name, final CLocation location) {
    final CWorkingArea workingArea = createCWorkingArea(control, object, name);
    if (location != null) {
      workingArea.setDefaultLocation(ExtendedMode.MINIMIZED, CLocation.base()
        .minimalWest());
      workingArea.setDefaultLocation(ExtendedMode.NORMALIZED, location);
      workingArea.setLocation(location);
    }
    workingArea.setVisible(true);
    return workingArea;
  }

  public static CControl getCControl(final Object object) {
    return OBJECT_CONTROLS.get(object);
  }

  public static CWorkingArea getCWorkingArea(final Object object,
    final String name) {
    final Map<String, CWorkingArea> workingAreas = OBJECT_WORKING_AREAS.get(object);
    if (workingAreas == null) {
      return null;
    } else {
      final CWorkingArea workingArea = workingAreas.get(name);
      return workingArea;
    }
  }

  public static void setCControl(final Object object, final CControl control) {
    OBJECT_CONTROLS.put(object, control);
  }

  public static void setCWorkingArea(final Object object, final String name,
    final CWorkingArea workingArea) {
    Map<String, CWorkingArea> workingAreas = OBJECT_WORKING_AREAS.get(object);
    if (workingAreas == null) {
      workingAreas = new HashMap<String, CWorkingArea>();
      OBJECT_WORKING_AREAS.put(object, workingAreas);
    }
    workingAreas.put(name, workingArea);
    final CControl control = workingArea.getControl().getOwner();
    setCControl(object, control);

  }

  public static void setFlapSizes(final CControl control) {
    final CContentArea contentArea = control.getContentArea();
    final Dimension zeroSize = new Dimension(0, 0);
    contentArea.getNorthArea().setPreferredSize(zeroSize);
    contentArea.getEastArea().setPreferredSize(zeroSize);
    contentArea.getSouthArea().setPreferredSize(zeroSize);
  }

}
