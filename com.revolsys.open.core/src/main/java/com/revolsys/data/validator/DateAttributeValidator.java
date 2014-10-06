/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/java/com/revolsys/gis/model/data/validator/BooleanAttributeValidator.java $
 * $Author: paul.austin@revolsys.com $
 * $Date: 2006-01-31 15:36:21 -0800 (Tue, 31 Jan 2006) $
 * $Revision: 75 $

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

import java.util.Date;

import com.revolsys.data.record.schema.FieldDefinition;

public class DateAttributeValidator implements AttributeValueValidator {

  @Override
  public boolean isValid(final FieldDefinition attributeDefinition, final Object value) {
    return (value instanceof Date);

  }
}
