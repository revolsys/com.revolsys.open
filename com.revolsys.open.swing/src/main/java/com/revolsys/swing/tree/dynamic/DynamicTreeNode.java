/*
 * Created on 09-Nov-2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.revolsys.swing.tree.dynamic;

import java.util.Iterator;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import com.revolsys.awt.SwingWorkerManager;

public abstract class DynamicTreeNode extends DefaultMutableTreeNode implements
  Runnable {
  /**
   * 
   */
  private static final long serialVersionUID = 1396159389429851162L;

  private DefaultMutableTreeModelChangedTask refreshTask;

  private final DynamicNodeLoader childLoader;

  private final String name;

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

  public void populateChildren(final JTree tree) {
    this.refreshTask = new DefaultMutableTreeModelChangedTask(tree, this);
    SwingWorkerManager.invokeLater(this);
  }

  @Override
  public void run() {
    final List locations = this.childLoader.loadNodes(getUserObject());
    for (final Iterator children = locations.iterator(); children.hasNext();) {
      final MutableTreeNode childNode = (MutableTreeNode)children.next();
      add(childNode);
    }
    SwingWorkerManager.invokeLater(this.refreshTask);
  }

  @Override
  public String toString() {
    return getName();
  }

}
