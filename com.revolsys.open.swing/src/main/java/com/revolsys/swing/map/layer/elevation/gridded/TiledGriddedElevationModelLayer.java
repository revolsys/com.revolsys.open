package com.revolsys.swing.map.layer.elevation.gridded;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.list.Lists;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReaderFactory;
import com.revolsys.elevation.gridded.scaledint.ScaledIntegerGriddedDigitalElevation;
import com.revolsys.elevation.gridded.scaledint.ScaledIntegerGriddedDigitalElevationModelGrid;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.elevation.ElevationModelLayer;
import com.revolsys.swing.map.layer.elevation.gridded.renderer.RasterizerGriddedElevationModelLayerRenderer;
import com.revolsys.swing.map.layer.elevation.gridded.renderer.TiledMultipleGriddedElevationModelLayerRenderer;
import com.revolsys.swing.map.layer.record.style.panel.LayerStylePanel;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayer;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class TiledGriddedElevationModelLayer
  extends AbstractTiledLayer<GriddedElevationModel, TiledGriddedElevationModelLayerTile>
  implements ElevationModelLayer {
  private Resource baseResource;

  private String fileExtension = ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION;

  private String filePrefix = null;

  private final List<Double> resolutions = Lists.newArray(new double[] {
    2000.0, 1000.0, 500.0, 200.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.0, 1.0
  });

  private int tileSizePixels = 1000;

  private String url;

  private ScaledIntegerGriddedDigitalElevationModelGrid elevationModel;

  private double scaleZ;

  private final int minResolution = 1;

  public TiledGriddedElevationModelLayer() {
    super("tiledGriddedElevationModelLayer");
    setIcon("gridded_dem");
  }

  public TiledGriddedElevationModelLayer(final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  @Override
  public double getElevation(final double x, final double y) {
    if (this.elevationModel == null) {
      final GriddedElevationModel elevationModel = getElevationModel(x, y);
      if (elevationModel == null) {
        return Double.NaN;
      } else {
        final double elevation = elevationModel.getValue(x, y);
        // System.out.println(elevation + "\t" +
        // elevationModel.getElevationBilinear(x, y) + "\t"
        // + elevationModel.getElevationBicubic(x, y));
        return elevation;
      }
    } else {
      final double elevation = this.elevationModel.getValue(x, y);
      // System.out.println(elevation + "\t" +
      // this.elevationModel.getElevationBilinear(x, y) + "\t"
      // + this.elevationModel.getElevationBicubic(x, y));
      return elevation;
    }
  }

  protected GriddedElevationModel getElevationModel(final double x, final double y) {
    final TiledMultipleGriddedElevationModelLayerRenderer renderer = getRenderer();
    final int resolution = (int)renderer.getLayerResolution();
    if (resolution > 0) {
      final int tileSize = resolution * this.tileSizePixels;
      final int tileX = (int)Math.floor(x / tileSize) * tileSize;
      final int tileY = (int)Math.floor(y / tileSize) * tileSize;
      final TiledGriddedElevationModelLayerTile tile = newTile(resolution, tileSize, tileX, tileY);
      final TiledGriddedElevationModelLayerTile cachedTile = renderer.getCachedTile(tile);
      if (cachedTile != null) {
        return cachedTile.getElevationModel();
      }
    }
    return null;
  }

  public String getFileExtension() {
    return this.fileExtension;
  }

  public String getFilePrefix() {
    return this.filePrefix;
  }

  @Override
  public List<TiledGriddedElevationModelLayerTile> getOverlappingMapTiles(
    final AbstractTiledLayerRenderer<?, ?> renderer, final ViewRenderer view) {
    final List<TiledGriddedElevationModelLayerTile> tiles = new ArrayList<>();
    try {
      final int resolution = (int)getResolution(view);
      if (resolution > 0) {
        final int tileSize = resolution * this.tileSizePixels;
        final BoundingBox viewBoundingBox = view.getBoundingBox();
        final BoundingBox maxBoundingBox = getBoundingBox();
        final GeometryFactory geometryFactory = getGeometryFactory();
        final BoundingBox boundingBox = viewBoundingBox.bboxToCs(geometryFactory)
          .bboxIntersection(maxBoundingBox);
        final double minX = boundingBox.getMinX();
        final double minY = boundingBox.getMinY();
        final double maxX = boundingBox.getMaxX();
        final double maxY = boundingBox.getMaxY();

        // Tiles start at the North-West corner of the map
        final int minTileX = (int)Math.floor(minX / tileSize) * tileSize;
        final int minTileY = (int)Math.floor(minY / tileSize) * tileSize;
        final int maxTileX = (int)Math.floor(maxX / tileSize) * tileSize;
        final int maxTileY = (int)Math.floor(maxY / tileSize) * tileSize;

        for (int tileY = minTileY; tileY <= maxTileY; tileY += tileSize) {
          for (int tileX = minTileX; tileX <= maxTileX; tileX += tileSize) {
            final TiledGriddedElevationModelLayerTile tile = newTile(resolution, tileSize, tileX,
              tileY);
            tiles.add(tile);
          }
        }
      }
    } catch (final RuntimeException e) {
      setError(e);
    }
    return tiles;
  }

  @Override
  public double getResolution(final ViewRenderer view) {
    final double metresPerPixel = view.getMetresPerPixel();
    final int count = this.resolutions.size();
    for (int i = 0; i < count - 1; i++) {
      final double resolution1 = this.resolutions.get(i);
      final double resolution2 = this.resolutions.get(i + 1);

      if (metresPerPixel >= resolution1
        || resolution1 - metresPerPixel < (resolution1 - resolution2) * 0.7) {
        // Within 70% of more detailed
        return resolution1;
      }
    }
    return this.resolutions.get(count - 1);
  }

  public int getTileSizePixels() {
    return this.tileSizePixels;
  }

  public String getUrl() {
    return this.url;
  }

  @Override
  protected boolean initializeDo() {
    final boolean initialized = super.initializeDo();
    if (initialized) {
      this.baseResource = Resource.getResource(this.url);
      if (this.baseResource.isFile() && this.baseResource.exists()) {
        final Path basePath = this.baseResource.getPath();
        final int coordinateSystemId = getHorizontalCoordinateSystemId();
        final ScaledIntegerGriddedDigitalElevationModelGrid elevationModel = new ScaledIntegerGriddedDigitalElevationModelGrid(
          basePath, this.filePrefix, coordinateSystemId, this.tileSizePixels, this.minResolution,
          this.scaleZ);
        elevationModel.setCacheChannels(false);
        this.elevationModel = elevationModel;

      }
    }
    return initialized;
  }

  @Override
  public boolean isUseElevationAtScale(final double scale) {
    if (this.elevationModel == null) {
      return ElevationModelLayer.super.isUseElevationAtScale(scale);
    } else {
      return true;
    }
  }

  public GriddedElevationModel newGriddedElevationModel(final int tileSize, final int tileX,
    final int tileY) {
    final String fileName = Strings.toString("_", this.filePrefix,
      getHorizontalCoordinateSystemId(), tileSize, tileX, tileY) + "." + this.fileExtension;
    final Resource path = this.baseResource //
      .createRelative(this.fileExtension) //
      .createRelative(getHorizontalCoordinateSystemId()) //
      .createRelative(tileSize) //
      .createRelative(tileX) //
      .createRelative(fileName);
    return GriddedElevationModel.newGriddedElevationModel(path);
  }

  @Override
  public TabbedValuePanel newPropertiesPanel() {
    final TabbedValuePanel propertiesPanel = super.newPropertiesPanel();
    newPropertiesPanelStyle(propertiesPanel);
    return propertiesPanel;
  }

  protected void newPropertiesPanelStyle(final TabbedValuePanel propertiesPanel) {
    if (getRenderer() != null) {
      final LayerStylePanel stylePanel = new LayerStylePanel(this);
      propertiesPanel.addTab("Style", "palette", stylePanel);
    }
  }

  @Override
  protected ValueField newPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.newPropertiesTabGeneralPanelSource(parent);

    if (this.url.startsWith("file:")) {
      final String fileName = this.url.replaceFirst("file:(//)?", "");
      SwingUtil.addLabelledReadOnlyTextField(panel, "Base Directory", fileName);
    } else {
      SwingUtil.addLabelledReadOnlyTextField(panel, "Base URL", this.url);
    }
    SwingUtil.addLabelledReadOnlyTextField(panel, "File Prefix", this.filePrefix);
    if (Property.hasValue(this.fileExtension)) {
      SwingUtil.addLabelledReadOnlyTextField(panel, "File Extension", this.fileExtension);
      final GriddedElevationModelReaderFactory factory = IoFactory
        .factoryByFileExtension(GriddedElevationModelReaderFactory.class, this.fileExtension);
      if (factory != null) {
        SwingUtil.addLabelledReadOnlyTextField(panel, "File Type", factory.getName());
      }
    }
    SwingUtil.addLabelledReadOnlyTextField(panel, "Tile Size Pixels", this.tileSizePixels);
    GroupLayouts.makeColumns(panel, 2, true);
    return panel;
  }

  @Override
  protected AbstractTiledLayerRenderer<GriddedElevationModel, TiledGriddedElevationModelLayerTile> newRenderer() {
    return new TiledMultipleGriddedElevationModelLayerRenderer(this);
  }

  @Override
  public BufferedGeoreferencedImage newRenderImage() {
    return BufferedGeoreferencedImage.newImage(BoundingBox.empty(), this.tileSizePixels,
      this.tileSizePixels);
  }

  protected TiledGriddedElevationModelLayerTile newTile(final int resolution, final int tileSize,
    final int tileX, final int tileY) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int coordinateSystemId = getHorizontalCoordinateSystemId();
    final BoundingBox tileBoundingBox = geometryFactory.newBoundingBox(2, tileX, tileY,
      tileX + tileSize, tileY + tileSize);
    return new TiledGriddedElevationModelLayerTile(this, tileBoundingBox, coordinateSystemId,
      tileSize, resolution, tileX, tileY);
  }

  @Override
  public void setBoundingBox(final BoundingBox boundingBox) {
    super.setBoundingBox(boundingBox);
  }

  public void setFileExtension(final String fileExtension) {
    this.fileExtension = fileExtension;
  }

  public void setFilePrefix(final String filePrefix) {
    this.filePrefix = filePrefix;
  }

  @Override
  public GeometryFactory setGeometryFactoryDo(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      GeometryFactory geometryFactory3d;
      if (geometryFactory.getAxisCount() == 2) {
        final double scaleX = geometryFactory.getScaleX();
        final double scaleY = geometryFactory.getScaleY();
        final double[] scales = {
          scaleX, scaleY, 1000.0
        };
        geometryFactory3d = geometryFactory.convertAxisCountAndScales(3, scales);
      } else {
        geometryFactory3d = geometryFactory.convertAxisCount(3);
      }
      this.scaleZ = geometryFactory3d.getScaleZ();

      final GeometryFactory newGeometryFactory = super.setGeometryFactoryDo(geometryFactory3d);
      setBoundingBox(newGeometryFactory.getAreaBoundingBox());
      final TiledMultipleGriddedElevationModelLayerRenderer renderer = getRenderer();
      if (renderer != null) {
        renderer.updateBoundingBox();
      }
      return newGeometryFactory;
    }
    return super.getGeometryFactory();
  }

  @SuppressWarnings("unchecked")
  public void setStyle(Object style) {
    if (style instanceof Map) {
      final Map<String, Object> map = (Map<String, Object>)style;
      style = MapObjectFactory.toObject(map);
    }

    if (style instanceof TiledMultipleGriddedElevationModelLayerRenderer) {
      final TiledMultipleGriddedElevationModelLayerRenderer renderer = (TiledMultipleGriddedElevationModelLayerRenderer)style;
      setRenderer(renderer);
    } else if (style instanceof RasterizerGriddedElevationModelLayerRenderer) {
      final TiledMultipleGriddedElevationModelLayerRenderer renderer = new TiledMultipleGriddedElevationModelLayerRenderer(
        this, (RasterizerGriddedElevationModelLayerRenderer)style);
      setRenderer(renderer);
    } else {
      Logs.error(this, "Cannot create renderer for: " + style);
    }
  }

  public void setTileSizePixels(final int tileSizePixels) {
    this.tileSizePixels = tileSizePixels;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "geometryFactory", getGeometryFactory());
    addToMap(map, "url", this.url);
    addToMap(map, "fileExtension", this.fileExtension);
    addToMap(map, "filePrefix", this.filePrefix);
    addToMap(map, "tileSizePixels", this.tileSizePixels);
    return map;
  }
}
