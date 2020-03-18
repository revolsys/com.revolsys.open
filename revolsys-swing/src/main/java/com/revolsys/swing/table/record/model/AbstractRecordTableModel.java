package com.revolsys.swing.table.record.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.jeometry.common.data.identifier.Code;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

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

  @Override
  @PreDestroy
  public void dispose() {
    super.dispose();
    this.recordDefinition = null;
  }

  public String getColumnFieldName(final int attributeIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldName(attributeIndex);
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

  public String toDisplayValue(final int rowIndex, final int attributeIndex,
    final Object objectValue) {
    String text;
    final RecordDefinition recordDefinition = getRecordDefinition();
    final String fieldName = getColumnFieldName(attributeIndex);
    if (objectValue == null || fieldName == null) {
      if (recordDefinition.isIdField(fieldName)) {
        return "NEW";
      } else {
        text = "-";
      }
    } else {
      if (objectValue instanceof Geometry) {
        final Geometry geometry = (Geometry)objectValue;
        return geometry.getGeometryType();
      }
      CodeTable codeTable = null;
      if (!recordDefinition.isIdField(fieldName)) {
        codeTable = recordDefinition.getCodeTableByFieldName(fieldName);
      }
      if (codeTable == null || !isShowCodeValues()) {
        text = DataTypes.toString(objectValue);
      } else {
        if (!codeTable.isLoadAll() || codeTable.isLoaded()) {
          final List<Object> values = codeTable.getValues(Identifier.newIdentifier(objectValue));
          if (values == null || values.isEmpty()) {
            text = DataTypes.toString(objectValue);
          } else if (values.size() == 1) {
            final Object codeValue = values.get(0);
            if (codeValue instanceof Code) {
              text = ((Code)codeValue).getDescription();
            } else {
              text = DataTypes.toString(codeValue);
            }
          } else {
            text = Strings.toString(values);
          }
        } else {
          if (!codeTable.isLoading()) {
            final CodeTable tableToLoad = codeTable;
            Invoke.background("Load " + codeTable, () -> loadCodeTable(tableToLoad));
          }
          text = "...";
        }
      }
      if (text.length() == 0) {
        text = "-";
      }
    }
    return text;
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
