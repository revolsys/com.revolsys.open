/*
 * Created on 09-Nov-2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.revolsys.swing.tree.dynamic;

import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.parallel.SwingWorkerManager;

public abstract class DynamicTreeNode extends DefaultMutableTreeNode {
  /**
   * 
   */
  private static final long serialVersionUID = 1396159389429851162L;

  private final DynamicNodeLoader childLoader;

  private final String name;

  private DefaultTreeModel model;

  public DynamicTreeNode(final String name, final DynamicNodeLoader childLoader) {
    this.name = name;
    this.childLoader = childLoader;
  }

  public void clearChildren() {
    removeAllChildren();
  }

  /**
   * @return Returns the name.
   */
  protected final String getName() {
    return this.name;
  }

  public void loadNodes() {
    @SuppressWarnings("unchecked")
    final List<MutableTreeNode> nodes = this.childLoader.loadNodes(getUserObject());
    for (final MutableTreeNode childNode : nodes) {
      add(childNode);
    }
    model.nodeStructureChanged(this);
  }

  public void populateChildren(final JTree tree) {
    model = (DefaultTreeModel)tree.getModel();

    SwingWorkerManager.invokeLater(new InvokeMethodRunnable(this, "loadNodes"));
  }

  @Override
  public String toString() {
    return getName();
  }

}
