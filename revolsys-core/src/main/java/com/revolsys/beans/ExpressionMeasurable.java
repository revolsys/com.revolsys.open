package com.revolsys.beans;

import static javax.measure.Quantity.Scale.ABSOLUTE;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlExpression;

import com.revolsys.util.JexlUtil;

import tech.units.indriya.AbstractQuantity;
import tech.units.indriya.ComparableQuantity;
import tech.units.indriya.internal.function.Calculator;
import tech.units.indriya.internal.function.ScaleHelper;
import tech.units.indriya.quantity.Quantities;

public class ExpressionMeasurable<Q extends Quantity<Q>> extends AbstractQuantity<Q> {

  private static final long serialVersionUID = 1L;

  private JexlContext context;

  private final JexlExpression expression;

  protected ExpressionMeasurable(final JexlExpression expression, final JexlContext context,
    final Unit<Q> unit) {
    super(unit, ABSOLUTE);
    this.expression = expression;
    this.context = context;
  }

  public ExpressionMeasurable(final String expression, final Unit<Q> unit) {
    super(unit, ABSOLUTE);
    try {
      this.expression = JexlUtil.newExpression(expression);
    } catch (final Exception e) {
      throw new IllegalArgumentException("Expression " + expression + " is not valid", e);
    }
  }

  @Override
  public ComparableQuantity<Q> add(final Quantity<Q> that) {
    return ScaleHelper.addition(this, that,
      (thisValue, thatValue) -> Calculator.of(thisValue).add(thatValue).peek());
  }

  @Override
  public ComparableQuantity<Q> divide(final Number divisor) {
    return ScaleHelper.scalarMultiplication(this,
      thisValue -> Calculator.of(thisValue).divide(divisor).peek());
  }

  @Override
  public ComparableQuantity<?> divide(final Quantity<?> that) {
    return ScaleHelper.multiplication(this, that,
      (thisValue, thatValue) -> Calculator.of(thisValue).divide(thatValue).peek(), Unit::divide);
  }

  @Override
  public Double getValue() {
    if (this.expression == null) {
      return Double.NaN;
    } else {
      try {
        return Double
          .valueOf(JexlUtil.evaluateExpression(this.context, this.expression).toString());
      } catch (final NullPointerException e) {
        return 0.0;
      }
    }
  }

  @Override
  public ComparableQuantity<?> inverse() {
    final Number resultValueInThisUnit = Calculator.of(getValue()).reciprocal().peek();
    return Quantities.getQuantity(resultValueInThisUnit, getUnit().inverse(), getScale());
  }

  @Override
  public ComparableQuantity<Q> multiply(final Number factor) {
    return ScaleHelper.scalarMultiplication(this,
      thisValue -> Calculator.of(thisValue).multiply(factor).peek());
  }

  @Override
  public ComparableQuantity<?> multiply(final Quantity<?> that) {
    return ScaleHelper.multiplication(this, that,
      (thisValue, thatValue) -> Calculator.of(thisValue).multiply(thatValue).peek(),
      Unit::multiply);
  }

  @Override
  public Quantity<Q> negate() {
    final Number resultValueInThisUnit = Calculator.of(getValue()).negate().peek();
    return Quantities.getQuantity(resultValueInThisUnit, getUnit(), getScale());
  }

  @Override
  public ComparableQuantity<Q> subtract(final Quantity<Q> that) {
    return ScaleHelper.addition(this, that,
      (thisValue, thatValue) -> Calculator.of(thisValue).subtract(thatValue).peek());
  }

}
