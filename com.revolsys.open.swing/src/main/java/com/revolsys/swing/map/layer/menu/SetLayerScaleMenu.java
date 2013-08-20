package com.revolsys.swing.map.layer.menu;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.ComponentFactory;
import com.revolsys.swing.map.component.MapScale;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.util.ExceptionUtil;

public class SetLayerScaleMenu implements ComponentFactory<JMenu> {
  private static final double[] SCALES = {
    0.0, 16000000.0, 8000000.0, 4000000.0, 2000000.0, 1000000.0, 500000.0,
    250000.0, 125000.0, 50000.0, 20000.0, 10000.0, 5000.0, 2500.0, 2000.0,
    1000.0
  };

  private final boolean min;

  public SetLayerScaleMenu(final boolean min) {
    this.min = min;
  }

  @Override
  public SetLayerScaleMenu clone() {
    try {
      return (SetLayerScaleMenu)super.clone();
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
    final Layer layer = ObjectTree.getMouseClickItem();
    if (layer != null) {
      if (layer.isHasGeometry()) {
        final JMenu menu = new JMenu(name);
        double layerScale;
        if (this.min) {
          layerScale = layer.getMinimumScale();
        } else {
          layerScale = layer.getMaximumScale();
          if (layerScale == Long.MAX_VALUE) {
            layerScale = 0;
          }
        }
        for (final double scale : SCALES) {
          String label;
          if (scale == 0) {
            label = "Unlimited";
          } else {
            label = MapScale.formatScale(scale);
          }
          final InvokeMethodAction action = new InvokeMethodAction(label, this,
            "setScale", scale);
          final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(action);
          final boolean selected = scale == layerScale;
          menuItem.setSelected(selected);
          menu.add(menuItem);
        }
        return menu;
      }
    }
    return null;
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getToolTip() {
    return null;
  }

  public void setScale(final double scale) {
    final Layer layer = ObjectTree.getMouseClickItem();
    if (layer != null) {
      if (this.min) {
        layer.setMinimumScale((long)scale);
      } else {
        layer.setMaximumScale((long)scale);
      }
    }
  }

}
