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
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.data.types.EnumerationDataType;

public class RecordValidator {
  private static final Logger log = Logger.getLogger(RecordValidator.class);

  private final AttributeValueValidator dataObjectAttributeValidator = new RecordAttributeValidator(
    this);

  private final Map<DataType, AttributeValueValidator> objectValidators = new HashMap<DataType, AttributeValueValidator>();

  public RecordValidator() {
    setObjectValidator(DataTypes.BOOLEAN, new BooleanAttributeValidator());
    setObjectValidator(DataTypes.DECIMAL,
      new BigDecimalAttributeValidator(true));
    setObjectValidator(DataTypes.INTEGER, new BigDecimalAttributeValidator(
      Long.MIN_VALUE, Long.MAX_VALUE));
    setObjectValidator(DataTypes.BYTE, new BigDecimalAttributeValidator(
      Byte.MIN_VALUE, Byte.MAX_VALUE));
    setObjectValidator(DataTypes.SHORT, new BigDecimalAttributeValidator(
      Short.MIN_VALUE, Short.MAX_VALUE));
    setObjectValidator(DataTypes.INT, new BigDecimalAttributeValidator(
      Integer.MIN_VALUE, Integer.MAX_VALUE));
    setObjectValidator(DataTypes.LONG, new BigDecimalAttributeValidator(
      Long.MIN_VALUE, Long.MAX_VALUE));
    setObjectValidator(DataTypes.DECIMAL,
      new BigDecimalAttributeValidator(true));
    setObjectValidator(DataTypes.FLOAT, new BigDecimalAttributeValidator(true));
    setObjectValidator(DataTypes.DOUBLE, new BigDecimalAttributeValidator(true));
    setObjectValidator(DataTypes.DATE, new DateAttributeValidator());
    setObjectValidator(DataTypes.GEOMETRY, new GeometryAttributeValidator());
    setObjectValidator(DataTypes.POINT, new GeometryAttributeValidator());
    setObjectValidator(DataTypes.MULTI_LINE_STRING,
      new GeometryAttributeValidator());
    setObjectValidator(DataTypes.POLYGON, new GeometryAttributeValidator());
    setObjectValidator(DataTypes.MULTI_POINT, new GeometryAttributeValidator());
    setObjectValidator(DataTypes.MULTI_POLYGON,
      new GeometryAttributeValidator());
  }

  public void addValidators(
    final Map<DataType, AttributeValueValidator> validators) {
    for (final Entry<DataType, AttributeValueValidator> entry : validators.entrySet()) {
      final DataType dataType = entry.getKey();
      final AttributeValueValidator validator = entry.getValue();
      setObjectValidator(dataType, validator);
    }

  }

  public AttributeValueValidator getObjectValidator(final DataType dataType) {
    AttributeValueValidator validator = objectValidators.get(dataType);
    if (validator == null) {
      if (dataType instanceof EnumerationDataType) {
        final EnumerationDataType enumerationDataType = (EnumerationDataType)dataType;
        validator = new EnumerationAttributeValidator(enumerationDataType);
      } else {
        final String packageName = getClass().getPackage().getName();
        final String className = packageName + "." + dataType
          + "AttributeValidator";
        try {
          final Class<?> validatorClass = Class.forName(className);
          validator = (AttributeValueValidator)validatorClass.newInstance();
        } catch (final Throwable e) {
          validator = dataObjectAttributeValidator;
        }
        objectValidators.put(dataType, validator);
      }
    }
    return validator;
  }

  public boolean isValid(final Object object) {
    // TODO does not do checks from super classes
    if (object instanceof Record) {
      final Record dataObject = (Record)object;
      boolean valid = true;

      final RecordDefinition type = dataObject.getMetaData();
      for (int i = 0; i < type.getAttributeCount(); i++) {
        final Object value = dataObject.getValue(i);
        final DataType dataType = type.getAttributeType(i);
        final Attribute attribDef = type.getAttribute(i);
        final String attributeName = type.getAttributeName(i);

        if (value == null) {
          if (attribDef.isRequired()) {
            final Object defaultValue = type.getDefaultValue(attributeName);
            if (defaultValue != null) {
              log.error("Attribute " + attributeName
                + "value must be specified");
              valid = false;
            }
          }
        } else {
          final AttributeValueValidator validator = getObjectValidator(dataType);
          if (validator != null) {
            if (!validator.isValid(attribDef, value)) {
              if (!(validator instanceof RecordAttributeValidator)) {
                log.error("Attribute " + attributeName + " '" + value
                  + "' is not a valid value for type '" + dataType + "'");
              }
              valid = (i == dataObject.getMetaData()
                .getGeometryAttributeIndex());
            }
          }
        }

      }
      return valid;
    } else {
      return false;
    }
  }

  public void setObjectValidator(final DataType dataType,
    final AttributeValueValidator validator) {
    objectValidators.put(dataType, validator);
  }
}
