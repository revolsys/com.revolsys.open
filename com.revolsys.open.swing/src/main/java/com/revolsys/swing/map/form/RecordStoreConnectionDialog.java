package com.revolsys.swing.map.form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXList;

import com.revolsys.record.io.RecordStoreConnection;
import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.swing.EventQueue;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BaseDialog;
import com.revolsys.swing.list.renderer.IconListCellRenderer;
import com.revolsys.swing.map.border.FullSizeLayoutManager;

public class RecordStoreConnectionDialog extends BaseDialog {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final JXList buttons;

  private final JLayeredPane panels;

  public RecordStoreConnectionDialog(final RecordStoreConnectionRegistry registry) {
    this(registry, null);
  }

  public RecordStoreConnectionDialog(final RecordStoreConnectionRegistry registry,
    final RecordStoreConnection connection) {
    super(SwingUtil.getActiveWindow(), "Add Record Store Connection",
      ModalityType.APPLICATION_MODAL);
    if (connection != null) {
      setTitle("Edit Record Store Connection " + connection.getName());
    }
    this.panels = new JLayeredPane();
    this.panels.setOpaque(true);
    this.panels.setBackground(Color.WHITE);
    this.panels.setVisible(true);
    this.panels.setLayout(new FullSizeLayoutManager());

    int index = 0;
    this.panels.setPreferredSize(new Dimension(500, 310));
    final OracleRecordStoreConnectionPanel oraclePanel = new OracleRecordStoreConnectionPanel(
      registry, connection);
    SwingUtil.setTitledBorder(oraclePanel, "Oracle");
    this.panels.add(oraclePanel, 0);

    final AddRecordStoreConnectionPanel postgresPanel = new AddRecordStoreConnectionPanel(registry);
    SwingUtil.setTitledBorder(postgresPanel, "PostgreSQL/PostGIS");
    this.panels.add(postgresPanel, new Integer(index++));

    final AddRecordStoreConnectionPanel filePanel = new AddRecordStoreConnectionPanel(registry);
    SwingUtil.setTitledBorder(filePanel, "File");
    this.panels.add(filePanel, new Integer(index++));

    this.buttons = new JXList(new Object[] {
      "Oracle", "PostgreSQL/PostGIS"
    });
    this.buttons.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    EventQueue.addListSelection(this.buttons, () -> selectionChangeType());

    final Map<Object, Icon> icons = new HashMap<>();
    icons.put("Oracle", Icons.getIcon("database"));
    icons.put("PostgreSQL/PostGIS", Icons.getIcon("database"));
    final IconListCellRenderer renderer = new IconListCellRenderer(icons);
    renderer.setAlignmentY(Component.CENTER_ALIGNMENT);
    renderer.setHorizontalAlignment(SwingConstants.CENTER);
    renderer.setVerticalTextPosition(SwingConstants.BOTTOM);
    renderer.setHorizontalTextPosition(SwingConstants.CENTER);
    this.buttons.setCellRenderer(renderer);
    final JScrollPane buttonScroll = new JScrollPane(this.buttons);
    buttonScroll.setPreferredSize(new Dimension(150, 400));

    add(buttonScroll, BorderLayout.WEST);
    add(new JScrollPane(this.panels), BorderLayout.CENTER);
    pack();
    this.buttons.setSelectedIndex(0);
  }

  public void selectionChangeType() {
    final int index = this.buttons.getSelectedIndex();
    final int componentCount = this.panels.getComponentCount();
    if (index > -1 && index < componentCount) {
      for (int i = 0; i < componentCount; i++) {
        final Component component = this.panels.getComponent(i);
        if (i == index) {
          component.setVisible(true);
        } else {
          component.setVisible(false);
        }
      }
    }
  }
}
