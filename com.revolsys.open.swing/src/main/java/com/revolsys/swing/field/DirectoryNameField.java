package com.revolsys.swing.field;

import java.awt.Color;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.springframework.util.StringUtils;

import com.revolsys.swing.layout.SpringLayoutUtil;
import com.revolsys.swing.listener.InvokeMethodActionListener;

public class DirectoryNameField extends JPanel implements
  ValidatingField<String> {
  private static final long serialVersionUID = -8433151755294925911L;

  private final JTextField directoryName = new JTextField(70);

  private final JButton browseButton = new JButton();

  public DirectoryNameField() {
    super(new SpringLayout());

    add(directoryName);
    browseButton.setText("Browse...");
    browseButton.addActionListener(new InvokeMethodActionListener(this,
      "browseClick"));
    add(browseButton);
    SpringLayoutUtil.makeCompactGrid(this, 1, 2, 5, 5, 5, 5);
  }

  public void browseClick() {
    try {
      final JFileChooser fileChooser = new JFileChooser();

      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      final String directoryPath = getDirectoryPath();
      final File initialFile = new File(directoryPath);

      if (initialFile.getParentFile() != null
        && initialFile.getParentFile().exists()) {
        fileChooser.setCurrentDirectory(initialFile.getParentFile());
      }

      fileChooser.setMultiSelectionEnabled(false);

      if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(SwingUtilities.windowForComponent(this))) {
        final File file = fileChooser.getSelectedFile();
        if (file != null) {
          directoryName.setText(file.getCanonicalPath());
        }
      }
    } catch (final Throwable t) {
      JOptionPane.showMessageDialog(this, t.getMessage(), "Error",
        JOptionPane.ERROR_MESSAGE);
    }
  }

  public File getDirectoryFile() {
    final String directoryPath = getDirectoryPath();
    if (StringUtils.hasText(directoryPath)) {
      return new File(directoryPath);
    } else {
      return null;
    }
  }

  public String getDirectoryPath() {
    return directoryName.getText();
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
    return (T)directoryName.getText();
  }

  @Override
  public boolean isFieldValid() {

    final File directory = getDirectoryFile();
    if (directory == null || !directory.exists()) {
      directoryName.setForeground(Color.RED);
      directoryName.setSelectedTextColor(Color.RED);
      directoryName.setBackground(Color.PINK);
      return false;
    } else {
      directoryName.setForeground(Color.BLACK);
      directoryName.setSelectedTextColor(Color.BLACK);
      directoryName.setBackground(Color.WHITE);
      return true;
    }
  }

  public void setDirectoryPath(final String directoryPath) {
    directoryName.setText(directoryPath);
  }

  @Override
  public void setFieldValue(final String fieldValue) {
    setDirectoryPath(fieldValue);
  }
}
