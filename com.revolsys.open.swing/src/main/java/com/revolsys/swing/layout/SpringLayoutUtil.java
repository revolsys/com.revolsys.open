package com.revolsys.swing.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.awt.MenuContainer;
import java.awt.MenuItem;
import java.awt.Window;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SpringLayout.Constraints;
import javax.swing.SwingUtilities;

public class SpringLayoutUtil {
  /* Used by makeCompactGrid. */
  private static SpringLayout.Constraints getConstraintsForCell(final int row, final int col,
    final Container parent, final int cols) {
    final SpringLayout layout = (SpringLayout)parent.getLayout();
    final Component c = parent.getComponent(row * cols + col);
    return layout.getConstraints(c);
  }

  public static Window getWindow(final Object source) {

    if (source instanceof JMenuItem) {
      final JMenuItem menuItem = (JMenuItem)source;
      MenuContainer menuContainer = menuItem.getParent();
      while (menuContainer != null && !(menuContainer instanceof JPopupMenu)) {
        if (menuContainer instanceof MenuItem) {
          menuContainer = ((MenuItem)menuContainer).getParent();
        } else {
          menuContainer = null;
        }
      }
      if (menuContainer != null) {
        final JPopupMenu menu = (JPopupMenu)menuContainer;
        final Component invoker = menu.getInvoker();
        return SwingUtilities.getWindowAncestor(invoker);
      }

    } else if (source instanceof Component) {
      return SwingUtilities.getWindowAncestor((Component)source);
    }
    return null;
  }

  public static void makeColumns(final Container container, final int numColumns,
    final int initialX, final int initialY, final int xPad, final int yPad) {
    final Spring xPadSpring = Spring.constant(xPad);
    final Spring yPadSpring = Spring.constant(yPad);

    LayoutManager layout = container.getLayout();
    if (!(layout instanceof SpringLayout)) {
      layout = new SpringLayout();
      container.setLayout(layout);
    }
    final SpringLayout springLayout = (SpringLayout)layout;
    final Component[] components = container.getComponents();
    if (components.length > 0) {
      final Spring[] columnWidths = new Spring[numColumns];
      for (int i = 0; i < columnWidths.length; i++) {
        columnWidths[i] = Spring.constant(0);
      }
      final int numRows = (int)Math.ceil((double)components.length / numColumns);
      final Spring[] rowHeights = new Spring[numRows];
      for (int i = 0; i < rowHeights.length; i++) {
        rowHeights[i] = Spring.constant(0);
      }
      for (int i = 0; i < components.length; i++) {
        final Component component = components[i];
        final Constraints componentConstraints = springLayout.getConstraints(component);
        final Spring width = componentConstraints.getWidth();
        final Spring height = componentConstraints.getHeight();
        final int column = i % numColumns;
        final int row = (int)Math.floor((double)i / numColumns);
        columnWidths[column] = Spring.max(columnWidths[column], width);
        rowHeights[row] = Spring.max(rowHeights[row], height);
      }
      final Spring[] columnX = new Spring[numColumns];
      final Spring initialXSpring = Spring.constant(initialX);
      columnX[0] = initialXSpring;
      for (int i = 1; i < columnX.length; i++) {
        columnX[i] = Spring.sum(columnX[i - 1], Spring.sum(xPadSpring, columnWidths[i - 1]));
      }
      final Spring[] rowY = new Spring[numRows];
      final Spring initialYSpring = Spring.constant(initialY);
      rowY[0] = initialYSpring;
      for (int i = 1; i < rowY.length; i++) {
        rowY[i] = Spring.sum(rowY[i - 1], Spring.sum(yPadSpring, rowHeights[i - 1]));
      }
      final Constraints containerConstraints = springLayout.getConstraints(container);
      containerConstraints.setWidth(Spring.sum(columnX[numColumns - 1],
        Spring.sum(columnWidths[numColumns - 1], initialXSpring)));
      containerConstraints.setHeight(
        Spring.sum(rowY[numRows - 1], Spring.sum(rowHeights[numRows - 1], initialYSpring)));

      for (int i = 0; i < components.length; i++) {
        final Component component = components[i];
        final Constraints componentConstraints = springLayout.getConstraints(component);
        final int column = i % numColumns;
        final int row = (int)Math.floor((double)i / numColumns);
        componentConstraints.setX(columnX[column]);
        componentConstraints.setWidth(Spring.width(component));
        componentConstraints.setY(rowY[row]);
        componentConstraints.setHeight(Spring.height(component));
      }
    }
  }

