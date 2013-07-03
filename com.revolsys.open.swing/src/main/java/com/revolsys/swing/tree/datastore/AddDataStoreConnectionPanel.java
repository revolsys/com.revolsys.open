package com.revolsys.swing.tree.datastore;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.store.ConnectionRegistry;
import com.revolsys.gis.data.store.DataObjectStoreConnection;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;

public class AddDataStoreConnectionPanel extends ValueField {
  private static final long serialVersionUID = 2750736040832727823L;

  private final JTextField passwordField;

  private final JTextField usernameField;

  private final JTextField urlField;

  private final JTextField nameField;

  private final ConnectionRegistry<DataObjectStoreConnection> registry;

  private final String name;

  public AddDataStoreConnectionPanel(
    final ConnectionRegistry<DataObjectStoreConnection> registry) {
    this(registry, null);

  }

  public AddDataStoreConnectionPanel(
    final ConnectionRegistry<DataObjectStoreConnection> registry,
    final String name) {
    this.registry = registry;
    this.name = name;
    add(new JLabel("Name:"));
    nameField = new JTextField("", 50);
    if (StringUtils.hasText(name)) {
      setTitle("Add Data Store " + name);
      nameField.setText(name);
      nameField.setEditable(false);
    } else {
      setTitle("Add Data Store");
    }
    add(nameField);
    add(new JLabel("URL:"));
    urlField = new JTextField("jdbc:<driver>:<connection>", 50);
    add(urlField);
    add(new JLabel("Username:"));
    usernameField = new JTextField("", 20);
    add(usernameField);
    add(new JLabel("Password:"));
    passwordField = new JTextField("", 20);
    add(passwordField);
    GroupLayoutUtil.makeColumns(this, 2);

  }

  @Override
  public void save() {
    super.save();
    final Map<String, Object> properties = new LinkedHashMap<String, Object>();
    if (StringUtils.hasText(name)) {
      properties.put("name", name);
    } else {
      properties.put("name", nameField.getText());

    }
    final Map<String, String> connectionParameters = new LinkedHashMap<String, String>();
    properties.put("connection", connectionParameters);
    connectionParameters.put("url", urlField.getText());
    connectionParameters.put("password", passwordField.getText());
    connectionParameters.put("username", usernameField.getText());
    registry.createConnection(properties);
  }
}
