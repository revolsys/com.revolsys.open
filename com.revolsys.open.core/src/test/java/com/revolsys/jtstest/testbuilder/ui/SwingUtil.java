package com.revolsys.jtstest.testbuilder.ui;

import java.awt.Component;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jtstest.testbuilder.model.GeometryTransferable;
import com.revolsys.jtstest.testrunner.StringUtil;

public class SwingUtil {

  public static FileFilter XML_FILE_FILTER = createFileFilter(
    "JTS Test XML File (*.xml)", ".xml");

  public static FileFilter JAVA_FILE_FILTER = createFileFilter(
    "Java File (*.java)", ".java");

  public static FileFilter PNG_FILE_FILTER = createFileFilter(
    "PNG File (*.png)", ".png");

  /**
   * 
   * @param comp
   * @param fileChooser
   * @return filename chosen, or
   * null if choose was cancelled for some reason
   */
  public static String chooseFilenameWithConfirm(final Component comp,
    final JFileChooser fileChooser) {
    try {
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(comp)) {
        final File file = fileChooser.getSelectedFile();
        if (!SwingUtil.confirmOverwrite(comp, file)) {
          return null;
        }
        final String fullFileName = fileChooser.getSelectedFile().toString();
        return fullFileName;
      }
    } catch (final Exception x) {
      SwingUtil.reportException(comp, x);
    }
    return null;
  }

  public static boolean confirmOverwrite(final Component comp, final File file) {
    if (file.exists()) {
      final int decision = JOptionPane.showConfirmDialog(comp, file.getName()
        + " exists. Overwrite?", "Confirmation", JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);
      if (decision == JOptionPane.NO_OPTION) {
        return false;
      }
    }
    return true;
  }

  public static void copyToClipboard(final Object o, final boolean isFormatted) {
    if (o instanceof Geometry) {
      Toolkit.getDefaultToolkit()
        .getSystemClipboard()
        .setContents(new GeometryTransferable((Geometry)o, isFormatted), null);
    } else {
      // transfer as string
      Toolkit.getDefaultToolkit()
        .getSystemClipboard()
        .setContents(new StringSelection(o.toString()), null);
    }
  }

  /**
   * 
   * Example usage:
   * <pre>
   * SwingUtil.createFileFilter("JEQL script (*.jql)", "jql")
   * </pre>
   * @param description
   * @param extension
   * @return the file filter
   */
  public static FileFilter createFileFilter(final String description,
    final String extension) {
    final String dotExt = extension.startsWith(".") ? extension : "."
      + extension;
    final FileFilter ff = new FileFilter() {
      @Override
      public boolean accept(final File f) {
        return f.isDirectory() || f.toString().toLowerCase().endsWith(dotExt);
      }

      @Override
      public String getDescription() {
        return description;
      }
    };
    return ff;
  }

  public static Transferable getContents(final Clipboard clipboard) {
    try {
      return clipboard.getContents(null);
    } catch (final Throwable t) {
      return null;
    }
  }

  public static Double getDouble(final JTextField txt, final Double defaultVal) {
    final String str = txt.getText();
    if (str.trim().length() <= 0) {
      return defaultVal;
    }

    double val = 0;
    try {
      val = Double.parseDouble(str);
    } catch (final NumberFormatException ex) {
    }
    return new Double(val);
  }

  public static Object getFromClipboard() {
    final Transferable transferable = getContents(Toolkit.getDefaultToolkit()
      .getSystemClipboard());

    try {
      if (transferable.isDataFlavorSupported(GeometryTransferable.GEOMETRY_FLAVOR)) {
        return transferable.getTransferData(GeometryTransferable.GEOMETRY_FLAVOR);
      }
      // attempt to read as string
      return transferable.getTransferData(DataFlavor.stringFlavor);
    } catch (final Exception ex) {
      // eat exception, since there isn't anything we can do
    }
    return null;
  }

  public static Integer getInteger(final JTextField txt,
    final Integer defaultVal) {
    final String str = txt.getText();
    if (str.trim().length() <= 0) {
      return defaultVal;
    }

    int val = 0;
    try {
      val = Integer.parseInt(str);
    } catch (final NumberFormatException ex) {
    }
    return new Integer(val);
  }

  public static Object getSelectedValue(final JComboBox cb, final Object[] val) {
    final int selIndex = cb.getSelectedIndex();
    if (selIndex == -1) {
      return null;
    }
    return val[selIndex];
  }

  public static void reportException(final Component c, final Exception e) {
    JOptionPane.showMessageDialog(c, StringUtil.wrap(e.toString(), 80),
      "Exception", JOptionPane.ERROR_MESSAGE);
    e.printStackTrace(System.out);
  }

  public static void setEnabledWithBackground(final Component comp,
    final boolean isEnabled) {
    comp.setEnabled(isEnabled);
    if (isEnabled) {
      comp.setBackground(SystemColor.text);
    } else {
      comp.setBackground(SystemColor.control);
    }
  }

}
