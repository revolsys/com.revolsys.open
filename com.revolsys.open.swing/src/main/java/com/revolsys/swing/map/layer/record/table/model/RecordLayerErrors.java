package com.revolsys.swing.map.layer.record.table.model;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.beans.ObjectPropertyException;
import com.revolsys.record.Record;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public class RecordLayerErrors {
  private final String title;

  private final List<Throwable> exceptions = new ArrayList<>();

  private final AbstractRecordLayer layer;

  private final List<String> messages = new ArrayList<>();

  private final List<Record> records = new ArrayList<>();

  private final Collection<String> fieldNames;

  public RecordLayerErrors(final String title, final AbstractRecordLayer layer) {
    this(title, layer, layer.getFieldNames());
  }

  public RecordLayerErrors(final String title, final AbstractRecordLayer layer,
    final Collection<String> fieldNames) {
    this.title = title;
    this.layer = layer;
    this.fieldNames = fieldNames;
  }

  public void addRecord(final LayerRecord record, final String errorMessage) {
    this.records.add(record);
    this.messages.add(errorMessage);
    this.exceptions.add(null);
  }

  public void addRecord(final LayerRecord record, final Throwable exception) {
    this.records.add(record);
    String message;
    if (exception instanceof ObjectPropertyException) {
      final ObjectPropertyException objectPropertyException = (ObjectPropertyException)exception;
      message = objectPropertyException.getPropertyName() + ": "
        + objectPropertyException.getMessage();
    } else {
      message = exception.getMessage();
    }
    if (!Property.hasValue(message)) {
      message = exception.getClass().getSimpleName();
    }
    this.messages.add(message);
    this.exceptions.add(exception);
  }

  public boolean showErrorDialog() {
    if (this.records.isEmpty()) {
      return true;
    } else {
      Invoke.later(() -> {
        final RecordLayerErrorsTableModel tableModel = new RecordLayerErrorsTableModel(this.layer,
          this.records, this.messages, this.exceptions, this.fieldNames);
        final String layerPath = this.layer.getPath();
        final BasePanel panel = new BasePanel(new VerticalLayout(),
          new JLabel("<html><p><b style=\"color:red\">Error " + this.title
            + " for layer:</b></p><p>" + layerPath + "</p>"),
          tableModel.newPanel());
        final Rectangle screenBounds = SwingUtil.getScreenBounds();
        panel.setPreferredSize(
          new Dimension(screenBounds.width - 300, tableModel.getRowCount() * 22 + 75));

        final Window window = Dialogs.getWindow();
        final JOptionPane pane = new JOptionPane(panel, JOptionPane.ERROR_MESSAGE,
          JOptionPane.DEFAULT_OPTION, null, null, null);

        pane.setComponentOrientation(window.getComponentOrientation());

        final JDialog dialog = pane.createDialog(window, "Error " + this.title + ": " + layerPath);

        dialog.pack();
        SwingUtil.setLocationCentre(screenBounds, dialog);
        dialog.setVisible(true);
        SwingUtil.dispose(dialog);
      });
      return false;
    }
  }
}
