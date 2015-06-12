package com.revolsys.swing.map.form;

import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.revolsys.data.record.io.RecordStoreConnection;
import com.revolsys.data.types.DataTypes;
import com.revolsys.io.connection.ConnectionRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.util.PasswordUtil;
import com.revolsys.util.Property;

public class OracleRecordStoreConnectionPanel extends ValueField implements PropertyChangeListener {
  private static final long serialVersionUID = 2750736040832727823L;

  private String name;

  private final Map<String, Field> fieldsByName = new HashMap<>();

  @SuppressWarnings("unchecked")
  public OracleRecordStoreConnectionPanel(final ConnectionRegistry<RecordStoreConnection> registry,
    final RecordStoreConnection connection) {
    addField(new TextField("name", 50));

    addField(new TextField("url", 255));

    addField(new TextField("host", 255));

    addField(new NumberTextField("port", DataTypes.INT, 1, 65535));

    addField(new TextField("serviceName", 64));

    addField(new TextField("username", 30));

    addField(new TextField("password", 30));

    GroupLayoutUtil.makeColumns(this, 2, true);

    if (connection != null) {
      this.name = connection.getName();
      setFieldValue("name", this.name);
      final Map<String, Object> config = connection.getConfig();
      final Map<String, String> connectionParameters = (Map<String, String>)config.get("connection");
      setFieldValues(connectionParameters);
    }
    setPreferredSize(new Dimension(400, 300));
  }

  private void addField(final Field field) {
    if (field != null) {
      final String name = field.getFieldName();
      this.fieldsByName.put(name, field);
      SwingUtil.addLabel(this, name);
      add((Component)field);
      Property.addListener(field, this);
    }
  }

  public Field getField(final String fieldName) {
    return this.fieldsByName.get(fieldName);
  }

  public <V> V getFieldValue(final String fieldName) {
    final Field field = getField(fieldName);
    if (field == null) {
      return null;
    } else {
      return field.getFieldValue();
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    // TODO Auto-generated method stub

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

  public void setFieldValue(final String name, final Object value) {
    final Field field = getField(name);
    if (field != null) {
      field.setFieldValue(value);
    }
    if (name.equals("url")) {
      final String url = (String)value;
      if (Property.hasValue(url)) {
        final Pattern pattern = Pattern.compile("jdbc:oracle:thin:@([^:]+):(\\d+):([^:]+)");
        final Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
          final String host = matcher.group(1);
          setFieldValue("host", host);
          final String port = matcher.group(2);
          setFieldValue("port", port);
          final String serviceName = matcher.group(3);
          setFieldValue("serviceName", serviceName);
        }
      }
    }
  }

  public void setFieldValues(final Map<String, ? extends Object> values) {
    if (values != null) {
      for (final Entry<String, ? extends Object> entry : values.entrySet()) {
        final String name = entry.getKey();
        final Object value = entry.getValue();
        setFieldValue(name, value);
      }
    }
  }
}