  /**
   * Aligns the first <code>rows</code> <code>cols</code> components of
   * <code>parent</code> in a grid. Each component in a column is as wide as the
   * maximum preferred width of the components in that column; height is
   * similarly determined for each row. The parent is made just big enough to
   * fit them all.
   *
   * @param rows number of rows
   * @param cols number of columns
   * @param initialX x location to start the grid at
   * @param initialY y location to start the grid at
   * @param xPad x padding between cells
   * @param yPad y padding between cells
   */
  public static void makeCompactGrid(final Container parent, final int rows, final int cols,
    final int initialX, final int initialY, final int xPad, final int yPad) {
    SpringLayout layout;
    try {
      layout = (SpringLayout)parent.getLayout();
    } catch (final ClassCastException exc) {
      layout = new SpringLayout();
      parent.setLayout(layout);
    }

    // Align all cells in each column and make them the same width.
    Spring x = Spring.constant(initialX);
    for (int c = 0; c < cols; c++) {
      Spring width = Spring.constant(0);
      for (int r = 0; r < rows; r++) {
        width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
      }
      for (int r = 0; r < rows; r++) {
        final SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
        constraints.setX(x);
        constraints.setWidth(width);
      }
      x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
    }

    // Align all cells in each row and make them the same height.
    Spring y = Spring.constant(initialY);
    for (int r = 0; r < rows; r++) {
      Spring height = Spring.constant(0);
      for (int c = 0; c < cols; c++) {
        height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
      }
      for (int c = 0; c < cols; c++) {
        final SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
        constraints.setY(y);
        constraints.setHeight(height);
      }
      y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
    }

    // Set the parent's size.
    final SpringLayout.Constraints parenConstraints = layout.getConstraints(parent);
    parenConstraints.setConstraint(SpringLayout.SOUTH, y);
    parenConstraints.setConstraint(SpringLayout.EAST, x);
  }

  /**
   * Aligns the first <code>rows</code> <code>cols</code> components of
   * <code>parent</code> in a grid. Each component is as big as the maximum
   * preferred width and height of the components. The parent is made just big
   * enough to fit them all.
   *
   * @param rows number of rows
   * @param cols number of columns
   * @param initialX x location to start the grid at
   * @param initialY y location to start the grid at
   * @param xPad x padding between cells
   * @param yPad y padding between cells
   */
  public static void makeGrid(final Container parent, final int rows, final int cols,
    final int initialX, final int initialY, final int xPad, final int yPad) {
    LayoutManager layout = parent.getLayout();
    if (!(layout instanceof SpringLayout)) {
      layout = new SpringLayout();
      parent.setLayout(layout);
    }
    final SpringLayout springLayout = (SpringLayout)parent.getLayout();

    final Spring xPadSpring = Spring.constant(xPad);
    final Spring yPadSpring = Spring.constant(yPad);
    final Spring initialXSpring = Spring.constant(initialX);
    final Spring initialYSpring = Spring.constant(initialY);
    final int max = rows * cols;

    // Calculate Springs that are the max of the width/height so that all
    // cells have the same size.
    Spring maxWidthSpring = springLayout.getConstraints(parent.getComponent(0)).getWidth();
    Spring maxHeightSpring = springLayout.getConstraints(parent.getComponent(0)).getWidth();
    for (int i = 1; i < max; i++) {
      final SpringLayout.Constraints cons = springLayout.getConstraints(parent.getComponent(i));

      maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
      maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
    }

    // Apply the new width/height Spring. This forces all the
    // components to have the same size.
    for (int i = 0; i < max; i++) {
      final SpringLayout.Constraints cons = springLayout.getConstraints(parent.getComponent(i));

      cons.setWidth(maxWidthSpring);
      cons.setHeight(maxHeightSpring);
    }

    // Then adjust the x/y constraints of all the cells so that they
    // are aligned in a grid.
    SpringLayout.Constraints lastCons = null;
    SpringLayout.Constraints lastRowCons = null;
    for (int i = 0; i < max; i++) {
      final SpringLayout.Constraints cons = springLayout.getConstraints(parent.getComponent(i));
      if (i % cols == 0) { // start of new row
        lastRowCons = lastCons;
        cons.setX(initialXSpring);
      } else { // x position depends on previous component
        cons.setX(Spring.sum(lastCons.getConstraint(SpringLayout.EAST), xPadSpring));
      }

      if (i / cols == 0) { // first row
        cons.setY(initialYSpring);
      } else { // y position depends on previous row
        cons.setY(Spring.sum(lastRowCons.getConstraint(SpringLayout.SOUTH), yPadSpring));
      }
      lastCons = cons;
    }

    // Set the parent's size.
    final SpringLayout.Constraints pCons = springLayout.getConstraints(parent);
    pCons.setConstraint(SpringLayout.SOUTH,
      Spring.sum(Spring.constant(yPad), lastCons.getConstraint(SpringLayout.SOUTH)));
    pCons.setConstraint(SpringLayout.EAST,
      Spring.sum(Spring.constant(xPad), lastCons.getConstraint(SpringLayout.EAST)));
  }

