package com.revolsys.io.xbase;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.io.ResourceEndianOutput;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.NonExistingResource;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.DateUtil;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;

public class XbaseRecordWriter extends AbstractRecordWriter {
  private static final Logger log = Logger.getLogger(XbaseRecordWriter.class);

  private final List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

  private final List<String> fieldNames = new ArrayList<String>();

  private final Resource resource;

  private final RecordDefinition recordDefinition;

  private int numRecords = 0;

  private ResourceEndianOutput out;

  private boolean useZeroForNull = true;

  private boolean initialized;

  private Map<String, String> shortNames = new HashMap<String, String>();

  private Charset charset = FileUtil.UTF8;

  public XbaseRecordWriter(final RecordDefinition recordDefinition,
    final Resource resource) {
    this.recordDefinition = recordDefinition;
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
    String name = this.shortNames.get(fullName);
    if (name == null) {
      name = fullName.toUpperCase();
    }
    if (name.length() > 10) {
      name = name.substring(0, 10);
    }
    int i = 1;
    while (this.fieldNames.contains(name)) {
      final String suffix = String.valueOf(i);
      name = name.substring(0, name.length() - suffix.length()) + i;
      i++;
    }

    final FieldDefinition field = new FieldDefinition(name, fullName, type,
      length, decimalPlaces);
    this.fieldNames.add(name);
    this.fields.add(field);
    return field;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void close() {
    try {
      if (this.out != null) {
        try {
          this.out.write(0x1a);
          this.out.seek(1);
          final Date now = new Date();
          this.out.write(now.getYear());
          this.out.write(now.getMonth() + 1);
          this.out.write(now.getDate());

          this.out.writeLEInt(this.numRecords);
        } finally {
          try {
            this.out.close();
          } finally {
            this.out = null;
          }
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  public Charset getCharset() {
    return this.charset;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public Map<String, String> getShortNames() {
    return this.shortNames;
  }

  protected boolean hasField(final String name) {
    if (Property.hasValue(name)) {
      return this.fieldNames.contains(name.toUpperCase());
    } else {
      return false;
    }
  }

  protected void init() throws IOException {
    if (!this.initialized) {
      this.initialized = true;
      if (!(this.resource instanceof NonExistingResource)) {
        final Map<String, String> shortNames = getProperty("shortNames");
        if (shortNames != null) {
          this.shortNames = shortNames;
        }
        this.out = new ResourceEndianOutput(this.resource);
        writeHeader();
      }
      final Resource codePageResource = SpringUtil.getResourceWithExtension(
        this.resource, "cpg");
      if (!(codePageResource instanceof NonExistingResource)) {
        final PrintWriter writer = SpringUtil.getPrintWriter(codePageResource);
        try {
          writer.print(this.charset.name());
        } finally {
          writer.close();
        }
      }
    }
  }

  public boolean isUseZeroForNull() {
    return this.useZeroForNull;
  }

  protected void preFirstWrite(final Record object) throws IOException {
  }

  public void setCharset(final Charset charset) {
    this.charset = charset;
  }

  public void setShortNames(final Map<String, String> shortNames) {
    this.shortNames = shortNames;
  }

  public void setUseZeroForNull(final boolean useZeroForNull) {
    this.useZeroForNull = useZeroForNull;
  }

  @Override
  public String toString() {
    return this.resource.toString();
  }

  @Override
  public void write(final Record object) {
    try {
      if (!this.initialized) {
        init();
        preFirstWrite(object);
      }
      if (this.out != null) {
        this.out.write(' ');
      }
      for (final FieldDefinition field : this.fields) {
        if (!writeField(object, field)) {
          final String attributeName = field.getFullName();
          log.warn("Unable to write attribute '" + attributeName
            + "' with value " + object.getValue(attributeName));
        }
      }
      this.numRecords++;
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected boolean writeField(final Record object, final FieldDefinition field)
      throws IOException {
    if (this.out == null) {
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
            if (this.useZeroForNull) {
              numString = numberFormat.format(0);
            }
          } else if (value instanceof Number) {
            Number number = (Number)value;
            final int decimalPlaces = field.getDecimalPlaces();
            if (decimalPlaces >= 0) {
              if (number instanceof BigDecimal) {
                final BigDecimal bigDecimal = new BigDecimal(number.toString());
                number = bigDecimal.setScale(decimalPlaces,
                  RoundingMode.HALF_UP);
              } else if (number instanceof Double || number instanceof Float) {
                final double doubleValue = number.doubleValue();
                final double precisionScale = field.getPrecisionScale();
                number = MathUtil.makePrecise(precisionScale, doubleValue);
              }
            }
            numString = numberFormat.format(number);
          } else {
            throw new IllegalArgumentException("Not a number " + attributeName
              + "=" + value);
          }
          final int numLength = numString.length();
          if (numLength > fieldLength) {
            for (int i = 0; i < fieldLength; i++) {
              this.out.write('9');
            }
          } else {
            for (int i = numLength; i < fieldLength; i++) {
              this.out.write(' ');
            }
            this.out.writeBytes(numString);
          }
          return true;
        case FieldDefinition.FLOAT_TYPE:
          String floatString = "";
          if (value != null) {
            floatString = value.toString();
          }
          final int floatLength = floatString.length();
          if (floatLength > fieldLength) {
            for (int i = 0; i < fieldLength; i++) {
              this.out.write('9');
            }
          } else {
            for (int i = floatLength; i < fieldLength; i++) {
              this.out.write(' ');
            }
            this.out.writeBytes(floatString);
          }
          return true;

        case FieldDefinition.CHARACTER_TYPE:
          String string = "";
          if (value != null) {
            string = StringConverterRegistry.toString(value);
          }
          final byte[] bytes = string.getBytes(this.charset);
          if (bytes.length >= fieldLength) {
            this.out.write(bytes, 0, fieldLength);
          } else {
            this.out.write(bytes);
            for (int i = bytes.length; i < fieldLength; i++) {
              this.out.write(' ');
            }
          }
          return true;

        case FieldDefinition.DATE_TYPE:
          if (value instanceof Date) {
            final Date date = (Date)value;
            final String dateString = DateUtil.format("yyyyMMdd", date);
            this.out.writeBytes(dateString);

          } else if (value == null) {
            this.out.writeBytes("        ");
          } else {
            this.out.writeBytes(value.toString().substring(0, 8));
          }
          return true;

        case FieldDefinition.LOGICAL_TYPE:
          boolean logical = false;
          if (value instanceof Boolean) {
            final Boolean boolVal = (Boolean)value;
            logical = boolVal.booleanValue();
          } else if (value != null) {
            logical = Boolean.valueOf(value.toString());
          }
          if (logical) {
            this.out.write('T');
          } else {
            this.out.write('F');
          }
          return true;

        default:
          return false;
      }
    }
  }

  @SuppressWarnings("deprecation")
  private void writeHeader() throws IOException {
    if (this.out != null) {
      int recordLength = 1;

      this.fields.clear();
      int numFields = 0;
      for (final String name : this.recordDefinition.getAttributeNames()) {
        final int index = this.recordDefinition.getAttributeIndex(name);
        final int length = this.recordDefinition.getAttributeLength(index);
        final int scale = this.recordDefinition.getAttributeScale(index);
        final DataType attributeType = this.recordDefinition.getAttributeType(index);
        final Class<?> typeJavaClass = attributeType.getJavaClass();
        final int fieldLength = addDbaseField(name, attributeType,
          typeJavaClass, length, scale);
        if (fieldLength > 0) {
          recordLength += fieldLength;
          numFields++;
        }
      }
      this.out.write(0x03);
      final Date now = new Date();
      this.out.write(now.getYear());
      this.out.write(now.getMonth() + 1);
      this.out.write(now.getDate());
      // Write 0 as the number of records, come back and update this when closed
      this.out.writeLEInt(0);
      final short headerLength = (short)(33 + numFields * 32);

      this.out.writeLEShort(headerLength);
      this.out.writeLEShort((short)recordLength);
      this.out.writeLEShort((short)0);
      this.out.write(0);
      this.out.write(0);
      this.out.writeLEInt(0);
      this.out.writeLEInt(0);
      this.out.writeLEInt(0);
      this.out.write(0);
      this.out.write(1);
      this.out.writeLEShort((short)0);
      int offset = 1;
      for (final FieldDefinition field : this.fields) {
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
          this.out.writeBytes(name);
          final int numPad = 11 - name.length();
          for (int i = 0; i < numPad; i++) {
            this.out.write(0);
          }
          this.out.write(field.getType());
          this.out.writeLEInt(0);
          this.out.write(length);
          this.out.write(decimalPlaces);
          this.out.writeLEShort((short)0);
          this.out.write(0);
          this.out.writeLEShort((short)0);
          this.out.write(0);
          this.out.write(0);
          this.out.write(0);
          this.out.write(0);
          this.out.write(0);
          this.out.write(0);
          this.out.write(0);
          this.out.write(0);
          this.out.write(0);
          offset += length;
        }
      }
      this.out.write(0x0d);
    }
  }
}
