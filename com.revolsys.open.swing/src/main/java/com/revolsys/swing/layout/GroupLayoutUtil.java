package com.revolsys.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

public class GroupLayoutUtil {
  public static void makeColumns(final Container container, final int numColumns) {
    LayoutManager layout = container.getLayout();
    if (layout instanceof GroupLayout) {
      GroupLayout groupLayout = (GroupLayout)layout;

      int componentCount = container.getComponentCount();
      int numRows = (int)Math.ceil(componentCount / (double)numColumns);

      SequentialGroup horizontalGroup = groupLayout.createSequentialGroup();
      groupLayout.setHorizontalGroup(horizontalGroup);
      for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
        ParallelGroup columnGroup = groupLayout.createParallelGroup(Alignment.LEADING);
        horizontalGroup.addGroup(columnGroup);
        for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
          int componentIndex = rowIndex * numColumns + columnIndex;
          if (componentIndex < componentCount) {
            Component component = container.getComponent(componentIndex);
            columnGroup.addComponent(component);
          }
        }
      }

      SequentialGroup verticalGroup = groupLayout.createSequentialGroup();
      groupLayout.setVerticalGroup(verticalGroup);
      for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
        ParallelGroup rowGroup = groupLayout.createParallelGroup(Alignment.BASELINE);
        verticalGroup.addGroup(rowGroup);
        for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
          int componentIndex = rowIndex * numColumns + columnIndex;
          if (componentIndex < componentCount) {
            Component component = container.getComponent(componentIndex);
            rowGroup.addComponent(component);
          }
        }
      }
    } else {
      throw new IllegalArgumentException("Expecting a " + GroupLayout.class
        + " not " + layout.getClass());
    }
  }

}
