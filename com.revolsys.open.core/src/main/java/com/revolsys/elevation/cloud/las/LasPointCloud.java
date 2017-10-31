package com.revolsys.elevation.cloud.las;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint0Core;
import com.revolsys.elevation.cloud.las.pointformat.LasPointFormat;
import com.revolsys.elevation.cloud.las.zip.ArithmeticDecoder;
import com.revolsys.elevation.cloud.las.zip.LazDecompress;
import com.revolsys.elevation.cloud.las.zip.LazDecompressGpsTime11V1;
import com.revolsys.elevation.cloud.las.zip.LazDecompressGpsTime11V2;
import com.revolsys.elevation.cloud.las.zip.LazDecompressPoint10V1;
import com.revolsys.elevation.cloud.las.zip.LazDecompressPoint10V2;
import com.revolsys.elevation.cloud.las.zip.LazDecompressRgb12V1;
import com.revolsys.elevation.cloud.las.zip.LazDecompressRgb12V2;
import com.revolsys.elevation.cloud.las.zip.LazItemType;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.quadedge.QuadEdgeDelaunayTinBuilder;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.endian.EndianOutputStream;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.spring.resource.InputStreamResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Exceptions;

public class LasPointCloud implements PointCloud<LasPoint>, BaseCloseable, MapSerializer {

  public static void forEachPoint(final Object source, final Consumer<? super LasPoint> action) {
    final Resource resource = Resource.getResource(source);
    try (
      final LasPointCloud pointCloud = new LasPointCloud(resource)) {
      pointCloud.forEachPoint(action);
    }
  }

  private GeometryFactory geometryFactory = GeometryFactory.fixed3d(1000.0, 1000.0, 1000.0);

  private LasPointCloudHeader header;

  private List<LasPoint> points = new ArrayList<>();

  private ChannelReader reader;

  private Resource resource;

  private ZipInputStream zipIn;

  private boolean exists;

  public LasPointCloud(final GeometryFactory geometryFactory) {
    this(LasPointFormat.Core, geometryFactory);
  }

  public LasPointCloud(final LasPointFormat pointFormat, final GeometryFactory geometryFactory) {
    this.setHeader(new LasPointCloudHeader(pointFormat, geometryFactory));
  }

  public LasPointCloud(final Resource resource) {
    this(resource, null);
  }

