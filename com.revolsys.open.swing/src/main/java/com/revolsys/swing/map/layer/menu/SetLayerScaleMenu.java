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

public class SetLayerScaleMenu implements ComponentFactory<JMenu> {
  private static final double[] SCALES = {
    0.0, 16000000.0, 8000000.0, 4000000.0, 2000000.0, 1000000.0, 500000.0,
    250000.0, 125000.0, 50000.0, 20000.0, 10000.0, 5000.0, 2500.0, 2000.0,
    1000.0
  };

  private boolean min;

  public SetLayerScaleMenu(boolean min) {
    this.min = min;
  }

  public void setScale(double scale) {
    Layer layer = ObjectTree.getMouseClickItem();
    if (layer != null) {
      if (min) {
        layer.setMinimumScale((long)scale);
      } else {
        layer.setMaximumScale((long)scale);
      }
    }
  }

  @Override
  public void close(Component component) {
  }

  @Override
  public JMenu createComponent() {
    String name;
    if (min) {
      name = "Hide zoomed out beyond (minimum) scale";
    } else {
      name = "Hide zoomed in beyond (maximum) scale";
    }
    JMenu menu = new JMenu(name);
    Layer layer = ObjectTree.getMouseClickItem();
    if (layer != null) {
      double layerScale;
      if (min) {
        layerScale = layer.getMinimumScale();
      } else {
        layerScale = layer.getMaximumScale();
        if (layerScale == Long.MAX_VALUE) {
          layerScale = 0;
        }
      }
      for (double scale : SCALES) {
        String label;
        if (scale == 0) {
          label = "Unlimited";
        } else {
          label = MapScale.formatScale(scale);
        }
        InvokeMethodAction action = new InvokeMethodAction(label, this,
          "setScale", scale);
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(action);
        boolean selected = scale == layerScale;
        menuItem.setSelected(selected);
        menu.add(menuItem);
      }
    }
    return menu;
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

}
