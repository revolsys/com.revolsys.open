package com.revolsys.gis.grid;

public class Bcgs5000RectangularMapGridTest extends Bcgs10000RectangularMapGridTest {
  private static final RectangularMapGrid GRID = new Bcgs5000RectangularMapGrid();

  private static final double TILE_HEIGHT = BcgsConstants.HEIGHT_5000;

  private static final double TILE_WIDTH = BcgsConstants.WIDTH_5000;

  @Override
  protected void doTestBcgs10000ByName(final String parentTileName, final double parentLon,
    final double parentLat) {
    for (int number = 1; number <= 4; number++) {
      final double lon = parentLon - GridUtil.getNumberCol4(number) * TILE_WIDTH;
      final double lat = parentLat + GridUtil.getNumberRow4(number) * TILE_HEIGHT;

      final String tileName = parentTileName + "." + number;

      doTestBcgs5000ByName(tileName, lon, lat);
    }
  }

  protected void doTestBcgs5000ByName(final String parentTileName, final double parentLon,
    final double parentLat) {
    checkTileByName(GRID, parentTileName, parentLon, parentLat, TILE_WIDTH, TILE_HEIGHT);
  }
}