  public LasPointCloud(final Resource resource, GeometryFactory geometryFactory) {
    this.resource = resource;
    Resource fileResource = resource;
    if (resource.getFileNameExtension().equals("zip")) {
      boolean found = false;
      String baseName = resource.getBaseName();
      if (baseName.endsWith("las")) {
        baseName = baseName.replace(".las", "");
      }
      final String fileName = baseName + ".las";

      this.zipIn = this.resource.newBufferedInputStream(ZipInputStream::new);
      try {
        for (ZipEntry zipEntry = this.zipIn.getNextEntry(); zipEntry != null; zipEntry = this.zipIn
          .getNextEntry()) {
          final String name = zipEntry.getName();
          if (name.equalsIgnoreCase(fileName)) {
            fileResource = new InputStreamResource(this.zipIn);
            found = true;
            break;
          }
        }
      } catch (final IOException e) {
        throw Exceptions.wrap("Error reading: " + resource, e);
      }
      if (found) {
        try {
          final URI prjUrl = new URI("jar", resource.getUri() + "!/" + baseName + ".prj", null);
          final Resource prjResource = new UrlResource(prjUrl);
          final GeometryFactory geometryFactoryFromPrj = EsriCoordinateSystems
            .getGeometryFactory(prjResource);
          if (geometryFactoryFromPrj != null) {
            geometryFactory = geometryFactoryFromPrj;
          }
        } catch (URISyntaxException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } else {
        throw new IllegalArgumentException("Cannot find file: " + resource + "!" + fileName);
      }
    }
    this.reader = fileResource.newChannelReader(8192, ByteOrder.LITTLE_ENDIAN);
    if (this.reader == null) {
      this.exists = false;
    } else {
      this.exists = true;
      if (geometryFactory == null || !geometryFactory.isHasCoordinateSystem()) {
        final GeometryFactory geometryFactoryFromPrj = EsriCoordinateSystems
          .getGeometryFactory(resource);
        if (geometryFactoryFromPrj != null) {
          geometryFactory = geometryFactoryFromPrj;
        }
      }
      final LasPointCloudHeader header = new LasPointCloudHeader(this.reader, geometryFactory);
      this.setHeader(header);
    }
  }

  @SuppressWarnings("unchecked")
  public <P extends LasPoint0Core> P addPoint(final double x, final double y, final double z) {
    final LasPoint lasPoint = this.header.newLasPoint(this, x, y, z);
    this.points.add(lasPoint);
    return (P)lasPoint;
  }

  public void clear() {
    this.header.clear();
    this.points.clear();
  }

  @Override
  public void close() {
    final ChannelReader reader = this.reader;
    this.reader = null;
    if (reader != null) {
      reader.close();
    }
    if (this.zipIn != null) {
      try {
        this.zipIn.close();
      } catch (final IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  @Override
  public void forEachPoint(final Consumer<? super LasPoint> action) {
    final long pointCount = getPointCount();
    try {
      final ChannelReader reader = this.reader;
      if (reader == null) {
        this.points.forEach(action);
      } else if (pointCount == 0) {
        this.reader = null;
      } else if (this.header.isLaszip()) {
        forEachPointLaz(action);
      } else {
        try (
          BaseCloseable closable = this) {
          final LasPointFormat pointFormat = getPointFormat();
          for (int i = 0; i < pointCount; i++) {
            final LasPoint point = pointFormat.readLasPoint(this, reader);
            action.accept(point);
          }
        }
      }
    } finally {
      this.reader = null;
    }
  }

  private void forEachPointLaz(final Consumer<? super LasPoint> action) {
    try (
      ArithmeticDecoder decoder = new ArithmeticDecoder(this.reader);
      BaseCloseable closable = this;) {
      final LasZipHeader lasZipHeader = getLasZipHeader();
      final LazDecompress[] pointDecompressors = newLazDecompressors(lasZipHeader, decoder);

      if (lasZipHeader.isCompressor(LasZipHeader.LASZIP_COMPRESSOR_POINTWISE)) {
        forEachPointLazPointwise(decoder, pointDecompressors, action);
      } else {
        forEachPointLazChunked(decoder, pointDecompressors, action);
      }
    }
  }

  private void forEachPointLazChunked(final ArithmeticDecoder decoder,
    final LazDecompress[] pointDecompressors, final Consumer<? super LasPoint> action) {
    final ChannelReader reader = this.reader;
    final long chunkTableOffset = reader.getLong();
    final long chunkSize = getLasZipHeader().getChunkSize();
    long chunkReadCount = chunkSize;
    final long pointCount = getPointCount();
    for (int i = 0; i < pointCount; i++) {
      final LasPoint point;
      final LasPointFormat pointFormat = getPointFormat();
      if (chunkSize == chunkReadCount) {
        point = pointFormat.readLasPoint(this, reader);
        for (final LazDecompress pointDecompressor : pointDecompressors) {
          pointDecompressor.init(point);
        }
        decoder.reset();
        chunkReadCount = 0;
      } else {
        point = pointFormat.newLasPoint(this);
        for (final LazDecompress pointDecompressor : pointDecompressors) {
          pointDecompressor.read(point);
        }
      }
      action.accept(point);
      chunkReadCount++;
    }
  }

  private void forEachPointLazPointwise(final ArithmeticDecoder decoder,
    final LazDecompress[] pointDecompressors, final Consumer<? super LasPoint> action) {
    final LasPointFormat pointFormat = getPointFormat();
    {
      final ChannelReader reader = this.reader;
      final LasPoint point = pointFormat.readLasPoint(this, reader);
      for (final LazDecompress pointDecompressor : pointDecompressors) {
        pointDecompressor.init(point);
      }
      decoder.reset();

      action.accept(point);
    }
    final long pointCount = getPointCount();
    for (int i = 1; i < pointCount; i++) {
      final LasPoint point = pointFormat.newLasPoint(this);
      for (final LazDecompress pointDecompressor : pointDecompressors) {
        pointDecompressor.read(point);
      }
      action.accept(point);
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.header.getBoundingBox();
  }

  @Override
  public Predicate<Point> getDefaultFilter() {
    return point -> LasClassification.GROUND.equals(((LasPoint)point).getClassification());
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public LasPointCloudHeader getHeader() {
    return this.header;
  }

  public LasZipHeader getLasZipHeader() {
    return this.header.getLasZipHeader();
  }

  public long getPointCount() {
    return this.header.getPointCount();
  }

  public LasPointFormat getPointFormat() {
    return this.header.getPointFormat();
  }

  public List<LasPoint> getPoints() {
    return this.points;
  }

  public boolean isExists() {
    return this.exists;
  }

  public LazDecompress[] newLazDecompressors(final LasZipHeader lasZipHeader,
    final ArithmeticDecoder decoder) {
    final int numItems = lasZipHeader.getNumItems();
    final LazDecompress[] pointDecompressors = new LazDecompress[numItems];
    for (int i = 0; i < numItems; i++) {
      final LazItemType type = lasZipHeader.getType(i);
      final int version = lasZipHeader.getVersion(i);
      if (version < 1 || version > 2) {
        throw new RuntimeException(version + " not yet supported");
      }
      switch (type) {
        case POINT10:
          if (version == 1) {
            pointDecompressors[i] = new LazDecompressPoint10V1(this, decoder);
          } else {
            pointDecompressors[i] = new LazDecompressPoint10V2(this, decoder);
          }
        break;
        case GPSTIME11:
          if (version == 1) {
            pointDecompressors[i] = new LazDecompressGpsTime11V1(decoder);
          } else {
            pointDecompressors[i] = new LazDecompressGpsTime11V2(decoder);
          }
        break;
        case RGB12:
          if (version == 1) {
            pointDecompressors[i] = new LazDecompressRgb12V1(decoder);
          } else {
            pointDecompressors[i] = new LazDecompressRgb12V2(decoder);
          }
        break;

        default:
          throw new RuntimeException(type + " not yet supported");
      }
    }
    return pointDecompressors;
  }

  @Override
  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final QuadEdgeDelaunayTinBuilder tinBuilder = new QuadEdgeDelaunayTinBuilder(geometryFactory);
    forEachPoint((lasPoint) -> {
      tinBuilder.insertVertex(lasPoint);
    });
    final TriangulatedIrregularNetwork tin = tinBuilder.newTriangulatedIrregularNetwork();
    return tin;
  }

  @Override
  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork(
    final Predicate<? super Point> filter) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final QuadEdgeDelaunayTinBuilder tinBuilder = new QuadEdgeDelaunayTinBuilder(geometryFactory);
    forEachPoint((lasPoint) -> {
      if (filter.test(lasPoint)) {
        tinBuilder.insertVertex(lasPoint);
      }
    });
    final TriangulatedIrregularNetwork tin = tinBuilder.newTriangulatedIrregularNetwork();
    return tin;
  }

  public void read() {
    if (this.reader != null) {
      this.points = new ArrayList<>((int)getPointCount());
      forEachPoint(this.points::add);
    }
  }

  @SuppressWarnings("unchecked")
  public <P extends Point> int read(final Predicate<P> filter) {
    if (this.reader != null) {
      this.points = new ArrayList<>((int)getPointCount());
      forEachPoint((point) -> {
        if (filter.test((P)point)) {
          this.points.add(point);
        }
      });
    }
    return this.points.size();
  }

  public void setCoordinateSystemInternal(final CoordinateSystem coordinateSystem) {
    this.geometryFactory = this.geometryFactory.convertCoordinateSystem(coordinateSystem);
  }

  private void setHeader(final LasPointCloudHeader header) {
    this.header = header;
    this.geometryFactory = header.getGeometryFactory();

  }

  @Override
  public double toDoubleX(final int x) {
    return this.geometryFactory.toDoubleX(x);
  }

  @Override
  public double toDoubleY(final int y) {
    return this.geometryFactory.toDoubleY(y);
  }

  @Override
  public double toDoubleZ(final int z) {
    return this.geometryFactory.toDoubleZ(z);
  }

  @Override
  public int toIntX(final double x) {
    return this.geometryFactory.toIntX(x);
  }

  @Override
  public int toIntY(final double y) {
    return this.geometryFactory.toIntY(y);
  }

  @Override
  public int toIntZ(final double z) {
    return this.geometryFactory.toIntZ(z);
  }

  @Override
  public MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();
    addToMap(map, "url", this.resource.getUri());
    addToMap(map, "header", this.header);
    return map;
  }

  public void writePointCloud(final Object target) {
    final Resource resource = Resource.getResource(target);
    try (
      EndianOutputStream out = resource.newBufferedOutputStream(EndianOutputStream::new)) {
      this.header.writeHeader(out);
      for (final LasPoint point : this.points) {
        point.write(out);
      }
    }
  }
}
