package com.revolsys.core.test.gis.data.query;

import java.util.Arrays;

import org.jeometry.common.datatype.DataTypes;
import org.jeometry.common.io.PathName;

import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Q;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;

import junit.framework.Assert;

public class QueryValueTest {
  public static void main(final String[] args) {
    new QueryValueTest().run();
  }

  private final FieldDefinition descriptionAttribute;

  private final FieldDefinition idField;

  private final FieldDefinition nameAttribute;

  private final Record record;

  private final RecordDefinitionImpl recordDefinition;

  public QueryValueTest() {
    this.recordDefinition = new RecordDefinitionImpl(PathName.newPathName("/Test"));
    this.idField = this.recordDefinition.addField("ID", DataTypes.INT, true);
    this.nameAttribute = this.recordDefinition.addField("NAME", DataTypes.STRING, 255, true);
    this.descriptionAttribute = this.recordDefinition.addField("DESCRIPTION", DataTypes.STRING, 255,
      false);

    this.record = new ArrayRecord(this.recordDefinition);
    this.record.setValue("ID", 10);
    this.record.setValue("NAME", "foobar");
  }

  public void assertConditionFalse(final Condition trueCondition1, final Record record) {
    final boolean result1 = trueCondition1.test(record);
    Assert.assertFalse(result1);
  }

  public void assertConditionTrue(final Condition trueCondition1, final Record record) {
    final boolean result1 = trueCondition1.test(record);
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
    final Condition trueCondition1 = Q.and(Q.equal(this.idField, 10));
    assertConditionTrue(trueCondition1, this.record);
    final Condition trueCondition2 = Q.and(Q.equal(this.idField, 10),
      Q.equal(this.nameAttribute, "foobar"));
    assertConditionTrue(trueCondition2, this.record);

    final Condition falseCondition1 = Q.and(Q.equal(this.idField, 10),
      Q.equal(this.nameAttribute, "foobar1"));
    assertConditionFalse(falseCondition1, this.record);
  }

  private void testBetween() {
    final Condition trueCondition1 = Q.between(this.idField, 9, 10);
    assertConditionTrue(trueCondition1, this.record);
    final Condition trueCondition2 = Q.between(this.idField, 10, 10);
    assertConditionTrue(trueCondition2, this.record);
    final Condition trueCondition3 = Q.between(this.idField, 10, 11);
    assertConditionTrue(trueCondition3, this.record);
    final Condition trueCondition4 = Q.between(this.idField, 9, 10);
    assertConditionTrue(trueCondition4, this.record);

    final Condition falseCondition1 = Q.between(this.idField, 11, 12);
    assertConditionFalse(falseCondition1, this.record);
  }

  private void testEqual() {
    final Condition trueCondition1 = Q.equal(this.idField, 10);
    assertConditionTrue(trueCondition1, this.record);
    final Condition trueCondition2 = Q.equal(this.idField, "10");
    assertConditionTrue(trueCondition2, this.record);

    final Condition falseCondition1 = Q.equal(this.idField, 11);
    assertConditionFalse(falseCondition1, this.record);
  }

  private void testGreaterThan() {
    final Condition trueCondition1 = Q.greaterThan(this.idField, 9);
    assertConditionTrue(trueCondition1, this.record);
    final Condition trueCondition2 = Q.greaterThan(this.idField, "9");
    assertConditionTrue(trueCondition2, this.record);

    final Condition falseCondition1 = Q.greaterThan(this.idField, 10);
    assertConditionFalse(falseCondition1, this.record);
  }

  private void testGreaterThanEqual() {
    final Condition trueCondition1 = Q.greaterThanEqual(this.idField, 10);
    assertConditionTrue(trueCondition1, this.record);
    final Condition trueCondition2 = Q.greaterThanEqual(this.idField, "10");
    assertConditionTrue(trueCondition2, this.record);

    final Condition falseCondition1 = Q.greaterThanEqual(this.idField, 11);
    assertConditionFalse(falseCondition1, this.record);
  }

  @SuppressWarnings("unchecked")
  private void testILike() {
    for (final Object like : Arrays.asList(10, "%10", "10%", "%10%", "%1%", "%0%")) {
      final Condition trueCondition = Q.iLike(this.idField, like);
      assertConditionTrue(trueCondition, this.record);
    }
    for (final String like : Arrays.asList("%Foobar", "fooBar%", "%foObar%", "%fOo%", "%bAr%",
      "%o%B%")) {
      final Condition trueCondition = Q.iLike(this.nameAttribute, like);
      assertConditionTrue(trueCondition, this.record);
    }

    for (final String like : Arrays.asList("%Foobar1", "Foobar1%", "%Foobar1%", "%Foo1%", "%Bar1%",
      "%a%b%")) {
      final Condition falseCondition = Q.iLike(this.nameAttribute, like);
      assertConditionFalse(falseCondition, this.record);
    }
  }

