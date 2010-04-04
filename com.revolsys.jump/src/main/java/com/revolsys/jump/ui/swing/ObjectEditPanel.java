package com.revolsys.jump.ui.swing;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.text.JTextComponent;

import com.revolsys.jump.feature.filter.OperatorComboBox;
import com.revolsys.jump.feature.filter.operator.Operator;
import com.revolsys.jump.util.BeanUtil;

public class ObjectEditPanel extends EditPanel<Object> {
  /**
   * 
   */
  private static final long serialVersionUID = 5878456141564246789L;

  private String[] propertyNames;

  private String[] labels;

  private Map<String, Component> fields = new HashMap<String, Component>();

  public ObjectEditPanel(final String[] propertyNames) {
    this(propertyNames, propertyNames);
  }

  public ObjectEditPanel(final String[] propertyNames, final String[] labels) {
    super(new SpringLayout());
    this.propertyNames = propertyNames;
    this.labels = labels;
  }

  public void setValue(final Object value) {
    super.setValue(value);
    removeAll();
    int numFields = propertyNames.length;
    for (int i = 0; i < numFields; i++) {
      String label = labels[i];
      add(new JLabel(label));

      String propertyName = propertyNames[i];
      Method method = BeanUtil.getReadMethod(value, propertyName);
      try {
        Object propertyValue = method.invoke(value, new Object[0]);
        Class<?> valueClass = method.getReturnType();
        if (propertyValue != null) {
          valueClass = propertyValue.getClass();
        }
        Component field = getField(value, propertyName, valueClass);
        fields.put(propertyName, field);
        add(field);
      } catch (InvocationTargetException e) {
        add(new JLabel(e.getTargetException().getLocalizedMessage()));
        e.printStackTrace();
      } catch (Exception e) {
        add(new JLabel(e.getLocalizedMessage()));
        e.printStackTrace();
      }

    }
    SpringUtilities.makeCompactGrid(this, numFields, 2, 5, 5, 3, 3);
  }

  private Component getField(final Object value, final String propertyName, final Class<?> valueClass) {
    if (valueClass.equals(Operator.class)) {
      return new OperatorComboBox();
    } else {
      return new JTextField(50);
    }
  }

  public void save() {
    Object value = getValue();
    int numFields = propertyNames.length;
    for (int i = 0; i < numFields; i++) {
      String propertyName = propertyNames[i];
      Object propertyValue = null;
      JComponent field = (JComponent)fields.get(propertyName);
      if (field instanceof JTextComponent) {
        JTextComponent textField = (JTextComponent)field;
        propertyValue = textField.getText();
      } else if (field instanceof JComboBox) {
        JComboBox comboField = (JComboBox)field;
        propertyValue = comboField.getSelectedItem();
      }
      BeanUtil.setProperty(value, propertyName, propertyValue);

    }
  }
}
