package com.revolsys.swing.tree.node.webservice;

import javax.swing.Icon;

import com.revolsys.swing.Icons;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.node.ParentTreeNode;
import com.revolsys.webservice.WebServiceConnectionManager;
import com.revolsys.webservice.WebServiceConnectionRegistry;

public class WebServiceConnectionsTreeNodes {
  public static final Icon ICON = Icons.getIcon("world_link");

  static {
    BaseTreeNode.addNodeIcon(WebServiceConnectionRegistry.class, ICON);
  }

  public static BaseTreeNode newWebServiceConnectionManagerTreeNode() {
    final ParentTreeNode treeNode = new ParentTreeNode(WebServiceConnectionManager.get(), ICON);
    treeNode.setOpen(true);
    return treeNode;
  }

}
