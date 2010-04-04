package com.revolsys.jump.ui.swing;

import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.WorkbenchContext;

public final class FieldComponentFactoryRegistry {
  private FieldComponentFactoryRegistry() {
  }

  public static final String KEY = FieldComponentFactoryRegistry.class.getName();

  public static void setFactory(final WorkbenchContext context,
    final String type, final FieldComponentFactory factory) {
    Blackboard blackboard = context.getBlackboard();
    Map<String, FieldComponentFactory> fields = getFields(blackboard);
    fields.put(type, factory);
  }

  public static FieldComponentFactory getFactory(
    final WorkbenchContext context, final String type) {
    Blackboard blackboard = context.getBlackboard();
    Map<String, FieldComponentFactory> fields = getFields(blackboard);
    return fields.get(type);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, FieldComponentFactory> getFields(
    final Blackboard blackboard) {
    Map<String, FieldComponentFactory> fields = (Map<String, FieldComponentFactory>)blackboard.get(KEY);
    if (fields == null) {
      fields = new HashMap<String, FieldComponentFactory>();
      blackboard.put(KEY, fields);
    }
    return fields;
  }
}
