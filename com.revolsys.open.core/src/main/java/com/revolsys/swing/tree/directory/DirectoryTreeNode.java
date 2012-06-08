/*
 * Created on 01-Oct-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.revolsys.swing.tree.directory;

import java.io.File;

import com.revolsys.swing.tree.dynamic.DynamicNodeLoader;
import com.revolsys.swing.tree.dynamic.DynamicTreeNode;

public class DirectoryTreeNode extends DynamicTreeNode {
  /**
   * 
   */
  private static final long serialVersionUID = 4884351636521116665L;

  public DirectoryTreeNode(final File file, final DynamicNodeLoader childLoader) {
    super(file.getPath(), childLoader);
    setUserObject(file);
  }

  public DirectoryTreeNode(final File directory, final String name,
    final DynamicNodeLoader childLoader) {
    super(name, childLoader);
    setUserObject(directory);
  }

  @Override
  public boolean getAllowsChildren() {
    return true;
  }

  @Override
  public boolean isLeaf() {
    return false;
  }
}
