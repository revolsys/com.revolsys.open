package com.revolsys.swing.map.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.TransferHandler.TransferSupport;
import javax.swing.tree.TreePath;

import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.tree.renderer.LayerGroupTreeCellRenderer;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class ProjectTreeNodeModel extends
  AbstractObjectTreeNodeModel<Project, Layer> {

  public ProjectTreeNodeModel() {
    setSupportedClasses(Project.class);
    setSupportedChildClasses(Layer.class);
    setObjectTreeNodeModels(this, new LayerGroupTreeNodeModel(),
      new BaseLayerTreeNodeModel("Layer"));
    setRenderer(new LayerGroupTreeCellRenderer());
  }

  @Override
  public int addChild(final Project project, final int index, final Layer layer) {
    project.add(index, layer);
    return index;
  }

  @Override
  public int addChild(final Project project, final Layer layer) {
    project.add(layer);
    return getChildCount(project);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean dndImportData(final TransferSupport support,
    final Project project, final int index) throws IOException,
    UnsupportedFlavorException {
    if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      final Transferable transferable = support.getTransferable();
      final List<File> files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
      project.openFiles(index, files);
      return true;
    } else {
      return super.dndImportData(support, project, index);
    }
  }

  @Override
  protected List<Layer> getChildren(final Project project) {
    return project.getLayers();
  }

  @Override
  public boolean isCopySupported(final Layer layer) {
    return layer.isClonable();
  }

  @Override
  public boolean isDndCanImport(final TreePath path,
    final TransferSupport support) {
    if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      support.setDropAction(DnDConstants.ACTION_COPY);
      support.setShowDropLocation(true);
      return true;
    } else {
      return super.isDndCanImport(path, support);
    }
  }

  @Override
  public boolean isLeaf(final Project node) {
    return false;
  }

  @Override
  public boolean removeChild(final Project project, final Layer layer) {
    return project.remove(layer);
  }
}
