package com.revolsys.beans;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlExpression;

import com.revolsys.util.JexlUtil;

import tec.uom.se.AbstractConverter;
import tec.uom.se.AbstractQuantity;
import tec.uom.se.ComparableQuantity;
import tec.uom.se.quantity.Quantities;

public class ExpressionMeasurable<Q extends Quantity<Q>> extends AbstractQuantity<Q> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private JexlContext context;

  private final JexlExpression expression;

  protected ExpressionMeasurable(final JexlExpression expression, final JexlContext context,
    final Unit<Q> unit) {
    super(unit);
    this.expression = expression;
    this.context = context;
  }

  public ExpressionMeasurable(final String expression, final Unit<Q> unit) {
    super(unit);
    try {
      this.expression = JexlUtil.newExpression(expression);
    } catch (final Exception e) {
      throw new IllegalArgumentException("Expression " + expression + " is not valid", e);
    }
  }

  @Override
  public ComparableQuantity<Q> add(final Quantity<Q> that) {
    final double value = getValue();
    if (getUnit().equals(that.getUnit())) {
      return Quantities.getQuantity(value + that.getValue().doubleValue(), getUnit());
    }
    final Quantity<Q> converted = that.to(getUnit());
    return Quantities.getQuantity(value + converted.getValue().doubleValue(), getUnit());
  }

  @Override
  public BigDecimal decimalValue(final Unit<Q> unit, final MathContext ctx)
    throws ArithmeticException {
    final double value = getValue();
    final BigDecimal decimal = BigDecimal.valueOf(value); // TODO check value if
    // it is a BD, otherwise
    // use different
    // converter
    return super.getUnit().equals(unit) ? decimal
      : ((AbstractConverter)super.getUnit().getConverterTo(unit)).convert(decimal, ctx);
  }

  @Override
  public ComparableQuantity<Q> divide(final Number that) {
    final double value = getValue();
    return Quantities.getQuantity(value / that.doubleValue(), getUnit());
  }

  @Override
  public ComparableQuantity<?> divide(final Quantity<?> that) {
    final double value = getValue();
    return Quantities.getQuantity(value / that.getValue().doubleValue(),
      getUnit().divide(that.getUnit()));
  }

  @Override
  public double doubleValue(final Unit<Q> unit) {
    final double value = getValue();
    return super.getUnit().equals(unit) ? value
      : super.getUnit().getConverterTo(unit).convert(value);
  }

  @Override
  public boolean equals(final Object obj) {
    final double value = getValue();
    if (this == obj) {
      return true;
    }
    if (obj instanceof Quantity<?>) {
      final Quantity<?> that = (Quantity<?>)obj;
      return Objects.equals(getUnit(), that.getUnit())
        && Equalizer.hasEquality(value, that.getValue());
    }
    return false;
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
  public AbstractQuantity<Q> inverse() {
    final double value = getValue();
    return (AbstractQuantity<Q>)Quantities.getQuantity(1d / value, getUnit().inverse());
  }

  @Override
  public boolean isBig() {
    return false;
  }

  @Override
  public long longValue(final Unit<Q> unit) {
    final double result = doubleValue(unit);
    if (result < Long.MIN_VALUE || result > Long.MAX_VALUE) {
      throw new ArithmeticException("Overflow (" + result + ")");
    }
    return (long)result;
  }

  @Override
  public ComparableQuantity<Q> multiply(final Number that) {
    final double value = getValue();
    return Quantities.getQuantity(value * that.doubleValue(), getUnit());
  }

  @Override
  public ComparableQuantity<?> multiply(final Quantity<?> that) {
    final double value = getValue();
    return Quantities.getQuantity(value * that.getValue().doubleValue(),
      getUnit().multiply(that.getUnit()));
  }

  @Override
  public ComparableQuantity<Q> subtract(final Quantity<Q> that) {
    final double value = getValue();
    if (getUnit().equals(that.getUnit())) {
      return Quantities.getQuantity(value - that.getValue().doubleValue(), getUnit());
    }
    final Quantity<Q> converted = that.to(getUnit());
    return Quantities.getQuantity(value - converted.getValue().doubleValue(), getUnit());
  }
}
