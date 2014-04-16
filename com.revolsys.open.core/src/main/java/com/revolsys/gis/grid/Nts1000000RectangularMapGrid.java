package com.revolsys.gis.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.PrecisionModel;

public class Nts1000000RectangularMapGrid extends AbstractRectangularMapGrid {

  private final GeometryFactory geometryFactory = GeometryFactory.wgs84();

  private static final Pattern NAME_PATTERN = Pattern.compile("^"
    + NtsConstants.REGEX_1000000 + ".*");

  private PrecisionModel precisionModel = new PrecisionModel(1);

  private final double tileHeight;

  private final double tileWidth;

  public Nts1000000RectangularMapGrid() {
    this(NtsConstants.WIDTH_1000000, NtsConstants.HEIGHT_1000000);
  }

  public Nts1000000RectangularMapGrid(final double width, final double height) {
    this.tileWidth = width;
    this.tileHeight = height;
    setName("NTS 1:1 000 000");
  }

  public int getBlock(final String sheet) {
    return Integer.parseInt(sheet.substring(0, sheet.length() - 4));
  }

  public BoundingBox getBoundingBox(final String mapTileName) {
    final double lat = getLatitude(mapTileName);
    final double lon = getLongitude(mapTileName);
    return new Envelope(getGeometryFactory(), lon, lat, lon - tileWidth, lat
      + tileHeight);
  }

  @Override
  public CoordinateSystem getCoordinateSystem() {
    return geometryFactory.getCoordinateSystem();
  }

  @Override
  public String getFormattedMapTileName(final String name) {
    return name;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public double getLatitude(final int block) {
    final int index = block % 10;
    return NtsConstants.MAX_LATITUDE + index * NtsConstants.HEIGHT_1000000;
  }

  public double getLatitude(final String mapTileName) {
    final int block = getNtsBlock(mapTileName);
    return getLatitude(block);
  }

  public double getLongitude(final int block) {
    final int index = block / 10;
    return NtsConstants.MAX_LONGITUDE - index * NtsConstants.WIDTH_1000000;
  }

  public double getLongitude(final String mapTileName) {
    final int block = getNtsBlock(mapTileName);
    return getLongitude(block);
  }

  @Override
  public String getMapTileName(final double x, final double y) {
    final int lonRound = (int)Math.ceil(x);
    final int lonRowIndex = (int)(lonRound - NtsConstants.MAX_LONGITUDE);
    final int lonIndexCol = (int)(-lonRowIndex / NtsConstants.WIDTH_1000000);
    final int colIndex = (lonIndexCol) * 10;

    final int latRound = (int)Math.floor(y);
    final int latIndexRow = (int)(latRound - NtsConstants.MAX_LATITUDE);
    final int rowIndex = (int)(latIndexRow / NtsConstants.HEIGHT_1000000);

    final int block = (rowIndex + colIndex);
    return String.valueOf(block);

  }

  /**
   * Get the sheet which is the specified number of sheets east and/or north
   * from the current sheet.
   * 
   * @param sheet The current sheet.
   * @param east The number of sheets east.
   * @param north The number of sheets north.
   * @return The new map sheet.
   */
  public String getMapTileName(final String sheet, final int east,
    final int north) {
    final double sourceLon = getLongitude(sheet);
    final double sourceLat = getLatitude(sheet);
    final double lon = precisionModel.makePrecise(sourceLon + east
      * getTileWidth());
    final double lat = sourceLat + north * getTileHeight();
    return getMapTileName(lon, lat);
  }

  public int getNtsBlock(final String mapTileName) {
    if (StringUtils.hasText(mapTileName)) {
      final Matcher matcher = NAME_PATTERN.matcher(mapTileName);
      if (matcher.matches()) {
        final String name = matcher.group(1);
        final int block = Integer.parseInt(name);
        return block;
      }
    }
    throw new IllegalArgumentException(mapTileName
      + " does not start with a valid NTS block");
  }

  public PrecisionModel getPrecisionModel() {
    return precisionModel;
  }

  @Override
  public RectangularMapTile getTileByLocation(final double x, final double y) {
    final String mapTileName = getMapTileName(x, y);
    final String formattedMapTileName = getFormattedMapTileName(mapTileName);
    final BoundingBox boundingBox = getBoundingBox(mapTileName);
    return new SimpleRectangularMapTile(this, formattedMapTileName,
      mapTileName, boundingBox);
  }

  @Override
  public RectangularMapTile getTileByName(final String mapTileName) {
    final BoundingBox boundingBox = getBoundingBox(mapTileName);
    final double lon = boundingBox.getMaxX();
    final double lat = boundingBox.getMinY();
    final String name = getMapTileName(lon, lat);
    final String formattedMapTileName = getFormattedMapTileName(mapTileName);
    return new SimpleRectangularMapTile(this, formattedMapTileName, name,
      boundingBox);
  }

  @Override
  public double getTileHeight() {
    return tileHeight;
  }

  @Override
  public List<RectangularMapTile> getTiles(final BoundingBox boundingBox) {
    final BoundingBox envelope = boundingBox.convert(getGeometryFactory());
    final List<RectangularMapTile> tiles = new ArrayList<RectangularMapTile>();
    final int minXCeil = (int)Math.ceil(envelope.getMinX() / tileWidth);
    final double minX = minXCeil * tileWidth;

    final int maxXCeil = (int)Math.ceil(envelope.getMaxX() / tileWidth) + 1;

    final int minYFloor = (int)Math.floor(envelope.getMinY() / tileHeight);
    final double minY = minYFloor * tileHeight;

    final int maxYCeil = (int)Math.ceil(envelope.getMaxY() / tileHeight);

    final int numX = maxXCeil - minXCeil;
    final int numY = maxYCeil - minYFloor;
    final int max = 100;
    if (numX > max || numY > max) {
      LoggerFactory.getLogger(getClass()).error(
        "Request would return too many tiles width=" + numX + " (max=" + max
          + ") height=" + numY + "(max=" + max + ").");
      return tiles;
    }
    for (int y = 0; y < numY; y++) {
      final double lat = minY + y * tileHeight;
      for (int x = 0; x < numX; x++) {
        final double lon = minX + x * tileWidth;
        final RectangularMapTile tile = getTileByLocation(lon, lat);
        tiles.add(tile);
      }
    }
    return tiles;
  }

  @Override
  public double getTileWidth() {
    return tileWidth;
  }

  public void setPrecisionModel(final PrecisionModel precisionModel) {
    this.precisionModel = precisionModel;
  }
}
