package com.revolsys.swing.tree.file;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class FileTreeNodeModel extends AbstractObjectTreeNodeModel<File, File> {

  public FileTreeNodeModel() {
    setSupportedClasses(File.class);
    setSupportedChildClasses(File.class);
    setObjectTreeNodeModels(this);
  }

  @Override
  protected List<File> getChildren(final File file) {
    if (file.isDirectory()) {
      final File[] files = file.listFiles();
      if (files != null) {
        return Arrays.asList(files);
      }
    }
    return Collections.emptyList();
  }

  @Override
  public boolean isLeaf(final File file) {
    return !file.isDirectory();
  }
}
