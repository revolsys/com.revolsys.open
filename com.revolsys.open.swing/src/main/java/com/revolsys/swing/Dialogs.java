package com.revolsys.swing;

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import com.revolsys.swing.component.BaseDialog;

public class Dialogs {

  public static BaseDialog newModal(final String title) {
    return new BaseDialog(title, ModalityType.APPLICATION_MODAL);
  }

  public static int showConfirmDialog(final Object message, final String title,
    final int optionType) {
    final Window parent = SwingUtil.windowOnTop();
    return JOptionPane.showConfirmDialog(parent, message, title, optionType);
  }

  public static int showConfirmDialog(final Object message, final String title,
    final int optionType, final int messageType) {
    final Window parent = SwingUtil.windowOnTop();
    return JOptionPane.showConfirmDialog(parent, message, title, optionType, messageType);
  }

  public static int showDialog(final JFileChooser fileChooser, final String approveButtonText) {
    final Window parent = SwingUtil.windowOnTop();
    return fileChooser.showDialog(parent, approveButtonText);
  }

  public static void showErrorDialog(final String title, final String message, final Throwable e) {
    final String exceptionMessage = e.getMessage().replaceAll("\n", "<br />");
    final String errorMessage = "<html><body><p style=\"margin-bottom: 10px\"><strong>" + message
      + "</strong></p><pre>" + exceptionMessage + "</pre></body></p>";

    final JScrollPane scrollPane = new JScrollPane(new JLabel(errorMessage));
    final Dimension preferredSize = scrollPane.getPreferredSize();
    final Rectangle bounds = SwingUtil.getScreenBounds();
    final int width = Math.min(bounds.width - 200, preferredSize.width + 20);
    final int height = Math.min(bounds.height - 100, preferredSize.height + 20);

    scrollPane.setPreferredSize(new Dimension(width, height));

    showMessageDialog(scrollPane, title, JOptionPane.ERROR_MESSAGE);
  }

  public static String showInputDialog(final Object message, final String title) {
    final Window parent = SwingUtil.windowOnTop();
    return JOptionPane.showInputDialog(parent, message, title);
  }

  public static String showInputDialog(final Object message, final String title,
    final int messageType) {
    final Window parent = SwingUtil.windowOnTop();
    return JOptionPane.showInputDialog(parent, message, title, messageType);
  }

  public static Object showInputDialog(final Object message, final String title,
    final int messageType, final Icon icon, final Object[] selectionValues,
    final Object initialSelectionValue) {
    final Window parent = SwingUtil.windowOnTop();
    return JOptionPane.showInputDialog(parent, message, title, messageType, icon, selectionValues,
      initialSelectionValue);
  }

  public static String showInputDialog(final String title) {
    final Window parent = SwingUtil.windowOnTop();
    return JOptionPane.showInputDialog(parent, title);
  }

  public static void showMessageDialog(final Object message) {
    final Window parent = SwingUtil.windowOnTop();
    JOptionPane.showMessageDialog(parent, message);
  }

  public static void showMessageDialog(final Object message, final String title,
    final int messageType) {
    final Window parent = SwingUtil.windowOnTop();
    JOptionPane.showMessageDialog(parent, message, title, messageType);
  }

  public static int showOpenDialog(final JFileChooser fileChooser) {
    final Window parent = SwingUtil.windowOnTop();
    return fileChooser.showOpenDialog(parent);
  }

  public static int showSaveDialog(final JFileChooser fileChooser) {
    final Window parent = SwingUtil.windowOnTop();
    return fileChooser.showSaveDialog(parent);
  }
}
