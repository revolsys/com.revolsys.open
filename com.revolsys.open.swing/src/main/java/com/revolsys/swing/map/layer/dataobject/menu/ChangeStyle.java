package com.revolsys.swing.map.layer.dataobject.menu;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.panel.GeometryStylePanel;
import com.revolsys.swing.tree.TreeUtil;

public class ChangeStyle extends AbstractAction {

  public ChangeStyle() {
    putValue(NAME, "Change Style");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final Object source = event.getSource();
    Window window;
    if (source instanceof Component) {
      final Component component = (Component)source;
      window = SwingUtilities.getWindowAncestor(component);
    } else {
      window = null;
    }

    final GeometryStyleRenderer renderer = TreeUtil.getFirstSelectedNode(
      source, GeometryStyleRenderer.class);
    if (renderer != null) {
      GeometryStylePanel panel = renderer.createStylePanel();
      panel.showDialog(window);
    }
  }
}
