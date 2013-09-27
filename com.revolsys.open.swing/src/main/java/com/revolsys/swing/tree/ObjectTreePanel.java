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

public class ObjectTreePanel extends JPanel {
  private static final long serialVersionUID = 1L;

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

    this.tree = new ObjectTree(model);
    this.tree.setMenuEnabled(menuEnabled);
    this.tree.setRootVisible(false);
    this.tree.setShowsRootHandles(true);
    this.tree.setLargeModel(true);
    final JScrollPane scrollPane = new JScrollPane(this.tree);
    add(scrollPane, BorderLayout.CENTER);

    if (object instanceof PropertyChangeSupportProxy) {
      final PropertyChangeSupportProxy propertyChangeSupportProxy = (PropertyChangeSupportProxy)object;
      this.propertyChangeSupport = propertyChangeSupportProxy.getPropertyChangeSupport();

      this.listeners.add(model);
      final InvokeMethodPropertyChangeListener repaintListener = new InvokeMethodPropertyChangeListener(
        this.tree, "repaint");
      this.listeners.add(repaintListener);

      for (final PropertyChangeListener listener : this.listeners) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
      }
    }
  }

  public ObjectTreePanel(final Object object,
    final ObjectTreeNodeModel<?, ?>... objectTreeNodeModels) {
    this(object, true, objectTreeNodeModels);
  }

  @Override
  protected void finalize() throws Throwable {
    for (final PropertyChangeListener listener : this.listeners) {
      this.propertyChangeSupport.removePropertyChangeListener(listener);
    }
  }

  public ObjectTree getTree() {
    return this.tree;
  }

  public ObjectTreeModel getTreeModel() {
    return this.tree.getModel();
  }

  public void setSelectionMode(final int mode) {
    final TreeSelectionModel selectionModel = this.tree.getSelectionModel();
    selectionModel.setSelectionMode(mode);
  }

}
