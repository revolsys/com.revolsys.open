package com.revolsys.swing.tree.datastore;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectStoreConnections;
import com.revolsys.swing.action.I18nAction;
import com.revolsys.swing.layout.SpringLayoutUtil;
import com.revolsys.swing.listener.InvokeMethodActionListener;
import com.revolsys.swing.tree.TreeUtil;

public class AddConnection extends I18nAction {
  /**
   * 
   */
  private static final long serialVersionUID = 2750736040832727823L;

  private JTextField passwordField;

  private JTextField usernameField;

  private JTextField urlField;

  private JTextField nameField;

  private DataObjectStoreConnections connections;

  private JDialog dialog;

  public AddConnection() {
    super(SilkIconLoader.getIcon("database_add"));
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final Object source = e.getSource();
    final Window window = SpringLayoutUtil.getWindow(source);
    connections = TreeUtil.getFirstSelectedNode(source,
      DataObjectStoreConnections.class);
    if (connections != null) {
      dialog = new JDialog(window);
      final Container contentPane = dialog.getContentPane();
      final JPanel panel = new JPanel(new BorderLayout());
      contentPane.add(panel);

      final JPanel connectionPanel = new JPanel(new SpringLayout());
      panel.add(connectionPanel, BorderLayout.NORTH);
      connectionPanel.add(new JLabel("Column:"));
      nameField = new JTextField("", 50);
      connectionPanel.add(nameField);
      connectionPanel.add(new JLabel("URL:"));
      urlField = new JTextField("jdbc:<driver>:<connection>", 50);
      connectionPanel.add(urlField);
      connectionPanel.add(new JLabel("Username:"));
      usernameField = new JTextField("", 20);
      connectionPanel.add(usernameField);
      connectionPanel.add(new JLabel("Password:"));
      passwordField = new JTextField("", 20);
      connectionPanel.add(passwordField);
      SpringLayoutUtil.makeColumns(connectionPanel, 2, 5, 5, 5, 5);

      final JButton connectButton = new JButton("OK");
      panel.add(connectButton, BorderLayout.SOUTH);
      connectButton.addActionListener(new InvokeMethodActionListener(this,
        "createConnection"));

      dialog.setSize(800, 600);
      dialog.setVisible(true);
    }
  }

  public void createConnection() {
    final Map<String, String> connectionParameters = new LinkedHashMap<String, String>();
    connectionParameters.put("url", urlField.getText());
    connectionParameters.put("password", passwordField.getText());
    connectionParameters.put("username", usernameField.getText());
    connections.createConnection(nameField.getText(), connectionParameters);
    dialog.setVisible(false);
  }
}
