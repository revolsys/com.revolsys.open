package com.revolsys.gis.grid;

import org.junit.Test;

import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.util.Assert;

public class Nts1000000RectangularMapGridTest {
  private static final RectangularMapGrid GRID = new Nts1000000RectangularMapGrid();

  private static final double TILE_HEIGHT = NtsConstants.HEIGHT_1000000;

  private static final double TILE_WIDTH = NtsConstants.WIDTH_1000000;

  protected void checkTileByName(
    final RectangularMapGrid grid,
    final String tileName,
    final double lon,
    final double lat,
    final double tileWidth,
    final double tileHeight) {
    final RectangularMapTile tile = grid.getTileByName(tileName);
    Assert.equals(tileName, tile.getName(), "Tile Name");
    final Envelope envelope = tile.getBoundingBox();
    Assert.equals(lon - tileWidth, envelope.getMinX(), "minX: " + tileName);
    Assert.equals(lon, envelope.getMaxX(), "maxX: " + tileName);
    Assert.equals(lat, envelope.getMinY(), "minY: " + tileName);
    Assert.equals(lat + tileHeight, envelope.getMaxY(), "maxY: " + tileName);
  }

  protected void doTestNts1000000ByName(
    final String parentTileName,
    final double parentLon,
    final double parentLat) {
    checkTileByName(GRID, parentTileName, parentLon, parentLat, TILE_WIDTH,
      TILE_HEIGHT);
  }

  @Test
  public void testGetByName() {
    for (int i = 80; i <= 110; i += 10) {
      for (int j = 2; j <= 4; j++) {
        final double lat = 48 + (j - 2) * 4;
        final double lon = -112 - (i / 10 - 8) * 8;

        final String tileName = String.valueOf(i + j);

        doTestNts1000000ByName(tileName, lon, lat);
      }
    }
  }
}
