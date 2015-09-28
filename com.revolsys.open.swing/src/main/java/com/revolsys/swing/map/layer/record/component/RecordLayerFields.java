package com.revolsys.swing.map.layer.record.component;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.util.WrappedException;

public class RecordLayerFields {
  public static <T extends Field> T createCompactField(final AbstractRecordLayer layer,
    final String fieldName, final boolean editable) {
    T field = createField((ObjectWithProperties)layer, "fieldFactories", fieldName, editable);
    if (field == null) {
      final RecordDefinition recordDefinition = layer.getRecordDefinition();
      field = createField(recordDefinition, "fieldFactories", fieldName, editable);
      if (field == null) {
        field = SwingUtil.newField(recordDefinition, fieldName, editable);
      }
    }
    field.setEditable(editable);
    return field;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Field> T createField(final ObjectWithProperties properties,
    final String propertyName, final String fieldName, final boolean editable) {
    final Supplier<Field> factory = getFactory(properties, propertyName, fieldName);
    if (factory == null) {
      return null;
    } else {
      return (T)factory.get();
    }
  }

  public static <T extends Field> T createFormField(final AbstractRecordLayer layer,
    final String fieldName, final boolean editable) {
    T field = createField((ObjectWithProperties)layer, "formFieldFactories", fieldName, editable);
    if (field == null) {
      final RecordDefinition recordDefinition = layer.getRecordDefinition();
      field = createField(recordDefinition, "formFieldFactories", fieldName, editable);
      if (field == null) {
        field = createCompactField(layer, fieldName, editable);
      }
    }
    field.setEditable(editable);
    return field;
  }

  @SuppressWarnings("unchecked")
  private static Supplier<Field> getFactory(final ObjectWithProperties properties,
    final String propertyName, final String fieldName) {
    final Map<String, Object> factories = getFieldFactoryMap(properties, propertyName);
    final Object factoryDef = factories.get(fieldName);
    if (factoryDef == null) {
      return null;
    } else if (factoryDef instanceof Supplier) {
      return (Supplier<Field>)factoryDef;
    } else {
      final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
      final String script = factoryDef.toString();
      try {
        final SimpleBindings bindings = new SimpleBindings();
        bindings.put("object", properties);
        bindings.put("fieldName", fieldName);
        return (Supplier<Field>)engine.eval(script, bindings);
      } catch (final ScriptException e) {
        throw new WrappedException(e);
      }
    }
  }

  private static Map<String, Object> getFieldFactoryMap(final ObjectWithProperties properties,
    final String propertyName) {
    Map<String, Object> factories = properties.getProperty(propertyName);
    if (factories == null) {
      factories = new TreeMap<>();
      properties.setProperty("fieldFactories", factories);
    }
    return factories;
  }
}
