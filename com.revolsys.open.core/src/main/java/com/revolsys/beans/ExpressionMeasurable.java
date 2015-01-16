package com.revolsys.beans;

import java.math.BigDecimal;
import java.math.MathContext;

import javax.measure.Measure;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlContext;

import com.revolsys.util.JexlUtil;

public class ExpressionMeasurable<Q extends Quantity> extends Measure<Q> {
  private static final long serialVersionUID = 1L;

  private final Expression expression;

  private final Unit<Q> unit;

  private JexlContext context;

  protected ExpressionMeasurable(final Expression expression,
    final JexlContext context, final Unit<Q> unit) {
    this.expression = expression;
    this.context = context;
    this.unit = unit;
  }

  public ExpressionMeasurable(final String expression, final Unit<Q> unit) {
    try {
      this.expression = JexlUtil.createExpression(expression);
    } catch (final Exception e) {
      throw new IllegalArgumentException("Expression " + expression
        + " is not valid", e);
    }
    this.unit = unit;
  }

  @Override
  public BigDecimal decimalValue(final Unit<Q> arg0, final MathContext arg1)
      throws ArithmeticException {
    throw new UnsupportedOperationException();
  }

  @Override
  public double doubleValue(final Unit<Q> unit) {
    final Double value = getValue();
    if (unit == this.unit || this.unit.equals(unit)) {
      return value;
    } else {
      final UnitConverter unitConverter = this.unit.getConverterTo(unit);
      return unitConverter.convert(value);
    }
  }

  @Override
  public Unit<Q> getUnit() {
    return this.unit;
  }

  @Override
  public Double getValue() {
    if (this.expression == null) {
      return Double.NaN;
    } else {
      try {
        return Double.valueOf(JexlUtil.evaluateExpression(this.context, this.expression)
          .toString());
      } catch (final NullPointerException e) {
        return 0.0;
      }
    }
  }

  public void setContext(final JexlContext context) {
    this.context = context;
  }

  @Override
  public Measure to(final Unit<Q> unit) {
    if (unit == this.unit || this.unit.equals(unit)) {
      return this;
    } else {
      final UnitConverter unitConverter = this.unit.getConverterTo(unit);
      final Unit<Q> transformedUnit = this.unit.transform(unitConverter);
      return new ExpressionMeasurable<Q>(this.expression, this.context, transformedUnit);
    }
  }
}
