package com.revolsys.swing.tree.node.layer;

import java.util.List;

import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.BaseTreeNode;

public class ProjectTreeNode extends LayerGroupTreeNode {
  public static BaseTree newTree(final Project project) {
    final ProjectTreeNode root = new ProjectTreeNode(project);
    final BaseTree tree = new BaseTree(root);
    tree.setProperty("treeType", Project.class.getName());
    return tree;
  }

  public ProjectTreeNode(final Project project) {
    super(project);
  }

  public Project getProject() {
    return (Project)super.getUserObject();
  }

  @Override
  protected List<BaseTreeNode> loadChildrenDo() {
    final List<BaseTreeNode> children = super.loadChildrenDo();

    final Project project = getProject();
    final LayerGroup baseMapLayers = project.getBaseMapLayers();
    boolean containsChild = false;
    for (final BaseTreeNode child : children) {
      if (child.getUserData() == baseMapLayers) {
        containsChild = true;
      }
    }
    if (!containsChild) {
      final LayerGroupTreeNode baseMapLayersNode = new LayerGroupTreeNode(baseMapLayers);
      children.add(baseMapLayersNode);
    }
    return children;
  }
}
