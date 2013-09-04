package com.revolsys.swing.tree.model;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JMenuItem;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.revolsys.beans.ClassRegistry;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.parallel.ExecutorServiceFactory;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.model.node.ListObjectTreeNodeModel;
import com.revolsys.swing.tree.model.node.ObjectTreeNodeModel;
import com.revolsys.swing.tree.model.node.StringTreeNodeModel;
import com.revolsys.util.ExceptionUtil;

public class ObjectTreeModel implements TreeModel, TreeWillExpandListener,
  TreeExpansionListener, PropertyChangeListener {

  private static final ClassRegistry<MenuFactory> CLASS_MENUS = new ClassRegistry<MenuFactory>();

  public static MenuFactory findMenu(final Class<?> clazz) {
    synchronized (CLASS_MENUS) {
      return CLASS_MENUS.find(clazz);
    }
  }

  public static MenuFactory findMenu(final Object object) {
    final Class<?> clazz = object.getClass();
    return findMenu(clazz);
  }

  public static MenuFactory getMenu(final Class<?> layerClass) {
    if (layerClass == null) {
      return new MenuFactory();
    } else {
      synchronized (CLASS_MENUS) {
        MenuFactory menu = CLASS_MENUS.get(layerClass);
        if (menu == null) {
          final Class<?> superClass = layerClass.getSuperclass();
          final MenuFactory parentMenu = getMenu(superClass);
          menu = parentMenu.clone();
          CLASS_MENUS.put(layerClass, menu);
        }
        return menu;
      }
    }
  }

  private final ClassRegistry<ObjectTreeNodeModel<Object, Object>> classNodeModels = new ClassRegistry<ObjectTreeNodeModel<Object, Object>>();

  private final TreeEventSupport eventHandler = new TreeEventSupport();

  private final Map<TreePath, Boolean> initialized = new WeakHashMap<TreePath, Boolean>();

  private final Map<Object, MenuFactory> objectMenus = new WeakHashMap<Object, MenuFactory>();

  private final Map<Object, TreePath> objectPathMap = new WeakHashMap<Object, TreePath>();

  private Object root;

  public Map<Object, Boolean> hiddenObjects = new WeakHashMap<Object, Boolean>();

  public ObjectTreeModel() {
    addNodeModel(new StringTreeNodeModel());
    addNodeModel(new ListObjectTreeNodeModel());
  }

  public ObjectTreeModel(final Object root) {
    this();
    this.root = root;
    this.objectPathMap.put(root, new TreePath(root));
  }

  public void addMenuItem(final Class<?> clazz, final JMenuItem menuItem) {
    final MenuFactory menu = getMenu(clazz);
    menu.addMenuItem(menuItem);
  }

  public void addMenuItem(final Class<?> clazz, final String groupName,
    final JMenuItem menuItem) {
    final MenuFactory menu = getMenu(clazz);
    menu.addComponent(groupName, menuItem);
  }

  public void addNodeModel(final Class<?> clazz,
    final ObjectTreeNodeModel<?, ?> model) {
    model.setObjectTreeModel(this);
    this.classNodeModels.put(clazz, (ObjectTreeNodeModel<Object, Object>)model);
  }

  public void addNodeModel(final ObjectTreeNodeModel<?, ?> model) {
    final Set<Class<?>> supportedClasses = model.getSupportedClasses();
    for (final Class<?> supportedClass : supportedClasses) {
      addNodeModel(supportedClass, model);
    }
  }

  @Override
  public void addTreeModelListener(final TreeModelListener listener) {
    this.eventHandler.addTreeModelListener(listener);
  }

  public void fireTreeNodesChanged(final TreePath path) {
    eventHandler.treeNodesChanged(new TreeModelEvent(this.root, path));
  }

  public void fireTreeNodesInserted(final TreePath path, final int index,
    final Object newValue) {
    try {
      if (newValue != null) {
        final TreeModelEvent e = new TreeModelEvent(this.root, path, new int[] {
          index
        }, new Object[] {
          newValue
        });
        this.eventHandler.treeNodesInserted(e);
      }
    } catch (final Throwable t) {
      ExceptionUtil.log(getClass(), t);
    }
  }

  public void fireTreeNodesRemoved(final TreePath path, final int index,
    final Object newValue) {
    try {
      if (newValue != null) {
        final TreeModelEvent e = new TreeModelEvent(this.root, path, new int[] {
          index
        }, new Object[] {
          newValue
        });
        this.eventHandler.treeNodesRemoved(e);
      }
    } catch (final ArrayIndexOutOfBoundsException t) {
    } catch (final Throwable t) {
      ExceptionUtil.log(getClass(), t);
    }
  }

  @Override
  public Object getChild(final Object parent, final int index) {
    final TreePath path = getPath(parent);
    if (path == null) {
      return "";
    } else {
      final ObjectTreeNodeModel<Object, Object> nodeModel = getNodeModel(path);
      if (!isInitialized(path, nodeModel, parent)) {
        return new String("Loading...");
      } else {
        final Object child = nodeModel.getChild(parent, index);
        if (child == null) {
          return "";
        }
        return child;
      }
    }
  }

  @Override
  public int getChildCount(final Object parent) {
    final TreePath path = getPath(parent);
    if (path == null) {
      return 0;
    } else {
      final ObjectTreeNodeModel<Object, Object> nodeModel = getNodeModel(path);
      if (!isInitialized(path, nodeModel, parent)) {
        return 1;
      } else {
        return nodeModel.getChildCount(parent);
      }
    }
  }

  public int getChildIndex(final TreePath treePath) {
    final TreePath parentPath = treePath.getParentPath();
    final Object parent = parentPath.getLastPathComponent();
    final Object child = treePath.getLastPathComponent();
    return getIndexOfChild(parent, child);

  }

  @Override
  public int getIndexOfChild(final Object parent, final Object child) {
    final TreePath path = getPath(parent);
    final ObjectTreeNodeModel<Object, Object> nodeModel = getNodeModel(path);
    if (!isInitialized(path, nodeModel, parent)) {
      return -1;
    } else {
      return nodeModel.getIndexOfChild(parent, child);
    }
  }

  public MenuFactory getMenu(final Object object) {
    synchronized (this.objectMenus) {
      final MenuFactory popupMenu = this.objectMenus.get(object);
      if (popupMenu != null) {
        return popupMenu;
      }
    }
    return findMenu(object);
  }

  public ObjectTreeNodeModel<Object, Object> getNodeModel(final Class<?> clazz) {
    final ObjectTreeNodeModel<Object, Object> model = this.classNodeModels.find(clazz);
    if (model == null) {
      final Class<?> superClass = clazz.getSuperclass();
      if (superClass != null) {
        return getNodeModel(superClass);
      } else {
        return null;
      }
    } else {
      return model;
    }
  }

  public ObjectTreeNodeModel<Object, Object> getNodeModel(final Object node) {
    final Class<?> nodeClass = node.getClass();
    return getNodeModel(nodeClass);
  }

  public ObjectTreeNodeModel<Object, Object> getNodeModel(final TreePath path) {
    if (path == null) {
      return null;
    } else {
      ObjectTreeNodeModel<Object, Object> parentNodeModel = null;
      for (final Object object : path.getPath()) {
        if (parentNodeModel == null) {
          parentNodeModel = getNodeModel(object);
        } else {
          final Class<? extends Object> nodeClass = object.getClass();
          final ObjectTreeNodeModel<Object, Object> nodeModel = (ObjectTreeNodeModel<Object, Object>)parentNodeModel.getObjectTreeNodeModel(nodeClass);
          if (nodeModel == null) {
            parentNodeModel = getNodeModel(object);
            if (parentNodeModel == null) {
              return null;
            }
          } else {
            parentNodeModel = nodeModel;
          }
        }
      }
      return parentNodeModel;
    }
  }

  public MenuFactory getObjectMenu(final Object object) {
    MenuFactory menu;
    synchronized (this.objectMenus) {
      menu = this.objectMenus.get(object);
      if (menu == null) {
        menu = new MenuFactory();
        this.objectMenus.put(object, menu);
      }
    }
    return menu;
  }

  public TreePath getPath(final Object source) {
    return this.objectPathMap.get(source);
  }

  @Override
  public Object getRoot() {
    return this.root;
  }

  public void initializeNode(final TreePath path,
    final ObjectTreeNodeModel<Object, Object> model, final Object node) {
    if (model != null) {
      model.initialize(node);
      Invoke.later(this, "setNodeInitialized", path, node);
    }
  }

  public void initializePath(final TreePath path) {
    final TreePath parent = path.getParentPath();
    if (parent != null) {
      initializePath(parent);
    }
    final Object object = path.getLastPathComponent();
    synchronized (this.objectPathMap) {
      if (!this.objectPathMap.containsKey(object)) {
        this.objectPathMap.put(object, path);
      }
    }
  }

  private boolean isInitialized(final TreePath path,
    final ObjectTreeNodeModel<Object, Object> model, final Object node) {
    if (model == null) {
      return false;
    } else if (!model.isLazyLoad()) {
      return true;
    } else {
      final Boolean initialized = this.initialized.get(path);
      if (initialized == null) {
        return false;
      } else {
        return initialized;
      }
    }
  }

  @Override
  public boolean isLeaf(final Object node) {
    if (isVisible(node)) {
      final TreePath path = getPath(node);
      final ObjectTreeNodeModel<Object, Object> nodeModel = getNodeModel(path);
      if (!isInitialized(path, nodeModel, node)) {
        return false;
      } else {
        return nodeModel.isLeaf(node);
      }
    } else {
      return true;
    }
  }

  public boolean isVisible(final Object object) {
    if (BooleanStringConverter.getBoolean(hiddenObjects.get(object))) {
      return false;
    } else {
      return true;
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (event instanceof IndexedPropertyChangeEvent) {
      final IndexedPropertyChangeEvent indexedEvent = (IndexedPropertyChangeEvent)event;
      final Object parent = indexedEvent.getSource();
      final TreePath parentPath = getPath(parent);
      if (parentPath != null) {
        final Object oldValue = indexedEvent.getOldValue();
        final Object newValue = indexedEvent.getNewValue();
        if (oldValue != null) {
          final TreePath childPath = parentPath.pathByAddingChild(oldValue);
          if (EqualsRegistry.equal(childPath, this.objectPathMap.get(oldValue))) {
            this.objectPathMap.remove(oldValue);
          }
        }
        if (newValue != null) {
          this.objectPathMap.put(newValue,
            parentPath.pathByAddingChild(newValue));
        }
      }
    }
  }

  public void remove(final TreePath treePath) {
    final TreePath parentPath = treePath.getParentPath();
    final Object parent = parentPath.getLastPathComponent();
    final Object child = treePath.getLastPathComponent();
    final ObjectTreeNodeModel<Object, Object> nodeModel = getNodeModel(parentPath);
    if (nodeModel != null) {
      nodeModel.removeChild(parent, child);
    }
  }

  @Override
  public void removeTreeModelListener(final TreeModelListener listener) {
    this.eventHandler.removeTreeModelListener(listener);
  }

  public void setNodeInitialized(final TreePath path, final Object node) {
    this.initialized.put(path, true);
    final TreeModelEvent event = new TreeModelEvent(node, path);
    final ObjectTreeNodeModel<Object, Object> nodeModel = getNodeModel(path);
    setObjectPathMap(path, node, nodeModel);
    this.eventHandler.treeStructureChanged(event);
  }

  protected void setObjectPathMap(final TreePath path, final Object node,
    final ObjectTreeNodeModel<Object, Object> nodeModel) {
    synchronized (this.objectPathMap) {
      if (!this.objectPathMap.containsKey(node)) {
        this.objectPathMap.put(node, path);
      }
      if (nodeModel != null) {
        for (int i = 0; i < nodeModel.getChildCount(node); i++) {
          final Object child = nodeModel.getChild(node, i);
          if (!this.objectPathMap.containsKey(child)) {
            final TreePath childPath = path.pathByAddingChild(child);
            this.objectPathMap.put(child, childPath);
          }
        }
      }
    }
  }

  public void setRoot(final Object root) {
    if (root != this.root) {
      this.root = root;
      final TreeModelEvent e = new TreeModelEvent(root, new TreePath(root));
      this.eventHandler.treeStructureChanged(e);
    }
  }

  public void setVisible(final Object object, final boolean visible) {
    final boolean oldVisible = !hiddenObjects.containsKey(object);
    if (visible) {
      hiddenObjects.remove(object);
    } else {
      hiddenObjects.put(object, true);
    }
    if (visible != oldVisible) {
      final TreePath path = getPath(object);
      if (path != null) {
        fireTreeNodesChanged(path);
      }
    }
  }

  @Override
  public void treeCollapsed(final TreeExpansionEvent event) {
    final TreePath path = event.getPath();
    final Object node = path.getLastPathComponent();
    if (node != null) {
      final ObjectTreeNodeModel<Object, Object> nodeModel = getNodeModel(path);
      if (nodeModel != null) {
        if (isInitialized(path, nodeModel, node)) {
          for (int i = 0; i < nodeModel.getChildCount(node); i++) {
            final Object child = nodeModel.getChild(node, i);
            this.objectPathMap.remove(child);
          }
        }
      }
    }
  }

  @Override
  public void treeExpanded(final TreeExpansionEvent event) {
    final TreePath path = event.getPath();
    final Object node = path.getLastPathComponent();
    if (node != null) {
      final ObjectTreeNodeModel<Object, Object> nodeModel = getNodeModel(path);
      if (isInitialized(path, nodeModel, node)) {
        setObjectPathMap(path, node, nodeModel);
      }
    }
  }

  @Override
  public void treeWillCollapse(final TreeExpansionEvent event)
    throws ExpandVetoException {
  }

  @Override
  public void treeWillExpand(final TreeExpansionEvent event)
    throws ExpandVetoException {
    final TreePath path = event.getPath();
    final Object component = path.getLastPathComponent();
    final ObjectTreeNodeModel<Object, Object> model = getNodeModel(path);
    if (model != null && model.isLazyLoad()) {
      final Boolean initialized = this.initialized.get(path);
      if (initialized == null) {
        this.initialized.put(path, false);
        ExecutorServiceFactory.invokeMethod(this, "initializeNode", path,
          model, component);
      }
    }
  }

  @Override
  public void valueForPathChanged(final TreePath path, final Object newValue) {
    // final TreePath parentPath = path.getParentPath();
    // final Object parent = parentPath.getLastPathComponent();
    // final Object value = path.getLastPathComponent();
    // TODO
  }
}
