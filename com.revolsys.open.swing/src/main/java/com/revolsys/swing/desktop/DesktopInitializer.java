package com.revolsys.swing.desktop;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.desktop.QuitStrategy;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Set;

import org.jeometry.common.logging.Logs;

import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.preferences.PreferencesDialog;
import com.revolsys.util.OS;

public class DesktopInitializer {
  public static void initialize(final Image image, final Set<File> initialFiles) {
    if (OS.isMac()) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");

      if (image != null) {
        final Taskbar taskbar = Taskbar.getTaskbar();
        taskbar.setIconImage(image);
      }

      final Desktop desktop = Desktop.getDesktop();

      desktop.setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
      desktop.setOpenFileHandler(event -> {
        final List<File> files = event.getFiles();
        final LayerGroup layerGroup = Project.get();
        if (layerGroup == null) {
          initialFiles.addAll(files);
        } else {
          layerGroup.openFiles(files);
        }
      });
      desktop.setOpenURIHandler(event -> {
        final URI uri = event.getURI();
        final LayerGroup layerGroup = Project.get();
        if (layerGroup != null) {
          try {
            layerGroup.openFile(uri.toURL());
          } catch (final MalformedURLException e) {
            Logs.error(LayerGroup.class, "Cannot open: " + uri, e);
          }
        }
      });
      desktop.setPreferencesHandler(e -> {
        final PreferencesDialog preferencesDialog = new PreferencesDialog();
        preferencesDialog.showPanel();
      });
    }

  }

}
