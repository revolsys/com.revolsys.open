package com.revolsys.jump.feature.filter;

import javax.swing.JComboBox;

import com.revolsys.jump.feature.filter.operator.EqualsOperator;
import com.revolsys.jump.feature.filter.operator.NotEqualsOperator;

@SuppressWarnings("serial")
public class OperatorComboBox extends JComboBox {
  public OperatorComboBox() {
    addItem(new EqualsOperator());
    addItem(new NotEqualsOperator());
  }
}
