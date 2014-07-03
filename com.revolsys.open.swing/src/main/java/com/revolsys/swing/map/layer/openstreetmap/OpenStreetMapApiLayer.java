package com.revolsys.swing.map.layer.openstreetmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.map.InvokeMethodMapObjectFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.openstreetmap.model.OsmConstants;
import com.revolsys.io.openstreetmap.model.OsmDocument;
import com.revolsys.io.openstreetmap.model.OsmElement;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.swing.map.layer.record.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.util.MathUtil;

public class OpenStreetMapApiLayer extends AbstractDataObjectLayer {

  public static AbstractDataObjectLayer create(
    final Map<String, Object> properties) {
    return new OpenStreetMapApiLayer(properties);
  }

  private String serverUrl = "http://api.openstreetmap.org/";

  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "openStreetMapVectorApi", "Open Street Map (Vector API)",
    OpenStreetMapApiLayer.class, "create");

  private static final int TILE_SCALE_X = 50;

  private static final double TILE_WIDTH = 1.0 / TILE_SCALE_X;

  private static final int TILE_SCALE_Y = 100;

  private static final double TILE_HEIGHT = 1.0 / TILE_SCALE_Y;

  private final Map<BoundingBox, OsmDocument> boundingBoxTileMap = new HashMap<>();

  public OpenStreetMapApiLayer(final Map<String, Object> properties) {
    super(properties);
    setType("openStreetMapVectorApi");
  }

  @Override
  protected List<LayerRecord> doQuery(final BoundingBox boundingBox) {
    final Map<Identifier, LayerRecord> recordMap = new HashMap<>();
    final List<BoundingBox> boundingBoxes = getTileBoundingBoxes(boundingBox);
    for (final BoundingBox tileBoundingBox : boundingBoxes) {
      final OsmDocument document = getTile(tileBoundingBox);
      for (final OsmElement record : document.getRecords()) {
        final Geometry geometry = record.getGeometryValue();
        if (geometry != null && !geometry.isEmpty()) {
          if (boundingBox.intersects(geometry.getBoundingBox())) {
            final Identifier identifier = record.getIdentifier();
            final OsmProxyLayerRecord layerRecord = new OsmProxyLayerRecord(
              this, document, identifier);
            recordMap.put(identifier, layerRecord);
          }
        }
      }
    }
    this.boundingBoxTileMap.keySet().retainAll(boundingBoxes);
    return new ArrayList<>(recordMap.values());
  }

  @Override
  protected List<LayerRecord> doQuery(final Query query) {
    return Collections.emptyList();
  }

  @Override
  public RecordDefinition getMetaData() {
    return OsmElement.META_DATA;
  }

  public String getServerUrl() {
    return this.serverUrl;
  }

  private synchronized OsmDocument getTile(final BoundingBox boundingBox) {
    OsmDocument document = this.boundingBoxTileMap.get(boundingBox);
    if (document == null) {
      document = OsmDocument.create(this.serverUrl, boundingBox);
      this.boundingBoxTileMap.put(boundingBox, document);
    }
    return document;
  }

  public List<BoundingBox> getTileBoundingBoxes(BoundingBox boundingBox) {
    boundingBox = boundingBox.convert(OsmConstants.WGS84_2D);
    final List<BoundingBox> boundingBoxes = new ArrayList<>();
    final double minX = Math.floor(boundingBox.getMinX() * TILE_SCALE_X)
      / TILE_SCALE_X;
    final double minY = Math.floor(boundingBox.getMinY() * TILE_SCALE_Y)
      / TILE_SCALE_Y;
    final double maxX = Math.ceil(boundingBox.getMaxX() * TILE_SCALE_X)
      / TILE_SCALE_X;
    final double maxY = Math.ceil(boundingBox.getMaxY() * TILE_SCALE_Y)
      / TILE_SCALE_Y;
    int indexY = 0;
    for (double y = minY; y < maxY;) {
      int indexX = 0;
      indexY++;
      final double nextY = MathUtil.makePrecise(TILE_SCALE_Y, minY + indexY
        * TILE_HEIGHT);
      for (double x = minX; x < maxX;) {
        indexX++;
        final double nextX = MathUtil.makePrecise(TILE_SCALE_X, minX + indexX
          * TILE_WIDTH);
        final BoundingBoxDoubleGf tileBoundingBox = new BoundingBoxDoubleGf(
          OsmConstants.WGS84_2D, 2, x, y, nextX, nextY);
        boundingBoxes.add(tileBoundingBox);
        x = nextX;
      }
      y = nextY;
    }
    return boundingBoxes;
  }

  public void setServerUrl(final String serverUrl) {
    this.serverUrl = serverUrl;
  }

}
