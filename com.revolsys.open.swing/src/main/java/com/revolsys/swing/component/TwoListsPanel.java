package com.revolsys.swing.component;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SpringLayout.Constraints;

import com.revolsys.swing.Icons;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.listener.InvokeMethodListSelectionListener;

public class TwoListsPanel<T> extends JPanel {
  /**
   *
   */
  private static final long serialVersionUID = 374118950070073549L;

  private final JList leftList;

  private final JButton moveLeftButton = new InvokeMethodAction(
    Icons.getIcon("arrow_left"), this, "moveSelectedLeft").createButton();

  private final JButton moveRightButton = new InvokeMethodAction(
    Icons.getIcon("arrow_right"), this, "moveSelectedLeft").createButton();

  private final JList rightList;

  public TwoListsPanel() {
    final SpringLayout layout = new SpringLayout();
    setLayout(layout);
    this.leftList = createList();
    final JScrollPane leftScroll = new JScrollPane(this.leftList);
    add(leftScroll);

    add(this.moveRightButton);
    // add(moveLeftButton);
    this.rightList = createList();
    final JScrollPane rightScroll = new JScrollPane(this.rightList);
    add(rightScroll);

    final Constraints panelConstraints = layout.getConstraints(this);

    final Constraints leftConstraints = layout.getConstraints(leftScroll);
    leftConstraints.setX(Spring.constant(5));
    leftConstraints.setY(Spring.constant(5));

    final Constraints moveRightConstraints = layout.getConstraints(this.moveRightButton);
    final Spring buttonLeft = Spring.sum(leftConstraints.getWidth(),
      Spring.constant(10));
    moveRightConstraints.setX(buttonLeft);
    moveRightConstraints.setY(Spring.constant(5));
    moveRightConstraints.setWidth(Spring.constant(30));

    final Constraints rightConstraints = layout.getConstraints(rightScroll);
    rightConstraints.setX(Spring.sum(buttonLeft,
      Spring.sum(moveRightConstraints.getWidth(), Spring.constant(5))));
    rightConstraints.setY(Spring.constant(5));

    final Spring maxHeight = Spring.max(leftConstraints.getHeight(),
      rightConstraints.getHeight());

    leftConstraints.setHeight(maxHeight);
    rightConstraints.setHeight(maxHeight);

    panelConstraints.setWidth(Spring.sum(
      Spring.constant(25),
      Spring.sum(moveRightConstraints.getWidth(),
        Spring.sum(leftConstraints.getWidth(), rightConstraints.getWidth()))));
    panelConstraints.setHeight(Spring.sum(Spring.constant(10), maxHeight));
    //
    // panelConstraints.setWidth(Spring.sum(Spring.constant(20),
    // Spring.width(leftScroll)));

    // layout.putConstraint(SpringLayout.NORTH, leftScroll, 5,
    // SpringLayout.NORTH, this);
    // layout.putConstraint(SpringLayout.WEST, leftScroll, 5,
    // SpringLayout.WEST, this);
    // layout.putConstraint(SpringLayout.SOUTH, leftScroll, 5,
    // SpringLayout.SOUTH, this);
    // layout.putConstraint(SpringLayout.EAST, leftScroll, 5,
    // SpringLayout.EAST, this);
    // layout.putConstraint(SpringLayout.SOUTH, leftScroll, 5,
    // SpringLayout.SOUTH, this);

    // layout.putConstraint(SpringLayout.NORTH, moveRightButton, 5,
    // SpringLayout.NORTH, this);
    // layout.putConstraint(SpringLayout.WEST, moveRightButton, 5,
    // SpringLayout.EAST, leftScroll);
    // layout.putConstraint(SpringLayout.EAST, moveRightButton, 5,
    // SpringLayout.WEST, rightScroll);
    //
    // layout.putConstraint(SpringLayout.NORTH, rightScroll, 5,
    // SpringLayout.NORTH, this);
    // layout.putConstraint(SpringLayout.EAST, rightScroll, 5,
    // SpringLayout.EAST, this);
    // layout.putConstraint(SpringLayout.SOUTH, rightScroll, 5,
    // SpringLayout.SOUTH, this);
  }

  public void addLeftItems(final T... items) {
    final DefaultListModel model = (DefaultListModel)this.leftList.getModel();
    for (final T item : items) {
      model.addElement(item);
    }
  }

  public void addRightItems(final T... items) {
    final DefaultListModel model = (DefaultListModel)this.rightList.getModel();
    for (final T item : items) {
      model.addElement(item);
    }
  }

  private JList createList() {
    final JList list = new JList(new DefaultListModel());
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    list.setVisibleRowCount(-1);
    list.setLayoutOrientation(JList.VERTICAL);
    list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    list.addListSelectionListener(new InvokeMethodListSelectionListener(this,
        "updateSelection"));
    // list.setPreferredSize(new Dimension(300, 600));
    return list;
  }

  private void moveSelected(final JList list1, final JList list2) {
    final Object[] selectedObjects = list1.getSelectedValues();
    final DefaultListModel model1 = (DefaultListModel)list1.getModel();
    final DefaultListModel model2 = (DefaultListModel)list2.getModel();
    for (final Object selectedObject : selectedObjects) {
      model1.removeElement(selectedObject);
      model2.addElement(selectedObject);
    }
  }

  public void moveSelectedLeft() {
    moveSelected(this.rightList, this.leftList);
  }

  public void moveSelectedRight() {
    moveSelected(this.rightList, this.leftList);
  }

  public void updateSelection() {
    this.moveRightButton.setEnabled(this.leftList.getSelectedIndex() != -1);
    this.moveLeftButton.setEnabled(this.rightList.getSelectedIndex() != -1);
  }

}
