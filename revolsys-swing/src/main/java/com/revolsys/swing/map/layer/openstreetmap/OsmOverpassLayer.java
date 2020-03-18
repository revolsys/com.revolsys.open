package com.revolsys.swing.map.layer.openstreetmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.number.Doubles;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.io.format.openstreetmap.model.OsmConstants;
import com.revolsys.record.io.format.openstreetmap.model.OsmDocument;
import com.revolsys.record.io.format.openstreetmap.model.OsmElement;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.util.Property;

public class OsmOverpassLayer extends AbstractRecordLayer {
  private static final int TILE_SCALE_X = 50;

  private static final int TILE_SCALE_Y = 50;

  private static final double TILE_HEIGHT = 1.0 / TILE_SCALE_Y;

  private static final double TILE_WIDTH = 1.0 / TILE_SCALE_X;

  public static AbstractLayer newLayer(final Map<String, Object> properties) {
    return new OsmOverpassLayer(properties);
  }

  private final Map<BoundingBox, OsmDocument> boundingBoxTileMap = new HashMap<>();

  private String serverUrl = "http://api.openstreetmap.org/";

  public OsmOverpassLayer(final Map<String, Object> properties) {
    super("openStreetMapVectorApi");
    setProperties(properties);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return OsmElement.RECORD_DEFINITION;
  }

  @Override
  public List<LayerRecord> getRecords(BoundingBox boundingBox) {
    if (hasGeometryField()) {
      boundingBox = convertBoundingBox(boundingBox);
      if (Property.hasValue(boundingBox)) {
        final Map<Identifier, LayerRecord> recordMap = new HashMap<>();
        final List<BoundingBox> boundingBoxes = getTileBoundingBoxes(boundingBox);
        for (final BoundingBox tileBoundingBox : boundingBoxes) {
          final OsmDocument document = getTile(tileBoundingBox);
          for (final OsmElement record : document.getRecords()) {
            final Geometry geometry = record.getGeometry();
            if (geometry != null && !geometry.isEmpty()) {
              if (boundingBox.bboxIntersects(geometry.getBoundingBox())) {
                final Identifier identifier = record.getIdentifier();
                // final OsmProxyLayerRecord layerRecord = new
                // OsmProxyLayerRecord(
                // this, document, identifier);
                // recordMap.put(identifier, layerRecord);
              }
            }
          }
        }
        this.boundingBoxTileMap.keySet().retainAll(boundingBoxes);
        return new ArrayList<>(recordMap.values());
      }
    }
    return Collections.emptyList();
  }

  public String getServerUrl() {
    return this.serverUrl;
  }

  private synchronized OsmDocument getTile(final BoundingBox boundingBox) {
    OsmDocument document = this.boundingBoxTileMap.get(boundingBox);
    if (document == null) {
      document = OsmDocument.newDocument(this.serverUrl, boundingBox);
      this.boundingBoxTileMap.put(boundingBox, document);
    }
    return document;
  }

  public List<BoundingBox> getTileBoundingBoxes(BoundingBox boundingBox) {
    boundingBox = boundingBox.bboxToCs(OsmConstants.WGS84_2D);
    final List<BoundingBox> boundingBoxes = new ArrayList<>();
    final double minX = Math.floor(boundingBox.getMinX() * TILE_SCALE_X) / TILE_SCALE_X;
    final double minY = Math.floor(boundingBox.getMinY() * TILE_SCALE_Y) / TILE_SCALE_Y;
    final double maxX = Math.ceil(boundingBox.getMaxX() * TILE_SCALE_X) / TILE_SCALE_X;
    final double maxY = Math.ceil(boundingBox.getMaxY() * TILE_SCALE_Y) / TILE_SCALE_Y;
    int indexY = 0;
    for (double y = minY; y < maxY;) {
      int indexX = 0;
      indexY++;
      final double nextY = Doubles.makePrecise(TILE_SCALE_Y, minY + indexY * TILE_HEIGHT);
      for (double x = minX; x < maxX;) {
        indexX++;
        final double nextX = Doubles.makePrecise(TILE_SCALE_X, minX + indexX * TILE_WIDTH);
        final BoundingBox tileBoundingBox = OsmConstants.WGS84_2D.newBoundingBox(x, y, nextX,
          nextY);
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
