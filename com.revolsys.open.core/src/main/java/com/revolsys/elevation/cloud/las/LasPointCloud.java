package com.revolsys.elevation.cloud.las;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint0Core;
import com.revolsys.elevation.cloud.las.pointformat.LasPointFormat;
import com.revolsys.elevation.cloud.las.zip.LazChunkedIterator;
import com.revolsys.elevation.cloud.las.zip.LazPointwiseIterator;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.quadedge.QuadEdgeDelaunayTinBuilder;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.ZipUtil;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.endian.EndianOutputStream;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Pair;

public class LasPointCloud extends BaseObjectWithProperties
  implements PointCloud<LasPoint>, BaseCloseable, MapSerializer, Iterable<LasPoint> {

  public static void forEachPoint(final Object source, final Consumer<? super LasPoint> action) {
    try (
      final LasPointCloud pointCloud = PointCloud.newPointCloud(source)) {
      pointCloud.forEachPoint(action);
    }
  }

  private GeometryFactory geometryFactory = GeometryFactory.fixed3d(1000.0, 1000.0, 1000.0);

  private LasPointCloudHeader header;

  private List<LasPoint> points = new ArrayList<>();

  private ChannelReader reader;

  private Resource resource;

  private boolean exists;

  private ByteBuffer byteBuffer;

  public LasPointCloud(final LasPointFormat pointFormat, final GeometryFactory geometryFactory) {
    this.setHeader(new LasPointCloudHeader(pointFormat, geometryFactory));
  }

  public LasPointCloud(final Resource resource, final MapEx properties) {
    setProperties(properties);
    this.resource = resource;
    Resource fileResource = resource;
    if (resource.getFileNameExtension().equals("zip")) {
      final Pair<Resource, GeometryFactory> result = ZipUtil
        .getZipResourceAndGeometryFactory(resource, ".las", this.geometryFactory);
      fileResource = result.getValue1();
      this.geometryFactory = result.getValue2();
    }

    this.reader = fileResource.newChannelReader(this.byteBuffer);
    if (this.reader == null) {
      this.exists = false;
    } else {
      this.reader.setByteOrder(ByteOrder.LITTLE_ENDIAN);
      this.exists = true;
      if (this.geometryFactory == null || !this.geometryFactory.isHasHorizontalCoordinateSystem()) {
        final GeometryFactory geometryFactoryFromPrj = GeometryFactory.floating3d(resource);
        if (geometryFactoryFromPrj != null) {
          this.geometryFactory = geometryFactoryFromPrj;
        }
      }
      final LasPointCloudHeader header = new LasPointCloudHeader(this.reader, this.geometryFactory);
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
  }

  @Override
  public void forEachPoint(final Consumer<? super LasPoint> action) {
    try {
      final Iterable<LasPoint> iterable = iterable();
      iterable.forEach(action);
    } finally {
      this.reader = null;
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.header.getBoundingBox();
  }

  @Override
  public Predicate<Point> getDefaultFilter() {
    return point -> LasClassification.GROUND == ((LasPoint)point).getClassification();
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

  public Iterable<LasPoint> iterable() {
    final ChannelReader reader = this.reader;
    final long pointCount = getPointCount();
    if (pointCount == 0) {
      this.reader = null;
      return Collections.emptyList();
    } else if (reader == null) {
      return Collections.unmodifiableList(this.points);
    } else if (this.header.isLaszip()) {
      final LasZipHeader lasZipHeader = getLasZipHeader();
      if (lasZipHeader.isCompressor(LasZipHeader.LASZIP_COMPRESSOR_POINTWISE)) {
        return new LazPointwiseIterator(this, reader);
      } else {
        return new LazChunkedIterator(this, reader);
      }

    } else {
      return new LasPointCloudIterator(this, reader);
    }
  }

  @Override
  public Iterator<LasPoint> iterator() {
    return iterable().iterator();
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

  public void setByteBuffer(final ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
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
