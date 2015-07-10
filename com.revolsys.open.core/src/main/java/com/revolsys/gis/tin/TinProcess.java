package com.revolsys.gis.tin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import com.revolsys.data.record.Record;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.util.MathUtil;

public class TinProcess extends BaseInOutProcess<Record, Record> {
  private static final Logger LOG = LoggerFactory.getLogger(TinProcess.class);

  private TriangulatedIrregularNetwork tin;

  private Channel<Record> tinIn;

  private Reader<Record> tinReader;

  private BoundingBox boundingBox;

  private Map<String, Object> updatedAttributeValues;

  private File tinCache;

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  private String getId() {
    final GeometryFactory geometryFactory = this.boundingBox.getGeometryFactory();
    final String string = MathUtil.toString(this.boundingBox.getMinX()) + "_"
      + MathUtil.toString(this.boundingBox.getMinY()) + "_"
      + MathUtil.toString(this.boundingBox.getMaxX()) + "_"
      + MathUtil.toString(this.boundingBox.getMaxY());
    if (geometryFactory == null) {
      return string;
    } else {
      return geometryFactory.getSrid() + "-" + string;
    }

  }

  public File getTinCache() {
    return this.tinCache;
  }

  public Channel<Record> getTinIn() {
    return this.tinIn;
  }

  public Reader<Record> getTinReader() {
    return this.tinReader;
  }

  public Map<String, Object> getUpdatedAttributeValues() {
    return this.updatedAttributeValues;
  }

  @Override
  protected void init() {
    super.init();
    if (this.tinIn != null) {
      this.tinIn.readConnect();
    }
  }

  protected void loadTin() {
    if (this.tinCache != null && this.tinCache.exists()) {
      final File tinFile = new File(this.tinCache, getId() + ".tin");
      if (tinFile.exists()) {
        LOG.info("Loading tin from file " + tinFile);
        final FileSystemResource resource = new FileSystemResource(tinFile);
        try {
          this.tin = TinReader.read(this.boundingBox, resource);
        } catch (final Exception e) {
          LOG.error("Unable to read tin file " + resource, e);
        }
      }
    }
    if (this.tin == null) {
      LOG.info("Loading tin from database");
      this.tin = new TriangulatedIrregularNetwork(this.boundingBox);
      List<LineString> lines = new ArrayList<LineString>();
      if (this.tinIn != null) {
        readTinFeatures(this.tin, lines, this.tinIn);
      }
      if (this.tinReader != null) {
        readTinFeatures(this.tin, lines, this.tinReader);
      }
      for (final LineString line : lines) {
        this.tin.insertNodes(line);
      }
      for (final LineString line : lines) {
        this.tin.insertEdge(line);
      }
      lines = null;
      this.tin.finishEditing();
      if (this.tinCache != null) {
        final File tinFile = new File(this.tinCache, getId() + ".tin");
        try {
          this.tinCache.mkdirs();
          final FileSystemResource resource = new FileSystemResource(tinFile);
          TinWriter.write(resource, this.tin);
        } catch (final Exception e) {
          LOG.error("Unable to cache tin to file " + tinFile);
        }
      }
    }
  }

  @Override
  protected void postRun(final Channel<Record> in, final Channel<Record> out) {
    if (this.tin == null) {
      LOG.info("Tin not created as there were no features");
    }
    this.tin = null;
    if (this.tinIn != null) {
      this.tinIn.readDisconnect();
    }
    if (this.tinReader != null) {
      this.tinReader.close();
    }
    this.boundingBox = null;
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    if (this.tin == null) {
      loadTin();
    }
    final LineString geometry = object.getGeometry();
    if (geometry instanceof LineString) {
      final LineString line = geometry;
      final com.revolsys.jts.geom.BoundingBox envelope = line.getBoundingBox();
      if (envelope.intersects(this.boundingBox)) {
        final LineString newLine = this.tin.getElevation(line);
        if (line != newLine) {
          object.setGeometryValue(newLine);
        }
        if (this.updatedAttributeValues != null) {
          for (final Entry<String, Object> entry : this.updatedAttributeValues.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (object.getValueByPath(key) == null) {
              object.setValueByPath(key, value);
            }
          }
        }
      }
    }
    out.write(object);
  }

  private void readTinFeatures(final TriangulatedIrregularNetwork tin, final List<LineString> lines,
    final Iterable<Record> iterable) {
    for (final Record object : iterable) {
      final Geometry geometry = object.getGeometry();
      if (geometry instanceof Point) {
        final Point point = (Point)geometry;
        tin.insertNode(point);
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        final LineString points = line;
        lines.add(points);
      }
    }
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setTinCache(final File tinCache) {
    this.tinCache = tinCache;
  }

  public void setTinIn(final Channel<Record> tinIn) {
    this.tinIn = tinIn;
  }

  public void setTinReader(final Reader<Record> tinReader) {
    this.tinReader = tinReader;
  }

  public void setUpdatedAttributeValues(final Map<String, Object> updatedAttributeValues) {
    this.updatedAttributeValues = updatedAttributeValues;
  }
}
