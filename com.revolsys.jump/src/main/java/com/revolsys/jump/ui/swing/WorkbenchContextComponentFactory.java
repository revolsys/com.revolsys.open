package com.revolsys.jump.ui.swing;

import java.lang.reflect.Constructor;

import javax.swing.Icon;
import javax.swing.JComponent;

import com.vividsolutions.jump.workbench.WorkbenchContext;

public class WorkbenchContextComponentFactory extends AbstractComponentFactory<JComponent> {
  private Class<?> componentClass;

  private WorkbenchContext context;

  private Constructor<?> constructor;

  public WorkbenchContextComponentFactory(final Class<?> componentClass,
    final WorkbenchContext context, final String name, final Icon icon,
    final String toolTip) {
    super(name, icon, toolTip);
    this.componentClass = componentClass;
    this.context = context;
    try {
      constructor = componentClass.getConstructor(new Class[] {
        WorkbenchContext.class
      });
    } catch (Throwable e) {
      throw new RuntimeException(componentClass
        + " must have a constructor with argument " + WorkbenchContext.class, e);
    }
  }

  public JComponent createComponent() {
    try {
      return (JComponent)constructor.newInstance(new Object[] {
        context
      });
    } catch (RuntimeException e) {
      throw e;
    } catch (Error e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Unable to construct " + componentClass, e);
    }
  }

}
