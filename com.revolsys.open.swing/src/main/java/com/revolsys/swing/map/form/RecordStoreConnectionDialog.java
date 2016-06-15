package com.revolsys.swing.map.form;

import java.awt.GridLayout;

import com.revolsys.record.io.RecordStoreConnection;
import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BaseDialog;

public class RecordStoreConnectionDialog extends BaseDialog {
  private static final long serialVersionUID = 1L;

  public RecordStoreConnectionDialog(final RecordStoreConnectionRegistry registry) {
    this(registry, null);
  }

  public RecordStoreConnectionDialog(final RecordStoreConnectionRegistry registry,
    final RecordStoreConnection connection) {
    super(SwingUtil.getActiveWindow(), "Add Record Store Connection",
      ModalityType.APPLICATION_MODAL);
    setLayout(new GridLayout(1, 1));
    if (connection != null) {
      setTitle("Edit Record Store Connection " + connection.getName());
    }

    final RecordStoreConnectionForm form = new RecordStoreConnectionForm(registry, connection);
    add(form);

    pack();
  }
}
