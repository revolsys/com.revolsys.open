package com.revolsys.jump.ui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JComponent;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.FileNamePanel;

public class FileFieldComponentFactory implements FieldComponentFactory {
  private WorkbenchContext workbenchContext;

  public FileFieldComponentFactory(final WorkbenchContext workbenchContext) {
    this.workbenchContext = workbenchContext;
  }

  public Object getValue(final JComponent component) {
    if (component instanceof FileNamePanel) {
      FileNamePanel fileNamePanel = (FileNamePanel)component;
      return fileNamePanel.getSelectedFile();
    }
    return null;
  }

  public void setValue(final JComponent component, final Object value) {
    if (component instanceof FileNamePanel) {
      FileNamePanel fileNamePanel = (FileNamePanel)component;
      File file = null;
      if (value != null) {
        file = new File(value.toString());
      }
      fileNamePanel.setSelectedFile(file);
    }
  }

  public JComponent createComponent() {
    FileNamePanel fileNamePanel = new FileNamePanel(
      workbenchContext.getErrorHandler());
    fileNamePanel.setUpperDescription("");
    return fileNamePanel;
  }

  public JComponent createComponent(final ValueChangeListener listener) {
    final FileNamePanel fileNamePanel = new FileNamePanel(
      workbenchContext.getErrorHandler());
    fileNamePanel.setUpperDescription("");
    fileNamePanel.addBrowseListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        File file = fileNamePanel.getSelectedFile();
        listener.valueChanged(new ValueChangeEvent(fileNamePanel,
          file.getAbsolutePath()));
      }
    });
    return fileNamePanel;
  }

}
