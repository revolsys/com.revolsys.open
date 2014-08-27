package com.revolsys.swing.tree.node.layer;

import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.util.Property;

public class ProjectTreeNode extends LayerGroupTreeNode {

  public static BaseTree createTree(final Project project) {
    final ProjectTreeNode root = new ProjectTreeNode(project);

    final BaseTree tree = new BaseTree(root);

    tree.expandAllNodes();
    return tree;
  }

  public ProjectTreeNode(final Project project) {
    super(project);
    Property.addListener(project, this);
  }
}
