package com.revolsys.swing.map.action;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectReaderFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.util.LayerUtil;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.util.CollectionUtil;

@SuppressWarnings("serial")
public class AddFileLayerAction extends AbstractAction {

  public AddFileLayerAction() {
    putValue(NAME, "Open File Layer");
    putValue(SMALL_ICON, SilkIconLoader.getIcon("page_add"));
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final Object source = e.getSource();
    Window window;
    if (source instanceof Component) {
      final Component component = (Component)source;
      window = SwingUtilities.getWindowAncestor(component);
    } else {
      window = null;
    }

    final JFileChooser fileChooser = SwingUtil.createFileChooser(getClass(),
      "currentDirectory");

    Set<DataObjectReaderFactory> factories = IoFactoryRegistry.getInstance()
      .getFactories(DataObjectReaderFactory.class);
    for (DataObjectReaderFactory factory : factories) {
      List<String> fileExtensions = factory.getFileExtensions();
      String description = factory.getName();
      description += " (" + CollectionUtil.toString(fileExtensions) + ")";
      final FileNameExtensionFilter filter = new FileNameExtensionFilter(
        description, fileExtensions.toArray(new String[0]));
      fileChooser.addChoosableFileFilter(filter);
    }
    fileChooser.setAcceptAllFileFilterUsed(false);
    final int status = fileChooser.showDialog(window, "Open File");
    if (status == JFileChooser.APPROVE_OPTION) {
      final LayerGroup layerGroup = ObjectTree.getMouseClickItem();
      final File file = fileChooser.getSelectedFile();
      SwingWorkerManager.execute(
        "Open File: " + FileUtil.getCanonicalPath(file), LayerUtil.class,
        "openFile", layerGroup, file);
    }
    SwingUtil.saveFileChooserDirectory(getClass(), "currentDirectory",
      fileChooser);
  }
}
