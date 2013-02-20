package com.revolsys.swing.tree;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.TreeSelectionModel;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.swing.listener.InvokeMethodPropertyChangeListener;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.ObjectTreeNodeModel;

@SuppressWarnings("serial")
public class ObjectTreePanel extends JPanel {
  private final List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

  private PropertyChangeSupport propertyChangeSupport;

  private final ObjectTree tree;

  public ObjectTreePanel(final Object object, final boolean menuEnabled,
    final ObjectTreeNodeModel<?, ?>... objectTreeNodeModels) {
    super(new BorderLayout());
    final ObjectTreeModel model = new ObjectTreeModel(object);
    for (final ObjectTreeNodeModel<?, ?> objectTreeNodeModel : objectTreeNodeModels) {
      model.addNodeModel(objectTreeNodeModel);
    }

    tree = new ObjectTree(model);
    tree.setMenuEnabled(menuEnabled);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setLargeModel(true);
    final JScrollPane scrollPane = new JScrollPane(tree);
    add(scrollPane, BorderLayout.CENTER);

    if (object instanceof PropertyChangeSupportProxy) {
      final PropertyChangeSupportProxy propertyChangeSupportProxy = (PropertyChangeSupportProxy)object;
      propertyChangeSupport = propertyChangeSupportProxy.getPropertyChangeSupport();

      listeners.add(model);
      final InvokeMethodPropertyChangeListener repaintListener = new InvokeMethodPropertyChangeListener(
        tree, "repaint");
      listeners.add(repaintListener);

      for (final PropertyChangeListener listener : listeners) {
        propertyChangeSupport.addPropertyChangeListener(listener);
      }
    }
  }

  public ObjectTreePanel(final Object object,
    final ObjectTreeNodeModel<?, ?>... objectTreeNodeModels) {
    this(object, true, objectTreeNodeModels);
  }

  @Override
  protected void finalize() throws Throwable {
    for (final PropertyChangeListener listener : listeners) {
      propertyChangeSupport.removePropertyChangeListener(listener);
    }
  }

  public ObjectTree getTree() {
    return tree;
  }

  public ObjectTreeModel getTreeModel() {
    return (ObjectTreeModel)tree.getModel();
  }

  public void setSelectionMode(final int mode) {
    final TreeSelectionModel selectionModel = tree.getSelectionModel();
    selectionModel.setSelectionMode(mode);
  }

}
