package com.revolsys.io.openstreetmap.model;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;

import org.springframework.util.StringUtils;

import com.revolsys.data.record.AbstractRecord;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataTypes;
import com.revolsys.io.xml.StaxUtils;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.util.Property;

public abstract class OsmElement extends AbstractRecord implements OsmConstants {
  private long id;

  private boolean visible;

  private int version;

  private long changeset;

  private Timestamp timestamp;

  private String user;

  private int uid;

  private Map<String, String> tags;

  private Geometry geometry;

  public static final RecordDefinition META_DATA;

  static {
    final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(
        "osm.record");
    recordDefinition.addAttribute("id", DataTypes.LONG);
    recordDefinition.addAttribute("visible", DataTypes.BOOLEAN);
    recordDefinition.addAttribute("version", DataTypes.INT);
    recordDefinition.addAttribute("changeset", DataTypes.LONG);
    recordDefinition.addAttribute("timestamp", DataTypes.DATE_TIME);
    recordDefinition.addAttribute("user", DataTypes.STRING);
    recordDefinition.addAttribute("uid", DataTypes.INT);
    recordDefinition.addAttribute("tags", DataTypes.MAP);
    recordDefinition.addAttribute("geometry", DataTypes.GEOMETRY);
    META_DATA = recordDefinition;
  }

  public OsmElement() {
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
    if (StringUtils.hasText(key)) {
      if (key.length() <= 255) {
        if (StringUtils.hasText(value)) {
          if (value.length() <= 255) {
            if (this.tags == null) {
              this.tags = new HashMap<>();
            }
            this.tags.put(key, value);
          } else {
            throw new IllegalArgumentException("Value length " + key.length()
              + " must be <= 255");
          }
        } else {
          this.tags.remove(key);
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
    if (tags != null) {
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
    return META_DATA;
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
    final Map<String, String> tags = this.tags;
    if (tags == null) {
      return Collections.emptyMap();
    }
    return new HashMap<>(this.tags);
  }

  public Timestamp getTimestamp() {
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

  @Override
  public void setState(final RecordState state) {
  }

  public void setTags(final Map<String, String> tags) {
    this.tags.clear();
    addTags(tags);
  }

  public void setTimestamp(final Timestamp timestamp) {
    this.timestamp = timestamp;
  }

  public void setUid(final int uid) {
    this.uid = uid;
  }

  public void setUser(final String user) {
    this.user = user;
  }

  @Override
  public void setValue(final int index, final Object value) {
    final String propertyName = getRecordDefinition().getAttributeName(index);
    Property.set(this, propertyName, value);
  }

  public void setVersion(final int version) {
    this.version = version;
  }

  public void setVisible(final boolean visible) {
    this.visible = visible;
  }

}
