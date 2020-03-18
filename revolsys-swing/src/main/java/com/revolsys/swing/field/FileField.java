package com.revolsys.swing.field;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.revolsys.io.FileUtil;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.EventQueue;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.Property;

public class FileField extends ValueField implements Field {
  private static final long serialVersionUID = -8433151755294925911L;

  private final JButton browseButton = new JButton();

  private final JFileChooser fileChooser = new JFileChooser();

  private final TextField fileName;

  public FileField(final int fileSelectionMode) {
    this("file", fileSelectionMode);
  }

  public FileField(final String fieldName) {
    this(fieldName, JFileChooser.FILES_ONLY);
  }

  public FileField(final String fieldName, final int fileSelectionMode) {
    super(fieldName, null);
    this.fileName = new TextField(fieldName, 70);
    add(this.fileName);
    this.browseButton.setText("Browse...");
    EventQueue.addAction(this.browseButton, () -> browseClick());
    add(this.browseButton);
    GroupLayouts.makeColumns(this, 2, false, true);
    setFileSelectionMode(fileSelectionMode);

    final String directoryPath = getFilePath();
    final File initialFile = new File(directoryPath);

    if (initialFile.getParentFile() != null && initialFile.getParentFile().exists()) {
      this.fileChooser.setCurrentDirectory(initialFile.getParentFile());
    }

    this.fileChooser.setMultiSelectionEnabled(false);

  }

  private void browseClick() {
    try {

      if (JFileChooser.APPROVE_OPTION == Dialogs.showOpenDialog(this.fileChooser)) {
        final File file = this.fileChooser.getSelectedFile();
        if (file != null) {
          this.fileName.setText(file.getCanonicalPath());
        }
      }
    } catch (final Throwable t) {
      Dialogs.showMessageDialog(t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  @Override
  public Field clone() {
    return new FileField(JFileChooser.DIRECTORIES_ONLY);
  }

  @Override
  public Color getFieldSelectedTextColor() {
    return TextField.DEFAULT_SELECTED_TEXT_COLOR;
  }

  @Override
  public FieldSupport getFieldSupport() {
    return this.fileName.getFieldSupport();
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
      return FileUtil.getFile(path);
    } else {
      return null;
    }
  }

  public String getFilePath() {
    return this.fileName.getText();
  }

  public Path getPath() {
    final String path = getFilePath();
    if (Property.hasValue(path)) {
      return Paths.get(path);
    } else {
      return null;
    }
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
  public void setFieldSelectedTextColor(Color color) {
    if (color == null) {
      color = Field.DEFAULT_SELECTED_FOREGROUND;
    }
    this.fileName.setSelectedTextColor(color);
  }

  @Override
  public void setFieldValid() {
    super.setFieldValid();
    this.fileName.setSelectedTextColor(Field.DEFAULT_SELECTED_FOREGROUND);
  }

  @Override
  public boolean setFieldValue(final Object fieldValue) {
    final String fileName;
    if (fieldValue == null) {
      fileName = "";
    } else {
      fileName = fieldValue.toString();
    }
    setPath(fileName);
    return true;
  }

  public void setFileSelectionMode(final int fileSelectionMode) {
    this.fileChooser.setFileSelectionMode(fileSelectionMode);
  }

  public void setPath(final String filePath) {
    if (this.fileName != null) {
      this.fileName.setText(filePath);
    }
    if (this.fileChooser != null) {
      final File file = getFile();
      if (file != null) {
        if (file.exists()) {
          this.fileChooser.setSelectedFile(file);
        } else {
          for (File directory = file.getParentFile(); directory != null; directory = directory
            .getParentFile()) {
            if (directory.exists()) {
              this.fileChooser.setCurrentDirectory(directory);
              return;
            }
          }
        }
      }
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
