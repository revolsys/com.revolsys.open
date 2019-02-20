package com.revolsys.gis.grid;

public class Bcgs1000RectangularMapGridTest extends Bcgs2000RectangularMapGridTest {
  private static final RectangularMapGrid GRID = new Bcgs1000RectangularMapGrid();

  private static final double TILE_HEIGHT = BcgsConstants.HEIGHT_1000;

  private static final double TILE_WIDTH = BcgsConstants.WIDTH_1000;

  protected void doTestBcgs1000ByName(final String parentTileName, final double parentLon,
    final double parentLat) {
    checkTileByName(GRID, parentTileName, parentLon, parentLat, TILE_WIDTH, TILE_HEIGHT);
  }

  @Override
  protected void doTestBcgs2000ByName(final String parentTileName, final double parentLon,
    final double parentLat) {
    for (int number = 1; number <= 4; number++) {
      final double lon = parentLon - GridUtil.getNumberCol4(number) * TILE_WIDTH;
      final double lat = parentLat + GridUtil.getNumberRow4(number) * TILE_HEIGHT;

      final String tileName = parentTileName + "." + number;
      doTestBcgs1000ByName(tileName, lon, lat);
    }
  }
}
