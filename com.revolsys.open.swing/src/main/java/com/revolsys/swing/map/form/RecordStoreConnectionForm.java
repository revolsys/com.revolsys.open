package com.revolsys.swing.map.form;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.io.IoFactory;
import com.revolsys.jdbc.exception.DatabaseNotFoundException;
import com.revolsys.jdbc.io.JdbcDatabaseFactory;
import com.revolsys.record.io.AbstractRecordIoFactory;
import com.revolsys.record.io.FileRecordStoreFactory;
import com.revolsys.record.io.RecordStoreConnection;
import com.revolsys.record.io.RecordStoreConnectionManager;
import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.record.io.RecordStoreFactory;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.field.CheckBox;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.FileField;
import com.revolsys.swing.field.NumberTextField;
import com.revolsys.swing.field.PasswordField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.PasswordUtil;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public final class RecordStoreConnectionForm extends Form {
  private static final List<String> CONNECTION_FIELD_NAMES = Arrays.asList("url", "user",
    "password");

  private static final long serialVersionUID = 2750736040832727823L;

  public static void addHandlers() {
    RecordStoreConnectionManager.setInvalidRecordStoreFunction((connection, exception) -> {
      return Invoke.andWait(() -> {
        final RecordStoreConnectionRegistry registry = connection.getRegistry();
        final RecordStoreConnectionForm form = new RecordStoreConnectionForm(registry, connection,
          exception);
        return form.showDialog();
      });
    });

    RecordStoreConnectionManager.setMissingRecordStoreFunction((name) -> {
      final RecordStoreConnectionRegistry registry = RecordStoreConnectionManager.get()
        .getUserConnectionRegistry();
      Invoke.andWait(() -> {
        final RecordStoreConnectionForm form = new RecordStoreConnectionForm(registry, name);
        form.showDialog();
      });
      final RecordStoreConnection connection = registry.getConnection(name);
      if (connection == null) {
        return null;
      } else {
        return connection.getRecordStore();
      }
    });
  }

  private final List<String> recordStoreTypes;

  private final Map<String, RecordStoreFactory> recordStoreFactoryByName = new TreeMap<>();

  private final RecordStoreConnection connection;

  private final RecordStoreConnectionRegistry registry;

  private MapEx config;

  public RecordStoreConnectionForm(final RecordStoreConnectionRegistry registry) {
    this(registry, (RecordStoreConnection)null);
  }

  public RecordStoreConnectionForm(final RecordStoreConnectionRegistry registry,
    final RecordStoreConnection connection) {
    this(registry, connection, null);
  }

  @SuppressWarnings("unchecked")
  public RecordStoreConnectionForm(final RecordStoreConnectionRegistry registry,
    final RecordStoreConnection connection, Throwable exception) {
    super(new VerticalLayout());
    this.registry = registry;
    this.connection = connection;
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
    if (exception != null) {
      if (exception instanceof DatabaseNotFoundException) {
        final DatabaseNotFoundException databaseException = (DatabaseNotFoundException)exception;
        exception = databaseException.getCause();
      }
      final JOptionPane errorPane = new JOptionPane(exception.getMessage(),
        JOptionPane.ERROR_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {}, null);
      errorPane.setBorder(BorderFactory.createLineBorder(WebColors.LightCoral, 2));
      add(errorPane);
    }
    addNewPanelTitledLabelledFields( //
      "General", //
      new TextField("name", 50), //
      new TextField("url", 50), //
      ComboBox.newComboBox("recordStoreType", this.recordStoreTypes) //
    );
    if (Property.hasValue(allConnectionUrlMap)) {
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
      addNewPanelTitledLabelledFields( //
        "Named Connections", //
        connectionNamesField //
      );
    }
    addNewPanelTitledLabelledFields( //
      "File Connection", //
      new FileField("file", JFileChooser.FILES_AND_DIRECTORIES) //
    );
    addNewPanelTitledLabelledFields( //
      "JDBC Connection", //
      new TextField("host", 40), //
      new NumberTextField("port", DataTypes.INT, 1, 65535), //
      new TextField("database", 30), //
      new TextField("user", 30), //
      new PasswordField("password", 30), //
      new CheckBox("savePassword") //
    );
    if (connection == null) {
      this.config = new LinkedHashMapEx();
      setTitle("Add Record Store Connection");
    } else {
      final String name = connection.getName();
      setTitle("Edit Record Store Connection " + name);
      setFieldValue("name", name);
      final boolean savePassword = connection.isSavePassword();
      setFieldValue("savePassword", savePassword);
      this.config = connection.getProperties();
      final Map<String, String> connectionParameters = (Map<String, String>)this.config
        .get("connection");
      for (final String fieldName : CONNECTION_FIELD_NAMES) {
        Object fieldValue = connectionParameters.get(fieldName);
        if (Property.hasValue(fieldValue)) {
          if ("password".equals(fieldName) && fieldValue instanceof String) {
            fieldValue = PasswordUtil.decrypt((String)fieldValue);
          }
          setFieldValue(fieldName, fieldValue);
        }
      }
    }
  }

  public RecordStoreConnectionForm(final RecordStoreConnectionRegistry registry,
    final String name) {
    this(registry);
    setTitle(getTitle() + " " + name);
    setFieldValue("name", name);
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
    final String name = getFieldValue("name");
    this.config.put("name", name);
    final Map<String, Object> connectionParameters = new LinkedHashMap<>();
    this.config.put("connection", connectionParameters);
    this.config.put("savePassword", getFieldValue("savePassword"));

    for (final String fieldName : CONNECTION_FIELD_NAMES) {
      Object fieldValue = getFieldValue(fieldName);
      if ("password".equals(fieldName) && fieldValue instanceof String) {
        fieldValue = PasswordUtil.encrypt((String)fieldValue);
      }
      connectionParameters.put(fieldName, fieldValue);
    }
    if (this.connection == null) {
      this.registry.newConnection(this.config);
    } else {
      final String oldName = this.connection.getName();
      if (Strings.equals(oldName, name)) {
        this.connection.setProperties(this.config);
      } else {
        this.registry.removeConnection(this.connection);
        this.registry.newConnection(this.config);
      }
    }
    this.registry.save();
  }
}
