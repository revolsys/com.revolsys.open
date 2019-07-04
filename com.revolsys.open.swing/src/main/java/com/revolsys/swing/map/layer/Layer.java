package com.revolsys.swing.map.layer;

import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.collection.Child;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.ProjectFrame;
import com.revolsys.swing.map.ProjectFramePanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.style.panel.LayerStylePanel;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.Booleans;

public interface Layer extends GeometryFactoryProxy, PropertyChangeSupportProxy,
  ObjectWithProperties, PropertyChangeListener, Comparable<Layer>, MapSerializer, Child<LayerGroup>,
  Cloneable, BoundingBoxProxy, ProjectFramePanel {

  default void addParent(final List<Layer> path) {
    final LayerGroup parent = getLayerGroup();
    if (parent != null) {
      path.add(0, parent);
      parent.addParent(path);
    }
  }

  default void addVisibleBbox(final BoundingBoxEditor boundingBox) {
    if (isExists() && isVisible()) {
      boundingBox.addBbox(this);
    }
  }

  default boolean checkShowProperties() {
    boolean show = true;
    synchronized (this) {
      if (Booleans.getBoolean(getProperty("INTERNAL_PROPERTIES_VISIBLE"))) {
        show = false;
      } else {
        setProperty("INTERNAL_PROPERTIES_VISIBLE", true);
      }
    }
    return show;
  }

  void delete();

  BoundingBox getBoundingBox(boolean visibleLayersOnly);

  Collection<Class<?>> getChildClasses();

  @Override
  Icon getIcon();

  long getId();

  LayerGroup getLayerGroup();

  default MapPanel getMapPanel() {
    final LayerGroup project = getProject();
    if (project == null) {
      return null;
    } else {
      return project.getProperty(MapPanel.MAP_PANEL);
    }
  }

  long getMaximumScale();

  MenuFactory getMenu();

  long getMinimumScale();

  @Override
  String getName();

  /**
   * Get the path from the root project. The name of the layer group at the root is not included.
   * @return
   */
  @Override
  String getPath();

  default List<Layer> getPathList() {
    final List<Layer> path = new ArrayList<>();
    path.add(this);
    addParent(path);
    return path;
  }

  Project getProject();

  <L extends LayerRenderer<? extends Layer>> L getRenderer();

  BoundingBox getSelectedBoundingBox();

  String getType();

  default Viewport2D getViewport() {
    final MapPanel mapPanel = getMapPanel();
    if (mapPanel == null) {
      return null;
    } else {
      return mapPanel.getViewport();
    }
  }

  void initialize();

  boolean isClonable();

  boolean isDeleted();

  default boolean isDescendant(final Layer layer) {
    return layer == this;
  }

  boolean isEditable();

  boolean isEditable(double scale);

  boolean isExists();

  default boolean isHasChanges() {
    return false;
  }

  default boolean isHasGeometry() {
    return true;
  }

  boolean isHasSelectedRecords();

  boolean isInitialized();

  boolean isOpen();

  boolean isQueryable();

  boolean isQuerySupported();

  boolean isReadOnly();

  boolean isSelectable();

  boolean isSelectable(double scale);

  boolean isSelectSupported();

  boolean isVisible();

  boolean isVisible(double scale);

  TabbedValuePanel newPropertiesPanel();

  void refresh();

  void refreshAll();

  boolean saveChanges();

  boolean saveSettings(Path directory);

  void setEditable(boolean editable);

  void setLayerGroup(LayerGroup layerGroup);

  /**
   * Set the maximum scale. This is the scale that if you zoom in to a more
   * detailed scale than the maximum scale the layer will not be visible. This
   * is inverse from the logical definition of maximum. If scale < maximumScale
   * it will not be shown.
   */
  void setMaximumScale(long maximumScale);

  /**
   * Set the minimum scale. This is the scale that if you zoom out to a less
   * detailed scale than the minimum scale the layer will not be visible. This
   * is inverse from the logical definition of minimum. If scale > minimumScale
   * it will not be shown.
   */
  void setMinimumScale(long minimumScale);

  void setName(String name);

  void setOpen(boolean open);

  void setQueryable(boolean b);

  void setReadOnly(boolean readOnly);

  void setRenderer(final LayerRenderer<? extends Layer> renderer);

  void setSelectable(boolean selectable);

  void setVisible(boolean visible);

  void showProperties();

  void showProperties(String tabName);

  default void showRendererProperties(final LayerRenderer<?> renderer) {
    final MapPanel map = getMapPanel();
    if (map != null && isExists() && checkShowProperties()) {
      try {
        final Window window = SwingUtilities.getWindowAncestor(map);
        final TabbedValuePanel panel = newPropertiesPanel();
        panel.setSelectdTab("Style");
        final LayerStylePanel stylePanel = panel.getTab("Style");
        if (stylePanel != null) {
          stylePanel.setSelectedRenderer(renderer);
          panel.showDialog(window);
        }
        refresh();
      } finally {
        removeProperty("INTERNAL_PROPERTIES_VISIBLE");
      }
    }
  }

  default void showTableView() {
    showTableView(MapEx.EMPTY);
  }

  default void showTableView(final MapEx config) {
    final ProjectFrame projectFrame = ProjectFrame.get(this);
    if (projectFrame != null) {
      projectFrame.addBottomTab(this, config);
    }
  }
}
