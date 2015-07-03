package com.revolsys.swing.map.tools;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import com.revolsys.data.record.io.RecordIo;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.FileField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.menu.MenuFactory;

public class ConvertFileTool extends ValueField implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public static void addMenuItem(final MenuFactory menuFactory) {
    menuFactory.addMenuItemTitleIcon("convert", "Convert Record File", "table_save",
      () -> new ConvertFileTool().showDialog());
  }

  private final FileField sourceFileField = new FileField("sourceFile");

  private final FileField targetFileField = new FileField("targetFile");

  public ConvertFileTool() {
    this.sourceFileField.addPropertyChangeListener("sourceFile", this);
    this.targetFileField.addPropertyChangeListener("targetFile", this);

    SwingUtil.addLabel(this, "Source File");
    add(this.sourceFileField);
    SwingUtil.addLabel(this, "Target File");
    add(this.targetFileField);
    GroupLayoutUtil.makeColumns(this, 2, true, true);
  }

  @Override
  protected void doSave() {
    final File sourceFile = this.sourceFileField.getFile();
    final File targetFile = this.targetFileField.getFile();
    RecordIo.copyRecords(sourceFile, targetFile);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {

  }
}
