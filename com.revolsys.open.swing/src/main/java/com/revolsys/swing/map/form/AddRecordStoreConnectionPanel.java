package com.revolsys.swing.map.form;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTextField;

import com.revolsys.io.connection.ConnectionRegistry;
import com.revolsys.record.io.RecordStoreConnection;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.util.PasswordUtil;
import com.revolsys.util.Property;

public class AddRecordStoreConnectionPanel extends ValueField {
  private static final long serialVersionUID = 2750736040832727823L;

  private final String name;

  private final JTextField nameField;

  private final JTextField passwordField;

  private final ConnectionRegistry<RecordStoreConnection> registry;

  private final JTextField urlField;

  private final JTextField usernameField;

  public AddRecordStoreConnectionPanel(final ConnectionRegistry<RecordStoreConnection> registry) {
    this(registry, null);

  }

  public AddRecordStoreConnectionPanel(final ConnectionRegistry<RecordStoreConnection> registry,
    final String name) {
    this.registry = registry;
    this.name = name;
    add(new JLabel("Name:"));
    this.nameField = new JTextField("", 50);
    if (Property.hasValue(name)) {
      setTitle("Add Record Store " + name);
      this.nameField.setText(name);
      this.nameField.setEditable(false);
    } else {
      setTitle("Add Record Store");
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
    final Map<String, Object> properties = new LinkedHashMap<>();
    if (Property.hasValue(this.name)) {
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
