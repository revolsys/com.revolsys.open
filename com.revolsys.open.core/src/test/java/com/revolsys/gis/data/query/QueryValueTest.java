package com.revolsys.gis.data.query;

import java.util.Arrays;

import junit.framework.Assert;

import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataTypes;

public class QueryValueTest {
  public static void main(final String[] args) {
    new QueryValueTest().run();
  }

  private final DataObjectMetaDataImpl metaData;

  private final Attribute idAttribute;

  private final DataObject record;

  private final Attribute nameAttribute;

  private final Attribute descriptionAttribute;

  public QueryValueTest() {
    metaData = new DataObjectMetaDataImpl("Test");
    idAttribute = metaData.addAttribute("ID", DataTypes.INT, true);
    nameAttribute = metaData.addAttribute("NAME", DataTypes.STRING, 255, true);
    descriptionAttribute = metaData.addAttribute("DESCRIPTION",
      DataTypes.STRING, 255, false);

    record = new ArrayDataObject(metaData);
    record.setValue("ID", 10);
    record.setValue("NAME", "foobar");
  }

  public void assertConditionFalse(final Condition trueCondition1,
    final DataObject record) {
    final boolean result1 = trueCondition1.accept(record);
    Assert.assertFalse(result1);
  }

  public void assertConditionTrue(final Condition trueCondition1,
    final DataObject record) {
    final boolean result1 = trueCondition1.accept(record);
    Assert.assertTrue(result1);
  }

  public void run() {
    testEqual();
    testNotEqual();
    testLessThan();
    testLessThanEqual();
    testGreaterThan();
    testGreaterThanEqual();
    testLike();
    testILike();
    testIsNull();
    testIsNotNull();
    testBetween();
    testIn();
    testAnd();
    testOr();
    testNot();
  }

  private void testAnd() {
    final Condition trueCondition1 = F.and(F.equal(idAttribute, 10));
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = F.and(F.equal(idAttribute, 10),
      F.equal(nameAttribute, "foobar"));
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = F.and(F.equal(idAttribute, 10),
      F.equal(nameAttribute, "foobar1"));
    assertConditionFalse(falseCondition1, record);
  }

  private void testBetween() {
    final Condition trueCondition1 = F.between(idAttribute, 9, 10);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = F.between(idAttribute, 10, 10);
    assertConditionTrue(trueCondition2, record);
    final Condition trueCondition3 = F.between(idAttribute, 10, 11);
    assertConditionTrue(trueCondition3, record);
    final Condition trueCondition4 = F.between(idAttribute, 9, 10);
    assertConditionTrue(trueCondition4, record);

    final Condition falseCondition1 = F.between(idAttribute, 11, 12);
    assertConditionFalse(falseCondition1, record);
  }

  private void testEqual() {
    final Condition trueCondition1 = F.equal(idAttribute, 10);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = F.equal(idAttribute, "10");
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = F.equal(idAttribute, 11);
    assertConditionFalse(falseCondition1, record);
  }

  private void testGreaterThan() {
    final Condition trueCondition1 = F.greaterThan(idAttribute, 9);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = F.greaterThan(idAttribute, "9");
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = F.greaterThan(idAttribute, 10);
    assertConditionFalse(falseCondition1, record);
  }

  private void testGreaterThanEqual() {
    final Condition trueCondition1 = F.greaterThanEqual(idAttribute, 10);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = F.greaterThanEqual(idAttribute, "10");
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = F.greaterThanEqual(idAttribute, 11);
    assertConditionFalse(falseCondition1, record);
  }

  @SuppressWarnings("unchecked")
  private void testILike() {
    for (final Object like : Arrays.asList(10, "%10", "10%", "%10%", "%1%",
      "%0%")) {
      final Condition trueCondition = F.iLike(idAttribute, like);
      assertConditionTrue(trueCondition, record);
    }
    for (final String like : Arrays.asList("%Foobar", "fooBar%", "%foObar%",
      "%fOo%", "%bAr%", "%o%B%")) {
      final Condition trueCondition = F.iLike(nameAttribute, like);
      assertConditionTrue(trueCondition, record);
    }

    for (final String like : Arrays.asList("%Foobar1", "Foobar1%", "%Foobar1%",
      "%Foo1%", "%Bar1%", "%a%b%")) {
      final Condition falseCondition = F.iLike(nameAttribute, like);
      assertConditionFalse(falseCondition, record);
    }
  }

  private void testIn() {
    final Condition trueCondition1 = F.in(idAttribute, 10, 11);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = F.in(idAttribute, "10");
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = F.in(idAttribute, 11);
    assertConditionFalse(falseCondition1, record);
  }

  private void testIsNotNull() {
    final Condition trueCondition1 = F.isNotNull(idAttribute);
    assertConditionTrue(trueCondition1, record);

    final Condition falseCondition1 = F.isNotNull(descriptionAttribute);
    assertConditionFalse(falseCondition1, record);
  }

  private void testIsNull() {
    final Condition trueCondition1 = F.isNull(descriptionAttribute);
    assertConditionTrue(trueCondition1, record);

    final Condition falseCondition1 = F.isNull(idAttribute);
    assertConditionFalse(falseCondition1, record);
  }

  private void testLessThan() {
    final Condition trueCondition1 = F.lessThan(idAttribute, 11);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = F.lessThan(idAttribute, "11");
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = F.lessThan(idAttribute, 10);
    assertConditionFalse(falseCondition1, record);
  }

  private void testLessThanEqual() {
    final Condition trueCondition1 = F.lessThanEqual(idAttribute, 10);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = F.lessThanEqual(idAttribute, "10");
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = F.lessThanEqual(idAttribute, 9);
    assertConditionFalse(falseCondition1, record);
  }

  @SuppressWarnings("unchecked")
  private void testLike() {
    for (final Object like : Arrays.asList(10, "%10", "10%", "%10%", "%1%",
      "%0%")) {
      final Condition trueCondition = F.like(idAttribute, like);
      assertConditionTrue(trueCondition, record);
    }
    for (final String like : Arrays.asList("%foobar", "foobar%", "%foobar%",
      "%foo%", "%bar%", "%o%b%")) {
      final Condition trueCondition = F.like(nameAttribute, like);
      assertConditionTrue(trueCondition, record);
    }

    for (final String like : Arrays.asList("%Foobar", "Foobar%", "%Foobar%",
      "%Foo%", "%Bar%", "%O%b%")) {
      final Condition falseCondition = F.like(nameAttribute, like);
      assertConditionFalse(falseCondition, record);
    }
  }

  private void testNot() {
    final Condition trueCondition1 = F.not(F.equal(idAttribute, 11));
    assertConditionTrue(trueCondition1, record);

    final Condition falseCondition1 = F.not(F.equal(idAttribute, 10));
    assertConditionFalse(falseCondition1, record);
  }

  private void testNotEqual() {
    final Condition trueCondition1 = F.notEqual(idAttribute, 11);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = F.notEqual(idAttribute, "11");
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = F.notEqual(idAttribute, 10);
    assertConditionFalse(falseCondition1, record);
  }

  private void testOr() {
    final Condition trueCondition1 = F.or(F.equal(idAttribute, 10));
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = F.or(F.equal(idAttribute, 11),
      F.equal(nameAttribute, "foobar"));
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = F.or(F.equal(idAttribute, 11),
      F.equal(nameAttribute, "foobar1"));
    assertConditionFalse(falseCondition1, record);
  }
}
