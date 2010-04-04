package com.revolsys.jump.feature.filter;

import com.revolsys.jump.feature.filter.operator.Operator;

public class NameValue implements Cloneable {
  private String name;

  private Operator operator;

  private Object value;

  public NameValue() {
  }

  public NameValue(final String name, final Operator operator,
    final Object value) {
    this.name = name;
    this.operator = operator;
    this.value = value;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * @return the operator
   */
  public Operator getOperator() {
    return operator;
  }

  /**
   * @param operator the operator to set
   */
  public void setOperator(final Operator operator) {
    this.operator = operator;
  }

  /**
   * @return the value
   */
  public Object getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(final Object value) {
    this.value = value;
  }

  public Object clone() {
    return new NameValue(name, operator, value);
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (name != null) {
      result = prime * result + name.hashCode();
    }
    if (value != null) {
      result = prime * result + value.hashCode();
    }
    return result;
  }

  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final NameValue other = (NameValue)obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }

  public String toString() {
    return name + operator + value;
  }
}
