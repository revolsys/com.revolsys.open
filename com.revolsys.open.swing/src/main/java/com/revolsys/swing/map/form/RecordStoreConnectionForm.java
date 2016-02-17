package com.revolsys.swing.map.form;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFileChooser;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.Maps;
import com.revolsys.datatype.DataTypes;
import com.revolsys.io.IoFactory;
import com.revolsys.io.connection.ConnectionRegistry;
import com.revolsys.jdbc.io.JdbcDatabaseFactory;
import com.revolsys.record.io.AbstractRecordIoFactory;
import com.revolsys.record.io.FileRecordStoreFactory;
import com.revolsys.record.io.RecordStoreConnection;
import com.revolsys.record.io.RecordStoreFactory;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.FileField;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.field.TextField;
import com.revolsys.util.PasswordUtil;
import com.revolsys.util.Property;

public final class RecordStoreConnectionForm extends Form {
  private static final long serialVersionUID = 2750736040832727823L;

  private final List<String> recordStoreTypes;

  private final Map<String, RecordStoreFactory> recordStoreFactoryByName = new TreeMap<>();

  @SuppressWarnings("unchecked")
  public RecordStoreConnectionForm(final ConnectionRegistry<RecordStoreConnection> registry,
    final RecordStoreConnection connection) {
    super(new VerticalLayout());

    final Map<String, String> allConnectionUrlMap = new TreeMap<>();

    for (final RecordStoreFactory recordStoreFactory : IoFactory
      .factories(RecordStoreFactory.class)) {
      if (recordStoreFactory instanceof AbstractRecordIoFactory) {
        // Ignore these for now
      } else {
        final String name = recordStoreFactory.getName();
        this.recordStoreFactoryByName.put(name, recordStoreFactory);
        if (recordStoreFactory instanceof JdbcDatabaseFactory) {
          final JdbcDatabaseFactory databaseFactory = (JdbcDatabaseFactory)recordStoreFactory;
          final Map<String, String> connectionUrlMap = databaseFactory.getConnectionUrlMap();
          allConnectionUrlMap.putAll(connectionUrlMap);
        }
      }
    }
    this.recordStoreTypes = Lists.toArray(this.recordStoreFactoryByName.keySet());

    final List<String> connectionNames = new ArrayList<>();
    connectionNames.add(null);
    connectionNames.addAll(allConnectionUrlMap.keySet());

    final ComboBox<String> connectionNamesField = ComboBox.newComboBox("namedConnection",
      connectionNames);
    connectionNamesField.addItemListener((e) ->

    {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        final String connectionName = (String)e.getItem();
        if (connectionName != null) {
          final String url = allConnectionUrlMap.get(connectionName);
          setFieldValue("url", url);
        }
      }
    });

    addComponents(addNewPanelTitledLabelledFields( //
      "General", //
      new TextField("name", 50), //
      new TextField("url", 50), //
      ComboBox.newComboBox("recordStoreType", this.recordStoreTypes) //
    ), addNewPanelTitledLabelledFields( //
      "Named Connections", //
      connectionNamesField //
    ), addNewPanelTitledLabelledFields( //
      "File Connection", //
      new FileField("file") //
    ), addNewPanelTitledLabelledFields( //
      "JDBC Connection", //
      new TextField("host", 40), //
      new NumberTextField("port", DataTypes.INT, 1, 65535), //
      new TextField("database", 30), //
      new TextField("user", 30), //
      new TextField("password", 30) //
    ));
    if (connection != null) {
      final String name = connection.getName();
      setFieldValue("name", name);
      final Map<String, Object> config = connection.getConfig();
      final Map<String, String> connectionParameters = (Map<String, String>)config
        .get("connection");
      setFieldValues(connectionParameters);
    }
  }

  @Override
  protected void postSetFieldValues(final Map<String, Object> newValues) {
    String recordStoreType = (String)newValues.get("recordStoreType");
    if (Property.hasValue(recordStoreType)) {
      if (this.recordStoreTypes.contains(recordStoreType)) {
        refreshUrlFromFieldValues(recordStoreType);
      }
      return;
    }
    final String url = (String)newValues.get("url");
    if (Property.hasValue(url)) {
      for (final RecordStoreFactory recordStoreFactory : this.recordStoreFactoryByName.values()) {
        final Map<String, Object> urlFieldValues = recordStoreFactory.parseUrl(url);
        if (!urlFieldValues.isEmpty()) {
          Maps.retainIfNotEqual(urlFieldValues, newValues);
          if (recordStoreFactory instanceof FileRecordStoreFactory) {
            final FileRecordStoreFactory fileRecordStoreFactory = (FileRecordStoreFactory)recordStoreFactory;
            final FileField fileField = getField("file");
            if (fileRecordStoreFactory.isDirectory()) {
              fileField.setFileSelectionMode(JFileChooser.FILES_ONLY);
            } else {
              fileField.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }
          }
          setFieldValues(urlFieldValues);
          return;
        }
      }
    }
    recordStoreType = getFieldValue("recordStoreType");
    refreshUrlFromFieldValues(recordStoreType);
  }

  private void refreshUrlFromFieldValues(final String recordStoreType) {
    if (recordStoreType != null) {
      final RecordStoreFactory databaseFactory = this.recordStoreFactoryByName.get(recordStoreType);
      if (databaseFactory != null) {
        final Map<String, Object> fieldValues = getFieldValues();
        final String newUrl = databaseFactory.toUrl(fieldValues);
        if (Property.hasValue(newUrl)) {
          setFieldValue("url", newUrl);
          return;
        }
      }
    }
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
