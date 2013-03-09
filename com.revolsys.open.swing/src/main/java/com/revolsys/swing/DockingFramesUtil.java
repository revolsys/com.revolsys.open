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
import bibliothek.gui.dock.common.intern.CControlAccess;
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
      final CControlAccess controlAccess = workingArea.getControl();
      final CControl control = controlAccess.getOwner();
      final DefaultSingleCDockable dockable = new DefaultSingleCDockable(id,
        title, component);
      control.addDockable(dockable);
      dockable.setWorkingArea(workingArea);
      dockable.setMinimizable(true);
      dockable.setDefaultLocation(ExtendedMode.MINIMIZED, CLocation.base()
        .minimalWest());
      dockable.setVisible(true);
      return dockable;
    }
  }

  public static DefaultSingleCDockable addDockable(
    final Object workingAreaObject, String workingAreaName, final String id,
    final String title, final Component component) {
    CWorkingArea workingArea = getCWorkingArea(workingAreaObject,
      workingAreaName);
    if (workingArea == null) {
      throw new IllegalArgumentException("Cannot find working area "
        + workingAreaName + " for " + workingAreaObject);
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
    final Object object, final String name, CLocation location) {
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

  public static void setCWorkingArea(final Object object, final String name,
    final CWorkingArea workingArea) {
    Map<String, CWorkingArea> workingAreas = OBJECT_WORKING_AREAS.get(object);
    if (workingAreas == null) {
      workingAreas = new HashMap<String, CWorkingArea>();
      OBJECT_WORKING_AREAS.put(object, workingAreas);
    }
    workingAreas.put(name, workingArea);
    CControl control = workingArea.getControl().getOwner();
    setCControl(object, control);

  }

  public static void setCControl(final Object object, final CControl control) {
    OBJECT_CONTROLS.put(object, control);
  }

  public static CControl getCControl(final Object object) {
    return OBJECT_CONTROLS.get(object);
  }

  public static void setFlapSizes(final CControl control) {
    final CContentArea contentArea = control.getContentArea();
    final Dimension zeroSize = new Dimension(0, 0);
    contentArea.getNorthArea().setPreferredSize(zeroSize);
    contentArea.getEastArea().setPreferredSize(zeroSize);
    contentArea.getSouthArea().setPreferredSize(zeroSize);
  }

}