  public static void makeRows(final Container container, final int initialX, final int initialY,
    final int xPad, final int yPad, final int... componentsPerRow) {
    final SpringLayout layout = (SpringLayout)container.getLayout();
    final Spring xPadSpring = Spring.constant(xPad);
    final Spring yPadSpring = Spring.constant(yPad);
    final Spring initialXSpring = Spring.constant(initialX);
    final Spring initialYSpring = Spring.constant(initialY);

    int i = 0;
    Spring width = null;
    final int componentCount = container.getComponentCount();
    Spring y = initialYSpring;
    for (int row = 0; row < componentsPerRow.length && i < componentCount; row++) {
      final int numComponents = componentsPerRow[row];
      Spring rowHeight = Spring.constant(0);
      if (row > 0) {
        y = Spring.sum(y, yPadSpring);
      }
      Spring x = initialXSpring;
      for (int col = 0; col < numComponents && i < componentCount; col++) {
        final Component component = container.getComponent(i);
        if (col > 0) {
          x = Spring.sum(x, xPadSpring);
        }
        final Constraints componentConstraints = layout.getConstraints(component);
        componentConstraints.setX(x);
        componentConstraints.setWidth(Spring.width(component));
        componentConstraints.setY(y);
        componentConstraints.setHeight(Spring.height(component));
        x = Spring.sum(x, Spring.width(component));
        rowHeight = Spring.max(rowHeight, Spring.height(component));
        i++;
      }
      y = Spring.sum(y, rowHeight);
      if (width == null) {
        width = x;
      } else {
        width = Spring.max(width, x);
      }
    }

    final Constraints containerConstraints = layout.getConstraints(container);
    containerConstraints.setWidth(Spring.sum(initialXSpring, width));
    containerConstraints.setHeight(Spring.sum(initialYSpring, y));
  }

  public static void singleColumn(final Container container, final int initialY, final int yPad) {
    Spring height = Spring.constant(initialY + yPad * container.getComponentCount());
    Spring width = Spring.constant(0);

    final SpringLayout layout = (SpringLayout)container.getLayout();
    Component previous = container;
    for (final Component component : container.getComponents()) {
      final Constraints constraints = layout.getConstraints(component);
      width = Spring.max(width, constraints.getWidth());
      height = Spring.sum(height, constraints.getHeight());
      if (previous == container) {
        layout.putConstraint(SpringLayout.NORTH, previous, initialY, SpringLayout.NORTH, component);
      } else {
        layout.putConstraint(SpringLayout.SOUTH, previous, yPad, SpringLayout.NORTH, component);
      }

      previous = component;
    }

    final Constraints containerConstraints = layout.getConstraints(container);
    containerConstraints.setConstraint(SpringLayout.SOUTH, height);
    containerConstraints.setConstraint(SpringLayout.EAST, width);
  }

}
