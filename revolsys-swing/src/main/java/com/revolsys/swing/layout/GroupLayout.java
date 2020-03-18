package com.revolsys.swing.layout;

import java.awt.Component;
import java.awt.Container;

public class GroupLayout extends javax.swing.GroupLayout {
  public static GroupLayout newLayout(final Container container) {
    final GroupLayout layout = new GroupLayout(container);
    container.setLayout(layout);
    return layout;
  }

  public GroupLayout(final Container container) {
    super(container);
  }

  public <G extends Group> G addElements(final G group, final Object... elements) {
    for (final Object element : elements) {
      if (element instanceof Component) {
        final Component component = (Component)element;
        group.addComponent(component);
      } else if (element instanceof Group) {
        final Group childGroup = (Group)element;
        group.addGroup(childGroup);
      }
    }
    return group;
  }

  public ParallelGroup parallel(final Alignment alignment) {
    final ParallelGroup group = createParallelGroup(alignment);

    return group;
  }

  public ParallelGroup parallel(final Alignment alignment, final Object... elements) {
    final ParallelGroup group = createParallelGroup(alignment);
    addElements(group, elements);
    return group;
  }

  public ParallelGroup parallel(final Object... elements) {
    final ParallelGroup group = createParallelGroup();
    addElements(group, elements);
    return group;
  }

  public SequentialGroup sequential(final Object... elements) {
    final SequentialGroup group = createSequentialGroup();
    addElements(group, elements);
    return group;
  }

  public void setHorizontalGroup(final Object... elements) {
    super.setHorizontalGroup(sequential(elements));
  }

  public void setVerticalGroup(final Object... elements) {
    super.setVerticalGroup(sequential(elements));
  }
}
