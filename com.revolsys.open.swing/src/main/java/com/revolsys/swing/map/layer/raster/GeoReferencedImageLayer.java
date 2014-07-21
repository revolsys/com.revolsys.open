package com.revolsys.swing.map.layer.raster;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
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

import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.map.InvokeMethodMapObjectFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.raster.AbstractGeoReferencedImageFactory;
import com.revolsys.raster.GeoReferencedImage;
import com.revolsys.raster.GeoReferencedImageFactory;
import com.revolsys.raster.MappedLocation;
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
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.util.Property;

public class GeoReferencedImageLayer extends AbstractLayer {

  public static GeoReferencedImageLayer create(
    final Map<String, Object> properties) {
    return new GeoReferencedImageLayer(properties);
  }

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

    if (this.url.startsWith("file:")) {
      final String fileName = this.url.replaceFirst("file:(//)?", "");
      SwingUtil.addReadOnlyTextField(panel, "File", fileName);
    } else {
      SwingUtil.addReadOnlyTextField(panel, "URL", this.url);
    }
    final String fileNameExtension = FileUtil.getFileNameExtension(this.url);
    if (Property.hasValue(fileNameExtension)) {
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
    if (Property.hasValue(url)) {
      this.url = url;
      this.resource = SpringUtil.getResource(url);
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
    if (this.image == null) {
      return true;
    } else {
      return this.image.saveChanges();
    }
  }

  public BoundingBox fitToViewport() {
    final Project project = getProject();
    if (project == null || this.image == null || !isInitialized()) {
      return new BoundingBoxDoubleGf();
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
      return new BoundingBoxDoubleGf();
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
      return new BoundingBoxDoubleGf(getGeometryFactory());
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    if (this.image == null) {
      return getBoundingBox().getGeometryFactory();
    } else {
      return this.image.getGeometryFactory();
    }
  }

  public GeoReferencedImage getImage() {
    return this.image;
  }

  @Override
  public boolean isHasChanges() {
    if (this.image == null) {
      return false;
    } else {
      return this.image.isHasChanages();
    }
  }

  public boolean isShowOriginalImage() {
    return this.showOriginalImage;
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    if (this.image != null) {
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

  public Point sourcePixelToTargetPoint(final MappedLocation tiePoint) {
    final Point sourcePixel = tiePoint.getSourcePixel();
    return sourcePixelToTargetPoint(sourcePixel);
  }

  public Point sourcePixelToTargetPoint(final Point sourcePixel) {
    final BoundingBox boundingBox = getBoundingBox();
    final double[] coordinates = sourcePixel.getCoordinates();
    if (!isShowOriginalImage()) {
      final AffineTransform transform = this.image.getAffineTransformation(boundingBox);
      transform.transform(coordinates, 0, coordinates, 0, 1);
    }
    final double imageX = coordinates[0];
    final double imageY = coordinates[1];

    final GeoReferencedImage image = getImage();
    final double xPercent = imageX / image.getImageWidth();
    final double yPercent = imageY / image.getImageHeight();

    final double modelWidth = boundingBox.getWidth();
    final double modelHeight = boundingBox.getHeight();

    final double modelX = boundingBox.getMinX() + modelWidth * xPercent;
    final double modelY = boundingBox.getMinY() + modelHeight * yPercent;
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final Point imagePoint = geometryFactory.point(modelX, modelY);
    return imagePoint;
  }

  public Point targetPointToSourcePixel(Point targetPoint) {
    final GeoReferencedImage image = getImage();
    final BoundingBox boundingBox = getBoundingBox();
    targetPoint = targetPoint.convert(boundingBox.getGeometryFactory(), 2);
    final double modelX = targetPoint.getX();
    final double modelY = targetPoint.getY();
    final double modelDeltaX = modelX - boundingBox.getMinX();
    final double modelDeltaY = modelY - boundingBox.getMinY();

    final double modelWidth = boundingBox.getWidth();
    final double modelHeight = boundingBox.getHeight();

    final double xRatio = modelDeltaX / modelWidth;
    final double yRatio = modelDeltaY / modelHeight;

    final double imageX = image.getImageWidth() * xRatio;
    final double imageY = image.getImageHeight() * yRatio;
    final double[] coordinates = new double[] {
      imageX, imageY
    };
    if (!isShowOriginalImage()) {
      try {
        final AffineTransform transform = image.getAffineTransformation(
          boundingBox).createInverse();
        transform.transform(coordinates, 0, coordinates, 0, 1);
      } catch (final NoninvertibleTransformException e) {
      }
    }
    return new PointDouble(coordinates);
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
    MapSerializerUtil.add(map, "showOriginalImage", this.showOriginalImage);

    final Map<String, Object> imageSettings;
    if (this.image == null) {
      imageSettings = getProperty("imageSettings");
    } else {
      imageSettings = this.image.toMap();
    }
    MapSerializerUtil.add(map, "imageSettings", imageSettings);

    return map;
  }
}
