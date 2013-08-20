package com.revolsys.swing.tree.datastore;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.revolsys.awt.SwingWorkerManager;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.i18n.I18n;
import com.revolsys.swing.i18n.NamedJPanel;
import com.revolsys.swing.layout.SpringLayoutUtil;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.util.PasswordUtil;

public class AddDataObjectStoreLayerPanel extends NamedJPanel {
  /**
   * 
   */
  private static final long serialVersionUID = 7562150193060319217L;

  private static final Preferences PREFERENCES = Preferences.userNodeForPackage(AddDataObjectStoreLayerPanel.class);

  private final JPanel storePanel = new JPanel(new BorderLayout());

  public AddDataObjectStoreLayerPanel(final LayerGroup layerGroup) {
    super(I18n.getInstance(AddDataObjectStoreLayerPanel.class).getCharSequence(
      "addDataStoreLayer"), SilkIconLoader.getIcon("database_table"));
    setLayout(new BorderLayout());
    final JPanel connectionPanel = new JPanel(new SpringLayout());
    add(connectionPanel, BorderLayout.NORTH);
    connectionPanel.add(new JLabel("URL:"));
    final JTextField urlField = new JTextField(
      "jdbc:oracle:thin:@localhost:1521:pdalaptop", 50);
    connectionPanel.add(urlField);
    connectionPanel.add(new JLabel("Username:"));
    final JTextField usernameField = new JTextField("trim", 20);
    connectionPanel.add(usernameField);
    connectionPanel.add(new JLabel("Password:"));
    final JTextField passwordField = new JTextField("tr1mmer", 20);
    connectionPanel.add(passwordField);
    SpringLayoutUtil.makeColumns(connectionPanel, 2, 5, 5, 5, 5);

    add(this.storePanel, BorderLayout.CENTER);

    final JButton connectButton = new JButton("Connect");
    add(connectButton, BorderLayout.SOUTH);
    connectButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        SwingWorkerManager.invokeLater(new Runnable() {
          @Override
          public void run() {
            final Map<String, Object> connectionProperties = new LinkedHashMap<String, Object>();
            connectionProperties.put("url", urlField.getText());
            connectionProperties.put("username", usernameField.getText());
            final String password = PasswordUtil.encrypt(passwordField.getText());
            connectionProperties.put("password", password);

            final DataObjectStore dataStore = DataObjectStoreFactoryRegistry.createDataObjectStore(connectionProperties);
            AddDataObjectStoreLayerPanel.this.storePanel.add(
              new DataObjectStoreTreePanel(dataStore), BorderLayout.CENTER);
            revalidate();
          }
        });
      }
    });
  }
}
