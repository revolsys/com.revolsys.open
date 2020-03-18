package com.revolsys.swing.map.layer.elevation.gridded.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.list.Lists;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.rasterizer.HillShadeGriddedElevationModelRasterizer;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.Icons;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.elevation.gridded.GriddedElevationModelZRange;
import com.revolsys.swing.map.layer.elevation.gridded.TiledGriddedElevationModelLayer;
import com.revolsys.swing.map.layer.elevation.gridded.TiledGriddedElevationModelLayerTile;
import com.revolsys.swing.map.layer.elevation.gridded.TiledMultipleGriddedElevationModelStylePanel;
import com.revolsys.swing.map.layer.menu.TreeItemScaleMenu;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayer;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.BooleanCancellable;
import com.revolsys.util.Cancellable;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class TiledMultipleGriddedElevationModelLayerRenderer
  extends AbstractTiledLayerRenderer<GriddedElevationModel, TiledGriddedElevationModelLayerTile>
  implements IMultipleGriddedElevationModelLayerRenderer, GriddedElevationModelZRange {
  static {
    MenuFactory.addMenuInitializer(TiledMultipleGriddedElevationModelLayerRenderer.class, menu -> {
      menu.addMenuItem("layer", -1, "View/Edit Style", "palette",
        ((Predicate<TiledMultipleGriddedElevationModelLayerRenderer>)TiledMultipleGriddedElevationModelLayerRenderer::isEditing)
          .negate(),
        TiledMultipleGriddedElevationModelLayerRenderer::showProperties, false);

      menu.addComponentFactory("scale",
        new TreeItemScaleMenu<>(true, null,
          TiledMultipleGriddedElevationModelLayerRenderer::getMinimumScale,
          TiledMultipleGriddedElevationModelLayerRenderer::setMinimumScale));
      menu.addComponentFactory("scale",
        new TreeItemScaleMenu<>(false, null,
          TiledMultipleGriddedElevationModelLayerRenderer::getMaximumScale,
          TiledMultipleGriddedElevationModelLayerRenderer::setMaximumScale));
      RasterizerGriddedElevationModelLayerRenderer.initMenus(menu);
    });

  }

  private List<AbstractGriddedElevationModelLayerRenderer> renderers = new ArrayList<>();

  private double minZ = 0;

  private double maxZ = 3500;

  private TiledMultipleGriddedElevationModelLayerRenderer() {
    super("tiledMultipleGriddedElevationModelLayerRenderer", "Styles",
      Icons.getIcon("folder:palette"));
  }

  public TiledMultipleGriddedElevationModelLayerRenderer(
    final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  public TiledMultipleGriddedElevationModelLayerRenderer(
    final TiledGriddedElevationModelLayer layer) {
    this();
    setLayer(layer);
    final HillShadeGriddedElevationModelRasterizer hillshadeRasterizer = new HillShadeGriddedElevationModelRasterizer();
    final RasterizerGriddedElevationModelLayerRenderer hillshadeRenderer = new RasterizerGriddedElevationModelLayerRenderer(
      layer, this, hillshadeRasterizer);
    addRenderer(hillshadeRenderer);
    final RasterizerGriddedElevationModelLayerRenderer colorRampRenderer = new RasterizerGriddedElevationModelLayerRenderer(
      layer, this);
    addRenderer(colorRampRenderer);
  }

  public TiledMultipleGriddedElevationModelLayerRenderer(
    final TiledGriddedElevationModelLayer layer,
    final AbstractGriddedElevationModelLayerRenderer renderer) {
    this();
    setLayer(layer);
    addRenderer(renderer);
  }

  @Override
  public int addRenderer(final AbstractGriddedElevationModelLayerRenderer renderer) {
    return addRenderer(-1, renderer);
  }

  @Override
  public int addRenderer(int index, final AbstractGriddedElevationModelLayerRenderer renderer) {
    if (renderer == null) {
      return -1;
    } else {
      final String originalName = renderer.getName();
      String name = originalName;
      int i = 1;
      while (hasRendererWithSameName(renderer, name)) {
        name = originalName + i;
        i++;
      }
      renderer.setName(name);
      renderer.setParent(this);
      synchronized (this.renderers) {
        if (index < 0) {
          index = this.renderers.size();
        }
        this.renderers.add(index, renderer);
      }
      firePropertyChange("renderers", index, null, renderer);
      return index;
    }
  }

  @Override
  public boolean canAddChild(final Object object) {
    return object instanceof AbstractGriddedElevationModelLayerRenderer;
  }

  @Override
  public TiledMultipleGriddedElevationModelLayerRenderer clone() {
    final TiledMultipleGriddedElevationModelLayerRenderer clone = (TiledMultipleGriddedElevationModelLayerRenderer)super.clone();
    clone.renderers = JavaBeanUtil.clone(this.renderers);
    for (final AbstractGriddedElevationModelLayerRenderer renderer : clone.renderers) {
      renderer.setParent(clone);
    }
    return clone;
  }

  @Override
  public TiledGriddedElevationModelLayer getLayer() {
    return (TiledGriddedElevationModelLayer)super.getLayer();
  }

  @Override
  public double getMaxZ() {
    return this.maxZ;
  }

  @Override
  public double getMinZ() {
    return this.minZ;
  }

  @Override
  public List<AbstractGriddedElevationModelLayerRenderer> getRenderers() {
    synchronized (this.renderers) {
      return new ArrayList<>(this.renderers);
    }
  }

  @Override
  public boolean hasRendererWithSameName(final LayerRenderer<?> renderer, final String name) {
    for (final AbstractGriddedElevationModelLayerRenderer otherRenderer : this.renderers) {
      if (renderer != otherRenderer) {
        final String layerName = otherRenderer.getName();
        if (name.equals(layerName)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isEmpty() {
    return this.renderers.isEmpty();
  }

  @Override
  public boolean isSameLayer(final Layer layer) {
    return getLayer() == layer;
  }

  @Override
  public Form newStylePanel() {
    return new TiledMultipleGriddedElevationModelStylePanel(this);
  }

  @Override
  public void refresh() {
    for (final AbstractGriddedElevationModelLayerRenderer renderer : this.renderers) {
      renderer.refresh();
    }
  }

  @Override
  public int removeRenderer(final AbstractGriddedElevationModelLayerRenderer renderer) {
    boolean removed = false;
    synchronized (this.renderers) {
      final int index = this.renderers.indexOf(renderer);
      if (index != -1) {
        if (renderer.getParent() == this) {
          renderer.setParent(null);
        }
        removed = this.renderers.remove(renderer);
      }
      if (removed) {
        firePropertyChange("renderers", index, renderer, null);
      }
      return index;
    }
  }

  @Override
  protected void renderTile(final ViewRenderer view, final Cancellable cancellable,
    final TiledGriddedElevationModelLayerTile tile) {
    final TiledGriddedElevationModelLayer layer = getLayer();
    final BufferedGeoreferencedImage image = layer.newRenderImage();
    final GriddedElevationModel elevationModel = tile.getElevationModel();
    if (elevationModel != null) {
      final List<AbstractGriddedElevationModelLayerRenderer> renderers = getRenderers();
      for (final AbstractGriddedElevationModelLayerRenderer renderer : cancellable
        .cancellable(renderers)) {
        if (renderer.isVisible(view)) {
          renderer.setElevationModel(elevationModel);
          image.setBoundingBox(tile.getBoundingBox());
          renderer.render(view, layer, image);
        }
      }
    }
  }

  @Override
  protected void renderTiles(final ViewRenderer view, final BooleanCancellable cancellable,
    final List<TiledGriddedElevationModelLayerTile> mapTiles) {
    final TiledGriddedElevationModelLayer layer = getLayer();
    final BufferedGeoreferencedImage image = layer.newRenderImage();
    final List<AbstractGriddedElevationModelLayerRenderer> renderers = getRenderers();
    for (final AbstractGriddedElevationModelLayerRenderer renderer : cancellable
      .cancellable(renderers)) {
      if (renderer.isVisible(view)) {
        for (final TiledGriddedElevationModelLayerTile tile : cancellable.cancellable(mapTiles)) {
          final GriddedElevationModel elevationModel = tile.getElevationModel();
          if (elevationModel != null) {
            synchronized (renderer) {
              renderer.setElevationModel(elevationModel);
              image.setBoundingBox(tile.getBoundingBox());
              renderer.render(view, layer, image);
            }
          }
        }
      }
    }
  }

  @Override
  public void setLayer(
    final AbstractTiledLayer<GriddedElevationModel, TiledGriddedElevationModelLayerTile> layer) {
    super.setLayer(layer);
    for (final AbstractGriddedElevationModelLayerRenderer renderer : this.renderers) {
      renderer.setLayer((TiledGriddedElevationModelLayer)layer);
    }
    updateBoundingBox();
  }

  public void setMaxZ(final double maxZ) {
    this.maxZ = maxZ;
    updateBoundingBox();
  }

  public void setMinZ(final double minZ) {
    this.minZ = minZ;
    updateBoundingBox();
  }

  public void setRenderers(
    final List<? extends AbstractGriddedElevationModelLayerRenderer> renderers) {
    List<AbstractGriddedElevationModelLayerRenderer> oldValue;
    synchronized (this.renderers) {
      oldValue = Lists.toArray(this.renderers);
      for (final AbstractGriddedElevationModelLayerRenderer renderer : this.renderers) {
        renderer.setParent(null);
      }
      if (renderers == null) {
        this.renderers.clear();
      }
      this.renderers = new ArrayList<>(renderers);
      for (final AbstractGriddedElevationModelLayerRenderer renderer : this.renderers) {
        renderer.setParent(this);
        renderer.setLayer(getLayer());
      }
    }
    firePropertyChange("renderers", oldValue, this.renderers);
  }

  public void setStyles(final List<?> styles) {
    if (Property.hasValue(styles)) {
      final List<AbstractGriddedElevationModelLayerRenderer> renderers = new ArrayList<>();
      for (final Object childStyle : styles) {
        if (childStyle instanceof AbstractGriddedElevationModelLayerRenderer) {
          final AbstractGriddedElevationModelLayerRenderer renderer = (AbstractGriddedElevationModelLayerRenderer)childStyle;
          renderers.add(renderer);
        } else {
          Logs.error(this, "Cannot create renderer for: " + childStyle);
        }
      }
      setRenderers(renderers);
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    final List<AbstractGriddedElevationModelLayerRenderer> renderers = getRenderers();
    if (!renderers.isEmpty()) {
      final List<Map<String, Object>> rendererMaps = new ArrayList<>();
      for (final AbstractGriddedElevationModelLayerRenderer renderer : renderers) {
        rendererMaps.add(renderer.toMap());
      }
      addToMap(map, "styles", rendererMaps);
    }
    addToMap(map, "minZ", this.minZ);
    addToMap(map, "maxZ", this.maxZ);
    return map;
  }

  public void updateBoundingBox() {
    final TiledGriddedElevationModelLayer layer = getLayer();
    if (layer != null) {
      final BoundingBox boundingBox = layer.getBoundingBox();
      final double minX = boundingBox.getMinX();
      final double minY = boundingBox.getMinY();
      final double maxX = boundingBox.getMaxX();
      final double maxY = boundingBox.getMaxY();
      final GeometryFactory geometryFactory = layer.getGeometryFactory();
      final BoundingBox newBoundingBox = geometryFactory.newBoundingBox(3, minX, minY, this.minZ,
        maxX, maxY, this.maxZ);
      layer.setBoundingBox(newBoundingBox);
    }
  }
}
