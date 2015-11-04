package com.revolsys.swing.tree.node.layer;

import java.util.List;

import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.util.Property;

public class ProjectTreeNode extends LayerGroupTreeNode {

  public static BaseTree newTree(final Project project) {
    final ProjectTreeNode root = new ProjectTreeNode(project);
    final BaseTree tree = new BaseTree(root);
    return tree;
  }

  public ProjectTreeNode(final Project project) {
    super(project);
    Property.addListener(project, this);
  }

  @Override
  protected List<BaseTreeNode> doLoadChildren() {
    final List<BaseTreeNode> children = super.doLoadChildren();

    final Project project = getProject();
    final LayerGroup baseMapLayers = project.getBaseMapLayers();
    final LayerGroupTreeNode baseMapLayersNode = new LayerGroupTreeNode(baseMapLayers);
    children.add(baseMapLayersNode);
    return children;
  }

  public Project getProject() {
    return (Project)super.getUserObject();
  }
}
