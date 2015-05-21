package com.revolsys.format.openstreetmap.model;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;

import com.revolsys.data.record.AbstractRecord;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataTypes;
import com.revolsys.format.xml.StaxUtils;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.util.Property;

public class OsmElement extends AbstractRecord implements OsmConstants {
  public static final RecordDefinition RECORD_DEFINITION;

  static {
    final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(
      "/osm/record");
    recordDefinition.addField("id", DataTypes.LONG);
    recordDefinition.addField("visible", DataTypes.BOOLEAN);
    recordDefinition.addField("version", DataTypes.INT);
    recordDefinition.addField("changeset", DataTypes.LONG);
    recordDefinition.addField("timestamp", DataTypes.DATE_TIME);
    recordDefinition.addField("user", DataTypes.STRING);
    recordDefinition.addField("uid", DataTypes.INT);
    recordDefinition.addField("tags", DataTypes.MAP);
    recordDefinition.addField("geometry", DataTypes.GEOMETRY);
    recordDefinition.setGeometryFieldName("geometry");
    recordDefinition.setGeometryFactory(WGS84_2D);
    RECORD_DEFINITION = recordDefinition;
  }

  private long changeset = -1;

  private Geometry geometry;

  private long id;

  private Map<String, String> tags = Collections.emptyMap();

  private Date timestamp = new Date(0);

  private int uid = -1;

  private String user = "";

  private int version = -1;

  private boolean visible = true;

  public OsmElement() {
  }

  public OsmElement(final long id, final boolean visible, final int version,
    final long changeset, final Date timestamp, final String user,
    final int uid, final Map<String, String> tags) {
    this.id = id;
    this.visible = visible;
    this.version = version;
    this.changeset = changeset;
    this.timestamp = timestamp;
    this.user = user;
    this.uid = uid;
    this.tags = tags;
  }

  public OsmElement(final OsmElement element) {
    setInfo(element);
  }

  public OsmElement(final XMLStreamReader in) {
    id = StaxUtils.getLongAttribute(in, null, "id");
    visible = StaxUtils.getBooleanAttribute(in, null, "visible");
    version = StaxUtils.getIntAttribute(in, null, "version");
    changeset = StaxUtils.getIntAttribute(in, null, "changeset");
    user = in.getAttributeValue(null, "user");
    uid = StaxUtils.getIntAttribute(in, null, "uid");
  }

  public synchronized void addTag(final String key, final String value) {
    if (Property.hasValue(key)) {
      if (key.length() <= 255) {
        if (Property.hasValue(value)) {
          if (value.length() <= 255) {
            if (tags.isEmpty()) {
              tags = new HashMap<>();
            }
            tags.put(key, value);
          } else {
            throw new IllegalArgumentException("Value length " + key.length()
              + " must be <= 255");
          }
        } else {
          removeTag(key);
        }
      } else {
        throw new IllegalArgumentException("Value length " + value.length()
          + " must be <= 255");
      }
    } else {
      throw new IllegalArgumentException(
        "Key cannot be null or the emptry string");
    }
  }

  public void addTags(final Map<String, String> tags) {
    if (tags != null && !tags.isEmpty()) {

      for (final Entry<String, String> tag : tags.entrySet()) {
        final String key = tag.getKey();
        final String value = tag.getValue();
        addTag(key, value);
      }
    }
  }

  public long getChangeset() {
    return changeset;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Geometry> T getGeometryValue() {
    return (T)geometry;
  }

  public long getId() {
    return id;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return RECORD_DEFINITION;
  }

  @Override
  public RecordState getState() {
    return RecordState.New;
  }

  public String getTag(final String name) {
    if (tags == null) {
      return null;
    } else {
      return tags.get(name);
    }
  }

  public Map<String, String> getTags() {
    return new HashMap<>(tags);
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public int getUid() {
    return uid;
  }

  public String getUser() {
    return user;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final int index) {
    Object value = null;
    switch (index) {
      case 0:
        value = id;
      break;
      case 1:
        value = visible;
      break;
      case 2:
        value = version;
      break;
      case 3:
        value = changeset;
      break;
      case 4:
        value = timestamp;
      break;
      case 5:
        value = user;
      break;
      case 6:
        value = uid;
      break;
      case 7:
        value = tags;
      break;
      case 8:
        value = geometry;
      break;
    }
    return (V)value;
  }

  public int getVersion() {
    return version;
  }

  @Override
  public int hashCode() {
    return (int)(id ^ id >>> 32);
  }

  public boolean hasTags() {
    return tags != null && !tags.isEmpty();
  }

  public boolean isTagged() {
    return tags != null && !tags.isEmpty();
  }

  public boolean isVisible() {
    return visible;
  }

  protected void parseTag(final XMLStreamReader in) {
    final String key = in.getAttributeValue(null, "k");
    final String value = in.getAttributeValue(null, "v");
    addTag(key, value);
    StaxUtils.skipToEndElement(in, TAG);
  }

  public void removeTag(final String key) {
    if (tags.containsKey(key)) {
      tags.remove(key);
      if (tags.isEmpty()) {
        tags = Collections.emptyMap();
      }
    }
  }

  public void setChangeset(final long changeset) {
    this.changeset = changeset;
  }

  @Override
  public void setGeometryValue(final Geometry geometry) {
    if (geometry == null) {
      this.geometry = null;
    } else {
      this.geometry = geometry.convert(WGS84_2D, geometry.getAxisCount());
    }
  }

  public void setId(final long id) {
    this.id = id;
  }

  public void setInfo(final OsmElement element) {
    setId(element.getId());
    setChangeset(element.getChangeset());
    setTimestamp(element.getTimestamp());
    setUid(element.getUid());
    setUser(element.getUser());
    setVersion(element.getVersion());
    setTags(element.getTags());
  }

  @Override
  public void setState(final RecordState state) {
  }

  public void setTags(final Map<String, String> tags) {
    this.tags = Collections.emptyMap();
    addTags(tags);
  }

  public void setTimestamp(final Date timestamp) {
    this.timestamp = timestamp;
  }

  public void setTimestamp(final long timestamp) {
    setTimestamp(new Date(timestamp));
  }

  public void setUid(final int uid) {
    this.uid = uid;
  }

  public void setUser(final String user) {
    this.user = user;
  }

  @Override
  public boolean setValue(final int index, final Object value) {
    final String propertyName = getRecordDefinition().getFieldName(index);
    Property.set(this, propertyName, value);
    return true;
  }

  public void setVersion(final int version) {
    this.version = version;
  }

  public void setVisible(final boolean visible) {
    this.visible = visible;
  }

}
