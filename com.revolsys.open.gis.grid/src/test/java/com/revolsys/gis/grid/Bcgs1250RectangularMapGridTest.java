package com.revolsys.gis.grid;

public class Bcgs1250RectangularMapGridTest extends
  Bcgs2500RectangularMapGridTest {
  private static final RectangularMapGrid GRID = new Bcgs1250RectangularMapGrid();

  private static final double TILE_HEIGHT = BcgsConstants.HEIGHT_1250;

  private static final double TILE_WIDTH = BcgsConstants.WIDTH_1250;

  protected void doTestBcgs1250ByName(
    final String parentTileName,
    final double parentLon,
    final double parentLat) {
    checkTileByName(GRID, parentTileName, parentLon, parentLat, TILE_WIDTH,
      TILE_HEIGHT);
  }

  @Override
  protected void doTestBcgs2500ByName(
    final String parentTileName,
    final double parentLon,
    final double parentLat) {
    for (int number = 1; number <= 4; number++) {
      final double lon = parentLon - GridUtil.getNumberCol4(number)
        * TILE_WIDTH;
      final double lat = parentLat + GridUtil.getNumberRow4(number)
        * TILE_HEIGHT;

      final String tileName = parentTileName + "." + number;
      doTestBcgs1250ByName(tileName, lon, lat);
    }
  }
}
