package com.revolsys.io.xbase;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.io.ResourceEndianOutput;
import com.revolsys.io.AbstractWriter;
import com.revolsys.spring.NonExistingResource;
import com.vividsolutions.jts.geom.PrecisionModel;

public class XbaseDataObjectWriter extends AbstractWriter<DataObject> {
  private static final Logger log = Logger.getLogger(XbaseDataObjectWriter.class);

  private final List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

  private final List<String> fieldNames = new ArrayList<String>();

  private final Resource resource;

  private final DataObjectMetaData metaData;

  private int numRecords = 0;

  private ResourceEndianOutput out;

  private boolean useZeroForNull = true;

  private boolean initialized;

  private Map<String, String> shortNames = new HashMap<String, String>();

  public XbaseDataObjectWriter(final DataObjectMetaData metaData,
    final Resource resource) {
    this.metaData = metaData;
    this.resource = resource;
  }

  protected int addDbaseField(final String fullName, final DataType dataType,
    final Class<?> typeJavaClass, final int length, final int scale) {
    FieldDefinition field = null;
    if (dataType == DataTypes.DECIMAL) {
      if (length > 18) {
        throw new IllegalArgumentException("Length  must be less < 18 for "
          + fullName);
      }
      field = addFieldDefinition(fullName, FieldDefinition.NUMBER_TYPE, length,
        scale);
    } else if (typeJavaClass == String.class) {
      if (length > 254 || length <= 0) {
        field = addFieldDefinition(fullName, FieldDefinition.CHARACTER_TYPE,
          254);
      } else {
        field = addFieldDefinition(fullName, FieldDefinition.CHARACTER_TYPE,
          length);
      }
    } else if (typeJavaClass == Boolean.class) {
      field = addFieldDefinition(fullName, FieldDefinition.LOGICAL_TYPE, 1);
    } else if (typeJavaClass == Date.class) {
      field = addFieldDefinition(fullName, FieldDefinition.DATE_TYPE, 8);
    } else if (typeJavaClass == BigDecimal.class) {
      field = addFieldDefinition(fullName, FieldDefinition.NUMBER_TYPE, length,
        scale);
    } else if (typeJavaClass == BigInteger.class) {
      field = addFieldDefinition(fullName, FieldDefinition.NUMBER_TYPE, 18);
    } else if (typeJavaClass == Float.class) {
      field = addFieldDefinition(fullName, FieldDefinition.NUMBER_TYPE, 18);
    } else if (typeJavaClass == Double.class) {
      field = addFieldDefinition(fullName, FieldDefinition.NUMBER_TYPE, 18);
    } else if (typeJavaClass == Byte.class) {
      field = addFieldDefinition(fullName, FieldDefinition.NUMBER_TYPE, 3);
    } else if (typeJavaClass == Short.class) {
      field = addFieldDefinition(fullName, FieldDefinition.NUMBER_TYPE, 5);
    } else if (typeJavaClass == Integer.class) {
      field = addFieldDefinition(fullName, FieldDefinition.NUMBER_TYPE, 10);
    } else if (typeJavaClass == Long.class) {
      field = addFieldDefinition(fullName, FieldDefinition.NUMBER_TYPE, 18);
    } else {
      log.warn("Writing " + typeJavaClass + " is not supported");
      field = addFieldDefinition(fullName, FieldDefinition.OBJECT_TYPE, 0);
    }
    return field.getLength();
  }

  protected FieldDefinition addFieldDefinition(final String fullName,
    final char type, final int length) {
    return addFieldDefinition(fullName, type, length, 0);
  }

