package com.revolsys.swing.undo;

import java.util.List;

@SuppressWarnings({
  "unchecked", "rawtypes"
})
public class ListAddUndo extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1;

  private int index;

  private final List list;

  private final Object value;

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
      if (this.index == -1 || this.list.indexOf(this.value) != this.index) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      if (this.index > -1 && this.index < this.list.size()
        && this.list.get(this.index) == this.value) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void redoDo() {
    if (this.index == -1) {
      this.index = this.list.size();
    }
    this.list.add(this.index, this.value);
  }

  @Override
  protected void undoDo() {
    this.list.remove(this.value);
  }
}
