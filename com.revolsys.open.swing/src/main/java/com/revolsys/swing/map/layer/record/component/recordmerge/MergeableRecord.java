package com.revolsys.swing.map.layer.record.component.recordmerge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.action.enablecheck.BooleanEnableCheck;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.table.lambda.column.ColumnBasedTableModel;
import com.revolsys.util.Equals;

class MergeableRecord extends ArrayRecord {
  final List<MergeOriginalRecord> originalRecords = new ArrayList<>();

  ColumnBasedTableModel tableModel;

  final List<MergeFieldMatchType> matchTypes = new ArrayList<>();

  final Map<Integer, Object> overrideFieldValues = new HashMap<>();

  final BooleanEnableCheck canMergeEnableCheck = new BooleanEnableCheck(true);

  private final AbstractRecordLayer layer;

  MergeableRecord(final AbstractRecordLayer layer, final LayerRecord originalRecord) {
    super(originalRecord);
    this.layer = layer;
    setGeometryValue(originalRecord);
    this.originalRecords.add(new MergeOriginalRecord(this, originalRecord));
  }

  MergeableRecord(final AbstractRecordLayer layer, final Record mergedRecord,
    final MergeableRecord record1, final boolean forwards1, final MergeableRecord record2,
    final boolean forwards2) {
    super(mergedRecord);
    this.layer = layer;
    setGeometryValue(mergedRecord);
    setOriginalRecords(record1, forwards1);
    setOriginalRecords(record2, forwards2);
    int recordIndex = 0;
    for (final MergeOriginalRecord originalRecord : this.originalRecords) {
      originalRecord.mergeableRecord = this;
      originalRecord.recordIndex = recordIndex;
      recordIndex++;
    }
  }

  void cleaOverrideValue(final int fieldIndex) {
    this.overrideFieldValues.remove(fieldIndex);
  }

  void fireRowUpdated(final int fieldIndex) {
    if (this.tableModel != null) {
      this.tableModel.fireTableRowsUpdated(fieldIndex, fieldIndex);
    }
  }

  AbstractRecordLayer getLayer() {
    return this.layer;
  }

  public MergeFieldMatchType getMatchType(final int fieldIndex) {
    return getMatchTypes().get(fieldIndex);
  }

  List<MergeFieldMatchType> getMatchTypes() {
    if (this.matchTypes.isEmpty()) {
      for (int i = 0; i < getFieldCount(); i++) {
        this.matchTypes.add(MergeFieldMatchType.EQUAL);
      }
    }
    return this.matchTypes;
  }

  int getMaxRecordIndex() {
    return this.originalRecords.size() - 1;
  }

  MergeOriginalRecord getOriginalRecord(final int index) {
    return this.originalRecords.get(index);
  }

  @Override
  public boolean isValid(final CharSequence fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final int index = recordDefinition.getFieldIndex(fieldName);
    return isValid(index);
  }

  @Override
  public boolean isValid(final int index) {
    synchronized (this) {
      if (getState() == RecordState.INITIALIZING) {
        return true;
      } else {
        final FieldDefinition fieldDefinition = getFieldDefinition(index);
        if (fieldDefinition != null) {
          final Object value = getValue(index);
          return fieldDefinition.isValid(value);
        }
        return true;
      }
    }
  }

  private MergeFieldMatchType mergeValidateFieldDo(final int fieldIndex) {
    final List<MergeFieldMatchType> matchTypes = getMatchTypes();
    final String fieldName = getFieldName(fieldIndex);
    final Object mergedValue = getCodeValue(fieldName);
    final boolean overwridden = this.overrideFieldValues.containsKey(fieldIndex);
    MergeFieldMatchType matchType = MergeFieldMatchType.NOT_COMPARED;
    for (final MergeOriginalRecord originalRecord : this.originalRecords) {
      final MergeFieldOriginalFieldState fieldState = originalRecord.validateField(fieldIndex,
        mergedValue, overwridden);
      if (fieldState.compareMatchType(matchType) < 0) {
        matchType = fieldState.getMatchType();
      }
    }
    matchTypes.set(fieldIndex, matchType);
    fireRowUpdated(fieldIndex);
    return matchType;
  }

  void mergeValidateFields(final boolean firstRun) {
    final int fieldCount = getFieldCount();
    for (int fieldIndex = 0; fieldIndex < fieldCount; fieldIndex++) {
      final MergeFieldMatchType matchType = mergeValidateFieldDo(fieldIndex);
      if (firstRun && matchType.ordinal() <= MergeFieldMatchType.END_FIELD_NOT_VALID.ordinal()) {
        setValue(fieldIndex, null);
        mergeValidateFieldDo(fieldIndex);
      }
    }
    if (this.tableModel != null) {
      this.tableModel.fireTableDataChanged();
    }
    updateCanMerge();
  }

  private void setOriginalRecords(final MergeableRecord record, final boolean forwards) {
    for (final MergeOriginalRecord originalRecord : record.originalRecords) {
      originalRecord.setForwards(forwards);
      this.originalRecords.add(originalRecord);
    }
  }

  void setOverrideValue(final int fieldIndex, final Object value) {
    this.overrideFieldValues.put(fieldIndex, value);
  }

  @Override
  protected boolean setValue(final FieldDefinition fieldDefinition, final Object value) {
    final int fieldIndex = fieldDefinition.getIndex();
    if (this.overrideFieldValues != null && this.overrideFieldValues.containsKey(fieldIndex)) {
      final Object overwriteValue = this.overrideFieldValues.get(fieldIndex);
      if (!Equals.equals(overwriteValue, value)) {
        this.overrideFieldValues.remove(fieldIndex);
      }
    }
    final boolean set = super.setValue(fieldDefinition, value);
    if (set && this.tableModel != null) {
      mergeValidateFields(false);
      fireRowUpdated(fieldIndex);
    }
    return set;
  }

  private void updateCanMerge() {
    for (final MergeFieldMatchType matchType : getMatchTypes()) {
      if (matchType.compareTo(MergeFieldMatchType.END_FIELD_NOT_VALID) <= 0) {
        this.canMergeEnableCheck.setEnabled(false);
        return;
      }
    }
    this.canMergeEnableCheck.setEnabled(true);
  }
}
