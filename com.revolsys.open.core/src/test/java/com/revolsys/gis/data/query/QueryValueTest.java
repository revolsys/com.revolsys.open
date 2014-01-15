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
    final Condition trueCondition1 = Q.and(Q.equal(idAttribute, 10));
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = Q.and(Q.equal(idAttribute, 10),
      Q.equal(nameAttribute, "foobar"));
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = Q.and(Q.equal(idAttribute, 10),
      Q.equal(nameAttribute, "foobar1"));
    assertConditionFalse(falseCondition1, record);
  }

  private void testBetween() {
    final Condition trueCondition1 = Q.between(idAttribute, 9, 10);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = Q.between(idAttribute, 10, 10);
    assertConditionTrue(trueCondition2, record);
    final Condition trueCondition3 = Q.between(idAttribute, 10, 11);
    assertConditionTrue(trueCondition3, record);
    final Condition trueCondition4 = Q.between(idAttribute, 9, 10);
    assertConditionTrue(trueCondition4, record);

    final Condition falseCondition1 = Q.between(idAttribute, 11, 12);
    assertConditionFalse(falseCondition1, record);
  }

  private void testEqual() {
    final Condition trueCondition1 = Q.equal(idAttribute, 10);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = Q.equal(idAttribute, "10");
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = Q.equal(idAttribute, 11);
    assertConditionFalse(falseCondition1, record);
  }

  private void testGreaterThan() {
    final Condition trueCondition1 = Q.greaterThan(idAttribute, 9);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = Q.greaterThan(idAttribute, "9");
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = Q.greaterThan(idAttribute, 10);
    assertConditionFalse(falseCondition1, record);
  }

  private void testGreaterThanEqual() {
    final Condition trueCondition1 = Q.greaterThanEqual(idAttribute, 10);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = Q.greaterThanEqual(idAttribute, "10");
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = Q.greaterThanEqual(idAttribute, 11);
    assertConditionFalse(falseCondition1, record);
  }

  @SuppressWarnings("unchecked")
  private void testILike() {
    for (final Object like : Arrays.asList(10, "%10", "10%", "%10%", "%1%",
      "%0%")) {
      final Condition trueCondition = Q.iLike(idAttribute, like);
      assertConditionTrue(trueCondition, record);
    }
    for (final String like : Arrays.asList("%Foobar", "fooBar%", "%foObar%",
      "%fOo%", "%bAr%", "%o%B%")) {
      final Condition trueCondition = Q.iLike(nameAttribute, like);
      assertConditionTrue(trueCondition, record);
    }

    for (final String like : Arrays.asList("%Foobar1", "Foobar1%", "%Foobar1%",
      "%Foo1%", "%Bar1%", "%a%b%")) {
      final Condition falseCondition = Q.iLike(nameAttribute, like);
      assertConditionFalse(falseCondition, record);
    }
  }

  private void testIn() {
    final Condition trueCondition1 = Q.in(idAttribute, 10, 11);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = Q.in(idAttribute, "10");
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = Q.in(idAttribute, 11);
    assertConditionFalse(falseCondition1, record);
  }

  private void testIsNotNull() {
    final Condition trueCondition1 = Q.isNotNull(idAttribute);
    assertConditionTrue(trueCondition1, record);

    final Condition falseCondition1 = Q.isNotNull(descriptionAttribute);
    assertConditionFalse(falseCondition1, record);
  }

  private void testIsNull() {
    final Condition trueCondition1 = Q.isNull(descriptionAttribute);
    assertConditionTrue(trueCondition1, record);

    final Condition falseCondition1 = Q.isNull(idAttribute);
    assertConditionFalse(falseCondition1, record);
  }

  private void testLessThan() {
    final Condition trueCondition1 = Q.lessThan(idAttribute, 11);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = Q.lessThan(idAttribute, "11");
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = Q.lessThan(idAttribute, 10);
    assertConditionFalse(falseCondition1, record);
  }

  private void testLessThanEqual() {
    final Condition trueCondition1 = Q.lessThanEqual(idAttribute, 10);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = Q.lessThanEqual(idAttribute, "10");
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = Q.lessThanEqual(idAttribute, 9);
    assertConditionFalse(falseCondition1, record);
  }

  @SuppressWarnings("unchecked")
  private void testLike() {
    for (final Object like : Arrays.asList(10, "%10", "10%", "%10%", "%1%",
      "%0%")) {
      final Condition trueCondition = Q.like(idAttribute, like);
      assertConditionTrue(trueCondition, record);
    }
    for (final String like : Arrays.asList("%foobar", "foobar%", "%foobar%",
      "%foo%", "%bar%", "%o%b%")) {
      final Condition trueCondition = Q.like(nameAttribute, like);
      assertConditionTrue(trueCondition, record);
    }

    for (final String like : Arrays.asList("%Foobar", "Foobar%", "%Foobar%",
      "%Foo%", "%Bar%", "%O%b%")) {
      final Condition falseCondition = Q.like(nameAttribute, like);
      assertConditionFalse(falseCondition, record);
    }
  }

  private void testNot() {
    final Condition trueCondition1 = Q.not(Q.equal(idAttribute, 11));
    assertConditionTrue(trueCondition1, record);

    final Condition falseCondition1 = Q.not(Q.equal(idAttribute, 10));
    assertConditionFalse(falseCondition1, record);
  }

  private void testNotEqual() {
    final Condition trueCondition1 = Q.notEqual(idAttribute, 11);
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = Q.notEqual(idAttribute, "11");
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = Q.notEqual(idAttribute, 10);
    assertConditionFalse(falseCondition1, record);
  }

  private void testOr() {
    final Condition trueCondition1 = Q.or(Q.equal(idAttribute, 10));
    assertConditionTrue(trueCondition1, record);
    final Condition trueCondition2 = Q.or(Q.equal(idAttribute, 11),
      Q.equal(nameAttribute, "foobar"));
    assertConditionTrue(trueCondition2, record);

    final Condition falseCondition1 = Q.or(Q.equal(idAttribute, 11),
      Q.equal(nameAttribute, "foobar1"));
    assertConditionFalse(falseCondition1, record);
  }
}
