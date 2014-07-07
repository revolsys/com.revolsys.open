package com.revolsys.io.openstreetmap.pbf;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.springframework.core.io.Resource;

import com.google.protobuf.InvalidProtocolBufferException;
import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.LongHashMap;
import com.revolsys.data.io.RecordIterator;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.ProtocolBufferInputStream;
import com.revolsys.io.openstreetmap.model.OsmConstants;
import com.revolsys.io.openstreetmap.model.OsmElement;
import com.revolsys.io.openstreetmap.model.OsmNode;
import com.revolsys.io.openstreetmap.model.OsmWay;
import com.revolsys.io.openstreetmap.pbf.Osmformat.DenseInfo;
import com.revolsys.io.openstreetmap.pbf.Osmformat.DenseNodes;
import com.revolsys.io.openstreetmap.pbf.Osmformat.Info;
import com.revolsys.io.openstreetmap.pbf.Osmformat.Node;
import com.revolsys.io.openstreetmap.pbf.Osmformat.PrimitiveGroup;
import com.revolsys.io.openstreetmap.pbf.Osmformat.Way;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.spring.SpringUtil;

public class OsmPbfRecordIterator extends AbstractIterator<Record> implements
RecordIterator {

  private static Logger log = Logger.getLogger(OsmPbfRecordIterator.class.getName());

  private DataInputStream in;

  private final LongHashMap<Point> nodePoints = new LongHashMap<>();

  private final LinkedList<Record> currentRecords = new LinkedList<>();

  private String blobType = null;

  private final ProtocolBufferInputStream blobHeaderIn = new ProtocolBufferInputStream();

  private final ProtocolBufferInputStream blobIn = new ProtocolBufferInputStream();

  private int wayCount = 0;

  private int skippedWayCount = 0;

  public OsmPbfRecordIterator(final DataInputStream in) {
    this.in = in;
  }

  public OsmPbfRecordIterator(final Resource resource) {
    this(new DataInputStream(SpringUtil.getInputStream(resource)));
  }

  protected void addNode(final List<Record> currentRecords, final OsmNode node) {
    final long id = node.getId();
    final Point point = (Point)node.getGeometryValue();
    this.nodePoints.put(id, point);
    currentRecords.add(node);
  }

  @Override
  public void doClose() {
    FileUtil.closeSilent(this.in);
    this.in = null;
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    try {
      while (this.currentRecords.isEmpty()) {
        processBlob();
      }
      return this.currentRecords.removeFirst();
    } catch (final EOFException e) {
      System.out.println(this.wayCount);
      System.out.println(this.skippedWayCount);
      throw new NoSuchElementException();
    } catch (final IOException e) {
      throw new RuntimeException("Unable to get next blob from PBF stream.", e);
    }
  }

  // private void processRelations(final List<Relation> relations,
  // final PbfFieldDecoder fieldDecoder) {
  // for (final Relation relation : relations) {
  // org.openstreetmap.osmosis.core.domain.v0_6.Relation osmRelation;
  // CommonEntityData entityData;
  //
  // if (relation.hasInfo()) {
  // entityData = buildCommonEntityData(relation.getId(),
  // relation.getKeysList(), relation.getValsList(), relation.getInfo(),
  // fieldDecoder);
  //
  // } else {
  // entityData = buildCommonEntityData(relation.getId(),
  // relation.getKeysList(), relation.getValsList(), fieldDecoder);
  // }
  //
  // osmRelation = new org.openstreetmap.osmosis.core.domain.v0_6.Relation(
  // entityData);
  //
  // buildRelationMembers(osmRelation, relation.getMemidsList(),
  // relation.getRolesSidList(), relation.getTypesList(), fieldDecoder);
  //
  // // Add the bound object to the results.
  // decodedEntities.add(new RelationContainer(osmRelation));
  // }
  // }

  @Override
  public RecordDefinition getRecordDefinition() {
    return OsmElement.META_DATA;
  }

  private Map<String, String> getTags(final List<Integer> keys,
    final List<Integer> values, final PbfFieldDecoder fieldDecoder) {
    final Map<String, String> tags = new LinkedHashMap<>();

    // Ensure parallel lists are of equal size.
    if (keys.size() != values.size()) {
      throw new RuntimeException("Number of tag keys (" + keys.size()
        + ") and tag values (" + values.size() + ") don't match");
    }

    final Iterator<Integer> keyIterator = keys.iterator();
    final Iterator<Integer> valueIterator = values.iterator();
    while (keyIterator.hasNext()) {
      final String key = fieldDecoder.decodeString(keyIterator.next());
      final String value = fieldDecoder.decodeString(valueIterator.next());
      tags.put(key, value);
    }
    return tags;
  }

  private void processBlob() throws IOException {

    final byte[] blobContent = readBlobContent();

    try {
      if ("OSMHeader".equals(this.blobType)) {
        processOsmHeader(blobContent);
      } else if ("OSMData".equals(this.blobType)) {
        processOsmPrimitives(blobContent);
      } else {
        if (log.isLoggable(Level.FINER)) {
          log.finer("Skipping unrecognised blob type " + this.blobType);
        }
      }
    } catch (final InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  private void processNodes(final DenseNodes nodes,
    final PbfFieldDecoder fieldDecoder) {
    final List<Long> idList = nodes.getIdList();
    final List<Long> latList = nodes.getLatList();
    final List<Long> lonList = nodes.getLonList();

    // Ensure parallel lists are of equal size.
    if (idList.size() != latList.size() || idList.size() != lonList.size()) {
      throw new RuntimeException("Number of ids (" + idList.size()
        + "), latitudes (" + latList.size() + "), and longitudes ("
        + lonList.size() + ") don't match");
    }

    final Iterator<Integer> keysValuesIterator = nodes.getKeysValsList()
        .iterator();

    DenseInfo denseInfo;
    if (nodes.hasDenseinfo()) {
      denseInfo = nodes.getDenseinfo();
    } else {
      denseInfo = null;
    }

    long nodeId = 0;
    long latitude = 0;
    long longitude = 0;
    int userId = 0;
    String username = "";
    int userSid = 0;
    long time = 0;
    Date timestamp;
    long changesetId = 0;
    long changeset = -1;
    int version = -1;
    for (int i = 0; i < idList.size(); i++) {

      // Delta decode node fields.
      nodeId += idList.get(i);
      latitude += latList.get(i);
      longitude += lonList.get(i);

      if (denseInfo != null) {
        // Delta decode dense info fields.
        userId += denseInfo.getUid(i);
        userSid += denseInfo.getUserSid(i);
        time += denseInfo.getTimestamp(i);
        changesetId += denseInfo.getChangeset(i);
        changeset = changesetId;
        if (userId >= 0) {
          username = fieldDecoder.decodeString(userSid);
        } else {
          username = "";
        }

        version = denseInfo.getVersion(i);
        timestamp = fieldDecoder.decodeTimestamp(time);
      } else {
        version = -1;
        username = "";
        changeset = -1;
        timestamp = new Timestamp(0);
      }

      // Build the tags. The key and value string indexes are sequential
      // in the same PBF array. Each set of tags is delimited by an index
      // with a value of 0.
      final Map<String, String> tags = new LinkedHashMap<>();
      while (keysValuesIterator.hasNext()) {
        final int keyIndex = keysValuesIterator.next();
        if (keyIndex == 0) {
          break;
        }
        if (!keysValuesIterator.hasNext()) {
          throw new RuntimeException(
              "The PBF DenseInfo keys/values list contains a key with no corresponding value.");
        }
        final int valueIndex = keysValuesIterator.next();

        final String tagName = fieldDecoder.decodeString(keyIndex);
        final String tagValue = fieldDecoder.decodeString(valueIndex);
        tags.put(tagName, tagValue);
      }

      final double x = fieldDecoder.decodeLongitude(longitude);
      final double y = fieldDecoder.decodeLatitude(latitude);
      final OsmNode node = new OsmNode(nodeId, true, version, changeset,
        timestamp, username, userId, tags, x, y);

      // System.out.println("node dense " + nodeId);
      addNode(this.currentRecords, node);
    }
  }

  private void processNodes(final List<Node> nodes,
    final PbfFieldDecoder fieldDecoder) {
    // System.out.println("node");
    for (final Node node : nodes) {
      final long nodeId = node.getId();
      final boolean visible = true;
      int version = -1;
      long changeset = -1;
      int userId = -1;
      long time = 0;
      String username = "";
      final List<Integer> keysList = node.getKeysList();
      final List<Integer> valsList = node.getValsList();
      if (node.hasInfo()) {
        final Info info = node.getInfo();
        version = info.getVersion();
        changeset = info.getChangeset();
        userId = info.getUid();
        time = info.getTimestamp();
        final int userSid = info.getUserSid();
        if (userSid >= 0) {
          username = fieldDecoder.decodeString(userSid);
        }
      }
      final Date timestamp = fieldDecoder.decodeTimestamp(time);
      final Map<String, String> tags = getTags(keysList, valsList, fieldDecoder);
      final double latitude = fieldDecoder.decodeLatitude(node.getLat());
      final double longitude = fieldDecoder.decodeLatitude(node.getLon());
      final OsmNode osmNode = new OsmNode(nodeId, visible, version, changeset,
        timestamp, username, userId, tags, longitude, latitude);
      addNode(this.currentRecords, osmNode);
    }
  }

  private void processOsmHeader(final byte[] data)
      throws InvalidProtocolBufferException {
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

  private void processOsmPrimitives(final byte[] data)
      throws InvalidProtocolBufferException {
    final Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock.parseFrom(data);
    final PbfFieldDecoder fieldDecoder = new PbfFieldDecoder(block);

    for (final PrimitiveGroup primitiveGroup : block.getPrimitivegroupList()) {
      processNodes(primitiveGroup.getDense(), fieldDecoder);
      processNodes(primitiveGroup.getNodesList(), fieldDecoder);
      processWays(primitiveGroup.getWaysList(), fieldDecoder);
      // processRelations(primitiveGroup.getRelationsList(), fieldDecoder);
    }
  }

  private void processWays(final List<Way> ways,
    final PbfFieldDecoder fieldDecoder) {
    // System.out.println("way");
    for (final Way way : ways) {
      final long wayId = way.getId();
      final boolean visible = true;
      int version = -1;
      long changeset = -1;
      int userId = -1;
      long time = 0;
      String username = "";
      if (way.hasInfo()) {
        final Info info = way.getInfo();
        version = info.getVersion();
        changeset = info.getChangeset();
        userId = info.getUid();
        time = info.getTimestamp();
        final int userSid = info.getUserSid();
        if (userSid >= 0) {
          username = fieldDecoder.decodeString(userSid);
        }
      }
      final Date timestamp = fieldDecoder.decodeTimestamp(time);

      final List<Integer> keysList = way.getKeysList();
      final List<Integer> valsList = way.getValsList();
      final Map<String, String> tags = getTags(keysList, valsList, fieldDecoder);

      // Build up the list of way nodes for the way. The node ids are
      // delta encoded meaning that each id is stored as a delta against
      // the previous one.
      long nodeId = 0;
      final List<LineString> lines = new ArrayList<>();
      final List<Point> points = new ArrayList<>();
      for (final long nodeIdOffset : way.getRefsList()) {
        nodeId += nodeIdOffset;
        final Point point = this.nodePoints.get(nodeId);
        if (point == null) {
          if (points.size() > 1) {
            lines.add(OsmConstants.WGS84_2D.lineString(points));
          }
          points.clear();
        } else {
          points.add(point);
        }
      }

      if (points.size() > 1) {
        lines.add(OsmConstants.WGS84_2D.lineString(points));
      }
      if (!lines.isEmpty()) {
        final Geometry geometry = OsmConstants.WGS84_2D.geometry(lines);
        final OsmWay osmWay = new OsmWay(wayId, visible, version, changeset,
          timestamp, username, userId, tags, geometry);
        this.currentRecords.add(osmWay);
        this.wayCount++;
      } else {
        this.skippedWayCount++;
      }
    }
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
          default:
            this.blobIn.skipField(tag);
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
          throw new RuntimeException(
              "PBF blob contains incomplete compressed data.");
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
          default:
            this.blobHeaderIn.skipField(tag);
            break;
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
        }
      }

    } finally {
      this.blobHeaderIn.setInputStream(null);
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
