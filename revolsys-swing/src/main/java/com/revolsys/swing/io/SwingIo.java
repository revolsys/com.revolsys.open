package com.revolsys.swing.io;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JFileChooser;

import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.file.Paths;
import com.revolsys.io.filter.FileNameExtensionFilter;
import com.revolsys.predicate.Predicates;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.PreferencesUtil;

public class SwingIo {
  public static <F extends IoFactory> void exportToFile(final String title,
    final String preferencesGroup, final Class<F> factoryClass, final Predicate<F> factoryFilter,
    final String initialFileExtension, final boolean primaryExtension, final String baseName,
    final Consumer<File> exportAction) {
    Invoke.later(() -> {
      final JFileChooser fileChooser = SwingUtil.newFileChooser("Export " + title, preferencesGroup,
        "directory");
      fileChooser.setAcceptAllFileFilterUsed(false);
      final String defaultFileExtension = PreferencesUtil.getUserString(preferencesGroup,
        "fileExtension", initialFileExtension);
      final List<FileNameExtensionFilter> fileFilters = new ArrayList<>();
      for (final F factory : IoFactory.factories(factoryClass)) {
        if (factoryFilter.test(factory)) {
          final FileNameExtensionFilter fileFilter;
          if (primaryExtension) {
            fileFilter = factory.newFileFilter();
          } else {
            fileFilter = factory.newFileFilterAllExtensions();
          }
          fileFilters.add(fileFilter);
          final List<String> extensions = fileFilter.getExtensions();
          if (extensions.contains(defaultFileExtension)) {
            fileChooser.setFileFilter(fileFilter);
          }
        }
      }
      IoFactory.sortFilters(fileFilters);
      for (final FileNameExtensionFilter fileFilter : fileFilters) {
        fileChooser.addChoosableFileFilter(fileFilter);
      }

      fileChooser.setMultiSelectionEnabled(false);
      fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), baseName));

      final int returnVal = Dialogs.showSaveDialog(fileChooser);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        final FileNameExtensionFilter fileFilter = (FileNameExtensionFilter)fileChooser
          .getFileFilter();
        File file = fileChooser.getSelectedFile();
        if (file != null) {
          final String fileExtension = FileUtil.getFileNameExtension(file);
          final String expectedExtension = fileFilter.getExtensions().get(0);
          if (!fileExtension.equals(expectedExtension)) {
            file = FileUtil.getFileWithExtension(file, expectedExtension);
          }
          final File targetFile = file;
          PreferencesUtil.setUserString(preferencesGroup, "fileExtension", expectedExtension);
          PreferencesUtil.setUserString(preferencesGroup, "directory", file.getParent());
          final String description = "Export " + baseName + " to " + targetFile.getAbsolutePath();
          Invoke.background(description, () -> exportAction.accept(targetFile));
        }
      }
    });
  }

  public static <F extends IoFactory> void exportToFile(final String title,
    final String preferencesGroup, final Class<F> factoryClass, final String initialFileExtension,
    final Path path, final Consumer<File> exportAction) {
    final String baseName = Paths.getBaseName(path);
    exportToFile(title, preferencesGroup, factoryClass, Predicates.all(), initialFileExtension,
      true, baseName, exportAction);
  }

  public static <F extends IoFactory> void exportToFile(final String title,
    final String preferencesGroup, final Class<F> factoryClass, final String initialFileExtension,
    final String baseName, final Consumer<File> exportAction) {
    exportToFile(title, preferencesGroup, factoryClass, Predicates.all(), initialFileExtension,
      true, baseName, exportAction);
  }

  private SwingIo() {

  }
}
