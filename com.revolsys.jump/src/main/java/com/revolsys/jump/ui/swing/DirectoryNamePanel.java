package com.revolsys.jump.ui.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.GUIUtil;

public class DirectoryNamePanel extends JPanel {
  /**
   * 
   */
  private static final long serialVersionUID = -8433151755294925911L;

  private JComboBox comboBox = new JComboBox();

  private JButton browseButton = new JButton();

  private ErrorHandler errorHandler;

  private DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();

  private boolean directoryMustExist;

  private List<ActionListener> browseListeners = new ArrayList<ActionListener>();

  private static final int MAX_CACHE_SIZE = 10;

  public DirectoryNamePanel(final ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;

    jbInit();

    GUIUtil.fixEditableComboBox(comboBox);
  }

  public void setDirectoryMustExist(final boolean fileMustExist) {
    this.directoryMustExist = fileMustExist;
  }

  private void jbInit() {
    this.setLayout(new SpringLayout());

    comboBox.setPreferredSize(new Dimension(300, 21));
    comboBox.setEditable(true);
    comboBox.setModel(comboBoxModel);
    add(comboBox);

    browseButton.setText(I18N.get("ui.FileNamePanel.browse"));
    browseButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        try {
          File file = browse();
          if (file != null) {
            comboBox.getEditor().setItem(file.getAbsolutePath());
            fireBrowseEvent(e);
          }
        } catch (Throwable t) {
          errorHandler.handleThrowable(t);
        }
      }
    });
    add(browseButton);
    SpringUtilities.makeCompactGrid(this, 1, 2, 5, 5, 5, 5);
  }

  /**
   * Side effect: adds file to the combobox list of recently selected files.
   */
  public File getSelectedFile() {
    Assert.isTrue(isInputValid(), getValidationError());

    File file = new File(getComboBoxText());
    addFile(file);

    return file;
  }

  public boolean isInputValid() {
    return null == getValidationError();
  }

  private String getComboBoxText() {
    return (String)comboBox.getEditor().getItem();
  }

  public void setSlectedFile(final File file) {
    if (file == null) {
      comboBox.getEditor().setItem("");
    } else {
      addFile(file);
    }
  }

  public String getValidationError() {
    if (getComboBoxText().trim().equals("")) {
      return I18N.get("ui.FileNamePanel.no-file-was-specified");
    }

    File file = new File(getComboBoxText());

    if (directoryMustExist && !file.exists()) {
      return I18N.get("ui.FileNamePanel.specified-file-does-not-exist") + " "
        + getComboBoxText();
    }

    return null;
  }

  private void fireBrowseEvent(final ActionEvent e) {
    for (ActionListener listener : browseListeners) {
      listener.actionPerformed(e);
    }
  }

  /**
   * Notify the ActionListener whenever the user picks a file using the Browse
   * button
   */
  public void addBrowseListener(final ActionListener l) {
    browseListeners.add(l);
  }

  private File browse() {
    JFileChooser fileChooser;
    if (directoryMustExist) {
      fileChooser = GUIUtil.createJFileChooserWithExistenceChecking();
    } else {
      fileChooser = new JFileChooser();
    }
    fileChooser.setDialogTitle(I18N.get("ui.FileNamePanel.browse"));

    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    File initialFile = getInitialFile();

    if (initialFile.exists() && initialFile.isDirectory()) {
      fileChooser.setCurrentDirectory(initialFile);
    } else if (initialFile.getParentFile() != null
      && initialFile.getParentFile().exists()) {
      fileChooser.setCurrentDirectory(initialFile.getParentFile());
    }

    fileChooser.setMultiSelectionEnabled(false);

    if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(SwingUtilities.windowForComponent(this))) {
      return null;
    }
    return fileChooser.getSelectedFile();
  }

  public File getInitialFile() {
    return new File(getComboBoxText());
  }

  public void addFile(final File file) {
    try {
      File directory;
      if (file.isDirectory()) {
        directory = file;
      } else {
        directory = file.getParentFile();
      }
      String path = directory.getCanonicalPath();
      comboBoxModel.removeElement(path);
      comboBoxModel.insertElementAt(directory.getCanonicalPath(), 0);

      if (comboBoxModel.getSize() > MAX_CACHE_SIZE) {
        comboBoxModel.removeElementAt(comboBoxModel.getSize() - 1);
      }

      comboBox.setSelectedIndex(0);
    } catch (IOException e) {
    }
  }

  public List<File> getFiles() {
    List<File> files = new ArrayList<File>();
    for (int i = 0; i < comboBoxModel.getSize(); i++) {
      String path = (String)comboBox.getItemAt(i);
      files.add(new File(path));
    }
    return files;
  }

  public void setFiles(final List<File> files) {

    List<File> reverseFiles = new ArrayList<File>(files);
    Collections.reverse(reverseFiles);
    for (File file : reverseFiles) {
      addFile(file);
    }
  }

}
