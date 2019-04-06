package com.revolsys.core.test.datatype;

import java.util.Arrays;

import org.jeometry.common.datatype.DataType;
import org.jeometry.common.datatype.DataTypes;
import org.junit.Assert;
import org.junit.Test;

public class DataTypeTest {

  protected void assertEquals(final DataType dataType, final Object value1, final Object value2) {
    final String label = value1 + "=" + value2;
    Assert.assertTrue(label, dataType.equals(value1, value2));
    Assert.assertTrue(label, dataType.equals(value2, value1));
    Assert.assertTrue(label, DataType.equal(value1, value2));
  }

  protected void assertEqualsValues(final DataType dataType, final Object... values) {
    for (final Object value : values) {
      assertEquals(dataType, value, value);
      assertEquals(dataType, value, dataType.toString(value));
      assertNotEquals(dataType, value, "~");
      assertNotEquals(dataType, value, null);
    }
  }

  protected void assertNotEquals(final DataType dataType, final Object value1,
    final Object value2) {
    final String label = value1 + "!=" + value2;
    Assert.assertFalse(label, dataType.equals(value1, value2));
    Assert.assertFalse(label, dataType.equals(value2, value1));
    Assert.assertFalse(label, DataType.equal(value1, value2));
  }

  public void assertNumberEquals(final DataType dataType, final Object min, final Object max,
    final Object invalid) {
    assertEquals(dataType, min, min);
    assertEquals(dataType, min, String.valueOf(min));
    assertEquals(dataType, max, max);
    assertEquals(dataType, max, String.valueOf(max));
  }

  @Test
  public void testBoolean() {
    for (final Object falseValue : Arrays.asList(false, "fAlse", "n", "0")) {
      assertEquals(DataTypes.BOOLEAN, false, falseValue);
      assertNotEquals(DataTypes.BOOLEAN, true, "x");
    }
    for (final Object trueValue : Arrays.asList(true, "tRue", "y", "1")) {
      assertEquals(DataTypes.BOOLEAN, true, trueValue);
      assertNotEquals(DataTypes.BOOLEAN, false, trueValue);
      assertNotEquals(DataTypes.BOOLEAN, false, "x");
    }
  }

  @Test
  public void testByte() {
    assertEqualsValues(DataTypes.BYTE, Byte.MIN_VALUE, Byte.MAX_VALUE);
  }

  @Test
  public void testDouble() {
    assertEqualsValues(DataTypes.DOUBLE, -Double.MAX_VALUE, Double.MAX_VALUE, Double.NaN,
      Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN);
  }

  @Test
  public void testFloat() {
    assertEqualsValues(DataTypes.FLOAT, -Float.MAX_VALUE, Float.MAX_VALUE, Float.NaN,
      Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NaN);
  }

  @Test
  public void testInt() {
    assertEqualsValues(DataTypes.INT, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  @Test
  public void testLong() {
    assertEqualsValues(DataTypes.LONG, Long.MIN_VALUE, Long.MAX_VALUE);
  }

  @Test
  public void testShort() {
    assertEqualsValues(DataTypes.SHORT, Short.MIN_VALUE, Short.MAX_VALUE);
  }
}
