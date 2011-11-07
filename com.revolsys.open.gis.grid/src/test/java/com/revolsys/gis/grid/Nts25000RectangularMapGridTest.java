package com.revolsys.gis.grid;

public class Nts25000RectangularMapGridTest extends
  Nts50000RectangularMapGridTest {
  private static final RectangularMapGrid GRID = new Nts25000RectangularMapGrid();

  private static final double TILE_HEIGHT = NtsConstants.HEIGHT_25000;

  private static final double TILE_WIDTH = NtsConstants.WIDTH_25000;

  @Override
  protected void doTestNts50000(
    final String parentTileName,
    final double parentLon,
    final double parentLat) {
    for (char letter8 = 'a'; letter8 <= 'h'; letter8++) {
      final double lat = parentLat + GridUtil.getLetter8Row(letter8)
        * NtsConstants.HEIGHT_25000;
      final double lon = parentLon - GridUtil.getLetter8Col(letter8)
        * NtsConstants.WIDTH_25000;

      final String tileName = parentTileName + letter8;
      checkTileByName(GRID, tileName, lon, lat, TILE_WIDTH, TILE_HEIGHT);
    }
  }
}
