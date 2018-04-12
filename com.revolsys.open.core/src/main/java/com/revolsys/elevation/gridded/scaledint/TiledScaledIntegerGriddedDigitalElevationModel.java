package com.revolsys.elevation.gridded.scaledint;

import java.util.Map;

import com.revolsys.collection.map.LruMap;
import com.revolsys.elevation.gridded.AbstractGriddedElevationModel;
import com.revolsys.elevation.gridded.DirectFileElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.IntPair;

public class TiledScaledIntegerGriddedDigitalElevationModel extends AbstractGriddedElevationModel {

  private int gridTileWidth;

  private int gridTileHeight;

  private Resource baseResource;

  private int coordinateSystemId;

  private final Map<IntPair, DirectFileElevationModel> models = new LruMap<>(5000);

  public TiledScaledIntegerGriddedDigitalElevationModel() {
  }

  public TiledScaledIntegerGriddedDigitalElevationModel(final Resource baseResource,
    final String fileExtension, final GeometryFactory geometryFactory, final double minX,
    final double minY, final int gridTileWidth, final int gridTileHeight, final int gridCellSize) {
    super(geometryFactory, minX, minY, Integer.MAX_VALUE, Integer.MAX_VALUE, gridCellSize);
    this.gridTileWidth = gridTileWidth;
    this.gridTileHeight = gridTileHeight;
    this.coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
    final Resource fileExtensionDirectory = baseResource.createRelative("demcs");
    final Resource coordinateSystemDirectory = fileExtensionDirectory
      .createRelative(Integer.toString(this.coordinateSystemId));
    final Resource resolutionDirectory = coordinateSystemDirectory
      .createRelative(gridCellSize + "m");
    this.baseResource = resolutionDirectory;
  }

  @Override
  public void clear() {
  }

  @Override
  public double getElevationFast(final int gridX, final int gridY) {
    final double gridCellSize = getGridCellSize();
    final int tileMinGridX = Math.floorDiv(gridX, this.gridTileWidth) * this.gridTileWidth;
    final int tileMinGridY = Math.floorDiv(gridY, this.gridTileHeight) * this.gridTileHeight;

    final IntPair key = new IntPair(tileMinGridX, tileMinGridY);
    DirectFileElevationModel model = this.models.get(key);
    if (model == null) {

      final int tileMinX = (int)(tileMinGridX * gridCellSize);
      final int tileMinY = (int)(tileMinGridY * gridCellSize);
      model = new ScaledIntegerGriddedDigitalElevationModelFile(this.baseResource.toPath(),
        getGeometryFactory(), tileMinX, tileMinY, this.gridTileWidth, this.gridTileHeight,
        gridCellSize);
      this.models.put(key, model);
    }

    final int gridCellX = gridX - tileMinGridX;
    final int gridCellY = gridY - tileMinGridY;

    return model.getElevation(gridCellX, gridCellY);
    // final int offset = ScaledIntegerGriddedDigitalElevation.HEADER_SIZE
    // + (gridCellY * this.gridSize + gridCellX) * this.elevationByteCount;
    // double elevation;
    // if (this.isPath) {
    // final Path path = resource.toPath();
    // try (
    // SeekableByteChannel byteChannel = Files.newByteChannel(path,
    // StandardOpenOption.READ)) {
    // byteChannel.position(offset);
    // if (this.floatingPoint) {
    // final ByteBuffer bytes = ByteBuffer.allocate(4);
    // byteChannel.read(bytes);
    // elevation = bytes.getFloat(0);
    // } else {
    // final ByteBuffer bytes = ByteBuffer.allocate(2);
    // byteChannel.read(bytes);
    // elevation = bytes.getShort(0);
    // }
    // }
    // } else {
    // try (
    // DataInputStream in =
    // resource.newBufferedInputStream(DataInputStream::new)) {
    // in.skip(offset);
    // if (this.floatingPoint) {
    // elevation = in.readFloat();
    // } else {
    // elevation = in.readShort();
    // }
    // } catch (final IOException e) {
    // throw Exceptions.wrap("Unable to read: " + resource, e);
    // }
    // }
    // return elevation;
  }

  public int getGridTileHeight() {
    return this.gridTileHeight;
  }

  public int getGridTileWidth() {
    return this.gridTileWidth;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isNull(final int x, final int y) {
    return false;
  }

  @Override
  public GriddedElevationModel newElevationModel(final GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final double gridCellSize) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setElevation(final int x, final int y, final double elevation) {
  }

  public void setGridTileHeight(final int gridTileHeight) {
    this.gridTileHeight = gridTileHeight;
  }

  public void setGridTileWidth(final int gridTileWidth) {
    this.gridTileWidth = gridTileWidth;
  }

}