  private void testIn() {
    final Condition trueCondition1 = Q.in(this.idField, 10, 11);
    assertConditionTrue(trueCondition1, this.record);
    final Condition trueCondition2 = Q.in(this.idField, "10");
    assertConditionTrue(trueCondition2, this.record);

    final Condition falseCondition1 = Q.in(this.idField, 11);
    assertConditionFalse(falseCondition1, this.record);
  }

  private void testIsNotNull() {
    final Condition trueCondition1 = Q.isNotNull(this.idField);
    assertConditionTrue(trueCondition1, this.record);

    final Condition falseCondition1 = Q.isNotNull(this.descriptionAttribute);
    assertConditionFalse(falseCondition1, this.record);
  }

  private void testIsNull() {
    final Condition trueCondition1 = Q.isNull(this.descriptionAttribute);
    assertConditionTrue(trueCondition1, this.record);

    final Condition falseCondition1 = Q.isNull(this.idField);
    assertConditionFalse(falseCondition1, this.record);
  }

  private void testLessThan() {
    final Condition trueCondition1 = Q.lessThan(this.idField, 11);
    assertConditionTrue(trueCondition1, this.record);
    final Condition trueCondition2 = Q.lessThan(this.idField, "11");
    assertConditionTrue(trueCondition2, this.record);

    final Condition falseCondition1 = Q.lessThan(this.idField, 10);
    assertConditionFalse(falseCondition1, this.record);
  }

  private void testLessThanEqual() {
    final Condition trueCondition1 = Q.lessThanEqual(this.idField, 10);
    assertConditionTrue(trueCondition1, this.record);
    final Condition trueCondition2 = Q.lessThanEqual(this.idField, "10");
    assertConditionTrue(trueCondition2, this.record);

    final Condition falseCondition1 = Q.lessThanEqual(this.idField, 9);
    assertConditionFalse(falseCondition1, this.record);
  }

  @SuppressWarnings("unchecked")
  private void testLike() {
    for (final Object like : Arrays.asList(10, "%10", "10%", "%10%", "%1%", "%0%")) {
      final Condition trueCondition = Q.like(this.idField, like);
      assertConditionTrue(trueCondition, this.record);
    }
    for (final String like : Arrays.asList("%foobar", "foobar%", "%foobar%", "%foo%", "%bar%",
      "%o%b%")) {
      final Condition trueCondition = Q.like(this.nameAttribute, like);
      assertConditionTrue(trueCondition, this.record);
    }

    for (final String like : Arrays.asList("%Foobar", "Foobar%", "%Foobar%", "%Foo%", "%Bar%",
      "%O%b%")) {
      final Condition falseCondition = Q.like(this.nameAttribute, like);
      assertConditionFalse(falseCondition, this.record);
    }
  }

  private void testNot() {
    final Condition trueCondition1 = Q.not(Q.equal(this.idField, 11));
    assertConditionTrue(trueCondition1, this.record);

    final Condition falseCondition1 = Q.not(Q.equal(this.idField, 10));
    assertConditionFalse(falseCondition1, this.record);
  }

  private void testNotEqual() {
    final Condition trueCondition1 = Q.notEqual(this.idField, 11);
    assertConditionTrue(trueCondition1, this.record);
    final Condition trueCondition2 = Q.notEqual(this.idField, "11");
    assertConditionTrue(trueCondition2, this.record);

    final Condition falseCondition1 = Q.notEqual(this.idField, 10);
    assertConditionFalse(falseCondition1, this.record);
  }

  private void testOr() {
    final Condition trueCondition1 = Q.or(Q.equal(this.idField, 10));
    assertConditionTrue(trueCondition1, this.record);
    final Condition trueCondition2 = Q.or(Q.equal(this.idField, 11),
      Q.equal(this.nameAttribute, "foobar"));
    assertConditionTrue(trueCondition2, this.record);

    final Condition falseCondition1 = Q.or(Q.equal(this.idField, 11),
      Q.equal(this.nameAttribute, "foobar1"));
    assertConditionFalse(falseCondition1, this.record);
  }
}
