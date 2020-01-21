package com.revolsys.swing.map.layer.record.component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.swing.table.TableCellEditor;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.RecordLayerProxy;

public interface RecordLayerFieldUiFactory extends RecordLayerProxy {
  public static final String TABLE_CELL_EDITORS = "tableCellEditors";

  public static final String FORM_FIELD_FACTORIES = "formFieldFactories";

  public static final String FIELD_FACTORIES = "fieldFactories";

  private static <F> F newFromFactory(final AbstractRecordLayer layer, final String propertyName,
    final String fieldName) {
    for (final ObjectWithProperties properties : Arrays.asList(layer,
      layer.getRecordDefinition())) {
      final F value = newFromFactory(properties, propertyName, fieldName);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private static <F> F newFromFactory(final ObjectWithProperties properties,
    final String propertyName, final String fieldName) {
    if (properties == null) {
      return null;
    }
    final Map<String, Object> factories = properties.getProperty(propertyName);
    if (factories == null) {
      return null;
    }
    final Object factoryDef = factories.get(fieldName);
    if (factoryDef == null) {
      return null;
    }

    Supplier<F> factory;
    if (factoryDef instanceof Supplier) {
      factory = (Supplier<F>)factoryDef;
    } else {
      final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
      final String script = factoryDef.toString();
      try {
        final SimpleBindings bindings = new SimpleBindings();
        bindings.put("object", properties);
        bindings.put("fieldName", fieldName);
        factory = (Supplier<F>)engine.eval(script, bindings);
      } catch (final ScriptException e) {
        throw Exceptions.wrap(e);
      }
    }
    return factory.get();
  }

  public default <T extends Field> T newCompactField(final String fieldName,
    final boolean editable) {
    final AbstractRecordLayer layer = getRecordLayer();
    T field = newFromFactory(layer, FIELD_FACTORIES, fieldName);
    if (field == null) {
      final RecordDefinition recordDefinition = layer.getRecordDefinition();
      field = SwingUtil.newField(recordDefinition, fieldName, editable);
      field.setEditable(editable);
    }
    return field;
  }

  public default <T extends Field> T newFormField(final String fieldName, final boolean editable) {
    final AbstractRecordLayer layer = getRecordLayer();
    T field = newFromFactory(layer, FORM_FIELD_FACTORIES, fieldName);
    if (field == null) {
      field = newCompactField(fieldName, editable);
    }
    return field;
  }

  default TableCellEditor newTableCellEditor(final String fieldName) {
    final AbstractRecordLayer layer = getRecordLayer();
    return newFromFactory(layer, TABLE_CELL_EDITORS, fieldName);
  }

  default void setFieldUiFactory(final String propertyName, final String fieldName,
    final Supplier<?> factory) {
    final AbstractRecordLayer layer = getRecordLayer();
    synchronized (layer) {
      Map<String, Object> factories = layer.getProperty(propertyName);
      if (factories == null) {
        factories = new LinkedHashMap<>();
        layer.setProperty(propertyName, factories);
      }
      factories.put(fieldName, factory);
    }
  }
}
