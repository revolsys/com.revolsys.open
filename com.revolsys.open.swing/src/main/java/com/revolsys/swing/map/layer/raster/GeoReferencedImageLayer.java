package com.revolsys.swing.map.layer.raster;

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

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.map.InvokeMethodMapObjectFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.DockingFramesUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.raster.filter.WarpAffineFilter;
import com.revolsys.swing.map.layer.raster.filter.WarpFilter;
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

    final EnableCheck hasChanges = new TreeItemPropertyEnableCheck("hasChanges");

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Save Changes",
      "map_save", hasChanges, "saveChanges"));

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Cancel Changes",
      "map_cancel", "cancelChanges"));

    menu.addMenuItem("edit", TreeItemRunnable.createAction("View Tie-Points",
      "table_go", "showTiePointsTable"));

    menu.addCheckboxMenuItem("edit",
      TreeItemRunnable.createAction("Show Original Image", (String)null,
        editable, "toggleShowOriginalImage"), showOriginalImage);

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Fit to Screen",
      "arrow_out", editable, "fitToViewport"));
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
  protected ValueField addPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.addPropertiesTabGeneralPanelSource(parent);

    if (url.startsWith("file:")) {
      final String fileName = url.replaceFirst("file:(//)?", "");
      SwingUtil.addReadOnlyTextField(panel, "File", fileName);
    } else {
      SwingUtil.addReadOnlyTextField(panel, "URL", url);
    }
    final String fileNameExtension = FileUtil.getFileNameExtension(url);
    if (StringUtils.hasText(fileNameExtension)) {
      SwingUtil.addReadOnlyTextField(panel, "File Extension", fileNameExtension);
      final GeoReferencedImageFactory factory = IoFactoryRegistry.getInstance()
        .getFactoryByFileExtension(GeoReferencedImageFactory.class,
          fileNameExtension);
      if (factory != null) {
        SwingUtil.addReadOnlyTextField(panel, "File Type", factory.getName());
      }
    }
    GroupLayoutUtil.makeColumns(panel, 2, true);
    return panel;
  }

  public void cancelChanges() {
    if (this.image == null && this.resource != null) {
      GeoReferencedImage image = null;
      final Resource imageResource = SpringUtil.getResource(this.url);
      if (imageResource.exists()) {
        try {
          image = AbstractGeoReferencedImageFactory.loadGeoReferencedImage(imageResource);
          if (image == null) {
            LoggerFactory.getLogger(GeoReferencedImageLayer.class).error(
              "Cannot load image: " + this.url);
          }
        } catch (final RuntimeException e) {
          LoggerFactory.getLogger(GeoReferencedImageLayer.class).error(
            "Unable to load image: " + this.url, e);
        }
      } else {
        LoggerFactory.getLogger(GeoReferencedImageLayer.class).error(
          "Image does not exist: " + this.url);
      }
      setImage(image);
    } else {
      this.image.cancelChanges();
    }
    firePropertyChange("hasChanges", true, false);
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
      cancelChanges();
      return true;
    } else {
      LoggerFactory.getLogger(getClass()).error(
        "Layer definition does not contain a 'url' property");
      return false;
    }
  }

  @Override
  protected boolean doSaveChanges() {
    if (image == null) {
      return true;
    } else {
      return image.saveChanges();
    }
  }

  public BoundingBox fitToViewport() {
    final Project project = getProject();
    if (project == null || this.image == null || !isInitialized()) {
      return new BoundingBox();
    } else {
      final BoundingBox oldValue = this.image.getBoundingBox();
      final BoundingBox viewBoundingBox = project.getViewBoundingBox();
      if (viewBoundingBox.isEmpty()) {
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
      if (boundingBox.isEmpty()) {
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
    if (image == null) {
      return getBoundingBox().getGeometryFactory();
    } else {
      return image.getGeometryFactory();
    }
  }

  public GeoReferencedImage getImage() {
    return this.image;
  }

  public WarpFilter getWarpFilter() {
    if (isShowOriginalImage()) {
      return new WarpAffineFilter(getBoundingBox(), image.getImageWidth(),
        image.getImageHeight());
    } else {
      return image.getWarpFilter();
    }
  }

  @Override
  public boolean isHasChanges() {
    if (image == null) {
      return false;
    } else {
      return image.isHasChanages();
    }
  }

  public boolean isShowOriginalImage() {
    return showOriginalImage;
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

  public void setShowOriginalImage(final boolean showOriginalImage) {
    final Object oldValue = this.showOriginalImage;
    this.showOriginalImage = showOriginalImage;
    firePropertyChange("showOriginalImage", oldValue, showOriginalImage);
  }

  @Override
  public void setVisible(final boolean visible) {
    super.setVisible(visible);
    if (!visible) {
      setEditable(false);
    }
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
    MapSerializerUtil.add(map, "showOriginalImage", showOriginalImage);

    final Map<String, Object> imageSettings;
    if (image == null) {
      imageSettings = getProperty("imageSettings");
    } else {
      imageSettings = image.toMap();
    }
    MapSerializerUtil.add(map, "imageSettings", imageSettings);

    return map;
  }
}
