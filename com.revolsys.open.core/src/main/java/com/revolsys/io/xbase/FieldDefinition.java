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
package com.revolsys.io.xbase;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;

public class FieldDefinition {
  public static final char CHARACTER_TYPE = 'C';

  private static final Map<Character, DataType> DATA_TYPES = new HashMap<Character, DataType>();

  public static final char DATE_TYPE = 'D';

  public static final char FLOAT_TYPE = 'F';

  public static final char LOGICAL_TYPE = 'L';

  public static final char MEMO_TYPE = 'M';

  public static final char NUMBER_TYPE = 'N';

  public static final char OBJECT_TYPE = 'o';

  static {
    DATA_TYPES.put(CHARACTER_TYPE, DataTypes.STRING);
    DATA_TYPES.put(NUMBER_TYPE, DataTypes.DECIMAL);
    DATA_TYPES.put(LOGICAL_TYPE, DataTypes.BOOLEAN);
    DATA_TYPES.put(DATE_TYPE, DataTypes.DATE_TIME);
    DATA_TYPES.put(MEMO_TYPE, DataTypes.STRING);
    DATA_TYPES.put(FLOAT_TYPE, DataTypes.FLOAT);
    DATA_TYPES.put(OBJECT_TYPE, DataTypes.OBJECT);

  }

  private final DataType dataType;

  private final int decimalPlaces;

  private final int length;

  private final String name;

  private final String fullName;

  private DecimalFormat numberFormat;

  private final char type;

  private double precisionScale;

  public FieldDefinition(final String name, final String fullName,
    final char type, final int length) {
    this(name, fullName, type, length, 0);
  }

  public FieldDefinition(final String name, final String fullName,
    final char type, final int length, final int decimalPlaces) {
    this.name = name;
    this.fullName = fullName;
    this.type = type;
    this.dataType = DATA_TYPES.get(type);
    this.length = length;
    this.decimalPlaces = decimalPlaces;
    if (type == FieldDefinition.NUMBER_TYPE) {
      final StringBuilder format = new StringBuilder("0");
      if (decimalPlaces > 0) {
        format.append(".");
        for (int i = 0; i < decimalPlaces; i++) {
          format.append("#");
        }
        precisionScale = Math.pow(10, decimalPlaces);
      } else if (decimalPlaces == -1 && length > 2) {
        format.append(".");
        for (int i = 0; i < length - 2; i++) {
          format.append("#");
        }
      } else {
        precisionScale = 1;
      }
      numberFormat = new DecimalFormat(format.toString());
    }
  }

  public DataType getDataType() {
    return dataType;
  }

  public int getDecimalPlaces() {
    return decimalPlaces;
  }

  public String getFullName() {
    return fullName;
  }

  public int getLength() {
    return length;
  }

  public String getName() {
    return name;
  }

  public DecimalFormat getNumberFormat() {
    return numberFormat;
  }

  public double getPrecisionScale() {
    return precisionScale;
  }

  public char getType() {
    return type;
  }

  @Override
  public String toString() {
    return name + ":" + dataType + "(" + length + ")";
  }

}
