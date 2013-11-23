package com.revolsys.swing.table.selection;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

public class NullSelectionModel implements ListSelectionModel {
  public void addListSelectionListener(final ListSelectionListener lsl) {
  }

  public void addSelectionInterval(final int index0, final int index1) {
  }

  public void clearSelection() {
  }

  public int getAnchorSelectionIndex() {
    return -1;
  }

  public int getLeadSelectionIndex() {
    return -1;
  }

  public int getMaxSelectionIndex() {
    return -1;
  }

  public int getMinSelectionIndex() {
    return -1;
  }

  public int getSelectionMode() {
    return SINGLE_SELECTION;
  }

  public boolean getValueIsAdjusting() {
    return false;
  }

  public void insertIndexInterval(final int index, final int length,
    final boolean before) {
  }

  public boolean isSelectedIndex(final int index) {
    return false;
  }

  public boolean isSelectionEmpty() {
    return true;
  }

  public void removeIndexInterval(final int index0, final int index1) {
  }

  public void removeListSelectionListener(final ListSelectionListener lsl) {
  }

  public void removeSelectionInterval(final int index0, final int index1) {
  }

  public void setAnchorSelectionIndex(final int index) {
  }

  public void setLeadSelectionIndex(final int index) {
  }

  public void setSelectionInterval(final int index0, final int index1) {
  }

  public void setSelectionMode(final int selectionMode) {
  }

  public void setValueIsAdjusting(final boolean valueIsAdjusting) {
  }
}
