package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.tree.TreePath;

import com.revolsys.collection.list.Lists;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.node.ListTreeNode;
import com.revolsys.swing.tree.node.layer.LayerRendererTreeNode;
import com.revolsys.util.Property;

public class LayerStylePanel extends ValueField implements PropertyChangeListener {

  private static final long serialVersionUID = 1L;

  private LayerRendererTreeNode currentNode;

  private final JScrollPane editStyleContainer = new JScrollPane();

  private final AbstractLayer layer;

  private final ListTreeNode rootNode;

  private LayerRenderer<? extends Layer> rootRenderer;

  private final BaseTree tree;

  public LayerStylePanel(final AbstractLayer layer) {
    this.layer = layer;
    setLayout(new BorderLayout());
    this.rootRenderer = layer.getRenderer().clone();
    Property.removeAllListeners(this.rootRenderer);
    this.rootRenderer.setEditing(true);
    Property.addListener(this.rootRenderer, this);

    this.rootNode = new ListTreeNode(new LayerRendererTreeNode(this.rootRenderer));

    this.tree = new BaseTree(this.rootNode);
    this.tree.setProperty("treeType", LayerStylePanel.class.getName());
    this.tree.setRootVisible(false);
    final TreePath rendererPath = this.rootNode.getTreePath();
    this.tree.setSelectionPath(rendererPath);
    setEditStylePanel(this.rootRenderer);

    this.editStyleContainer.setOpaque(false);
    this.editStyleContainer.getViewport().setOpaque(false);
    this.editStyleContainer.setBorder(null);
    this.editStyleContainer.setViewportBorder(null);

    final JScrollPane treeScroll = new JScrollPane(this.tree);
    treeScroll.setBorder(null);
    treeScroll.setViewportBorder(null);
    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll,
      this.editStyleContainer);

    splitPane.setDividerLocation(200);
    setPreferredSize(new Dimension(810, 600));
    add(splitPane, BorderLayout.CENTER);
    this.tree.addTreeSelectionListener(e -> {
      final TreePath path = e.getPath();
      final Object node = path.getLastPathComponent();
      if (node instanceof LayerRendererTreeNode) {
        final LayerRendererTreeNode rendererNode = (LayerRendererTreeNode)node;
        if (rendererNode != this.currentNode) {
          final LayerRenderer<?> renderer = rendererNode.getRenderer();
          setEditStylePanel(renderer);
          this.currentNode = rendererNode;
        }
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    if ("replaceRenderer".equals(propertyName)) {
      saveStylePanel();
      final LayerRenderer<? extends Layer> oldRenderer = (LayerRenderer<? extends Layer>)event
        .getOldValue();
      final LayerRenderer<? extends Layer> newRenderer = (LayerRenderer<? extends Layer>)event
        .getNewValue();
      if (oldRenderer == this.rootRenderer && newRenderer != null && newRenderer != oldRenderer) {
        Property.removeListener(oldRenderer, this);
        this.rootNode.removeNode(0);

        Property.addListener(newRenderer, this);
        final LayerRendererTreeNode newNode = new LayerRendererTreeNode(newRenderer);
        this.rootNode.addNode(newNode);
        this.tree.expandOpenNodes();

        this.rootRenderer = newRenderer;
        // setVisible(newRenderer,true);

        setSelectedRenderer(newRenderer);
      }
    } else if ("renderers".equals(propertyName)) {
      this.tree.expandOpenNodes();
    }
  }

  @Override
  public void save() {
    super.save();
    final LayerRenderer<? extends Layer> renderer = this.rootRenderer;
    this.layer.setRenderer(renderer);
  }

  protected void saveStylePanel() {
    final Window window = SwingUtil.getWindowAncestor(this);
    if (window != null) {
      final Component component = window.getFocusOwner();
      if (component instanceof ValueField) {
        final ValueField valueField = (ValueField)component;
        valueField.save();
      }
    }
    final Component view = this.editStyleContainer.getViewport().getView();
    if (view instanceof ValueField) {
      final ValueField valueField = (ValueField)view;
      valueField.save();
    }

  }

  public void setEditStylePanel(final LayerRenderer<? extends Layer> renderer) {
    saveStylePanel();
    if (renderer != null) {
      final Form stylePanel = renderer.newStylePanel();
      this.editStyleContainer.setViewportView(stylePanel);
    }
  }

  public void setSelectedRenderer(final LayerRenderer<?> renderer) {
    final List<String> pathNames = renderer.getPathNames();
    final LayerRenderer<?> selectedRenderer = this.rootRenderer.getRenderer(pathNames);
    if (selectedRenderer != null) {
      final List<Object> path = Lists.newArray(this.rootNode);
      path.addAll(selectedRenderer.getPathRenderers());
      final TreePath treePath = this.tree.getTreePath(path);
      this.tree.expandPath(treePath);
      this.tree.setSelectionPath(treePath);
      setEditStylePanel(selectedRenderer);
    }
  }
}
