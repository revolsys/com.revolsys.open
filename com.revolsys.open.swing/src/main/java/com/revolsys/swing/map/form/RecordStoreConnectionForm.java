package com.revolsys.swing.map.form;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.datatype.DataTypes;
import com.revolsys.io.connection.ConnectionRegistry;
import com.revolsys.jdbc.io.JdbcDatabaseFactory;
import com.revolsys.record.io.RecordStoreConnection;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.util.PasswordUtil;

public class RecordStoreConnectionForm extends Form {
  private static final long serialVersionUID = 2750736040832727823L;

  @SuppressWarnings("unchecked")
  public RecordStoreConnectionForm(final ConnectionRegistry<RecordStoreConnection> registry,
    final RecordStoreConnection connection) {
    setOpaque(true);

    addLabelAndField(new TextField("name", 50));
    final TextField recordStoreType = new TextField("recordStoreType", 30);
    recordStoreType.setEditable(false);
    addLabelAndField(recordStoreType);

    addLabelAndField(new TextField("url", 255));

    addLabelAndField(new TextField("host", 255));

    addLabelAndField(new NumberTextField("port", DataTypes.INT, 1, 65535));

    addLabelAndField(new TextField("database", 64));

    addLabelAndField(new TextField("serviceName", 64));

    addLabelAndField(new TextField("user", 30));

    addLabelAndField(new TextField("password", 30));

    final Map<String, String> allConnectionUrlMap = new TreeMap<>();
    for (final JdbcDatabaseFactory databaseFactory : JdbcDatabaseFactory.databaseFactories()) {
      final Map<String, String> connectionUrlMap = databaseFactory.getConnectionUrlMap();
      allConnectionUrlMap.putAll(connectionUrlMap);

    }
    final List<String> connectionNames = new ArrayList<>();
    connectionNames.add(null);
    connectionNames.addAll(allConnectionUrlMap.keySet());
    final ComboBox<String> connectionNamesField = ComboBox.newComboBox("namedConnection",
      connectionNames);
    connectionNamesField.addItemListener((e) -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        final String connectionName = (String)e.getItem();
        if (connectionName != null) {
          final String url = allConnectionUrlMap.get(connectionName);
          setFieldValue("url", url);
        }
      }
    });
    addLabelAndField(connectionNamesField);

    GroupLayouts.makeColumns(this, 2, true, false);
    addFieldValueListener("url", (final String url) -> {
      for (final JdbcDatabaseFactory factory : JdbcDatabaseFactory.databaseFactories()) {
        final Map<String, Object> parameters = factory.parseJdbcUrl(url);
        if (!parameters.isEmpty()) {
          setFieldValues(parameters);
          return;
        }
      }
    });
    if (connection != null) {
      final String name = connection.getName();
      setFieldValue("name", name);
      final Map<String, Object> config = connection.getConfig();
      final Map<String, String> connectionParameters = (Map<String, String>)config
        .get("connection");
      setFieldValues(connectionParameters);
    }

    setPreferredSize(new Dimension(400, 300));
  }

  @Override
  public void save() {
    super.save();
    final Map<String, Object> properties = new LinkedHashMap<>();
    properties.put("name", getFieldValue("name"));
    final Map<String, String> connectionParameters = new LinkedHashMap<>();
    properties.put("connection", connectionParameters);

    for (final String fieldName : Arrays.asList("url", "user", "password")) {
      String fieldValue = getFieldValue(fieldName);
      if ("password".equals(fieldName)) {
        fieldValue = PasswordUtil.encrypt(fieldValue);
      }
      connectionParameters.put(fieldName, fieldValue);
    }
    // this.registry.newConnection(properties);

  }

  // @Override
  // public boolean setFieldValue(final String name, final Object value) {
  // final boolean result = super.setFieldValue(name, value);
  // if (name.equals("url")) {
  // final String url = (String)value;
  // if (Property.hasValue(url)) {
  // final Pattern pattern =
  // Pattern.compile("jdbc:oracle:thin:@([^:]+):(\\d+):([^:]+)");
  // final Matcher matcher = pattern.matcher(url);
  // if (matcher.matches()) {
  // final String host = matcher.group(1);
  // setFieldValue("host", host);
  // final String port = matcher.group(2);
  // setFieldValue("port", port);
  // final String serviceName = matcher.group(3);
  // setFieldValue("serviceName", serviceName);
  // }
  // }
  // }
  // return result;
  // }
}
