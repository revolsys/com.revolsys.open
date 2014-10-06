/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

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

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.FieldDefinition;

public class RecordAttributeValidator implements AttributeValueValidator {
  private final RecordValidator validator;

  public RecordAttributeValidator() {
    this(new RecordValidator());
  }

  public RecordAttributeValidator(final RecordValidator validator) {
    this.validator = validator;
  }

  @Override
  public boolean isValid(final FieldDefinition attributeDefinition, final Object value) {
    if (value instanceof Record) {
      return validator.isValid(value);
    } else {
      return false;
    }
  }
}
