package com.revolsys.swing.tree.record.old;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.springframework.util.StringUtils;

import com.revolsys.io.connection.ConnectionRegistry;
import com.revolsys.io.datastore.RecordStoreConnection;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.util.PasswordUtil;

public class AddDataStoreConnectionPanel extends ValueField {
  private static final long serialVersionUID = 2750736040832727823L;

  private final JTextField passwordField;

  private final JTextField usernameField;

  private final JTextField urlField;

  private final JTextField nameField;

  private final ConnectionRegistry<RecordStoreConnection> registry;

  private final String name;

  public AddDataStoreConnectionPanel(
    final ConnectionRegistry<RecordStoreConnection> registry) {
    this(registry, null);

  }

  public AddDataStoreConnectionPanel(
    final ConnectionRegistry<RecordStoreConnection> registry,
    final String name) {
    this.registry = registry;
    this.name = name;
    add(new JLabel("Name:"));
    this.nameField = new JTextField("", 50);
    if (StringUtils.hasText(name)) {
      setTitle("Add Data Store " + name);
      this.nameField.setText(name);
      this.nameField.setEditable(false);
    } else {
      setTitle("Add Data Store");
    }
    add(this.nameField);
    add(new JLabel("URL:"));
    this.urlField = new JTextField("jdbc:<driver>:<connection>", 50);
    add(this.urlField);
    add(new JLabel("Username:"));
    this.usernameField = new JTextField("", 20);
    add(this.usernameField);
    add(new JLabel("Password:"));
    this.passwordField = new JTextField("", 20);
    add(this.passwordField);
    GroupLayoutUtil.makeColumns(this, 2, true);

  }

  @Override
  public void save() {
    super.save();
    final Map<String, Object> properties = new LinkedHashMap<String, Object>();
    if (StringUtils.hasText(this.name)) {
      properties.put("name", this.name);
    } else {
      properties.put("name", this.nameField.getText());

    }
    final Map<String, String> connectionParameters = new LinkedHashMap<String, String>();
    properties.put("connection", connectionParameters);

    final String url = this.urlField.getText();
    connectionParameters.put("url", url);

    final String username = this.usernameField.getText();
    connectionParameters.put("username", username);

    final String password = this.passwordField.getText();
    connectionParameters.put("password", PasswordUtil.encrypt(password));
    this.registry.createConnection(properties);
  }
}
