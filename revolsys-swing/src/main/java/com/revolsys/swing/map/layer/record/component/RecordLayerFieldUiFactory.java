package com.revolsys.swing.map.layer.record.component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.RecordLayerProxy;
import com.revolsys.swing.scripting.ScriptEngines;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.editor.BaseTableCellEditor;

public interface RecordLayerFieldUiFactory extends RecordLayerProxy {

  public static final String TABLE_CELL_EDITORS = "tableCellEditors";

  public static final String FORM_FIELD_FACTORIES = "formFieldFactories";

  public static final String FIELD_FACTORIES = "fieldFactories";

  private static <F, O> F newFromFunction(final AbstractRecordLayer layer,
    final String propertyName, final O owner, final String fieldName) {
    for (final ObjectWithProperties properties : Arrays.asList(layer,
      layer.getRecordDefinition())) {
      final F value = newFromFunction(properties, propertyName, owner, fieldName);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private static <F, O> F newFromFunction(final ObjectWithProperties properties,
    final String propertyName, final O owner, final String fieldName) {
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

    Function<O, F> factory;
    if (factoryDef instanceof Function) {
      factory = (Function<O, F>)factoryDef;
    } else {
      final ScriptEngine engine = ScriptEngines.JS;
      final String script = factoryDef.toString();
      try {
        final SimpleBindings bindings = new SimpleBindings();
        bindings.put("object", properties);
        bindings.put("fieldName", fieldName);
        factory = (Function<O, F>)engine.eval(script, bindings);
      } catch (final ScriptException e) {
        throw Exceptions.wrap(e);
      }
    }
    return factory.apply(owner);
  }

  private static <F> F newFromSupplier(final AbstractRecordLayer layer, final String propertyName,
    final String fieldName) {
    for (final ObjectWithProperties properties : Arrays.asList(layer,
      layer.getRecordDefinition())) {
      final F value = newFromSupplier(properties, propertyName, fieldName);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private static <F> F newFromSupplier(final ObjectWithProperties properties,
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
      final ScriptEngine engine = ScriptEngines.JS;
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
    T field = newFromSupplier(layer, FIELD_FACTORIES, fieldName);
    if (field == null) {
      final RecordDefinition recordDefinition = layer.getRecordDefinition();
      field = SwingUtil.newField(recordDefinition, fieldName, editable);
      field.setEditable(editable);
    }
    return field;
  }

  public default <T extends Field> T newFormField(final String fieldName, final boolean editable) {
    final AbstractRecordLayer layer = getRecordLayer();
    T field = newFromSupplier(layer, FORM_FIELD_FACTORIES, fieldName);
    if (field == null) {
      field = newCompactField(fieldName, editable);
    }
    return field;
  }

  default BaseTableCellEditor newTableCellEditor(final BaseJTable table, final String fieldName) {
    final AbstractRecordLayer layer = getRecordLayer();
    return newFromFunction(layer, TABLE_CELL_EDITORS, table, fieldName);
  }

  default void setFieldUiFactory(final String propertyName, final String fieldName,
    final Function<?, ?> factory) {
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
