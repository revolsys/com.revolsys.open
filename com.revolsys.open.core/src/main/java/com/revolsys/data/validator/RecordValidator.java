/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/model/data/validator/RecordValidator.java $
 * $Author:paul.austin@revolsys.com $
 * $Date:2007-06-09 09:28:28 -0700 (Sat, 09 Jun 2007) $
 * $Revision:265 $

 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.data.validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.data.types.EnumerationDataType;

public class RecordValidator {
  private static final Logger log = Logger.getLogger(RecordValidator.class);

  private final FieldValueValidator recordAttributeValidator = new RecordAttributeValidator(this);

  private final Map<DataType, FieldValueValidator> objectValidators = new HashMap<DataType, FieldValueValidator>();

  public RecordValidator() {
    setObjectValidator(DataTypes.BOOLEAN, new BooleanAttributeValidator());
    setObjectValidator(DataTypes.DECIMAL, new BigDecimalAttributeValidator(true));
    setObjectValidator(DataTypes.INTEGER, new BigDecimalAttributeValidator(Long.MIN_VALUE,
      Long.MAX_VALUE));
    setObjectValidator(DataTypes.BYTE, new BigDecimalAttributeValidator(Byte.MIN_VALUE,
      Byte.MAX_VALUE));
    setObjectValidator(DataTypes.SHORT, new BigDecimalAttributeValidator(Short.MIN_VALUE,
      Short.MAX_VALUE));
    setObjectValidator(DataTypes.INT, new BigDecimalAttributeValidator(Integer.MIN_VALUE,
      Integer.MAX_VALUE));
    setObjectValidator(DataTypes.LONG, new BigDecimalAttributeValidator(Long.MIN_VALUE,
      Long.MAX_VALUE));
    setObjectValidator(DataTypes.DECIMAL, new BigDecimalAttributeValidator(true));
    setObjectValidator(DataTypes.FLOAT, new BigDecimalAttributeValidator(true));
    setObjectValidator(DataTypes.DOUBLE, new BigDecimalAttributeValidator(true));
    setObjectValidator(DataTypes.DATE, new DateAttributeValidator());
    setObjectValidator(DataTypes.GEOMETRY, new GeometryFieldValidator());
    setObjectValidator(DataTypes.POINT, new GeometryFieldValidator());
    setObjectValidator(DataTypes.MULTI_LINE_STRING, new GeometryFieldValidator());
    setObjectValidator(DataTypes.POLYGON, new GeometryFieldValidator());
    setObjectValidator(DataTypes.MULTI_POINT, new GeometryFieldValidator());
    setObjectValidator(DataTypes.MULTI_POLYGON, new GeometryFieldValidator());
  }

  public void addValidators(final Map<DataType, FieldValueValidator> validators) {
    for (final Entry<DataType, FieldValueValidator> entry : validators.entrySet()) {
      final DataType dataType = entry.getKey();
      final FieldValueValidator validator = entry.getValue();
      setObjectValidator(dataType, validator);
    }

  }

  public FieldValueValidator getObjectValidator(final DataType dataType) {
    FieldValueValidator validator = this.objectValidators.get(dataType);
    if (validator == null) {
      if (dataType instanceof EnumerationDataType) {
        final EnumerationDataType enumerationDataType = (EnumerationDataType)dataType;
        validator = new EnumerationAttributeValidator(enumerationDataType);
      } else {
        final String packageName = getClass().getPackage().getName();
        final String className = packageName + "." + dataType + "AttributeValidator";
        try {
          final Class<?> validatorClass = Class.forName(className);
          validator = (FieldValueValidator)validatorClass.newInstance();
        } catch (final Throwable e) {
          validator = this.recordAttributeValidator;
        }
        this.objectValidators.put(dataType, validator);
      }
    }
    return validator;
  }

  public boolean isValid(final Object object) {
    // TODO does not do checks from super classes
    if (object instanceof Record) {
      final Record record = (Record)object;
      boolean valid = true;

      final RecordDefinition type = record.getRecordDefinition();
      for (int i = 0; i < type.getFieldCount(); i++) {
        final Object value = record.getValue(i);
        final DataType dataType = type.getFieldType(i);
        final FieldDefinition attribDef = type.getField(i);
        final String fieldName = type.getFieldName(i);

        if (value == null) {
          if (attribDef.isRequired()) {
            final Object defaultValue = type.getDefaultValue(fieldName);
            if (defaultValue != null) {
              log.error("Attribute " + fieldName + "value must be specified");
              valid = false;
            }
          }
        } else {
          final FieldValueValidator validator = getObjectValidator(dataType);
          if (validator != null) {
            if (!validator.isValid(attribDef, value)) {
              if (!(validator instanceof RecordAttributeValidator)) {
                log.error(fieldName + "='" + value + "' is not a valid "
                  + dataType.getValidationName());
              }
              valid = i == record.getRecordDefinition().getGeometryFieldIndex();
            }
          }
        }

      }
      return valid;
    } else {
      return false;
    }
  }

  public void setObjectValidator(final DataType dataType, final FieldValueValidator validator) {
    this.objectValidators.put(dataType, validator);
  }
}
