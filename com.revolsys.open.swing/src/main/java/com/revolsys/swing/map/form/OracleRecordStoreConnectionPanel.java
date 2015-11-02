package com.revolsys.swing.map.form;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.datatype.DataTypes;
import com.revolsys.io.connection.ConnectionRegistry;
import com.revolsys.jdbc.io.JdbcDatabaseFactory;
import com.revolsys.jdbc.io.JdbcFactoryRegistry;
import com.revolsys.record.io.RecordStoreConnection;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.util.PasswordUtil;

public class OracleRecordStoreConnectionPanel extends Form {
  private static final long serialVersionUID = 2750736040832727823L;

  private String name;

  @SuppressWarnings("unchecked")
  public OracleRecordStoreConnectionPanel(final ConnectionRegistry<RecordStoreConnection> registry,
    final RecordStoreConnection connection) {
    setOpaque(true);

    addLabelAndField(new TextField("name", 50));

    addLabelAndField(new TextField("url", 255));

    addLabelAndField(new TextField("host", 255));

    addLabelAndField(new NumberTextField("port", DataTypes.INT, 1, 65535));

    addLabelAndField(new TextField("serviceName", 64));

    addLabelAndField(new TextField("username", 30));

    addLabelAndField(new TextField("password", 30));

    final JdbcDatabaseFactory databaseFactory = JdbcFactoryRegistry.databaseFactory("Oracle");
    final Map<String, String> connectionUrlMap = databaseFactory.getConnectionUrlMap();
    final ComboBox<String> connectionNames = ComboBox.newComboBox("Tns Connections",
      connectionUrlMap.keySet());
    connectionNames.addItemListener((e) -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        final String connectionName = (String)e.getItem();
        final String url = connectionUrlMap.get(connectionName);
        setFieldValue("url", url);
      }
    });
    addLabelAndField(connectionNames);

    GroupLayouts.makeColumns(this, 2, true);
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

    for (final String fieldName : Arrays.asList("url", "username", "password")) {
      String fieldValue = getFieldValue(fieldName);
      if ("password".equals(fieldName)) {
        fieldValue = PasswordUtil.encrypt(fieldValue);
      }
      connectionParameters.put(fieldName, fieldValue);
    }
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
