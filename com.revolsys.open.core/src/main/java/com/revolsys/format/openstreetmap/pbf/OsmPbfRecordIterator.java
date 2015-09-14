package com.revolsys.format.openstreetmap.pbf;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.google.protobuf.InvalidProtocolBufferException;
import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.LongHashMap;
import com.revolsys.format.openstreetmap.model.OsmConstants;
import com.revolsys.format.openstreetmap.model.OsmElement;
import com.revolsys.format.openstreetmap.model.OsmNode;
import com.revolsys.format.openstreetmap.model.OsmRelation;
import com.revolsys.format.openstreetmap.model.OsmWay;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.io.FileUtil;
import com.revolsys.io.ProtocolBufferInputStream;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public class OsmPbfRecordIterator extends AbstractIterator<Record>implements RecordReader {

  private static final int DATE_GRANULARITY = 1000;

  private static final int GRANULARITY = 100;

  public static Date toDate(final long time) {
    return new Date(DATE_GRANULARITY * time);
  }

  public static double toDegrees(final double offset, final double granularity, final long value) {
    return 0.000000001 * (offset + granularity * value);
  }

  public static double toDegrees(final long value) {
    return 0.000000001 * GRANULARITY * value;
  }

  private final ProtocolBufferInputStream blobHeaderIn = new ProtocolBufferInputStream();

  private final ProtocolBufferInputStream blobIn = new ProtocolBufferInputStream();

  private String blobType = null;

  private final ProtocolBufferInputStream blockIn = new ProtocolBufferInputStream();

  private final LinkedList<Record> currentRecords = new LinkedList<>();

  private boolean eof;

  private DataInputStream in;

  private final LongHashMap<Point> nodePoints = new LongHashMap<>();

  private final LongHashMap<Geometry> relationGeometries = new LongHashMap<>();

  private final LinkedList<List<Long>> relationMemberIds = new LinkedList<>();

  private final LinkedList<List<String>> relationMemberRoles = new LinkedList<>();

  private final LinkedList<List<Integer>> relationMemberTypes = new LinkedList<>();

  private final LinkedList<OsmRelation> relations = new LinkedList<>();

  private List<String> strings = Collections.emptyList();

  private final LongHashMap<Geometry> wayGeometries = new LongHashMap<>();

  private final LinkedList<List<Long>> wayNodeIds = new LinkedList<>();

  private final LinkedList<OsmWay> ways = new LinkedList<>();

  public OsmPbfRecordIterator(final DataInputStream in) {
    this.in = in;
  }

  public OsmPbfRecordIterator(final Resource resource) {
    this(new DataInputStream(resource.getInputStream()));
  }

  protected void addNode(final List<Record> currentRecords, final OsmNode node) {
    final long id = node.getId();
    final Point point = (Point)node.getGeometry();
    this.nodePoints.put(id, point);
    if (node.hasTags()) {
      currentRecords.add(node);
    }
  }

  private void addTags(final OsmElement element, final List<String> keys,
    final List<String> values) {

    if (keys.size() != values.size()) {
      throw new RuntimeException("Number of tag keys (" + keys.size() + ") and tag values ("
        + values.size() + ") don't match");
    }

    final Iterator<String> valueIterator = values.iterator();
    for (final String key : keys) {
      final String value = valueIterator.next();
      element.addTag(key, value);
    }
  }

  @Override
  public void doClose() {
    FileUtil.closeSilent(this.in);
    this.in = null;
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    try {
      while (this.currentRecords.isEmpty() && !this.eof) {
        try {
          parseBlob();
        } catch (final EOFException e) {
          this.eof = true;
        }
      }
      if (!this.currentRecords.isEmpty()) {
        return this.currentRecords.removeFirst();
      } else {
        final Record record = processWaysWithMissingNodes();
        if (record == null) {
          throw new NoSuchElementException();
        } else {
          return record;
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to get next blob from PBF stream.", e);
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return OsmElement.RECORD_DEFINITION;
  }

  private String getString(final int stringId) {
    return this.strings.get(stringId);
  }

  public boolean isPolygon(final OsmWay way, final Geometry geometry) {
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;

      if (line.isClosed()) {
        boolean isPolygon = true;
        if (!"yes".equals(way.getTag("area"))) {
          if (way.containsKey("barrier")) {
            isPolygon = false;
          } else if (way.containsKey("highway")) {
            isPolygon = false;
          }
        }
        if (isPolygon) {
          final Polygon polygon = line.getGeometryFactory().polygon(line);
          way.setGeometryValue(polygon);
        }
        return isPolygon;
      }
    }
    return false;
  }

  private void parseBlob() throws IOException {

    final byte[] blobContent = readBlobContent();

    try {
      if ("OSMHeader".equals(this.blobType)) {
        processOsmHeader(blobContent);
      } else if ("OSMData".equals(this.blobType)) {
        this.blockIn.setBuffer(blobContent);
        parseBlock(this.blockIn);

      } else {
      }
    } catch (final InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  private void parseBlock(final ProtocolBufferInputStream in) throws IOException {
    boolean running = true;
    this.strings = new ArrayList<>();
    double lonOffset = 0;
    double latOffset = 0;
    int granularity = GRANULARITY;
    int dateGranularity = DATE_GRANULARITY;

    while (running) {
      final int tag = in.readTag();
      switch (tag) {
        case 0:
          running = false;
        break;
        case 10:
          readStrings(in, this.strings);
        break;
        case 18:
          parseOsmElement(in);
        break;
        case 136:
          granularity = in.readInt32();
        break;
        case 144:
          dateGranularity = in.readInt32();
        break;
        case 152:
          latOffset = in.readInt64();
        break;
        case 160:
          lonOffset = in.readInt64();
        break;
        default:
          this.blobIn.skipField(tag);
          running = false;
        break;
      }
    }
  }

  private DenseInfo parseDenseInfo(final ProtocolBufferInputStream input) throws IOException {
    final DenseInfo info = new DenseInfo();
    final int inLength = input.startLengthDelimited();

    boolean running = true;
    while (running) {
      final int tag = input.readTag();
      switch (tag) {
        case 0:
          running = false;
        break;
        case 8:
          input.readInt(info.versions);
        break;
        case 10:
          input.readInts(info.versions);
        break;
        case 16:
          input.readLong(info.timestamps);
        break;
        case 18:
          input.readLongs(info.timestamps);
        break;
        case 24:
          input.readLong(info.changesets);
        case 26:
          input.readLongs(info.changesets);
        case 32:
          input.readInt(info.uids);
        break;
        case 34:
          input.readInts(info.uids);
        break;
        case 40:
          readStringById(input, info.userNames);
        break;
        case 42:
          readStringsByIds(input, info.userNames);
        break;
        case 48:
          input.readBool(info.visibles);
        break;
        case 50:
          input.readBools(info.visibles);
        break;
        default:
          input.skipField(tag);
        break;
      }
    }
    input.endLengthDelimited(inLength);
    return info;
  }

  private void parseDenseNodes(final ProtocolBufferInputStream in) throws IOException {

    final List<Long> ids = new ArrayList<>();
    final List<Double> latitudes = new ArrayList<>();
    final List<Double> longitudes = new ArrayList<>();
    final List<String> keysAndValues = new ArrayList<>();
    DenseInfo denseInfo = null;

    final int inLength = in.startLengthDelimited();
    boolean running = true;
    while (running) {
      final int tag = in.readTag();
      switch (tag) {
        case 0:
          running = false;
        break;
        case 8:
          in.readLong(ids);
        break;
        case 10:
          in.readLongs(ids);
        break;
        case 42:
          denseInfo = parseDenseInfo(in);
        break;
        case 64:
          readDegreesById(in, latitudes);
        break;
        case 66:
          readDegreesByIds(in, latitudes);
        break;
        case 72:
          readDegreesById(in, longitudes);
        break;
        case 74:
          readDegreesByIds(in, longitudes);
        break;
        case 80:
          readStringById(in, keysAndValues);
        break;
        case 82:
          readStringsByIds(in, keysAndValues);
        break;
        default:
          in.skipField(tag);
        break;
      }
    }
    in.endLengthDelimited(inLength);
    if (ids.size() != latitudes.size() || ids.size() != longitudes.size()) {
      throw new RuntimeException("Number of ids (" + ids.size() + "), latitudes ("
        + latitudes.size() + "), and longitudes (" + longitudes.size() + ") don't match");
    }
    if (denseInfo == null && keysAndValues.isEmpty()) {
      for (int i = 0; i < ids.size(); i++) {
        final long id = ids.get(i);
        final double latitude = latitudes.get(i);
        final double longitude = longitudes.get(i);
        final PointDouble point = new PointDouble(longitude, latitude);
        this.nodePoints.put(id, point);
      }
    } else {
      final Iterator<String> keysAndValuesIterator = keysAndValues.iterator();
      long id = 0;
      for (int i = 0; i < ids.size(); i++) {
        final long idOffset = ids.get(i);
        id += idOffset;
        final double latitude = latitudes.get(i);
        final double longitude = longitudes.get(i);
        final Point point = OsmConstants.WGS84_2D.point(longitude, latitude);
        this.nodePoints.put(id, point);
        OsmNode node = null;

        while (keysAndValuesIterator.hasNext()) {
          final String key = keysAndValuesIterator.next();
          if (key.length() == 0) {
            break;
          }
          if (!keysAndValuesIterator.hasNext()) {
            throw new RuntimeException(
              "The PBF DenseInfo keys/values list contains a key with no corresponding value.");
          }
          if (node == null) {
            node = new OsmNode();
            node.setId(id);
            node.setGeometryValue(point);
            this.currentRecords.add(node);
          }
          final String value = keysAndValuesIterator.next();

          node.addTag(key, value);
        }

        if (denseInfo != null && node != null) {
          node.setVersion(denseInfo.versions.get(i));
          node.setChangeset(denseInfo.changesets.get(i));
          node.setTimestamp(denseInfo.timestamps.get(i));
          node.setUid(denseInfo.uids.get(i));
          node.setUser(denseInfo.userNames.get(i));
          node.setVisible(denseInfo.visibles.get(i));
        }

      }
    }
  }

  private void parseInfo(final ProtocolBufferInputStream input, final OsmElement element)
    throws IOException {
    final int inLength = input.startLengthDelimited();

    boolean running = true;
    while (running) {
      final int tag = input.readTag();
      switch (tag) {
        case 0:
          running = false;
        break;
        case 8:
          final int version = input.readInt32();
          element.setVersion(version);
        break;
        case 16:
          final long time = input.readInt64();
          final Date timestamp = toDate(time);
          element.setTimestamp(timestamp);
        break;
        case 24:
          final long changeset = input.readInt64();
          element.setChangeset(changeset);
        break;
        case 32:
          final int uid = input.readInt32();
          element.setUid(uid);
        break;
        case 40:
          final int userSid = input.readUInt32();
          final String userName = getString(userSid);
          element.setUser(userName);
        break;
        case 48:
          final boolean visible = input.readBool();
          element.setVisible(visible);
        break;
        default:
          input.skipField(tag);
        break;
      }
    }
    input.endLengthDelimited(inLength);
  }

  private void parseNode(final ProtocolBufferInputStream input) throws IOException {

    final OsmNode node = new OsmNode();
    final List<String> keys = new ArrayList<>();
    final List<String> values = new ArrayList<>();
    double lat = 0;
    double lon = 0;
    final int inLength = input.startLengthDelimited();
    boolean running = true;
    while (running) {
      final int tag = input.readTag();
      switch (tag) {
        case 0:
          running = false;
        break;
        case 8:
          final long id = input.readInt64();
          node.setId(id);
        break;
        case 16:
          readStringById(input, keys);
        break;
        case 18:
          readStringsByIds(input, keys);
        break;
        case 24:
          readStringById(input, values);
        break;
        case 26:
          readStringsByIds(input, values);
        break;
        case 34:
          parseInfo(input, node);
        break;
        case 64:
          lat = toDegrees(input.readSInt64());
        break;
        case 72:
          lon = toDegrees(input.readSInt64());
        break;
      }
    }
    input.endLengthDelimited(inLength);
    final Point point = OsmConstants.WGS84_2D.point(lat, lon);
    node.setGeometryValue(point);
    addTags(node, keys, values);
    this.currentRecords.add(node);
  }

  private void parseOsmElement(final ProtocolBufferInputStream in) throws IOException {
    final int inLength = in.startLengthDelimited();
    boolean running = true;
    while (running) {
      final int tag = in.readTag();
      switch (tag) {
        case 0:
          running = false;
        break;
        case 10:
          parseNode(in);
        break;
        case 18:
          parseDenseNodes(in);
        break;
        case 26:
          parseWay(in);
        break;
        case 34: {
          parseRelation(in);
          break;
        }
        case 42: {
          // org.openstreetmap.osmosis.osmbinary.Osmformat.ChangeSet.Builder
          // subBuilder =
          // org.openstreetmap.osmosis.osmbinary.Osmformat.ChangeSet.newBuilder();
          // input.readMessage(subBuilder, extensionRegistry);
          // addChangesets(subBuilder.buildPartial());
          in.skipField(tag);
          break;
        }
        default:
          in.skipField(tag);
        break;
      }
    }
    in.endLengthDelimited(inLength);
  }

  private void parseRelation(final ProtocolBufferInputStream input) throws IOException {

    final OsmRelation relation = new OsmRelation();
    final List<String> keys = new ArrayList<>();
    final List<String> values = new ArrayList<>();
    final List<Long> memberIds = new ArrayList<>();
    final List<Integer> memberTypes = new ArrayList<>();

    final List<String> memberRoles = new ArrayList<>();

    final int inLength = input.startLengthDelimited();
    boolean running = true;
    while (running) {
      final int tag = input.readTag();
      switch (tag) {
        case 0:
          running = false;
        break;
        case 8:
          final long id = input.readInt64();
          relation.setId(id);
        break;
        case 16:
          readStringById(input, keys);
        break;
        case 18:
          readStringsByIds(input, keys);
        break;
        case 24:
          readStringById(input, values);
        break;
        case 26:
          readStringsByIds(input, values);
        break;
        case 34:
          parseInfo(input, relation);
        break;
        case 64:
          readStrings(input, memberRoles);
        break;
        case 66:
          readStringsByIds(input, memberRoles);
        break;
        case 72:
          input.readLong(memberIds);
        break;
        case 74:
          input.readLongs(memberIds);
        break;
        case 80:
          input.readEnum(memberTypes);
        break;
        case 82:
          input.readEnums(memberTypes);
        break;
      }
    }
    input.endLengthDelimited(inLength);
    addTags(relation, keys, values);

    final List<Geometry> parts = new ArrayList<>();
    long memberId = 0;
    for (int i = 0; i < memberIds.size(); i++) {
      final long memberIdOffset = memberIds.get(i);
      memberId += memberIdOffset;
      Geometry geometry = null;
      final int memberType = memberTypes.get(i);
      switch (memberType) {
        case 0:
          geometry = this.nodePoints.get(memberId);
        break;

        case 1:
          geometry = this.wayGeometries.get(memberId);
        break;
        default:
          throw new RuntimeException("Unknown member type " + memberType);
      }

      if (geometry != null) {
        parts.add(geometry);
      }
    }

    if (memberIds.size() == parts.size()) {
      final Geometry geometry = OsmConstants.WGS84_2D.geometry(parts);
      if (memberTypes.get(0) == 1 && !Property.hasValue(memberRoles.get(0))) {

      }
      relation.setGeometryValue(geometry);
      this.currentRecords.add(relation);
      final long relationId = relation.getId();
      this.relationGeometries.put(relationId, geometry);
    } else {
      this.relations.add(relation);
      this.relationMemberIds.add(memberIds);
      this.relationMemberRoles.add(memberRoles);
      this.relationMemberTypes.add(memberTypes);
    }
  }

  private void parseWay(final ProtocolBufferInputStream input) throws IOException {

    final OsmWay way = new OsmWay();
    final List<String> keys = new ArrayList<>();
    final List<String> values = new ArrayList<>();
    final List<Long> nodeIds = new ArrayList<>();
    final int inLength = input.startLengthDelimited();
    boolean running = true;
    long wayId = 0;
    while (running) {
      final int tag = input.readTag();
      switch (tag) {
        case 0:
          running = false;
        break;
        case 8:
          wayId = input.readInt64();
          way.setId(wayId);
        break;
        case 16:
          readStringById(input, keys);
        break;
        case 18:
          readStringsByIds(input, keys);
        break;
        case 24:
          readStringById(input, values);
        break;
        case 26:
          readStringsByIds(input, values);
        break;
        case 34:
          parseInfo(input, way);
        break;
        case 64:
          input.readLong(nodeIds);
        break;
        case 66:
          input.readLongs(nodeIds);
        break;
      }
    }
    input.endLengthDelimited(inLength);
    addTags(way, keys, values);

    final List<Point> points = new ArrayList<>();
    long nodeId = 0;
    for (final long nodeIdOffset : nodeIds) {
      nodeId += nodeIdOffset;
      final Point point = this.nodePoints.get(nodeId);

      if (point != null) {
        points.add(point);
      }
    }

    if (nodeIds.size() == points.size()) {
      Geometry geometry;
      if (points.size() == 1) {
        geometry = points.get(0);
      } else {
        geometry = OsmConstants.WGS84_2D.lineString(points);
      }
      way.setGeometryValue(geometry);
      isPolygon(way, geometry);
      if (way.hasTags()) {
        this.currentRecords.add(way);
      }
      geometry = way.getGeometry();
      this.wayGeometries.put(wayId, geometry);
    } else {
      this.ways.add(way);
      this.wayNodeIds.add(nodeIds);
    }
  }

  private void processOsmHeader(final byte[] data) throws InvalidProtocolBufferException {
    // Osmformat.HeaderBlock header = Osmformat.HeaderBlock.parseFrom(data);
    //
    // // Build the list of active and unsupported features in the file.
    // List<String> supportedFeatures = Arrays.asList("OsmSchema-V0.6",
    // "DenseNodes");
    // List<String> activeFeatures = new ArrayList<String>();
    // List<String> unsupportedFeatures = new ArrayList<String>();
    // for (String feature : header.getRequiredFeaturesList()) {
    // if (supportedFeatures.contains(feature)) {
    // activeFeatures.add(feature);
    // } else {
    // unsupportedFeatures.add(feature);
    // }
    // }
    //
    // // We can't continue if there are any unsupported features. We wait
    // // until now so that we can display all unsupported features instead of
    // // just the first one we encounter.
    // if (unsupportedFeatures.size() > 0) {
    // throw new RuntimeException("PBF file contains unsupported features " +
    // unsupportedFeatures);
    // }
    //
    // // Build a new bound object which corresponds to the header.
    // BoundingBox bound;
    // if (header.hasBbox()) {
    // HeaderBBox bbox = header.getBbox();
    // bound = new BoundingBox(bbox.getRight() * COORDINATE_SCALING_FACTOR,
    // bbox.getLeft() * COORDINATE_SCALING_FACTOR,
    // bbox.getTop() * COORDINATE_SCALING_FACTOR, bbox.getBottom() *
    // COORDINATE_SCALING_FACTOR,
    // header.getSource());
    // } else {
    // bound = new Bound(header.getSource());
    // }
    //
    // // Add the bound object to the results.
    // decodedEntities.add(new BoundContainer(bound));
  }

  public Record processWaysWithMissingNodes() {
    while (!this.ways.isEmpty()) {
      final OsmWay way = this.ways.removeFirst();
      final List<Long> nodeIds = this.wayNodeIds.removeFirst();
      final List<LineString> lines = new ArrayList<>();
      final List<Point> points = new ArrayList<>();
      long nodeId = 0;
      for (final Long nodeIdRef : nodeIds) {
        nodeId += nodeIdRef;
        final Point point = this.nodePoints.get(nodeId);
        if (point == null) {
          if (points.size() > 1) {
            final LineString line = OsmConstants.WGS84_2D.lineString(points);
            lines.add(line);
          }
          points.clear();
        } else {
          points.add(point);
        }
      }
      if (points.size() > 1) {
        final LineString line = OsmConstants.WGS84_2D.lineString(points);
        lines.add(line);
      }
      if (!lines.isEmpty()) {
        final Geometry geometry = OsmConstants.WGS84_2D.geometry(lines);
        way.setGeometryValue(geometry);
        final long wayId = way.getId();
        this.wayGeometries.put(wayId, geometry);
        return way;
      }
    }
    throw new NoSuchElementException();
  }

  private byte[] readBlobContent() throws IOException {
    try {
      final int blobSize = readBlobHeader();
      final byte[] data = new byte[blobSize];
      this.in.readFully(data);
      this.blobIn.setBuffer(data);
      byte[] raw = null;
      int rawSize = 0;
      byte[] zlibData = null;
      boolean running = true;
      while (running) {
        final int tag = this.blobIn.readTag();
        switch (tag) {
          case 0:
            running = false;
          break;
          case 10:
            raw = this.blobIn.readBytes();
          break;
          case 16:
            rawSize = this.blobIn.readInt32();
          break;
          case 26:
            zlibData = this.blobIn.readBytes();
          break;
          case 34:
            throw new RuntimeException("LZMA not supported");
          case 42:
            throw new RuntimeException("ZIP2 not supported");
          default:
            this.blobIn.skipField(tag);
            running = false;
          break;
        }
      }

      if (raw != null) {
        return raw;
      } else if (zlibData != null) {
        final Inflater inflater = new Inflater();
        inflater.setInput(zlibData);
        final byte[] blobData = new byte[rawSize];
        try {
          inflater.inflate(blobData);
        } catch (final DataFormatException e) {
          throw new RuntimeException("Unable to decompress PBF blob.", e);
        }
        if (!inflater.finished()) {
          throw new RuntimeException("PBF blob contains incomplete compressed data.");
        }
        return blobData;
      } else {
        throw new RuntimeException(
          "PBF blob uses unsupported compression, only raw or zlib may be used.");
      }
    } finally {
      this.blobIn.setInputStream(null);
    }
  }

  private int readBlobHeader() throws IOException {
    try {
      final int headerLength = this.in.readInt();
      final byte[] headerBuffer = new byte[headerLength];
      this.in.readFully(headerBuffer);
      this.blobHeaderIn.setBuffer(headerBuffer);

      this.blobType = null;
      int blobSize = 0;
      while (true) {
        final int tag = this.blobHeaderIn.readTag();
        switch (tag) {
          case 0:
            return blobSize;
          case 10: {
            this.blobType = this.blobHeaderIn.readString();
            break;
          }
          case 18: {
            this.blobHeaderIn.readBytes();
            break;
          }
          case 24: {
            blobSize = this.blobHeaderIn.readInt32();
            break;
          }
          default:
            this.blobHeaderIn.skipField(tag);
          break;
        }
      }

    } finally {
      this.blobHeaderIn.setInputStream(null);
    }
  }

  private void readDegreesById(final ProtocolBufferInputStream in, final List<Double> numbers)
    throws IOException {
    final long number = in.readSInt64();
    final double degrees = toDegrees(number);
    numbers.add(degrees);
  }

  private void readDegreesByIds(final ProtocolBufferInputStream in, final List<Double> numbers)
    throws IOException {
    final int length = in.readRawVarint32();
    final int oldLength = in.pushLimit(length);
    int number = 0;
    while (in.getBytesUntilLimit() > 0) {
      final long numberOffset = in.readSInt64();
      number += numberOffset;
      final double degrees = toDegrees(number);
      numbers.add(degrees);
    }
    in.popLimit(oldLength);
  }

  private void readStringById(final ProtocolBufferInputStream in, final List<String> strings)
    throws IOException {
    final int stringId = in.readUInt32();
    final String string = getString(stringId);
    strings.add(string);
  }

  protected void readStrings(final ProtocolBufferInputStream in, final List<String> strings)
    throws IOException {
    final int inLength = in.startLengthDelimited();
    while (true) {
      final int tag = in.readTag();
      switch (tag) {
        case 0:
          in.endLengthDelimited(inLength);
          return;
        case 10:
          final String string = in.readString();
          strings.add(string);
        break;
        default:
          this.blobIn.skipField(tag);
        break;
      }
    }
  }

  private void readStringsByIds(final ProtocolBufferInputStream in, final List<String> strings)
    throws IOException {
    final int length = in.readRawVarint32();
    final int oldLength = in.pushLimit(length);
    while (in.getBytesUntilLimit() > 0) {
      final int stringId = in.readUInt32();
      final String string = getString(stringId);
      strings.add(string);
    }
    in.popLimit(oldLength);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
