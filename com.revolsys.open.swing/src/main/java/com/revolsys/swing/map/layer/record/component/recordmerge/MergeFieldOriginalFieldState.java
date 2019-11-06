package com.revolsys.swing.map.layer.record.component.recordmerge;

import java.util.Collection;
import java.util.Collections;

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

  private final boolean fromField;

  private final boolean toField;

  private final DirectionalFields directionalFields;

  private final boolean errorOnNotEqual;

  public MergeFieldOriginalFieldState(final MergeOriginalRecord mergeOriginalRecord,
    final String fieldName) {
    final AbstractRecordLayer layer = mergeOriginalRecord.getLayer();
    final Collection<String> ignoreDifferentFieldNames = layer
      .getProperty("mergeRecordsNotComparedFieldNames", Collections.emptySet());
    final Collection<String> blockNotEqualFieldNames = layer
      .getProperty("mergeRecordsBlockNotEqualFieldNames", Collections.emptySet());
    final Collection<String> errorNotEqualFieldNames = layer
      .getProperty("mergeRecordsErrorNotEqualFieldNames", Collections.emptySet());
    this.dontMerge = blockNotEqualFieldNames.contains(fieldName);

    this.notCompared = (mergeOriginalRecord.isIdField(fieldName)
      || mergeOriginalRecord.isGeometryField(fieldName)
      || ignoreDifferentFieldNames.contains(fieldName)) && !this.dontMerge;

    this.errorOnNotEqual = errorNotEqualFieldNames.contains(fieldName);

    this.mergeOriginalRecord = mergeOriginalRecord;
    this.fieldName = fieldName;
    this.directionalFields = DirectionalFields.getProperty(mergeOriginalRecord);
    this.fromField = this.directionalFields.isFromField(this.fieldName);
    this.toField = this.directionalFields.isToField(this.fieldName);

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
    final int recordIndex = this.mergeOriginalRecord.recordIndex;
    final Object originalValue = this.mergeOriginalRecord.getCodeValue(this.fieldName);
    final MergeableRecord mergeableRecord = this.mergeOriginalRecord.mergeableRecord;
    final boolean valueEqual = DataType.equal(mergedValue, originalValue);
    this.matchType = MergeFieldMatchType.EQUAL;
    this.message = null;

    if (this.fromField && recordIndex != 0
      || this.toField && recordIndex != mergeableRecord.getMaxRecordIndex()
      || this.toField && recordIndex != mergeableRecord.getMaxRecordIndex()) {
      try {
        this.directionalFields.validateFieldAtMergeEnd(mergeableRecord, this.fieldName,
          originalValue);
        if (!valueEqual) {
          this.matchType = MergeFieldMatchType.ALLOWED_NOT_EQUAL;
        }
      } catch (final FieldValueInvalidException e) {
        this.message = e.getMessageText();
        if (overwridden) {
          this.matchType = MergeFieldMatchType.OVERRIDDEN;
        } else {
          this.matchType = MergeFieldMatchType.END_FIELD_NOT_VALID;
        }
      }
    } else if (this.notCompared) {
      this.matchType = MergeFieldMatchType.NOT_COMPARED;
    } else if (!valueEqual) {
      if (this.dontMerge) {
        this.matchType = MergeFieldMatchType.CANT_MERGE;
      } else if (overwridden) {
        this.matchType = MergeFieldMatchType.OVERRIDDEN;
      } else if (originalValue == null) {
        this.matchType = MergeFieldMatchType.WAS_NULL;
      } else {
        this.message = DataTypes.toString(originalValue) + " != " + DataTypes.toString(mergedValue);
        if (this.errorOnNotEqual) {
          this.matchType = MergeFieldMatchType.NOT_EQUAL;
        } else {
          this.matchType = MergeFieldMatchType.ALLOWED_NOT_EQUAL;
        }
      }
    }

    return this;
  }

}
