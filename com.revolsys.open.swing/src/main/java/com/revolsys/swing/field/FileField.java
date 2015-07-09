package com.revolsys.swing.field;

import java.awt.Color;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.revolsys.swing.EventQueue;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.SpringLayoutUtil;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;

public class FileField extends ValueField implements Field {
  private static final long serialVersionUID = -8433151755294925911L;

  private final TextField fileName = new TextField(70);

  private final JButton browseButton = new JButton();

  private final JFileChooser fileChooser = new JFileChooser();

  public FileField(final int fileSelectionMode) {
    this("file", fileSelectionMode);
  }

  public FileField(final String fieldName) {
    this(fieldName, JFileChooser.FILES_ONLY);
  }

  public FileField(final String fieldName, final int fileSelectionMode) {
    super(fieldName, null);

    add(this.fileName);
    this.browseButton.setText("Browse...");
    EventQueue.addAction(this.browseButton, () -> browseClick());
    add(this.browseButton);
    SpringLayoutUtil.makeCompactGrid(this, 1, 2, 5, 5, 5, 5);

    this.fileChooser.setFileSelectionMode(fileSelectionMode);

    final String directoryPath = getFilePath();
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
  public String getFieldValidationMessage() {
    final File directory = getFile();
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

  public File getFile() {
    final String path = getFilePath();
    if (Property.hasValue(path)) {
      return new File(path);
    } else {
      return null;
    }
  }

  public String getFilePath() {
    return this.fileName.getText();
  }

  @Override
  public boolean isFieldValid() {
    final File directory = getFile();
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

  @Override
  protected void setColor(final Color foregroundColor, final Color backgroundColor) {
    this.fileName.setForeground(foregroundColor);
    this.fileName.setBackground(backgroundColor);
  }

  @Override
  public void setEditable(final boolean editable) {
    setEnabled(editable);
  }

  @Override
  public void setFieldInvalid(final String message, final Color foregroundColor,
    final Color backgroundColor) {
    super.setFieldInvalid(message, foregroundColor, backgroundColor);
    this.fileName.setSelectedTextColor(foregroundColor);
  }

  @Override
  public void setFieldValid() {
    super.setFieldValid();
    this.fileName.setSelectedTextColor(Field.DEFAULT_SELECTED_FOREGROUND);
  }

  @Override
  public void setFieldValue(final Object fieldValue) {
    if (fieldValue == null) {
      setPath("");
    } else {
      setPath(fieldValue.toString());
    }
  }

  public void setPath(final String directoryPath) {
    if (this.fileName != null) {
      this.fileName.setText(directoryPath);
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
