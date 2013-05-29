package com.revolsys.swing.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.springframework.util.StringUtils;

import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.field.Field;
import com.revolsys.util.CaseConverter;

@SuppressWarnings("serial")
public class ValueField<V> extends JPanel implements Field<V> {
  private String title;

  private V fieldValue;

  private String fieldName;

  @Override
  public String getFieldName() {
    return fieldName;
  }

  public ValueField(String fieldName, V fieldValue) {
    setFieldName(fieldName);
    setFieldValue(fieldValue);
    setTitle(CaseConverter.toCapitalizedWords(fieldName));
  }

  public ValueField(V fieldValue) {
    this(null, fieldValue);
  }

  public ValueField() {
  }

  public ValueField(final boolean isDoubleBuffered) {
    super(isDoubleBuffered);
  }

  public ValueField(final LayoutManager layout) {
    super(layout);
  }

  public ValueField(final LayoutManager layout, final boolean isDoubleBuffered) {
    super(layout, isDoubleBuffered);
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public void cancel() {
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void cancel(final JDialog dialog) {
    cancel();
    dialog.setVisible(false);
  }

  @SuppressWarnings("unchecked")
  public <T> T getFieldValue() {
    return (T)fieldValue;
  }

  public void save() {
    save(this);
  }

  private void save(final Container container) {
    final Component[] components = container.getComponents();
    for (final Component component : components) {
      if (component instanceof ValueField<?>) {
        final ValueField<?> valuePanel = (ValueField<?>)component;
        valuePanel.save();
      } else if (component instanceof Container) {
        final Container childContainer = (Container)component;
        save(childContainer);
      }

    }
  }

  public void save(final JDialog dialog) {
    save();
    dialog.setVisible(false);
  }

  public void setFieldValue(final V value) {
    final V oldValue = this.fieldValue;
    this.fieldValue = value;
    firePropertyChange("fieldValue", oldValue, value);
    if (StringUtils.hasText(fieldName)) {
      firePropertyChange(fieldName, oldValue, value);
    }
  }

  public V showDialog(final Component component) {
    Window window;
    if (component == null) {
      window = null;
    } else if (component instanceof Window) {
      window = (Window)component;

    } else {
      window = SwingUtilities.windowForComponent(component);
    }
    final JDialog dialog = new JDialog(window, title,
      ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setLayout(new BorderLayout());

    dialog.add(this, BorderLayout.CENTER);

    final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(InvokeMethodAction.createButton("Cancel", this, "cancel",
      dialog));
    buttons.add(InvokeMethodAction.createButton("OK", this, "save", dialog));
    dialog.add(buttons, BorderLayout.SOUTH);

    dialog.pack();
    dialog.setVisible(true);

    return getFieldValue();
  }

  public String getTitle() {
    return title;
  }
}
