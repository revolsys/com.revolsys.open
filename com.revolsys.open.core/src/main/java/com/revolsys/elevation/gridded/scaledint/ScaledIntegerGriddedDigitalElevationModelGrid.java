package com.revolsys.elevation.gridded.scaledint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.BaseCloseable;

public class ScaledIntegerGriddedDigitalElevationModelGrid {

  private class FileChannelHolder implements BaseCloseable {
    private final int tileX;

    private final int tileY;

    private FileChannel fileChannel;

    private final AtomicInteger openCount = new AtomicInteger(1);

    private final AtomicInteger closeCount = new AtomicInteger(0);

    private final ByteBuffer bytes = ByteBuffer.allocateDirect(4);

    private final Path path;

    public FileChannelHolder(final int tileX, final int tileY, final Path path) throws IOException {
      this.tileX = tileX;
      this.tileY = tileY;
      this.path = path;
      this.fileChannel = newChannel();
    }

    @Override
    public void close() {
      synchronized (ScaledIntegerGriddedDigitalElevationModelGrid.this.channelsByXandY) {
        final int closedCount = this.closeCount.incrementAndGet();
        if (!ScaledIntegerGriddedDigitalElevationModelGrid.this.cacheChannels) {
          if (closedCount == this.openCount.get()) {
            closeDo();
          }
        }
      }
    }

    public synchronized void closeDo() {
      if (this.fileChannel != null) {
        try {
          this.fileChannel.close();
        } catch (final Exception e) {
        }
        this.fileChannel = null;
      }
    }

    @Override
    protected void finalize() throws Throwable {
      this.fileChannel.close();
    }

    public synchronized int getInt(final long offset) throws IOException {
      if (this.fileChannel == null) {
        this.fileChannel = newChannel();
      }
      this.bytes.rewind();
      this.fileChannel.read(this.bytes, offset);
      return this.bytes.getInt(0);
    }

    public FileChannel newChannel() throws IOException {
      return FileChannel.open(this.path, StandardOpenOption.READ);
    }

    public FileChannelHolder open() {
      synchronized (ScaledIntegerGriddedDigitalElevationModelGrid.this.channelsByXandY) {
        this.openCount.incrementAndGet();
        return this;
      }
    }

    private boolean remove() {
      if (this.openCount.get() == this.closeCount.get()) {
        ScaledIntegerGriddedDigitalElevationModelGrid.this.channelsByXandY.get(this.tileX)
          .remove(this.tileY);
        closeDo();
        return true;
      } else {
        return false;
      }
    }
  }

  private final double scaleZ;

  private final IntHashMap<IntHashMap<FileChannelHolder>> channelsByXandY = new IntHashMap<>();

  private final int gridSizePixels;

  private final int gridCellSize;

  private final int gridTileSize;

  private final int coordinateSystemId;

  private final Path tileBasePath;

  private final String filePrefix;

  private final int maxOpenChannels = 10000;

  private final LinkedList<FileChannelHolder> channels = new LinkedList<>();

  private boolean cacheChannels = true;

  public ScaledIntegerGriddedDigitalElevationModelGrid(final Path basePath, final String filePrefix,
    final int coordinateSystemId, final int gridTileSize, final int gridCellSize,
    final double scaleZ) {
    this.coordinateSystemId = coordinateSystemId;
    this.gridTileSize = gridTileSize;
    this.gridCellSize = gridCellSize;
    this.filePrefix = filePrefix;
    this.scaleZ = scaleZ;
    this.gridSizePixels = gridTileSize / gridCellSize;
    this.tileBasePath = basePath//
      .resolve(ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION)//
      .resolve(Integer.toString(coordinateSystemId))//
      .resolve(Integer.toString(gridTileSize))//
    ;
  }

  public double getElevation(final double x, final double y) {
    final int tileX = (int)Math.floor(x / this.gridTileSize) * this.gridTileSize;
    final int tileY = (int)Math.floor(y / this.gridTileSize) * this.gridTileSize;

    try {
      final int gridCellX = GriddedElevationModel.getGridCellX(tileX, this.gridCellSize, x);
      final int gridCellY = GriddedElevationModel.getGridCellY(tileY, this.gridCellSize, y);
      final int elevationByteSize = 4;
      final int offset = ScaledIntegerGriddedDigitalElevation.HEADER_SIZE
        + (gridCellY * this.gridSizePixels + gridCellX) * elevationByteSize;
      try (
        FileChannelHolder channelHolder = getFileChannel(tileX, tileY)) {
        final int elevationInt = channelHolder.getInt(offset);
        if (elevationInt == Integer.MIN_VALUE) {
          return Double.NaN;
        } else {
          return elevationInt / this.scaleZ;
        }
      }
    } catch (final NoSuchFileException e) {
      return Double.NaN;
    } catch (final IOException e) {
      return Double.NaN;
    }
  }

  private FileChannelHolder getFileChannel(final int tileX, final int tileY) throws IOException {
    synchronized (this.channelsByXandY) {

      IntHashMap<FileChannelHolder> channelsByY = this.channelsByXandY.get(tileX);
      if (channelsByY == null) {
        channelsByY = new IntHashMap<>();
        this.channelsByXandY.put(tileX, channelsByY);
      }
      FileChannelHolder fileChannelHolder = channelsByY.get(tileY);
      if (fileChannelHolder == null) {
        final StringBuilder fileNameBuilder = new StringBuilder(this.filePrefix);
        fileNameBuilder.append('_');
        fileNameBuilder.append(this.coordinateSystemId);
        fileNameBuilder.append('_');
        fileNameBuilder.append(this.gridTileSize);
        fileNameBuilder.append('_');
        fileNameBuilder.append(tileX);
        fileNameBuilder.append('_');
        fileNameBuilder.append(tileY);
        fileNameBuilder.append('.');
        fileNameBuilder.append(ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION);
        final String fileName = fileNameBuilder.toString();
        final Path path = this.tileBasePath//
          .resolve(Integer.toString(tileX))//
          .resolve(fileName);

        fileChannelHolder = new FileChannelHolder(tileX, tileY, path);
        channelsByY.put(tileY, fileChannelHolder);
        this.channels.add(fileChannelHolder);
        if (this.channels.size() > this.maxOpenChannels) {
          for (final Iterator<FileChannelHolder> iterator = this.channels.iterator(); this.channels
            .size() > this.maxOpenChannels && iterator.hasNext();) {
            final FileChannelHolder channelHolder = iterator.next();
            if (channelHolder.remove()) {
              iterator.remove();
            }
          }
        }
        return fileChannelHolder;
      } else {
        return fileChannelHolder.open();
      }
    }
  }

  public boolean isCacheChannels() {
    return this.cacheChannels;
  }

  public void setCacheChannels(final boolean cacheChannels) {
    this.cacheChannels = cacheChannels;
  }

  @SuppressWarnings("unchecked")
  public <G extends Geometry> G setElevations(final G geometry) {
    final GeometryEditor<?> editor = geometry.newGeometryEditor();
    editor.setAxisCount(3);
    for (final Vertex vertex : geometry.vertices()) {
      final double x = vertex.getX();
      final double y = vertex.getY();
      final double elevation = getElevation(x, y);
      if (Double.isFinite(elevation)) {
        final int[] vertexId = vertex.getVertexId();
        editor.setZ(vertexId, elevation);
      }
    }
    return (G)editor.newGeometry();
  }
}
