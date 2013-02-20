package com.revolsys.swing.map.layer.menu;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.map.component.MapScale;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.tree.ObjectTree;

@SuppressWarnings("serial")
public class SetLayerScaleMenu extends JMenu implements MenuListener {
  private static final double[] SCALES = {
    0.0, 16000000.0, 8000000.0, 4000000.0, 2000000.0, 1000000.0, 500000.0,
    250000.0, 125000.0, 50000.0, 20000.0, 10000.0, 5000.0, 2500.0, 2000.0,
    1000.0
  };

  private boolean min;

  public SetLayerScaleMenu(boolean min) {
    addMenuListener(this);
    if (min) {
      setText("Minimum Display Scale");
    } else {
      setText("Maximum Display Scale");
    }
    this.min = min;
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
      add(menuItem);
    }
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
  public void menuSelected(MenuEvent e) {
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
      for (int i = 0; i < getItemCount(); i++) {
        JMenuItem menuItem = getItem(i);
        double scale = SCALES[i];
        boolean selected = scale == layerScale;
        menuItem.setSelected(selected);
      }
    }
  }

  @Override
  public void menuDeselected(MenuEvent e) {
  }

  @Override
  public void menuCanceled(MenuEvent e) {
  }

}
