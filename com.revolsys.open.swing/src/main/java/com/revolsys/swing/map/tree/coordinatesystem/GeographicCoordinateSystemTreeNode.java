package com.revolsys.swing.map.tree.coordinatesystem;

import java.util.List;

import javax.swing.tree.TreeNode;

import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.swing.tree.model.node.AbstractTreeNode;

public class GeographicCoordinateSystemTreeNode extends AbstractTreeNode {

  public GeographicCoordinateSystemTreeNode(
    final GeographicCoordinateSystem coordinateSystem) {
    super(coordinateSystem);
    setName(coordinateSystem.getName());
  }

  @Override
  public List<TreeNode> getChildren() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return super.getName();
  }

}
