/*
 * Created on 09-Nov-2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.revolsys.swing.tree.directory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.io.filter.DirectoryFilenameFilter;
import com.revolsys.swing.tree.dynamic.DynamicNodeLoader;

/**
 * @author Administrator To change the template for this generated type comment
 *         go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 *         Comments
 */
public class DirectoryLoader implements DynamicNodeLoader {
  private static final DynamicNodeLoader childLoader = new DirectoryLoader();

  @Override
  public List loadNodes(final Object parentUserObject) {
    final List nodes = new ArrayList();
    final File directory = (File)parentUserObject;
    if (directory.isDirectory()) {
      final File[] directories = directory.listFiles(new DirectoryFilenameFilter());
      for (int i = 0; i < directories.length; i++) {
        final File childDirectory = directories[i];
        final DirectoryTreeNode node = new DirectoryTreeNode(childDirectory,
          childDirectory.getName(), childLoader);
        nodes.add(node);
      }
    }
    return nodes;
  }
}
