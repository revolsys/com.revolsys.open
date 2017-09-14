package com.revolsys.swing.map.layer.elevation.gridded;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.list.Lists;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.compactbinary.CompactBinaryGriddedElevation;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.logging.Logs;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.style.panel.LayerStylePanel;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayer;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayerRenderer;
import com.revolsys.util.Strings;

public class TiledGriddedElevationModelLayer
  extends AbstractTiledLayer<GriddedElevationModel, TiledGriddedElevationModelLayerTile>
  implements IGriddedElevationModelLayer {
  private final List<Double> resolutions = Lists.newArray(2000.0, 1000.0, 500.0, 200.0, 100.0, 50.0,
    20.0, 10.0, 5.0, 2.0, 1.0);

  private int tileSizePixels = 1000;

  private final Resource basePath = new PathResource("/Data/elevation/griddedDem");

  private String filePrefix = "bc_gridded_dem";

  private String fileExtension = CompactBinaryGriddedElevation.FILE_EXTENSION;

  public TiledGriddedElevationModelLayer() {
    super("tiledGriddedElevationModelLayer");
    setGeometryFactory(GeometryFactory.fixed(3005, 1.0, 1.0, 1000.0));
  }

  public TiledGriddedElevationModelLayer(final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  @Override
  public double getElevation(final double x, final double y) {
    final GriddedElevationModel elevationModel = getElevationModel(x, y);
    if (elevationModel == null) {
      return Double.NaN;
    } else {
      return elevationModel.getElevation(x, y);
    }
  }

  protected GriddedElevationModel getElevationModel(final double x, final double y) {
    final TiledMultipleGriddedElevationModelLayerRenderer renderer = getRenderer();
    final int resolution = (int)renderer.getResolution();
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
    final Viewport2D viewport) {
    final List<TiledGriddedElevationModelLayerTile> tiles = new ArrayList<>();
    try {
      final int resolution = (int)getResolution(viewport);
      if (resolution > 0) {
        final int tileSize = resolution * this.tileSizePixels;
        final BoundingBox viewBoundingBox = viewport.getBoundingBox();
        final BoundingBox maxBoundingBox = getBoundingBox();
        final GeometryFactory geometryFactory = getGeometryFactory();
        final BoundingBox boundingBox = viewBoundingBox.convert(geometryFactory)
          .intersection(maxBoundingBox);
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
  public double getResolution(final Viewport2D viewport) {
    final double metresPerPixel = viewport.getUnitsPerPixel();
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

  public GriddedElevationModel newGriddedElevationModel(final int tileSize, final int tileX,
    final int tileY) {
    final String fileName = Strings.toString("_", this.filePrefix, getCoordinateSystemId(),
      tileSize, tileX, tileY) + "." + this.fileExtension;
    final Resource path = this.basePath //
      .createRelative(this.fileExtension) //
      .createRelative(getCoordinateSystemId()) //
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
  protected AbstractTiledLayerRenderer<GriddedElevationModel, TiledGriddedElevationModelLayerTile> newRenderer() {
    return new TiledMultipleGriddedElevationModelLayerRenderer(this);
  }

  protected TiledGriddedElevationModelLayerTile newTile(final int resolution, final int tileSize,
    final int tileX, final int tileY) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int coordinateSystemId = getCoordinateSystemId();
    final BoundingBox tileBoundingBox = geometryFactory.newBoundingBox(2, tileX, tileY,
      tileX + tileSize, tileY + tileSize);
    return new TiledGriddedElevationModelLayerTile(this, tileBoundingBox, coordinateSystemId,
      tileSize, resolution, tileX, tileY);
  }

  @Override
  protected void setBoundingBox(final BoundingBox boundingBox) {
    super.setBoundingBox(boundingBox);
  }

  public void setFileExtension(final String fileExtension) {
    this.fileExtension = fileExtension;
  }

  public void setFilePrefix(final String filePrefix) {
    this.filePrefix = filePrefix;
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
    } else {
      Logs.error(this, "Cannot create renderer for: " + style);
    }
  }

  public void setTileSizePixels(final int tileSizePixels) {
    this.tileSizePixels = tileSizePixels;
  }

}
