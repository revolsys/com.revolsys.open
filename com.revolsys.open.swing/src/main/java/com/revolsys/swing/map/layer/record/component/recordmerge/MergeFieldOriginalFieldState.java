package com.revolsys.swing.map.layer.record.component.recordmerge;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.FieldValueInvalidException;
import com.revolsys.record.property.DirectionalFields;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;

class MergeFieldOriginalFieldState {

  String fieldName;

  private MergeFieldMatchType matchType = MergeFieldMatchType.EQUAL;

  private final MergeOriginalRecord mergeOriginalRecord;

  private final boolean notCompared;

  private String message;

  private final boolean dontMerge;

  public MergeFieldOriginalFieldState(final MergeOriginalRecord mergeOriginalRecord,
    final String fieldName, final boolean ignoreField, final boolean dontMerge) {
    this.mergeOriginalRecord = mergeOriginalRecord;
    this.fieldName = fieldName;
    this.notCompared = ignoreField;
    this.dontMerge = dontMerge;

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

  public MergeFieldOriginalFieldState validateField(final Object mergedValue,
    final boolean overwridden) {
    final Object originalValue = this.mergeOriginalRecord.getCodeValue(this.fieldName);
    this.matchType = MergeFieldMatchType.EQUAL;
    this.message = null;
    if (this.notCompared) {
      this.matchType = MergeFieldMatchType.NOT_COMPARED;
    } else if (!DataType.equal(mergedValue, originalValue)) {
      try {
        final MergeableRecord mergeableRecord = this.mergeOriginalRecord.mergeableRecord;
        final AbstractRecordLayer layer = this.mergeOriginalRecord.getLayer();
        final DirectionalFields directionalFields = DirectionalFields.getProperty(layer);
        final int recordIndex = this.mergeOriginalRecord.recordIndex;
        if (this.dontMerge) {
          this.matchType = MergeFieldMatchType.CANT_MERGE;
        } else if (overwridden) {
          this.matchType = MergeFieldMatchType.OVERRIDDEN;
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
    return this;
  }

}
