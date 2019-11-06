package com.revolsys.swing.map.layer.record.component.recordmerge;

import java.util.List;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.FieldValueInvalidException;
import com.revolsys.record.property.DirectionalFields;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;

class MergeFieldOriginalFieldState {

  String fieldName;

  private MergeFieldMatchType matchType = MergeFieldMatchType.EQUAL;

  private final MergeOriginalRecord mergeOriginalRecord;

  private final boolean ignoreField;

  private String message;

  private final boolean dontMerge;

  public MergeFieldOriginalFieldState(final MergeOriginalRecord mergeOriginalRecord,
    final String fieldName, final boolean ignoreField) {
    this.mergeOriginalRecord = mergeOriginalRecord;
    this.fieldName = fieldName;
    this.ignoreField = ignoreField;
    final List<String> dontMergeDifferentValueFieldNames = mergeOriginalRecord.getLayer()
      .getDontMergeDifferentValueFieldNames();
    this.dontMerge = dontMergeDifferentValueFieldNames.contains(fieldName);

  }

  public void clearOverrideError() {
    final int fieldIndex = getFieldIndex();
    this.mergeOriginalRecord.mergeableRecord.fireRowUpdated(fieldIndex);
  }

  public int compareMatchType(final MergeFieldMatchType matchType) {
    return getMatchType().compareTo(matchType);
  }

  int getFieldIndex() {
    return this.mergeOriginalRecord.getFieldIndex(this.fieldName);
  }

  public MergeFieldMatchType getMatchType() {
    return this.matchType;
  }

  public String getMessage() {
    return this.message;
  }

  public boolean isMatchType(final MergeFieldMatchType matchType) {
    return getMatchType() == matchType;
  }

  @Override
  public String toString() {
    return this.fieldName + "=" + this.mergeOriginalRecord.getCodeValue(this.fieldName);
  }

  public void validateField(final Object mergedValue, final Object originalValue) {
    this.matchType = MergeFieldMatchType.EQUAL;
    this.message = null;
    if (!DataType.equal(mergedValue, originalValue)) {
      try {
        final MergeableRecord mergeableRecord = this.mergeOriginalRecord.mergeableRecord;
        final AbstractRecordLayer layer = this.mergeOriginalRecord.getLayer();
        final DirectionalFields directionalFields = DirectionalFields.getProperty(layer);
        final int recordIndex = this.mergeOriginalRecord.recordIndex;
        if (this.dontMerge) {
          this.matchType = MergeFieldMatchType.CANT_MERGE;
        } else if (this.ignoreField) {
          this.matchType = MergeFieldMatchType.ALLOWED_NOT_EQUAL;
        } else if (originalValue == null) {
          this.matchType = MergeFieldMatchType.WAS_NULL;
        } else if (directionalFields.isFromField(this.fieldName) && recordIndex != 0) {
          directionalFields.validateFieldAtMergeEnd(this.mergeOriginalRecord, this.fieldName,
            originalValue);
          this.matchType = MergeFieldMatchType.ALLOWED_NOT_EQUAL;
        } else if (directionalFields.isToField(this.fieldName)
          && recordIndex != mergeableRecord.getMaxRecordIndex()) {
          directionalFields.validateFieldAtMergeEnd(this.mergeOriginalRecord, this.fieldName,
            originalValue);
          this.matchType = MergeFieldMatchType.ALLOWED_NOT_EQUAL;
        } else {
          this.message = DataTypes.toString(originalValue) + " != "
            + DataTypes.toString(mergedValue);
          this.matchType = MergeFieldMatchType.VALUES_DIFFERENT;
        }
      } catch (final FieldValueInvalidException e) {
        this.message = e.getMessageText();
        this.matchType = MergeFieldMatchType.VALUES_DIFFERENT;
      }
    }
  }

}
