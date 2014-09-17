package com.revolsys.swing.map.layer.menu;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.SwingConstants;

import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.ComponentFactory;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.component.MapScale;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.record.renderer.AbstractRecordLayerRenderer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.ExceptionUtil;

public class TreeItemScaleMenu implements ComponentFactory<JMenu> {

  private final boolean min;

  private final EnableCheck enableCheck;

  public TreeItemScaleMenu(final boolean min, final EnableCheck enableCheck) {
    this.min = min;
    this.enableCheck = enableCheck;
  }

  protected void addScaleMenuItem(final long layerScale, final Object object,
    final String methodName, final JMenu menu, final long scale) {
    final String label;
    if (scale <= 0) {
      label = "Unlimited";
    } else {
      label = MapScale.formatScale(scale);
    }

    final InvokeMethodAction action = new InvokeMethodAction(label, object,
      methodName, scale);
    final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(action);
    final boolean selected = scale == layerScale;
    menuItem.setSelected(selected);
    menuItem.setPreferredSize(new Dimension(140, 22));
    menuItem.setHorizontalTextPosition(SwingConstants.RIGHT);
    menu.add(menuItem);
  }

  @Override
  public TreeItemScaleMenu clone() {
    try {
      return (TreeItemScaleMenu)super.clone();
    } catch (final CloneNotSupportedException e) {
      return ExceptionUtil.throwUncheckedException(e);
    }
  }

  @Override
  public void close(final Component component) {
  }

  @Override
  public JMenu createComponent() {
    String name;
    if (this.min) {
      name = "Hide zoomed out beyond (minimum) scale";
    } else {
      name = "Hide zoomed in beyond (maximum) scale";
    }

    final JMenu menu = new JMenu(name);
    if (this.enableCheck == null || this.enableCheck.isEnabled()) {
      long layerScale = 0;
      final Object object = MenuFactory.getMenuSource();
      if (object instanceof Layer) {
        final Layer layer = (Layer)object;
        if (layer.isHasGeometry()) {
          if (this.min) {
            layerScale = layer.getMinimumScale();
          } else {
            layerScale = layer.getMaximumScale();
          }
        }
      } else if (object instanceof AbstractRecordLayerRenderer) {
        final AbstractRecordLayerRenderer renderer = (AbstractRecordLayerRenderer)object;
        if (this.min) {
          layerScale = renderer.getMinimumScale();
        } else {
          layerScale = renderer.getMaximumScale();
        }
      }
      String methodName;
      if (this.min) {
        methodName = "setMinimumScale";
      } else {
        methodName = "setMaximumScale";
      }
      if (layerScale == Long.MAX_VALUE) {
        layerScale = 0;
      }
      addScaleMenuItem(layerScale, object, methodName, menu, 0);
      boolean scaleIncluded = layerScale == 0;
      for (final long scale : MapPanel.SCALES) {
        if (layerScale == scale) {
          scaleIncluded = true;
        } else if (!scaleIncluded) {
          if (layerScale > scale) {
            addScaleMenuItem(layerScale, object, methodName, menu, layerScale);
            scaleIncluded = true;
          }
        }
        addScaleMenuItem(layerScale, object, methodName, menu, scale);
      }
      if (!scaleIncluded) {
        addScaleMenuItem(layerScale, object, methodName, menu, layerScale);
      }
    } else {
      menu.setEnabled(false);
    }
    return menu;
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getIconName() {
    return null;
  }

  @Override
  public String getName() {
    if (this.min) {
      return "Hide zoomed out beyond (minimum) scale";
    } else {
      return "Hide zoomed in beyond (maximum) scale";
    }
  }

  @Override
  public String getToolTip() {
    return null;
  }
}
