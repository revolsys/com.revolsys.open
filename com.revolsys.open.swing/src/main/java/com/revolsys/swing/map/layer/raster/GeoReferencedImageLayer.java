package com.revolsys.swing.map.layer.raster;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.event.CDockableStateListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;

import com.revolsys.beans.InvokeMethodCallable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.map.InvokeMethodMapObjectFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.geom.impl.PointDouble2D;
import com.revolsys.raster.AbstractGeoReferencedImageFactory;
import com.revolsys.raster.GeoReferencedImage;
import com.revolsys.raster.GeoReferencedImageFactory;
import com.revolsys.raster.MappedLocation;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.DockingFramesUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.AndEnableCheck;
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
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.MathUtil;
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
    menu.addGroup(0, "table");
    menu.addGroup(2, "edit");

    final EnableCheck readonly = new TreeItemPropertyEnableCheck("readOnly",
      false);
    final EnableCheck editable = new TreeItemPropertyEnableCheck("editable");
    final EnableCheck showOriginalImage = new TreeItemPropertyEnableCheck(
        "showOriginalImage");
    final EnableCheck hasChanges = new TreeItemPropertyEnableCheck("hasChanges");
    final EnableCheck hasTransform = new TreeItemPropertyEnableCheck(
        "hasTransform");

    menu.addMenuItem("table", TreeItemRunnable.createAction("View Tie-Points",
      "table_go", "showTiePointsTable"));

    menu.addCheckboxMenuItem("edit", TreeItemRunnable.createAction("Editable",
      "pencil", readonly, "toggleEditable"), editable);

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Save Changes",
      "map_save", hasChanges, "saveChanges"));

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Cancel Changes",
      "map_cancel", hasChanges, "cancelChanges"));

    menu.addCheckboxMenuItem("edit", TreeItemRunnable.createAction(
      "Show Original Image", (String)null, new AndEnableCheck(editable,
        hasTransform), "toggleShowOriginalImage"), showOriginalImage);

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Fit to Screen",
      "arrow_out", editable, "fitToViewport"));
  }

  private GeoReferencedImage image;

  private Resource resource;

  private String url;

  private boolean showOriginalImage = false;

  private int opacity = 255;

  public GeoReferencedImageLayer(final Map<String, Object> properties) {
    super(properties);
    setType("geoReferencedImage");
    setSelectSupported(false);
    setQuerySupported(false);
    setRenderer(new GeoReferencedImageLayerRenderer(this));
    final int opacity = CollectionUtil.getInteger(properties, "opacity", 255);
    setOpacity(opacity);
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

  public void deleteTiePoint(final MappedLocation tiePoint) {
    if (isEditable()) {
      this.image.deleteTiePoint(tiePoint);
    } else {
      LoggerFactory.getLogger("Cannot delete tie-point. Layer " + getClass())
      .error(getPath() + " is not editable");
    }
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

  public synchronized BoundingBox fitToViewport() {
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
      BoundingBox boundingBox = image.getBoundingBox();
      if (boundingBox.isEmpty()) {
        final boolean hasChanges = isHasChanges();
        boundingBox = fitToViewport();
        if (hasChanges) {
          saveChanges();
        } else {
          cancelChanges();
        }
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

  public int getOpacity() {
    return this.opacity;
  }

  @Override
  public boolean isHasChanges() {
    if (this.image == null) {
      return false;
    } else {
      return this.image.isHasChanages();
    }
  }

  public boolean isHasTransform() {
    if (this.image == null) {
      return false;
    } else {
      return this.image.isHasTransform();
    }
  }

  public boolean isShowOriginalImage() {
    return this.image.isHasTransform() && this.showOriginalImage;
  }

  @Override
  public boolean isVisible() {
    return super.isVisible() || isEditable();
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    if (this.image != null) {
      this.image.setBoundingBox(boundingBox);
    }
  }

  @Override
  public void setEditable(final boolean editable) {
    if (SwingUtilities.isEventDispatchThread()) {
      Invoke.background("Set editable", this, "setEditable", editable);
    } else {
      synchronized (getSync()) {
        if (editable) {
          setShowOriginalImage(true);
        } else {
          firePropertyChange("preEditable", false, true);
          if (isHasChanges()) {
            final Integer result = InvokeMethodCallable.invokeAndWait(
              JOptionPane.class,
              "showConfirmDialog",
              JOptionPane.getRootFrame(),
              "The layer has unsaved changes. Click Yes to save changes. Click No to discard changes. Click Cancel to continue editing.",
              "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);

            if (result == JOptionPane.YES_OPTION) {
              if (!saveChanges()) {
                setVisible(true);
                return;
              }
            } else if (result == JOptionPane.NO_OPTION) {
              cancelChanges();
            } else {
              setVisible(true);
              // Don't allow state change if cancelled
              return;
            }

          }
        }
        super.setEditable(editable);
        if (editable == false) {
          setShowOriginalImage(false);
        }
      }
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

  public void setOpacity(int opacity) {
    final int oldValue = this.opacity;
    if (opacity < 0) {
      opacity = 0;
    } else if (opacity > 255) {
      opacity = 255;
    }
    this.opacity = opacity;
    firePropertyChange("opacity", oldValue, opacity);
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

  public Point sourcePixelToTargetPoint(final BoundingBox boundingBox,
    final boolean useTransform, final double... coordinates) {
    if (useTransform) {
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

  public Point sourcePixelToTargetPoint(final MappedLocation tiePoint) {
    final Point sourcePixel = tiePoint.getSourcePixel();
    return sourcePixelToTargetPoint(sourcePixel);
  }

  public Point sourcePixelToTargetPoint(final Point sourcePixel) {
    final BoundingBox boundingBox = getBoundingBox();
    final double[] coordinates = sourcePixel.getCoordinates();
    final boolean useTransform = !isShowOriginalImage();
    return sourcePixelToTargetPoint(boundingBox, useTransform, coordinates);
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
    return new PointDouble2D(MathUtil.makePrecise(1, coordinates[0]),
      MathUtil.makePrecise(1, coordinates[1]));
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
    map.remove("showOriginalImage");
    map.remove("imageSettings");
    MapSerializerUtil.add(map, "url", this.url);
    MapSerializerUtil.add(map, "opacity", this.opacity, 1);
    return map;
  }

  @Override
  public void zoomToLayer() {
    final Project project = getProject();
    final GeometryFactory geometryFactory = project.getGeometryFactory();
    final BoundingBox layerBoundingBox = getBoundingBox();
    BoundingBox boundingBox = layerBoundingBox;
    final AffineTransform transform = this.image.getAffineTransformation(layerBoundingBox);
    if (!transform.isIdentity()) {
      final GeoReferencedImage image = getImage();

      final double width = image.getImageWidth() - 1;
      final double height = image.getImageHeight() - 1;
      final double[] targetCoordinates = MappedLocation.toModelCoordinates(
        image, layerBoundingBox, true, 0, height, width, height, width, 0, 0,
        0, 0, height);
      final LineString line = layerBoundingBox.getGeometryFactory().lineString(
        2, targetCoordinates);
      boundingBox = boundingBox.expandToInclude(line);
    }
    boundingBox = boundingBox.convert(geometryFactory)
        .expandPercent(0.1)
        .clipToCoordinateSystem();

    project.setViewBoundingBox(boundingBox);
  }
}
