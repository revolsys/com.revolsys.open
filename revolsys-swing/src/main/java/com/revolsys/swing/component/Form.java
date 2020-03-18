package com.revolsys.swing.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.Maps;
import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Pair;
import com.revolsys.util.Property;
import com.revolsys.value.ThreadBooleanValue;

public class Form extends BasePanel {
  private static final long serialVersionUID = 1L;

  private final Map<String, Field> fieldByName = new HashMap<>();

  private final ThreadBooleanValue settingFieldValue = new ThreadBooleanValue(false);

  private final PropertyChangeListener propertyChangeSetValue = Property
    .newListener(this::setFieldValue);

  private final Map<String, List<BiConsumer<String, Object>>> fieldValueListenersByFieldName = new HashMap<>();

  private final Map<String, Object> fieldValueByName = new HashMap<>();

  private String title;

  private boolean saved = false;

  public Form() {
  }

  public Form(final Component... components) {
    super(components);
  }

  public Form(final LayoutManager layout) {
    super(layout);
  }

  public Form(final LayoutManager layout, final Component... components) {
    super(layout, components);
  }

  public void addField(final Field field) {
    setField(field);
    add((JComponent)field);
  }

  protected void addFields(final Component component) {
    if (component instanceof Field) {
      final Field field = (Field)component;
      setField(field);
    } else if (component instanceof Container) {
      final Container container = (Container)component;
      for (final Component childComponent : container.getComponents()) {
        addFields(childComponent);
      }
    }
  }

  public boolean addFieldValueListener(final BiConsumer<String, Object> listener) {
    return addFieldValueListener(null, listener);
  }