  protected FieldDefinition addFieldDefinition(final String fullName,
    final char type, final int length, final int decimalPlaces) {
    String name = shortNames.get(fullName);
    if (name == null) {
      name = fullName.toUpperCase();
    }
    if (name.length() > 10) {
      name = name.substring(0, 10);
    }
    int i = 1;
    while (fieldNames.contains(name)) {
      final String suffix = String.valueOf(i);
      name = name.substring(0, name.length() - suffix.length()) + i;
      i++;
    }

    final FieldDefinition field = new FieldDefinition(name, fullName, type,
      length, decimalPlaces);
    fieldNames.add(name);
    fields.add(field);
    return field;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void close() {
    try {
      if (out != null) {
        try {
          out.write(0x1a);
          out.seek(1);
          final Date now = new Date();
          out.write(now.getYear());
          out.write(now.getMonth() + 1);
          out.write(now.getDate());

          out.writeLEInt(numRecords);
        } finally {
          try {
            out.close();
          } finally {
            out = null;
          }
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void flush() {
    out.flush();
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public Map<String, String> getShortNames() {
    return shortNames;
  }

  protected boolean hasField(final String name) {
    if (StringUtils.hasText(name)) {
      return fieldNames.contains(name.toUpperCase());
    } else {
      return false;
    }
  }

  protected void init() throws IOException {
    if (!initialized) {
      initialized = true;
      if (!(resource instanceof NonExistingResource)) {
        Map<String, String> shortNames = getProperty("shortNames");
        if (shortNames != null) {
          this.shortNames = shortNames;
        }
        this.out = new ResourceEndianOutput(resource);
        writeHeader();
      }
    }
  }

  public boolean isUseZeroForNull() {
    return useZeroForNull;
  }

  protected void preFirstWrite(final DataObject object) throws IOException {
  }

  public void setShortNames(final Map<String, String> shortNames) {
    this.shortNames = shortNames;
  }

  public void setUseZeroForNull(final boolean useZeroForNull) {
    this.useZeroForNull = useZeroForNull;
  }

  @Override
  public String toString() {
    return resource.toString();
  }

  public void write(final DataObject object) {
    try {
      if (!initialized) {
        init();
        preFirstWrite(object);
      }
      if (out != null) {
        out.write(' ');
      }
      for (final FieldDefinition field : fields) {
        if (!writeField(object, field)) {
          final String attributeName = field.getFullName();
          log.warn("Unable to write attribute '" + attributeName
            + "' with value " + object.getValue(attributeName));
        }
      }
      numRecords++;
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected boolean writeField(final DataObject object,
    final FieldDefinition field) throws IOException {
    if (out == null) {
      return true;
    } else {
      final String attributeName = field.getFullName();
      final Object value = object.getValue(attributeName);
      final int fieldLength = field.getLength();
      switch (field.getType()) {
        case FieldDefinition.NUMBER_TYPE:
          String numString = "";
          final DecimalFormat numberFormat = field.getNumberFormat();
          if (value == null) {
            if (useZeroForNull) {
              numString = numberFormat.format(0);
            }
          } else if (value instanceof Number) {

            Number number = (Number)value;
            final int decimalPlaces = field.getDecimalPlaces();
            if (decimalPlaces >= 0) {
              if (number instanceof BigDecimal) {
                final BigDecimal bigDecimal = (BigDecimal)number;
                number = bigDecimal.setScale(decimalPlaces,
                  RoundingMode.HALF_UP);
              } else if ((number instanceof Double)
                || (number instanceof Float)) {
                final double doubleValue = number.doubleValue();
                final PrecisionModel precisionModel = field.getPrecisionModel();
                number = precisionModel.makePrecise(doubleValue);
              }
            }
            numString = numberFormat.format(number);
          } else {
            throw new IllegalArgumentException("Not a number " + attributeName
              + "=" + value);
          }
          int numLength = numString.length();
          if (numLength > fieldLength) {
            for (int i = 0; i < fieldLength; i++) {
              out.write('9');
            }
          } else {
            for (int i = numLength; i < fieldLength; i++) {
              out.write(' ');
            }
            out.writeBytes(numString);
          }
          return true;
        case FieldDefinition.FLOAT_TYPE:
          String floatString = "";
          if (value != null) {
            floatString = value.toString();
          }
          int floatLength = floatString.length();
          if (floatLength > fieldLength) {
            for (int i = 0; i < fieldLength; i++) {
              out.write('9');
            }
          } else {
            for (int i = floatLength; i < fieldLength; i++) {
              out.write(' ');
            }
            out.writeBytes(floatString);
          }
          return true;

        case FieldDefinition.CHARACTER_TYPE:
          String string = "";
          if (value != null) {
            string = value.toString();
          }
          byte[] bytes = string.getBytes(Charset.forName("ISO-8859-1"));
          if (bytes.length >= fieldLength) {
            out.write(bytes, 0, fieldLength);
          } else {
            out.write(bytes);
            for (int i = bytes.length; i < fieldLength; i++) {
              out.write(' ');
            }
          }
          return true;

        case FieldDefinition.DATE_TYPE:
          if (value instanceof Date) {
            final Date date = (Date)value;
            final DateFormat format = new SimpleDateFormat("yyyyMMdd");
            final String dateString = format.format(date);
            out.writeBytes(dateString);

          } else if (value == null) {
            out.writeBytes("        ");
          } else {
            out.writeBytes(value.toString().substring(0, 8));
          }
          return true;

        case FieldDefinition.LOGICAL_TYPE:
          boolean logical = false;
          if (value instanceof Boolean) {
            final Boolean boolVal = (Boolean)value;
            logical = boolVal.booleanValue();
          } else if (value != null) {
            logical = Boolean.getBoolean(value.toString());
          }
          if (logical) {
            out.write('T');
          } else {
            out.write('F');
          }
          return true;

        default:
          return false;
      }
    }
  }

  @SuppressWarnings("deprecation")
  private void writeHeader() throws IOException {
    if (out != null) {
      int recordLength = 1;

      fields.clear();
      int numFields = 0;
      for (final String name : metaData.getAttributeNames()) {
        final int index = metaData.getAttributeIndex(name);
        final int length = metaData.getAttributeLength(index);
        final int scale = metaData.getAttributeScale(index);
        final DataType attributeType = metaData.getAttributeType(index);
        final Class<?> typeJavaClass = attributeType.getJavaClass();
        final int fieldLength = addDbaseField(name, attributeType,
          typeJavaClass, length, scale);
        if (fieldLength > 0) {
          recordLength += fieldLength;
          numFields++;
        }
      }
      out.write(0x03);
      final Date now = new Date();
      out.write(now.getYear());
      out.write(now.getMonth() + 1);
      out.write(now.getDate());
      // Write 0 as the number of records, come back and update this when closed
      out.writeLEInt(0);
      final short headerLength = (short)(33 + numFields * 32);

      out.writeLEShort(headerLength);
      out.writeLEShort((short)recordLength);
      out.writeLEShort((short)0);
      out.write(0);
      out.write(0);
      out.writeLEInt(0);
      out.writeLEInt(0);
      out.writeLEInt(0);
      out.write(0);
      out.write(1);
      out.writeLEShort((short)0);
      int offset = 1;
      for (final FieldDefinition field : fields) {
        if (field.getDataType() != DataTypes.OBJECT) {
          String name = field.getName();
          if (name.length() > 10) {
            name = name.substring(0, 10);
          }
          final int length = field.getLength();
           int decimalPlaces = field.getDecimalPlaces();
          if (decimalPlaces < 0) {
            decimalPlaces = 0;
          } else if (decimalPlaces > 15) {
            decimalPlaces = Math.min(length, 15);
          } else if (decimalPlaces > length) {
            decimalPlaces = Math.min(length, 15);
          }
          out.writeBytes(name);
          final int numPad = 11 - name.length();
          for (int i = 0; i < numPad; i++) {
            out.write(0);
          }
          out.write(field.getType());
          out.writeLEInt(0);
          out.write(length);
          out.write(decimalPlaces);
          out.writeLEShort((short)0);
          out.write(0);
          out.writeLEShort((short)0);
          out.write(0);
          out.write(0);
          out.write(0);
          out.write(0);
          out.write(0);
          out.write(0);
          out.write(0);
          out.write(0);
          out.write(0);
          offset += length;
        }
      }
      out.write(0x0d);
    }
  }
}
