package com.revolsys.swing.dnd.transferhandler;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import com.revolsys.swing.dnd.transferable.ObjectTransferable;
import com.revolsys.swing.dnd.transferable.TreePathListTransferable;

@SuppressWarnings("serial")
public class TreePathListTransferHandler extends TransferHandler {
  @Override
  public boolean canImport(final TransferSupport support) {
    if (support.isDataFlavorSupported(TreePathListTransferable.FLAVOR)) {
      final Component c = support.getComponent();
      if (c instanceof JList) {
        final JList.DropLocation loc = (JList.DropLocation)support.getDropLocation();
        final int index = loc.getIndex();
        if (index != -1) {
          final JList list = (JList)c;
          final DefaultListModel model = (DefaultListModel)list.getModel();

          try {
            final Transferable transferable = support.getTransferable();
            final Object data = transferable.getTransferData(TreePathListTransferable.FLAVOR);
            if (data instanceof ObjectTransferable) {
              final ObjectTransferable<Object[]> objectTransferable = (ObjectTransferable<Object[]>)data;
              final Object[] selectedObjects = objectTransferable.getObject();
              final Object owner = objectTransferable.getOwner();
              for (final Object object : selectedObjects) {
                return true;

              }
            }
            support.setShowDropLocation(true);
            return true;

          } catch (final Exception e) {
            return false;
          }
        }

      }
    }
    return false;
  }

  @Override
  protected Transferable createTransferable(final JComponent c) {
    if (c instanceof JList) {
      final JList list = (JList)c;
      final Object[] selectedPaths = list.getSelectedValues();
      return new ObjectTransferable<Object[]>(list, selectedPaths);
    } else {
      return null;
    }
  }

  @Override
  protected void exportDone(final JComponent c,
    final Transferable transferable, final int action) {
    try {
      final Object data = transferable.getTransferData(ObjectTransferable.FLAVOR);
      if (data instanceof ObjectTransferable) {
        final ObjectTransferable<Object[]> objectTransferable = (ObjectTransferable<Object[]>)data;
        final Object[] selectedObjects = objectTransferable.getObject();
        final Object owner = objectTransferable.getOwner();
        for (final Object object : selectedObjects) {
          if (owner != c) {
            final JList list = (JList)owner;
            final DefaultListModel model = (DefaultListModel)list.getModel();
            model.removeElement(object);
          }
        }
      }
    } catch (final UnsupportedFlavorException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public int getSourceActions(final JComponent c) {
    if (c instanceof JList) {
      return MOVE;
    } else {
      return NONE;
    }
  }

  @Override
  public boolean importData(final TransferSupport support) {
    if (support.isDataFlavorSupported(TreePathListTransferable.FLAVOR)) {
      final Component c = support.getComponent();
      if (c instanceof JList) {
        final JList.DropLocation loc = (JList.DropLocation)support.getDropLocation();
        int index = loc.getIndex();
        if (index != -1) {
          final JList list = (JList)c;
          final DefaultListModel model = (DefaultListModel)list.getModel();

          try {
            final Transferable transferable = support.getTransferable();
            final Object data = transferable.getTransferData(TreePathListTransferable.FLAVOR);
            if (data instanceof ObjectTransferable) {
              final ObjectTransferable<Object[]> objectTransferable = (ObjectTransferable<Object[]>)data;
              final Object[] selectedObjects = objectTransferable.getObject();
              final Object owner = objectTransferable.getOwner();
              for (final Object object : selectedObjects) {
                final int childIndex = model.indexOf(object);
                if (childIndex > -1) {
                  model.removeElement(object);
                }
                if (index != -1) {
                  if (childIndex > -1 && childIndex < index) {
                    index--;
                  }
                  model.add(index, object);
                  index++;
                } else {
                  model.addElement(object);
                }
              }
            }
            return true;

          } catch (final Exception e) {
            e.printStackTrace();
            return false;
          }
        }

      }
    }
    return false;
  }
}
