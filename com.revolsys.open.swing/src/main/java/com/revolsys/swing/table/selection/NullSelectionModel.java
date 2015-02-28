package com.revolsys.swing.table.selection;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

public class NullSelectionModel implements ListSelectionModel {

  public static final NullSelectionModel INSTANCE = new NullSelectionModel();

  @Override
  public void addListSelectionListener(final ListSelectionListener lsl) {
  }

  @Override
  public void addSelectionInterval(final int index0, final int index1) {
  }

  @Override
  public void clearSelection() {
  }

  @Override
  public int getAnchorSelectionIndex() {
    return -1;
  }

  @Override
  public int getLeadSelectionIndex() {
    return -1;
  }

  @Override
  public int getMaxSelectionIndex() {
    return -1;
  }

  @Override
  public int getMinSelectionIndex() {
    return -1;
  }

  @Override
  public int getSelectionMode() {
    return SINGLE_SELECTION;
  }

  @Override
  public boolean getValueIsAdjusting() {
    return false;
  }

  @Override
  public void insertIndexInterval(final int index, final int length, final boolean before) {
  }

  @Override
  public boolean isSelectedIndex(final int index) {
    return false;
  }

  @Override
  public boolean isSelectionEmpty() {
    return true;
  }

  @Override
  public void removeIndexInterval(final int index0, final int index1) {
  }

  @Override
  public void removeListSelectionListener(final ListSelectionListener lsl) {
  }

  @Override
  public void removeSelectionInterval(final int index0, final int index1) {
  }

  @Override
  public void setAnchorSelectionIndex(final int index) {
  }

  @Override
  public void setLeadSelectionIndex(final int index) {
  }

  @Override
  public void setSelectionInterval(final int index0, final int index1) {
  }

  @Override
  public void setSelectionMode(final int selectionMode) {
  }

  @Override
  public void setValueIsAdjusting(final boolean valueIsAdjusting) {
  }
}
