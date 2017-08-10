package com.revolsys.elevation.gridded.compactbinary;

import java.util.Map;

import com.revolsys.collection.map.LruMap;
import com.revolsys.elevation.gridded.AbstractGriddedElevationModel;
import com.revolsys.elevation.gridded.DirectFileElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReadFactory;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.IntPair;

public class TiledCompactBinaryGriddedElevationModel extends AbstractGriddedElevationModel {

  private int gridTileWidth;

  private int gridTileHeight;

  private Resource baseResource;

  private int coordinateSystemId;

  private final Map<IntPair, DirectFileElevationModel> models = new LruMap<>(5000);

  private CompactBinaryGriddedElevation factory;

  public TiledCompactBinaryGriddedElevationModel() {
  }

  public TiledCompactBinaryGriddedElevationModel(final Resource baseResource,
    final String fileExtension, final GeometryFactory geometryFactory, final double minX,
    final double minY, final int gridTileWidth, final int gridTileHeight, final int gridCellSize) {
    super(geometryFactory, minX, minY, Integer.MAX_VALUE, Integer.MAX_VALUE, gridCellSize);
    setFileExtension(fileExtension);
    this.gridTileWidth = gridTileWidth;
    this.gridTileHeight = gridTileHeight;
    this.coordinateSystemId = geometryFactory.getCoordinateSystemId();
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
  protected double getElevationDo(final int gridX, final int gridY, final int gridWidth) {
    final double gridCellSize = getGridCellSize();
    final int tileMinGridX = Math.floorDiv(gridX, this.gridTileWidth) * this.gridTileWidth;
    final int tileMinGridY = Math.floorDiv(gridY, this.gridTileHeight) * this.gridTileHeight;

    final IntPair key = new IntPair(tileMinGridX, tileMinGridY);
    DirectFileElevationModel model = this.models.get(key);
    if (model == null) {

      final int tileMinX = (int)(tileMinGridX * gridCellSize);
      final int tileMinY = (int)(tileMinGridY * gridCellSize);
      model = new CompactBinaryGriddedElevationModelFile(this.baseResource.toPath(),
        getGeometryFactory(), tileMinX, tileMinY, this.gridTileWidth, this.gridTileHeight,
        gridCellSize);
      this.models.put(key, model);
    }

    final int gridCellX = gridX - tileMinGridX;
    final int gridCellY = gridY - tileMinGridY;

    return model.getElevation(gridCellX, gridCellY);
    // final int offset = CompactBinaryGriddedElevation.HEADER_SIZE
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setElevation(final int x, final int y, final double elevation) {
  }

  public void setFileExtension(final String fileExtension) {
    this.factory = (CompactBinaryGriddedElevation)IoFactory
      .factoryByFileExtension(GriddedElevationModelReadFactory.class, fileExtension);
  }

  public void setGridTileHeight(final int gridTileHeight) {
    this.gridTileHeight = gridTileHeight;
  }

  public void setGridTileWidth(final int gridTileWidth) {
    this.gridTileWidth = gridTileWidth;
  }

}