  public boolean addFieldValueListener(final String fieldName,
    final BiConsumer<String, Object> listener) {
    synchronized (this.fieldValueListenersByFieldName) {
      final List<BiConsumer<String, Object>> listeners = Maps
        .getList(this.fieldValueListenersByFieldName, fieldName);
      if (listeners.contains(listener)) {
        return false;
      } else {
        return listeners.add(listener);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <V> boolean addFieldValueListener(final String fieldName, final Consumer<V> listener) {
    if (Property.hasValue(fieldName)) {
      return addFieldValueListener(fieldName, (name, value) -> {
        listener.accept((V)value);
      });
    } else {
      throw new IllegalArgumentException("A field name must be specified");
    }
  }

  @Override
  protected void addImpl(final Component comp, final Object constraints, final int index) {
    super.addImpl(comp, constraints, index);
    addFields(comp);
  }

  public void addLabelAndField(final Container container, final Field field) {
    if (field != null) {
      final String fieldName = field.getFieldName();
      SwingUtil.addLabel(container, fieldName);
      setField(field);
      container.add(field.getComponent());
    }
  }

  public void addLabelAndField(final Field field) {
    addLabelAndField(this, field);
  }

  public <F> F addLabelAndNewField(final String fieldName, final DataType dataType) {
    SwingUtil.addLabel(this, fieldName);
    return addNewField(fieldName, dataType);
  }

  @SuppressWarnings("unchecked")
  public <F> F addNewField(final String fieldName, final DataType dataType) {
    final Field field = newField(fieldName, dataType);
    addField(field);
    return (F)field;
  }

  public BasePanel addNewPanelTitledLabelledFields(final String title, final Field... fields) {
    final BasePanel panel = newPanelTitledLabelledFields(title, fields);
    add(panel);
    return panel;
  }

  @Override
  public void addNotify() {
    super.addNotify();
    for (final Entry<String, Field> entry : this.fieldByName.entrySet()) {
      final String fieldName = entry.getKey();
      final Field field = entry.getValue();
      Property.addListener(field, fieldName, this.propertyChangeSetValue);
    }
  }

  public void cancel() {
    this.saved = false;
  }

  public void cancel(final JDialog dialog) {
    cancel();
    SwingUtil.setVisible(dialog, false);
  }

  protected void fireFieldValueChanged(final String keyFieldName, final String fieldName,
    final Object fieldValue) {
    final List<BiConsumer<String, Object>> listeners = this.fieldValueListenersByFieldName
      .get(keyFieldName);
    if (listeners != null) {
      for (final BiConsumer<String, Object> listener : listeners) {
        try {
          listener.accept(fieldName, fieldValue);
        } catch (final Throwable e) {
          Logs.error(this, "Error calling listener " + fieldName + "=" + fieldValue, e);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <F> F getField(final String filedName) {
    return (F)this.fieldByName.get(filedName);
  }

  public <V> V getFieldValue(final String fieldName) {
    final Field field = getField(fieldName);
    if (field == null) {
      return null;
    } else {
      return field.getFieldValue();
    }
  }

  public Map<String, Object> getFieldValues() {
    final Map<String, Object> values = new TreeMap<>();
    for (final Entry<String, Field> entry : this.fieldByName.entrySet()) {
      final String fieldName = entry.getKey();
      final Field field = entry.getValue();
      final Object fieldValue = field.getFieldValue();
      values.put(fieldName, fieldValue);
    }
    return values;
  }

  public String getTitle() {
    return this.title;
  }

  public boolean isSettingFieldValue() {
    return this.settingFieldValue.isTrue();
  }

  @SuppressWarnings("unchecked")
  public <F> F newField(final String fieldName, final DataType dataType) {
    return (F)SwingUtil.newField(dataType, fieldName, null);
  }

  public BasePanel newPanelTitledLabelledFields(final String title, final Field... fields) {
    final BasePanel panel = BasePanel.newPanelTitled(title);
    for (final Field field : fields) {
      final String fieldName = field.getFieldName();
      final Component component = field.getComponent();
      panel.addWithLabel(fieldName, component);
    }
    GroupLayouts.makeColumns(panel, 2, true, true);
    return panel;
  }

  protected void postSetFieldValues(final Map<String, Object> newValues) {
  }

  protected void postSetFieldValuesErrors(
    final Map<String, Pair<Object, Throwable>> fieldValueErrors) {
    for (final Entry<String, Pair<Object, Throwable>> entry : fieldValueErrors.entrySet()) {
      final String fieldName = entry.getKey();
      final Pair<Object, Throwable> pair = entry.getValue();
      final Object fieldValue = pair.getValue1();
      final Throwable exception = pair.getValue2();
      Logs.error(this, "Error setting field " + fieldName + "=" + fieldValue, exception);
    }
  }

  public boolean removeFieldValueListener(final BiConsumer<String, Object> listener) {
    return removeFieldValueListener(null, listener);
  }

  public boolean removeFieldValueListener(final String fieldName,
    final BiConsumer<String, Object> listener) {
    synchronized (this.fieldValueListenersByFieldName) {
      return Maps.removeFromCollection(this.fieldValueListenersByFieldName, fieldName, listener);
    }
  }

  @Override
  public void removeNotify() {
    super.addNotify();
    for (final Entry<String, Field> entry : this.fieldByName.entrySet()) {
      final String fieldName = entry.getKey();
      final Field field = entry.getValue();
      Property.removeListener(field, fieldName, this.propertyChangeSetValue);
    }
  }

  public void save() {
    this.saved = true;
  }

  public void save(final JDialog dialog) {
    save();
    dialog.setVisible(false);
  }

  public void setField(final Field field) {
    if (field != null) {
      Invoke.later(() -> {
        final String fieldName = field.getFieldName();
        final Field oldField = this.fieldByName.put(fieldName, field);
        if (oldField != null) {
          Property.removeListener(oldField, fieldName, this.propertyChangeSetValue);
        }
        if (isDisplayable()) {
          Property.addListener(field, fieldName, this.propertyChangeSetValue);
        }
      });
    }
  }

  public void setFieldValue(final String fieldName, final Object value) {
    final Map<String, Object> values = Collections.singletonMap(fieldName, value);
    setFieldValues(values);
  }

  public void setFieldValues(final Map<String, ? extends Object> values) {
    if (Property.hasValue(values)) {
      Invoke.later(() -> {
        final Map<String, Object> newValues = new HashMap<>();
        final Map<String, Pair<Object, Throwable>> fieldValueErrors = new HashMap<>();
        try (
          BaseCloseable settingFieldValue = this.settingFieldValue.closeable(true)) {
          for (final Entry<String, ? extends Object> entry : values.entrySet()) {
            final String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();

            final Field field = getField(fieldName);
            if (field != null) {
              try {
                final boolean valueSet = field.setFieldValue(fieldValue);
                fieldValue = field.getFieldValue();
                if (!DataType.equal(this.fieldValueByName.put(fieldName, fieldValue), fieldValue)
                  || valueSet) {
                  newValues.put(fieldName, fieldValue);
                }
              } catch (final Throwable e) {
                fieldValueErrors.put(fieldName, new Pair<>(fieldValue, e));
              }
            }
          }
        }
        for (final Entry<String, Object> entry : newValues.entrySet()) {
          final String fieldName = entry.getKey();
          final Object fieldValue = entry.getValue();
          fireFieldValueChanged(null, fieldName, fieldValue);
          fireFieldValueChanged(fieldName, fieldName, fieldValue);
        }
        if (!isSettingFieldValue()) {
          if (!fieldValueErrors.isEmpty()) {
            postSetFieldValuesErrors(fieldValueErrors);
          }
          if (!newValues.isEmpty()) {
            postSetFieldValues(newValues);
          }
        }
      });
    }
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public boolean showDialog() {
    final JDialog dialog = Dialogs.newDocumentModal(this.title);
    dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    dialog.setLayout(new BorderLayout());

    dialog.add(this, BorderLayout.CENTER);

    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(RunnableAction.newButton("Cancel", () -> cancel(dialog)));
    buttons.add(RunnableAction.newButton("OK", () -> save(dialog)));
    dialog.add(buttons, BorderLayout.SOUTH);

    dialog.pack();
    SwingUtil.autoAdjustPosition(dialog);
    dialog.setVisible(true);
    SwingUtil.dispose(dialog);
    return this.saved;
  }
}
