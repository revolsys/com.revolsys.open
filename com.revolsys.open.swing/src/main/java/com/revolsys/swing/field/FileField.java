package com.revolsys.swing.field;

import java.awt.Color;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import com.revolsys.swing.EventQueue;
import com.revolsys.swing.layout.SpringLayoutUtil;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;

public class FileField extends JPanel implements Field {
  private static final long serialVersionUID = -8433151755294925911L;

  private final TextField fileName = new TextField(70);

  private final JButton browseButton = new JButton();

  private final String fieldName = "fieldValue";

  private String errorMessage;

  private String originalToolTip;

  final JFileChooser fileChooser = new JFileChooser();

  public FileField(int fileSelectionMode) {
    super(new SpringLayout());

    add(this.fileName);
    this.browseButton.setText("Browse...");
    EventQueue.addAction(this.browseButton, () -> browseClick());
    add(this.browseButton);
    SpringLayoutUtil.makeCompactGrid(this, 1, 2, 5, 5, 5, 5);

    this.fileChooser.setFileSelectionMode(fileSelectionMode);

    final String directoryPath = getDirectoryPath();
    final File initialFile = new File(directoryPath);

    if (initialFile.getParentFile() != null && initialFile.getParentFile().exists()) {
      this.fileChooser.setCurrentDirectory(initialFile.getParentFile());
    }

    this.fileChooser.setMultiSelectionEnabled(false);

  }

  private void browseClick() {
    try {

      if (JFileChooser.APPROVE_OPTION == this.fileChooser
        .showOpenDialog(SwingUtilities.windowForComponent(this))) {
        final File file = this.fileChooser.getSelectedFile();
        if (file != null) {
          this.fileName.setText(file.getCanonicalPath());
        }
      }
    } catch (final Throwable t) {
      JOptionPane.showMessageDialog(this, t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  @Override
  public Field clone() {
    return new FileField(JFileChooser.DIRECTORIES_ONLY);
  }

  @Override
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  public File getDirectoryFile() {
    final String directoryPath = getDirectoryPath();
    if (Property.hasValue(directoryPath)) {
      return new File(directoryPath);
    } else {
      return null;
    }
  }

  public String getDirectoryPath() {
    return this.fileName.getText();
  }

  @Override
  public String getFieldName() {
    return this.fieldName;
  }

  @Override
  public String getFieldValidationMessage() {
    final File directory = getDirectoryFile();
    if (directory == null) {
      return "No directory specified";
    } else if (directory.exists()) {
      return null;
    } else {
      return "Directory does not exist";
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    return (T)this.fileName.getText();
  }

  @Override
  public boolean isFieldValid() {
    final File directory = getDirectoryFile();
    if (directory == null || !directory.exists()) {
      this.fileName.setForeground(Color.RED);
      this.fileName.setSelectedTextColor(Color.RED);
      this.fileName.setBackground(Color.PINK);
      return false;
    } else {
      this.fileName.setForeground(Color.BLACK);
      this.fileName.setSelectedTextColor(Color.BLACK);
      this.fileName.setBackground(Color.WHITE);
      return true;
    }
  }

  public void setDirectoryPath(final String directoryPath) {
    this.fileName.setText(directoryPath);
  }

  @Override
  public void setEditable(final boolean editable) {
    setEnabled(editable);
  }

  @Override
  public void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = TextField.DEFAULT_BACKGROUND;
    }
    setBackground(color);
  }

  @Override
  public void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = TextField.DEFAULT_FOREGROUND;
    }
    setForeground(color);
  }

  @Override
  public void setFieldInvalid(final String message, final Color foregroundColor,
    final Color backgroundColor) {
    this.fileName.setForeground(foregroundColor);
    this.fileName.setSelectedTextColor(foregroundColor);
    this.fileName.setBackground(backgroundColor);
    this.errorMessage = message;
    super.setToolTipText(this.errorMessage);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public void setFieldValid() {
    this.fileName.setForeground(TextField.DEFAULT_FOREGROUND);
    this.fileName.setSelectedTextColor(TextField.DEFAULT_SELECTED_FOREGROUND);
    this.fileName.setBackground(TextField.DEFAULT_BACKGROUND);
    this.errorMessage = null;
    super.setToolTipText(this.originalToolTip);
  }

  @Override
  public void setFieldValue(final Object fieldValue) {
    setDirectoryPath(fieldValue.toString());
  }

  @Override
  public void setToolTipText(final String text) {
    this.originalToolTip = text;
    if (!Property.hasValue(this.errorMessage)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public void setUndoManager(final UndoManager undoManager) {
    this.fileName.setUndoManager(undoManager);
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
    setFieldValue(this.fileName.getText());
  }
}
