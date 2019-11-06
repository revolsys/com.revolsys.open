package com.revolsys.swing.map.layer.record.component.recordmerge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.revolsys.record.ArrayRecord;
import com.revolsys.record.property.DirectionalFields;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;

class MergeOriginalRecord extends ArrayRecord {
  final LayerRecord originalRecord;

  List<MergeFieldOriginalFieldState> fieldStates;

  boolean forwards;

  MergeableRecord mergeableRecord;

  int recordIndex;

  MergeOriginalRecord(final MergeableRecord mergeableRecord, final LayerRecord originalRecord) {
    super(originalRecord);
    setGeometryValue(originalRecord);
    this.mergeableRecord = mergeableRecord;
    this.originalRecord = originalRecord;
    this.forwards = true;
  }

  MergeFieldOriginalFieldState getFieldState(final int fieldIndex) {
    final List<MergeFieldOriginalFieldState> fieldStates = getFieldStates();
    return fieldStates.get(fieldIndex);
  }

  List<MergeFieldOriginalFieldState> getFieldStates() {
    if (this.fieldStates == null) {
      final List<MergeFieldOriginalFieldState> fieldStates = new ArrayList<>();
      final Collection<String> ignoreDifferentFieldNames = getLayer()
        .getProperty("mergeRecordsIgnoreDifferentFieldNames", Collections.emptySet());
      for (final String fieldName : this.mergeableRecord.getFieldNames()) {
        final boolean ignoreField = isIdField(fieldName) || isGeometryField(fieldName)
          || ignoreDifferentFieldNames.contains(fieldName);
        fieldStates.add(new MergeFieldOriginalFieldState(this, fieldName, ignoreField));
      }
      this.fieldStates = fieldStates;
    }
    return this.fieldStates;
  }

  AbstractRecordLayer getLayer() {
    return this.mergeableRecord.getLayer();
  }

  boolean isFieldState(final int fieldIndex, final MergeFieldMatchType matchType) {
    final MergeFieldOriginalFieldState fieldState = getFieldStates().get(fieldIndex);
    return fieldState.isMatchType(matchType);
  }

  void setForwards(final boolean forwards) {
    if (this.forwards != forwards) {
      final DirectionalFields directionalFields = DirectionalFields
        .getProperty(this.originalRecord);
      directionalFields.reverseFieldValuesAndGeometry(this);
      final List<MergeFieldOriginalFieldState> newFieldStates = new ArrayList<>(getFieldStates());

      int fieldIndex = 0;
      for (final String fieldName : getFieldNames()) {
        final String reverseFieldName = directionalFields.getReverseFieldName(fieldName);
        if (reverseFieldName != null) {
          final int reverseFieldIndex = getFieldIndex(reverseFieldName);
          final MergeFieldOriginalFieldState fieldState = this.getFieldStates()
            .get(reverseFieldIndex);
          fieldState.fieldName = fieldName;
          newFieldStates.set(fieldIndex, fieldState);
        }
        fieldIndex++;
      }
      this.fieldStates = newFieldStates;
      this.forwards = forwards;
    }
  }

  MergeFieldOriginalFieldState validateField(final int fieldIndex, final String fieldName,
    final Object mergedValue) {
    final MergeFieldOriginalFieldState fieldState = getFieldState(fieldIndex);
    final Object originalValue = getCodeValue(fieldName);
    fieldState.validateField(mergedValue, originalValue);
    return fieldState;
  }
}
