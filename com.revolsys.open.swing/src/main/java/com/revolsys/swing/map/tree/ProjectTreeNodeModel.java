package com.revolsys.swing.map.tree;

import java.util.List;

import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.tree.renderer.LayerGroupTreeCellRenderer;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class ProjectTreeNodeModel extends
  AbstractObjectTreeNodeModel<Project, Layer> {

  public ProjectTreeNodeModel() {
    setSupportedClasses(Project.class);
    setSupportedChildClasses(Layer.class);
    setObjectTreeNodeModels(this, new LayerGroupTreeNodeModel(),
      new BaseLayerTreeNodeModel("Layer"));
    setRenderer(new LayerGroupTreeCellRenderer());
  }

  @Override
  public int addChild(final Project project, final int index, final Layer layer) {
    project.add(index, layer);
    return index;
  }

  @Override
  public int addChild(final Project project, final Layer layer) {
    project.add(layer);
    return getChildCount(project);
  }

  @Override
  protected List<Layer> getChildren(final Project project) {
    return project.getLayers();
  }

  @Override
  public boolean isLeaf(final Project node) {
    return false;
  }

  @Override
  public boolean removeChild(final Project project, final Layer layer) {
    project.remove(layer);
    return true;
  }
}
