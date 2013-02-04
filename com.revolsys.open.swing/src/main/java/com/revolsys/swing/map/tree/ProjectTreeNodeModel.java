package com.revolsys.swing.map.tree;

import java.util.List;

import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.tree.renderer.LayerGroupTreeCellRenderer;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class ProjectTreeNodeModel extends
  AbstractObjectTreeNodeModel<Project, LayerGroup> {

  public ProjectTreeNodeModel() {
    setSupportedClasses(Project.class);
    setSupportedChildClasses(LayerGroup.class);
    setObjectTreeNodeModels(this, new LayerGroupTreeNodeModel());
    setRenderer(new LayerGroupTreeCellRenderer());
  }

  @Override
  protected List<LayerGroup> getChildren(final Project project) {
    return project.getLayerGroups();
  }
}
