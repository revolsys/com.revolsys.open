package com.revolsys.swing.map.layer.raster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.event.CDockableStateListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.DockingFramesUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.InvokeMethodMapObjectFactory;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.raster.filter.WarpAffineFilter;
import com.revolsys.swing.map.layer.raster.filter.WarpFilter;
import com.revolsys.swing.map.overlay.MappedLocation;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.util.Property;

public class GeoReferencedImageLayer extends AbstractLayer {

  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "geoReferencedImage", "Geo-referenced Image",
    GeoReferencedImageLayer.class, "create");

  static {
    final MenuFactory menu = ObjectTreeModel.getMenu(GeoReferencedImageLayer.class);
    menu.addGroup(1, "edit");

    final EnableCheck readonly = new TreeItemPropertyEnableCheck("readOnly",
      false);
    final EnableCheck editable = new TreeItemPropertyEnableCheck("editable");
    final EnableCheck showOriginalImage = new TreeItemPropertyEnableCheck(
      "showOriginalImage");

    menu.addCheckboxMenuItem("edit", TreeItemRunnable.createAction("Editable",
      "pencil", readonly, "toggleEditable"), editable);

    menu.addMenuItem("edit", TreeItemRunnable.createAction("View Tie-Points",
      "table_go", "showTiePointsTable"));

    menu.addCheckboxMenuItem("edit",
      TreeItemRunnable.createAction("Show Original Image", (String)null,
        editable, "toggleShowOriginalImage"), showOriginalImage);

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Fit to Screen",
      "arrow_out", editable, "fitToViewport"));

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Revert to Saved",
      "arrow_revert", editable, "revert"));

  }

  public static GeoReferencedImageLayer create(
    final Map<String, Object> properties) {
    return new GeoReferencedImageLayer(properties);
  }

  private GeoReferencedImage image;

  private Resource resource;

  private String url;

  private boolean showOriginalImage = true;

  public GeoReferencedImageLayer(final Map<String, Object> properties) {
    super(properties);
    setType("geoReferencedImage");
    setSelectSupported(false);
    setQuerySupported(false);
    setRenderer(new GeoReferencedImageLayerRenderer(this));
  }

  @Override
  public TabbedValuePanel createPropertiesPanel() {
    final TabbedValuePanel propertiesPanel = super.createPropertiesPanel();
    final TiePointsPanel tiePointsPanel = new TiePointsPanel(this);
    SwingUtil.setTitledBorder(tiePointsPanel, "Tie Points");

    propertiesPanel.addTab("Geo-Referencing", tiePointsPanel);
    return propertiesPanel;
  }

  @Override
  protected boolean doInitialize() {
    final String url = getProperty("url");
    if (StringUtils.hasText(url)) {
      this.url = url;
      resource = SpringUtil.getResource(url);
      revert();
      return true;
    } else {
      LoggerFactory.getLogger(getClass()).error(
        "Layer definition does not contain a 'url' property");
      return false;
    }
  }

  public BoundingBox fitToViewport() {
    final Project project = getProject();
    if (project == null || this.image == null) {
      return new BoundingBox();
    } else {
      final BoundingBox oldValue = this.image.getBoundingBox();
      final BoundingBox viewBoundingBox = project.getViewBoundingBox();
      if (viewBoundingBox.isNull()) {
        return viewBoundingBox;
      } else {
        final double viewRatio = viewBoundingBox.getAspectRatio();
        final double imageRatio = this.image.getImageAspectRatio();
        BoundingBox boundingBox;
        if (viewRatio > imageRatio) {
          boundingBox = viewBoundingBox.expandPercent(-1 + imageRatio
            / viewRatio, 0.0);
        } else if (viewRatio < imageRatio) {
          boundingBox = viewBoundingBox.expandPercent(0.0, -1 + viewRatio
            / imageRatio);
        } else {
          boundingBox = viewBoundingBox;
        }
        this.image.setBoundingBox(boundingBox);
        firePropertyChange("boundingBox", oldValue, boundingBox);
        return boundingBox;
      }
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    final GeoReferencedImage image = getImage();
    if (image == null) {
      return new BoundingBox();
    } else {
      final BoundingBox boundingBox = image.getBoundingBox();
      if (boundingBox == null || boundingBox.isNull()) {
        return fitToViewport();
      }
      return boundingBox;
    }
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (isExists() && (isVisible() || !visibleLayersOnly)) {
      return getBoundingBox();
    } else {
      return new BoundingBox(getGeometryFactory());
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return getBoundingBox().getGeometryFactory();
  }

  public GeoReferencedImage getImage() {
    return this.image;
  }

  protected List<MappedLocation> getTiePointsProperty() {
    final List<?> tiePointsProperty = getProperty("tiePoints");
    final List<MappedLocation> tiePoints = new ArrayList<MappedLocation>();
    if (tiePointsProperty != null) {
      for (final Object tiePointValue : tiePointsProperty) {
        if (tiePointValue instanceof MappedLocation) {
          tiePoints.add((MappedLocation)tiePointValue);
        } else if (tiePointValue instanceof Map) {
          final Map<String, Object> map = (Map<String, Object>)tiePointValue;
          tiePoints.add(new MappedLocation(map));
        }
      }

    }
    return tiePoints;
  }

  public WarpFilter getWarpFilter() {
    if (isShowOriginalImage()) {
      return new WarpAffineFilter(getBoundingBox(), image.getImageWidth(),
        image.getImageHeight());
    } else {
      return image.getWarpFilter();
    }
  }

  public boolean isShowOriginalImage() {
    return showOriginalImage;
  }

  public void revert() {
    if (this.image == null && this.resource != null) {
      GeoReferencedImage image = null;
      final Resource imageResource = SpringUtil.getResource(this.url);
      if (imageResource.exists()) {
        try {
          image = AbstractGeoReferencedImageFactory.loadGeoReferencedImage(imageResource);
          if (image == null) {
            LoggerFactory.getLogger(GeoReferencedImageLayer.class).error(
              "Cannot load image:" + this.url);
          }
        } catch (final RuntimeException e) {
          LoggerFactory.getLogger(GeoReferencedImageLayer.class).error(
            "Unable to load image" + this.url, e);
        }
      } else {
        LoggerFactory.getLogger(GeoReferencedImageLayer.class).error(
          "Image does not exist:" + this.url);
      }
      setImage(image);
    } else {
      this.image.revert();
    }
    if (this.image != null) {
      final Object boundingBoxProperty = getProperty("boundingBox");
      final BoundingBox boundingBox = StringConverterRegistry.toObject(
        BoundingBox.class, boundingBoxProperty);
      if (boundingBox != null && !boundingBox.isEmpty()) {
        image.setBoundingBox(boundingBox);
      }

      final List<MappedLocation> tiePoints = getTiePointsProperty();
      image.setTiePoints(tiePoints);

    }
    firePropertyChange("revert", false, true);
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    if (image != null) {
      this.image.setBoundingBox(boundingBox);
    }
  }

  public void setImage(final GeoReferencedImage image) {
    final GeoReferencedImage old = this.image;
    if (this.image != null) {
      Property.removeListener(image, this);
    }
    this.image = image;
    if (image == null) {
      setExists(false);
    } else {
      setExists(true);
      Property.addListener(image, this);
    }
    firePropertyChange("image", old, this.image);
  }

  @Override
  public void setProperty(final String name, Object value) {
    if ("boundingBox".equals(name)) {
      value = StringConverterRegistry.toObject(BoundingBox.class, value);
    }
    super.setProperty(name, value);
  }

  public void setShowOriginalImage(final boolean showOriginalImage) {
    final Object oldValue = this.showOriginalImage;
    this.showOriginalImage = showOriginalImage;
    firePropertyChange("showOriginalImage", oldValue, showOriginalImage);
  }

  public void showTiePointsTable() {
    if (SwingUtilities.isEventDispatchThread()) {
      final Object tableView = getProperty("TableView");
      DefaultSingleCDockable dockable = null;
      if (tableView instanceof DefaultSingleCDockable) {
        dockable = (DefaultSingleCDockable)tableView;
      }
      final TiePointsPanel tiePointsPanel;
      if (dockable == null) {
        final LayerGroup project = getProject();

        tiePointsPanel = new TiePointsPanel(this);

        if (tiePointsPanel != null) {
          final String id = getClass().getName() + "." + getId();
          dockable = DockingFramesUtil.addDockable(project,
            MapPanel.MAP_TABLE_WORKING_AREA, id, getName(), tiePointsPanel);

          if (dockable != null) {
            dockable.setCloseable(true);
            setProperty("TableView", dockable);
            dockable.addCDockableStateListener(new CDockableStateListener() {
              @Override
              public void extendedModeChanged(final CDockable dockable,
                final ExtendedMode mode) {
              }

              @Override
              public void visibilityChanged(final CDockable dockable) {
                final boolean visible = dockable.isVisible();
                if (!visible) {
                  dockable.getControl()
                    .getOwner()
                    .remove((SingleCDockable)dockable);
                  setProperty("TableView", null);
                }
              }
            });
            dockable.toFront();
          }
        }
      } else {
        dockable.toFront();
      }

    } else {
      Invoke.later(this, "showTiePointsTable");
    }
  }

  public void toggleShowOriginalImage() {
    final boolean showOriginalImage = isShowOriginalImage();
    setShowOriginalImage(!showOriginalImage);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    map.remove("querySupported");
    map.remove("selectSupported");
    map.remove("editable");
    map.remove("TableView");
    MapSerializerUtil.add(map, "url", this.url);
    MapSerializerUtil.add(map, "showOriginalImage", showOriginalImage, false);

    final BoundingBox boundingBox;
    if (image == null) {
      final Object boundingBoxProperty = getProperty("boundingBox");
      boundingBox = StringConverterRegistry.toObject(BoundingBox.class,
        boundingBoxProperty);
    } else {
      boundingBox = image.getBoundingBox();
    }
    if (boundingBox != null) {
      MapSerializerUtil.add(map, "boundingBox", boundingBox.toString());
    }
    List<MappedLocation> tiePoints;
    if (image == null) {
      tiePoints = getTiePointsProperty();
    } else {
      tiePoints = image.getTiePoints();
    }
    MapSerializerUtil.add(map, "tiePoints", tiePoints, Collections.emptyList());
    return map;
  }
}
