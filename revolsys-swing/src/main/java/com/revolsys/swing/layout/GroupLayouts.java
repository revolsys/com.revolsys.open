package com.revolsys.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;

public class GroupLayouts {
  public static GroupLayout getLayout(final Container container, final boolean containerGaps) {
    LayoutManager layout = container.getLayout();
    if (!(layout instanceof GroupLayout)) {
      layout = new GroupLayout(container);
      container.setLayout(layout);
    }
    final GroupLayout groupLayout = (GroupLayout)layout;
    groupLayout.setAutoCreateContainerGaps(containerGaps);
    groupLayout.setAutoCreateGaps(true);
    groupLayout.setLayoutStyle(BaseLayoutStyle.INSTANCE);
    return groupLayout;
  }

  public static void makeColumns(final Container container, final boolean containerGaps) {
    final GroupLayout groupLayout = getLayout(container, containerGaps);

    makeColumns(container, groupLayout, container.getComponentCount());
  }

  public static void makeColumns(final Container container, final GroupLayout groupLayout,
    final int columnCount) {
    final int componentCount = container.getComponentCount();
    final int numRows = (int)Math.ceil(componentCount / (double)columnCount);

    final SequentialGroup horizontalGroup = groupLayout.createSequentialGroup();
    groupLayout.setHorizontalGroup(horizontalGroup);
    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
      final ParallelGroup columnGroup = groupLayout.createParallelGroup(Alignment.LEADING);
      horizontalGroup.addGroup(columnGroup);
      for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
        final int componentIndex = rowIndex * columnCount + columnIndex;
        if (componentIndex < componentCount) {
          final Component component = container.getComponent(componentIndex);
          columnGroup.addComponent(component, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
            GroupLayout.PREFERRED_SIZE);
        }
      }
    }

    final SequentialGroup verticalGroup = groupLayout.createSequentialGroup();
    groupLayout.setVerticalGroup(verticalGroup);
    for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
      final ParallelGroup rowGroup = groupLayout.createParallelGroup(Alignment.BASELINE);
      verticalGroup.addGroup(rowGroup);
      for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
        final int componentIndex = rowIndex * columnCount + columnIndex;
        if (componentIndex < componentCount) {
          final Component component = container.getComponent(componentIndex);
          rowGroup.addComponent(component, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
            GroupLayout.PREFERRED_SIZE);
        }
      }
    }
  }

  public static void makeColumns(final Container container, final int columnCount,
    final boolean containerGaps) {
    final GroupLayout groupLayout = getLayout(container, containerGaps);

    makeColumns(container, groupLayout, columnCount);
  }

  public static void makeColumns(final Container container, final int columnCount,
    final boolean containerGaps, final boolean gaps) {
    final GroupLayout groupLayout = getLayout(container, containerGaps);
    groupLayout.setAutoCreateContainerGaps(containerGaps);
    groupLayout.setAutoCreateGaps(gaps);
    groupLayout.setLayoutStyle(LayoutStyle.getInstance());
    makeColumns(container, groupLayout, columnCount);
  }

  public static void makeColumns(final LayoutStyle layoutStyle, final Container container,
    final int columnCount) {
    final GroupLayout groupLayout = getLayout(container, true);
    groupLayout.setAutoCreateContainerGaps(false);
    groupLayout.setAutoCreateGaps(true);
    groupLayout.setLayoutStyle(layoutStyle);
    makeColumns(container, groupLayout, columnCount);
  }

  public static JPanel panelColumns(final Component... components) {
    final JPanel panel = new JPanel();
    panel.setOpaque(false);
    for (final Component component : components) {
      panel.add(component);
    }
    makeColumns(panel, components.length, false, true);
    return panel;
  }

  public static ParallelGroup parallel(final GroupLayout layout, final Alignment alignment,
    final Object... elements) {
    final ParallelGroup group = layout.createParallelGroup(alignment);
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

  public static ParallelGroup parallel(final GroupLayout layout, final Object... elements) {
    final ParallelGroup group = layout.createParallelGroup();
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

  public static SequentialGroup sequential(final GroupLayout layout, final Object... elements) {
    final SequentialGroup group = layout.createSequentialGroup();
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

}
