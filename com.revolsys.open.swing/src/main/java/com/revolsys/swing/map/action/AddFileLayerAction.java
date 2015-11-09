package com.revolsys.swing.map.action;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.raster.GeoreferencedImageFactory;
import com.revolsys.record.io.RecordReaderFactory;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.node.layer.LayerGroupTreeNode;
import com.revolsys.util.Strings;

public class AddFileLayerAction extends AbstractAction {
  private static final long serialVersionUID = 1L;

  public static List<FileNameExtensionFilter> getFileFilters(final Set<String> allExtensions,
    final Class<? extends IoFactory> factoryClass) {
    final List<FileNameExtensionFilter> filters = new ArrayList<>();
    final List<? extends IoFactory> factories = IoFactory.factories(factoryClass);
    for (final IoFactory factory : factories) {
      final List<String> fileExtensions = factory.getFileExtensions();
      final FileNameExtensionFilter filter = newFilter(factory);
      filters.add(filter);
      if (allExtensions != null) {
        allExtensions.addAll(fileExtensions);
      }
    }
    sortFilters(filters);
    return filters;
  }

  public static FileNameExtensionFilter newFileFilter(final String description,
    final Collection<String> fileExtensions) {
    final String[] array = fileExtensions.toArray(new String[0]);
    return new FileNameExtensionFilter(description, array);
  }

  public static FileNameExtensionFilter newFilter(final IoFactory factory) {
    final List<String> fileExtensions = factory.getFileExtensions();
    String description = factory.getName();
    description += " (" + Strings.toString(fileExtensions) + ")";
    return newFileFilter(description, fileExtensions);
  }

  public static void sortFilters(final List<FileNameExtensionFilter> filters) {
    Collections.sort(filters, new Comparator<FileNameExtensionFilter>() {
      @Override
      public int compare(final FileNameExtensionFilter filter1,
        final FileNameExtensionFilter filter2) {
        return filter1.getDescription().compareTo(filter2.getDescription());
      }
    });
  }

  public AddFileLayerAction() {
    putValue(NAME, "Open File Layer");
    putValue(SMALL_ICON, Icons.getIcon("page_add"));
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

    final JFileChooser fileChooser = SwingUtil.newFileChooser(getClass(), "currentDirectory");
    fileChooser.setMultiSelectionEnabled(true);

    final Set<String> allImageExtensions = new TreeSet<String>();
    final List<FileNameExtensionFilter> imageFileFilters = getFileFilters(allImageExtensions,
      GeoreferencedImageFactory.class);

    final Set<String> allRecordExtensions = new TreeSet<String>();
    final List<FileNameExtensionFilter> recordFileFilters = getFileFilters(allRecordExtensions,
      RecordReaderFactory.class);

    final Set<String> allExtensions = new TreeSet<String>();
    allExtensions.addAll(allRecordExtensions);
    allExtensions.addAll(allImageExtensions);
    final FileNameExtensionFilter allFilter = newFileFilter("All Supported Files", allExtensions);
    fileChooser.addChoosableFileFilter(allFilter);

    fileChooser
      .addChoosableFileFilter(newFileFilter("All Vector/Record Files", allRecordExtensions));

    fileChooser.addChoosableFileFilter(newFileFilter("All Image Files", allImageExtensions));

    for (final FileFilter fileFilter : recordFileFilters) {
      fileChooser.addChoosableFileFilter(fileFilter);
    }

    for (final FileFilter fileFilter : imageFileFilters) {
      fileChooser.addChoosableFileFilter(fileFilter);
    }

    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setFileFilter(allFilter);

    final int status = fileChooser.showDialog(window, "Open Files");
    if (status == JFileChooser.APPROVE_OPTION) {
      final Object menuSource = MenuFactory.getMenuSource();
      final LayerGroup layerGroup;
      if (menuSource instanceof LayerGroupTreeNode) {
        final LayerGroupTreeNode node = (LayerGroupTreeNode)menuSource;
        layerGroup = node.getGroup();
      } else if (menuSource instanceof LayerGroup) {
        layerGroup = (LayerGroup)menuSource;
      } else {
        layerGroup = Project.get();
      }
      for (final File file : fileChooser.getSelectedFiles()) {
        Invoke.background("Open file: " + FileUtil.getCanonicalPath(file),
          () -> layerGroup.openFile(file));
      }
    }
    SwingUtil.saveFileChooserDirectory(getClass(), "currentDirectory", fileChooser);
  }
}
