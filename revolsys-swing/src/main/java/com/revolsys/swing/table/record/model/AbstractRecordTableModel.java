package com.revolsys.swing.table.record.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.PreDestroy;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.util.Property;

public abstract class AbstractRecordTableModel extends AbstractTableModel {

  private static final long serialVersionUID = 1L;

  private boolean editable;

  private Set<String> readOnlyFieldNames = new HashSet<>();

  private RecordDefinition recordDefinition;

  public AbstractRecordTableModel() {
    this(null);
  }

  public AbstractRecordTableModel(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
  }

  public void addReadOnlyFieldNames(final Collection<String> fieldNames) {
    if (fieldNames != null) {
      this.readOnlyFieldNames.addAll(fieldNames);
    }
  }

  public void addReadOnlyFieldNames(final String... readOnlyFieldNames) {
    if (readOnlyFieldNames != null) {
      final List<String> fieldNames = Arrays.asList(readOnlyFieldNames);
      addReadOnlyFieldNames(fieldNames);
    }
  }

  protected void appendJsonList(final StringBuilder string, final List<?> list) {
    string.append('[');
    boolean first = true;
    for (final Object value : list) {
      if (Property.hasValue(value)) {
        if (first) {
          first = false;
        } else {
          string.append(',');
        }
        appendJsonValue(string, value);
      }
    }
    string.append(']');
  }

  protected void appendJsonObject(final StringBuilder string, final FieldDefinition field,
    final Map<String, ? extends Object> jsonObject) {
    boolean first = true;
    for (final String name : jsonObject.keySet()) {
      final Object value = jsonObject.get(name);
      if (Property.hasValue(value)) {
        if (first) {
          first = false;
        } else {
          string.append(',');
        }
        string.append(name);
        string.append('=');
        appendJsonValue(string, field, name, value);
      }
    }
  }

  protected void appendJsonObject(final StringBuilder string,
    final Map<String, ? extends Object> jsonObject) {
    string.append('{');
    boolean first = true;
    for (final String name : jsonObject.keySet()) {
      final Object value = jsonObject.get(name);
      if (Property.hasValue(value)) {
        if (first) {
          first = false;
        } else {
          string.append(',');
        }
        string.append(name);
        string.append('=');
        appendJsonValue(string, value);
      }
    }
    string.append('}');
  }

  protected void appendJsonValue(final StringBuilder string, final FieldDefinition field,
    final String childName, final Object value) {
    appendJsonValue(string, value);
  }

  protected void appendJsonValue(final StringBuilder string, final Object value) {
    if (value instanceof List<?>) {
      final List<?> list = (List<?>)value;
      appendJsonList(string, list);
    } else if (value instanceof Map<?, ?>) {
      @SuppressWarnings("unchecked")
      final Map<String, ?> map = (Map<String, ?>)value;
      appendJsonObject(string, map);
    } else {
      final String stringValue = DataTypes.toString(value);
      string.append(stringValue);
    }
  }

  @Override
  @PreDestroy
  public void dispose() {
    super.dispose();
    this.recordDefinition = null;
  }

  public FieldDefinition getColumnField(final int fieldIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getField(fieldIndex);
  }

  public String getColumnFieldName(final int fieldIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldName(fieldIndex);
  }

  public abstract String getColumnFieldName(int rowIndex, int columnIndex);

  public Set<String> getReadOnlyFieldNames() {
    return this.readOnlyFieldNames;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public boolean isEditable() {
    return this.editable;
  }

  protected boolean isIdField(final FieldDefinition field) {
    return this.recordDefinition.isIdField(field);
  }

  public boolean isReadOnly(final String fieldName) {
    return this.readOnlyFieldNames.contains(fieldName);
  }

  public abstract boolean isSelected(boolean selected, int rowIndex, int columnIndex);

  public boolean isShowCodeValues() {
    return true;
  }

  public void loadCodeTable(final CodeTable codeTable) {
    if (codeTable.isLoadAll() && !codeTable.isLoaded()) {
      codeTable.refreshIfNeeded();
      fireTableDataChanged();
    }
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
  }

  public void setReadOnlyFieldNames(final Collection<String> readOnlyFieldNames) {
    if (readOnlyFieldNames == null) {
      this.readOnlyFieldNames = new HashSet<>();
    } else {
      this.readOnlyFieldNames = new HashSet<>(readOnlyFieldNames);
    }
  }

  protected void setRecordDefinition(final RecordDefinition recordDefinition) {
    if (recordDefinition != this.recordDefinition) {
      this.recordDefinition = recordDefinition;
      fireTableStructureChanged();
    }
  }

  protected String toDisplayValue(final FieldDefinition field, final Object objectValue) {
    if (objectValue == null || field == null) {
      if (isIdField(field)) {
        return "NEW";
      } else {
        return "-";
      }
    } else {
      String text;
      if (objectValue instanceof Geometry) {
        final Geometry geometry = (Geometry)objectValue;
        return geometry.getGeometryType();
      } else if (field.getDataType().isAssignableTo(JsonObject.class)) {
        try {
          final JsonObject jsonObject = (JsonObject)field.toFieldValue(objectValue);
          if (jsonObject.isEmpty()) {
            return "-";
          } else {
            final StringBuilder string = new StringBuilder();
            appendJsonObject(string, field, jsonObject);
            return string.toString();
          }
        } catch (final Exception e) {
          return DataTypes.toString(objectValue);
        }
      }
      if (isShowCodeValues() && !isIdField(field)) {
        if (field.isCodeTableReady()) {
          text = field.toCodeString(objectValue);
        } else {
          final CodeTable codeTable = field.getCodeTable();
          if (!codeTable.isLoading()) {
            final CodeTable tableToLoad = codeTable;
            Invoke.background("Load " + codeTable, () -> loadCodeTable(tableToLoad));
          }
          return "...";
        }
      } else {
        text = field.toString(objectValue);
      }
      if (text.length() == 0) {
        text = "-";
      }
      return text;
    }
  }

  public String toDisplayValue(final int rowIndex, final int fieldIndex, final Object objectValue) {
    final FieldDefinition field = getColumnField(fieldIndex);
    return toDisplayValue(field, objectValue);
  }

  public Object toObjectValue(final String fieldName, final Object displayValue) {
    if (!Property.hasValue(displayValue)) {
      return null;
    }
    final RecordDefinition recordDefinition = getRecordDefinition();
    final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(fieldName);
    if (codeTable == null) {
      final FieldDefinition field = recordDefinition.getField(fieldName);
      final Object recordValue = field.toFieldValue(displayValue);
      return recordValue;
    } else {
      if (displayValue instanceof Identifier) {
        final Identifier identifier = (Identifier)displayValue;
        return identifier;
      } else {
        final Object objectValue = codeTable.getIdentifier(displayValue);
        return objectValue;
      }
    }
  }
}
