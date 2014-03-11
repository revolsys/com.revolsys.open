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
import com.revolsys.swing.listener.InvokeMethodListener;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.ObjectTreeNodeModel;
import com.revolsys.util.Property;

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
      final InvokeMethodListener repaintListener = new InvokeMethodListener(
        this.tree, "repaint");
      this.listeners.add(repaintListener);

      for (final PropertyChangeListener listener : this.listeners) {
        Property.addListener(object, listener);
      }
    }
  }

  public ObjectTreePanel(final Object object,
    final ObjectTreeNodeModel<?, ?>... objectTreeNodeModels) {
    this(object, true, objectTreeNodeModels);
  }

  public void destroy() {
    if (propertyChangeSupport != null) {
      Property.removeAllListeners(propertyChangeSupport);
      for (final PropertyChangeListener listener : this.listeners) {
        Property.removeListener(this.propertyChangeSupport, listener);
      }
      propertyChangeSupport = null;
    }
    listeners.clear();
    tree.setRoot(null);
  }

  @Override
  protected void finalize() throws Throwable {
    for (final PropertyChangeListener listener : this.listeners) {
      Property.removeListener(this.propertyChangeSupport, listener);
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
