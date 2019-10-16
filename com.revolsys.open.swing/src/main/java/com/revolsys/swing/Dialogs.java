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
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;

public class Dialogs {

  public static Window getWindow() {
    final Window currentWindow = MenuFactory.getCurrentWindow();
    final Window activeWindow = SwingUtil.windowActive();
    if (currentWindow == null) {
      return activeWindow;
    } else {
      return currentWindow;
    }
  }

  public static BaseDialog newDocumentModal(final String title) {
    final Window parent = getWindow();
    return new BaseDialog(parent, title, ModalityType.DOCUMENT_MODAL);
  }

  public static BaseDialog newModal(final String title) {
    final Window parent = getWindow();
    return new BaseDialog(parent, title, ModalityType.APPLICATION_MODAL);
  }

  public static int showConfirmDialog(final Object message, final String title,
    final int optionType) {
    return showConfirmDialog(message, title, optionType, JOptionPane.QUESTION_MESSAGE);
  }

  public static int showConfirmDialog(final Object message, final String title,
    final int optionType, final int messageType) {
    return Invoke.andWait(() -> {
      final Window parent = getWindow();
      return JOptionPane.showConfirmDialog(parent, message, title, optionType, messageType);
    });
  }

  public static int showDialog(final JFileChooser fileChooser, final String approveButtonText) {
    final Window parent = getWindow();
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
    final Window parent = getWindow();
    return JOptionPane.showInputDialog(parent, message, title);
  }

  public static String showInputDialog(final Object message, final String title,
    final int messageType) {
    final Window parent = getWindow();
    return JOptionPane.showInputDialog(parent, message, title, messageType);
  }

  public static Object showInputDialog(final Object message, final String title,
    final int messageType, final Icon icon, final Object[] selectionValues,
    final Object initialSelectionValue) {
    final Window parent = getWindow();
    return JOptionPane.showInputDialog(parent, message, title, messageType, icon, selectionValues,
      initialSelectionValue);
  }

  public static String showInputDialog(final String title) {
    final Window parent = getWindow();
    return JOptionPane.showInputDialog(parent, title);
  }

  public static void showMessageDialog(final Object message) {
    Invoke.andWait(() -> {
      final Window parent = getWindow();
      JOptionPane.showMessageDialog(parent, message);
    });
  }

  public static void showMessageDialog(final Object message, final String title,
    final int messageType) {
    Invoke.andWait(() -> {
      final Window parent = getWindow();
      JOptionPane.showMessageDialog(parent, message, title, messageType);
    });
  }

  public static int showOpenDialog(final JFileChooser fileChooser) {
    final Window parent = getWindow();
    return fileChooser.showOpenDialog(parent);
  }

  public static int showSaveDialog(final JFileChooser fileChooser) {
    final Window parent = getWindow();
    return fileChooser.showSaveDialog(parent);
  }
}
