package com.revolsys.swing.undo;

import java.util.List;

@SuppressWarnings({
  "unchecked", "rawtypes"
})
public class ListAddUndo extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1;

  private final List list;

  private final Object value;

  private int index;

  public ListAddUndo(final List list, final int index, final Object value) {
    super();
    this.list = list;
    this.index = index;
    this.value = value;
  }

  public ListAddUndo(final List list, final Object value) {
    this(list, -1, value);
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      if (index == -1 || list.indexOf(value) != index) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      if (index > -1 && index < list.size() && list.get(index) == value) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    if (index == -1) {
      index = list.size();
    }
    list.add(index, value);
  }

  @Override
  protected void doUndo() {
    list.remove(value);
  }
}
