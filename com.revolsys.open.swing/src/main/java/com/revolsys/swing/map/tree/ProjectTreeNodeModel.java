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
  protected List<Layer> getChildren(final Project project) {
    return project.getLayers();
  }
}
