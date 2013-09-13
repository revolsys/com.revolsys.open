package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.tree.TreePath;

import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.tree.BaseLayerRendererTreeNodeModel;
import com.revolsys.swing.map.tree.MultipleLayerRendererTreeNodeModel;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.ObjectTreePanel;
import com.revolsys.swing.tree.model.node.ObjectTreeNodeModel;

public class DataObjectLayerStylePanel extends JPanel implements MouseListener {

  private final JScrollPane editStyleContainer = new JScrollPane();

  private final ObjectTree tree;

  public DataObjectLayerStylePanel(final DataObjectLayer layer) {
    setLayout(new BorderLayout());
    final JLabel instructions = new JLabel(
      "<html><p style=\"padding: 2px 3px 2px\">Click on the style from the left to show the edit panel on the right for that style.</p></html>");
    add(instructions, BorderLayout.NORTH);

    final LayerRenderer<? extends Layer> renderer = layer.getRenderer();
    final ObjectTreePanel styleTree = new ObjectTreePanel(renderer,
      new BaseLayerRendererTreeNodeModel(),
      new MultipleLayerRendererTreeNodeModel());
    tree = styleTree.getTree();
    tree.setRootVisible(true);
    tree.setSelectionPath(new TreePath(renderer));
    tree.addMouseListener(this);
    setEditStylePanel(renderer);
    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
      styleTree, editStyleContainer);

    splitPane.setDividerLocation(300);
    setPreferredSize(new Dimension(1000, 400));
    add(splitPane, BorderLayout.CENTER);
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    if (e.getClickCount() == 1 && SwingUtil.isLeftButtonAndNoModifiers(e)) {
      final int x = e.getX();
      final int y = e.getY();
      final TreePath path = tree.getPathForLocation(x, y);
      if (path != null) {
        final ObjectTreeNodeModel<Object, Object> nodeModel = tree.getModel()
          .getNodeModel(path);
        if (nodeModel != null) {
          final Object node = path.getLastPathComponent();
          if (node instanceof LayerRenderer<?>) {
            final LayerRenderer<?> renderer = (LayerRenderer<?>)node;
            setEditStylePanel(renderer);
          }
        }
      }
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
  }

  public void setEditStylePanel(final LayerRenderer<? extends Layer> renderer) {
    final Component view = editStyleContainer.getViewport().getView();
    if (view instanceof ValueField) {
      final ValueField valueField = (ValueField)view;
      valueField.save();
    }
    final ValueField stylePanel = renderer.createStylePanel();
    editStyleContainer.setViewportView(stylePanel);
  }

  public void setSelectedRenderer(final LayerRenderer<?> renderer) {
    final LinkedList<Object> path = new LinkedList<Object>();
    LayerRenderer<?> parent = renderer;
    while (parent != null) {
      path.addFirst(parent);
      parent = parent.getParent();
    }
    if (!path.isEmpty()) {
      tree.setSelectionPath(ObjectTree.createTreePath(path));
    }
    setEditStylePanel(renderer);
  }
}
