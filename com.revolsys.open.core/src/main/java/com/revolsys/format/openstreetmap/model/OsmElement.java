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
    final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl("/osm/record");
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

  public OsmElement(final long id, final boolean visible, final int version, final long changeset,
    final Date timestamp, final String user, final int uid, final Map<String, String> tags) {
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
    this.id = StaxUtils.getLongAttribute(in, null, "id");
    this.visible = StaxUtils.getBooleanAttribute(in, null, "visible");
    this.version = StaxUtils.getIntAttribute(in, null, "version");
    this.changeset = StaxUtils.getIntAttribute(in, null, "changeset");
    this.user = in.getAttributeValue(null, "user");
    this.uid = StaxUtils.getIntAttribute(in, null, "uid");
  }

  public synchronized void addTag(final String key, final String value) {
    if (Property.hasValue(key)) {
      if (key.length() <= 255) {
        if (Property.hasValue(value)) {
          if (value.length() <= 255) {
            if (this.tags.isEmpty()) {
              this.tags = new HashMap<>();
            }
            this.tags.put(key, value);
          } else {
            throw new IllegalArgumentException("Value length " + key.length() + " must be <= 255");
          }
        } else {
          removeTag(key);
        }
      } else {
        throw new IllegalArgumentException("Value length " + value.length() + " must be <= 255");
      }
    } else {
      throw new IllegalArgumentException("Key cannot be null or the emptry string");
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
    return this.changeset;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Geometry> T getGeometryValue() {
    return (T)this.geometry;
  }

  public long getId() {
    return this.id;
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
    if (this.tags == null) {
      return null;
    } else {
      return this.tags.get(name);
    }
  }

  public Map<String, String> getTags() {
    return new HashMap<>(this.tags);
  }

  public Date getTimestamp() {
    return this.timestamp;
  }

  public int getUid() {
    return this.uid;
  }

  public String getUser() {
    return this.user;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final int index) {
    Object value = null;
    switch (index) {
      case 0:
        value = this.id;
      break;
      case 1:
        value = this.visible;
      break;
      case 2:
        value = this.version;
      break;
      case 3:
        value = this.changeset;
      break;
      case 4:
        value = this.timestamp;
      break;
      case 5:
        value = this.user;
      break;
      case 6:
        value = this.uid;
      break;
      case 7:
        value = this.tags;
      break;
      case 8:
        value = this.geometry;
      break;
    }
    return (V)value;
  }

  public int getVersion() {
    return this.version;
  }

  @Override
  public int hashCode() {
    return (int)(this.id ^ this.id >>> 32);
  }

  public boolean hasTags() {
    return this.tags != null && !this.tags.isEmpty();
  }

  public boolean isTagged() {
    return this.tags != null && !this.tags.isEmpty();
  }

  public boolean isVisible() {
    return this.visible;
  }

  protected void parseTag(final XMLStreamReader in) {
    final String key = in.getAttributeValue(null, "k");
    final String value = in.getAttributeValue(null, "v");
    addTag(key, value);
    StaxUtils.skipToEndElement(in, TAG);
  }

  public void removeTag(final String key) {
    if (this.tags.containsKey(key)) {
      this.tags.remove(key);
      if (this.tags.isEmpty()) {
        this.tags = Collections.emptyMap();
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
