package com.revolsys.swing.map.layer.record.component;

import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.exception.WrappedException;

import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.BaseDialog;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.table.RecordLayerTable;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerErrors;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.TablePanel;

public abstract class AbstractUpdateField extends BaseDialog {
  private static final long serialVersionUID = 1L;

  protected static EnableCheck newEnableCheck() {
    final EnableCheck enableCheck = MenuFactory.enableCheck((object) -> {
      if (object instanceof RecordLayerTable) {
        final RecordLayerTable table = (RecordLayerTable)object;
        final int columnIndex = TablePanel.getEventColumn();
        final RecordLayerTableModel tableModel = table.getModel();
        if (tableModel.isFieldEditable(columnIndex)) {
          if (tableModel.isIdField(columnIndex)) {
            return false;
          } else {
            return true;
          }
        } else {
          return false;
        }
      } else {
        return false;
      }
    });
    return enableCheck;
  }

  private final FieldDefinition fieldDefinition;

  private final DecimalFormat format = new DecimalFormat("#,###");

  private final AbstractRecordLayer layer;

  private JButton okButton;

  private final int recordCount;

  private final RecordLayerTable table;

  private final RecordLayerTableModel tableModel;

  private final String recordCountString;

  protected AbstractUpdateField(final String title) {
    super(title, ModalityType.DOCUMENT_MODAL);
    this.table = TablePanel.getEventTable();
    this.tableModel = this.table.getModel();
    this.layer = this.table.getLayer();
    final int eventColumn = TablePanel.getEventColumn();
    this.fieldDefinition = this.table.getColumnFieldDefinition(eventColumn);
    this.recordCount = this.tableModel.getRowCount();
    this.recordCountString = this.format.format(this.recordCount);
    initDialog();

  }

  protected void cancel() {
    if (isVisible()) {
      SwingUtil.dispose(this);
    }
  }

  private void finish() {
    if (this.recordCount > 100) {
      final int confirm = Dialogs.showConfirmDialog(
        "<html><p>Update <b style='color:#32CD32'>" + this.recordCountString + "</b> records?</p>"
          + "<p>This may take a long time or fail if there are many records.</p></html>",
        "Update Records?", JOptionPane.YES_NO_OPTION);
      if (confirm != JOptionPane.YES_OPTION) {
        setVisible(false);
        return;
      }
    }
    setVisible(false);

    final String title = getTitle();
    final Set<String> fieldNames = new LinkedHashSet<>();
    fieldNames.add(AbstractUpdateField.this.fieldDefinition.getName());
    fieldNames.addAll(AbstractUpdateField.this.layer.getFieldNames());
    final RecordLayerErrors errors = new RecordLayerErrors("Setting Field Values",
      AbstractUpdateField.this.layer, fieldNames);

    final Consumer<LayerRecord> action = record -> {
      try {
        updateRecord(record);
      } catch (final WrappedException e) {
        errors.addRecord(record, e.getCause());
      } catch (final Throwable e) {
        errors.addRecord(record, e);
      }
    };
    this.layer.processTasks(title, this.recordCount,
      AbstractUpdateField.this.tableModel::forEachRecord, action, monitor -> {
        if (!monitor.isCancelled()) {
          Invoke.later(() -> errors.showErrorDialog());
        }
      });
  }

  protected FieldDefinition getFieldDefinition() {
    return this.fieldDefinition;
  }

  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  protected String getProgressMonitorNote() {
    return "Set " + this.fieldDefinition.getName();
  }

  public String getRecordCountString() {
    return this.recordCountString;
  }

  protected void initDialog() {
    setLayout(new VerticalLayout());
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setResizable(false);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        cancel();
      }
    });

    final JPanel fieldPanel = initFieldPanel();
    final String fieldTitle = this.fieldDefinition.getTitle();
    fieldPanel.setBorder(BorderFactory.createTitledBorder("Set " + fieldTitle + " = "));

    add(fieldPanel);

    final JLabel recordCountLabel = new JLabel("<html><b style='color:#32CD32'>"
      + getRecordCountString() + "</b> records will be updated.</html>");
    recordCountLabel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
    add(recordCountLabel);

    final JComponent errorsPanel = initErrorsPanel();
    if (errorsPanel != null) {
      add(errorsPanel);
    }

    final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    add(buttonsPanel);

    final JButton cancelButton = RunnableAction.newButton("Cancel", this::cancel);
    buttonsPanel.add(cancelButton);

    this.okButton = RunnableAction.newButton("OK", this::finish);
    if (this.fieldDefinition.isRequired()) {
      this.okButton.setEnabled(false);
    }
    buttonsPanel.add(this.okButton);

    pack();
    SwingUtil.autoAdjustPosition(this);
  }

  protected JComponent initErrorsPanel() {
    return null;
  }

  protected abstract JPanel initFieldPanel();

  protected void setFormValid(final boolean valid) {
    this.okButton.setEnabled(valid);
  }

  protected abstract void updateRecord(final LayerRecord record);

}
