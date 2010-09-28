package com.revolsys.jump.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectEntry;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.types.DataType;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;

public class DataObjectFeature extends BasicFeature implements DataObject {
  private static final long serialVersionUID = 8337645038948769661L;

  private DataObjectState state = DataObjectState.New;

  private final DataObjectMetaData type;

  public DataObjectFeature(
    final DataObjectFeature feature) {
    super(feature.getSchema());
    setAttributes(feature.getAttributes());
    this.type = feature.type;
  }

  public DataObjectFeature(
    final DataObjectMetaData type,
    final Feature feature) {
    super(feature.getSchema());
    setAttributes(feature.getAttributes());
    this.type = type;
  }

  public DataObjectFeature(
    final DataObjectMetaDataFeatureSchema featureSchema) {
    super(featureSchema);
    this.type = featureSchema.getMetaData();
  }

  @Override
  public DataObject clone() {
    final DataObjectFeature newObject = new DataObjectFeature(this);
    newObject.setIdValue(null);
    return newObject;
  }

  /**
   * Clones this Feature.
   * 
   * @param deep whether or not to clone the geometry
   * @return a new Feature with the same attributes as this Feature
   */
  @Override
  public Feature clone(
    final boolean deep) {
    final DataObjectMetaDataFeatureSchema schema = (DataObjectMetaDataFeatureSchema)getSchema();
    final DataObjectMetaData metaData = getMetaData();
    final Feature clone = new DataObjectFeature(schema);
    for (int i = 0; i < schema.getAttributeCount(); i++) {
      Attribute attribute = metaData.getAttribute(i);
      DataType dataType = attribute.getType();
      Class<?> dataClass = dataType.getJavaClass();
      if (Geometry.class.isAssignableFrom(dataClass)) {
        final Geometry geometry = getGeometryValue();
        if (deep) {
          clone.setAttribute(i, geometry.clone());
        } else {
          clone.setAttribute(i, geometry);
        }
      } else {
        clone.setAttribute(i, getValue(i));
      }
    }
    return clone;
  }

  public FeatureDataObjectFactory getFactory() {
    return new FeatureDataObjectFactory();
  }

  @SuppressWarnings("unchecked")
  public <T extends Geometry> T getGeometryValue() {
    return (T)super.getGeometry();
  }

  public <T extends Object> T getIdValue() {
    return null;
  }

  public DataObjectMetaData getMetaData() {
    return type;
  }

  public DataObjectState getState() {
    return getState();
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(
    final CharSequence name) {
    try {
      return (T)getAttribute(name.toString());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Object> T getValue(
    final int index) {
    try {
      return (T)getAttribute(index);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public Map<String, Object> getValueMap(
    final Collection<? extends CharSequence> attributeNames) {
    final Map<String, Object> values = new HashMap<String, Object>();
    for (final CharSequence name : attributeNames) {
      final Object value = getValue(name);
      if (value != null) {
        values.put(name.toString(), value);
      }
    }
    return values;
  }

  /**
   * Get the values of all attributes.
   * 
   * @return The attribute value.
   */
  public List<Object> getValues() {
    return Arrays.asList(getAttributes());
  }

  public boolean hasAttribute(
    final CharSequence name) {
    return getMetaData().hasAttribute(name);
  }

  public void setGeometryValue(
    final Geometry geometry) {
    setGeometry(geometry);
  }

  public void setIdValue(
    final Object id) {
    if (state != DataObjectState.New) {
      throw new IllegalStateException(
        "Cannot change the ID on a persisted object");
    }
    final int index = getMetaData().getIdAttributeIndex();
    setValue(index, id);
  }

  public void setState(
    final DataObjectState state) {
    this.state = state;
  }

  public void setValue(
    final CharSequence name,
    final Object value) {
    final int index = getMetaData().getAttributeIndex(name);
    if (index >= 0) {
      setValue(index, value);
    }
  }

  public void setValue(
    final int index,
    final Object value) {
    updateState();
    super.setAttribute(index, value);
  }

  public void setValues(
    final Map<String, ? extends Object> values) {
    for (final Entry<String, ? extends Object> defaultValue : values.entrySet()) {
      final String name = defaultValue.getKey();
      final Object value = defaultValue.getValue();
      setValue(name, value);
    }
  }

  /**
   * Return a String representation of the Object. There is no guarantee as to
   * the format of this string.
   * 
   * @return The string value.
   */
  @Override
  public String toString() {
    final StringBuffer s = new StringBuffer();
    final DataObjectMetaData metaData = getMetaData();
    s.append(metaData.getName()).append("(\n");
    for (int i = 0; i < getAttributes().length; i++) {
      final Object value = getValue(i);
      if (value != null) {
        s.append(metaData.getAttributeName(i))
          .append('=')
          .append(value)
          .append('\n');
      }
    }
    s.append(')');
    return s.toString();
  }

  protected void updateState() {
    switch (state) {
      case Persisted:
        state = DataObjectState.Modified;
      break;
      case Deleted:
        throw new IllegalStateException(
          "Cannot modify an object which has been deleted");
    }
  }
  @Override
  public Set<Entry<String, Object>> entrySet() {
    Set<Entry<String, Object>> entries = new LinkedHashSet<Entry<String,Object>>();
    for(int i = 0; i < getAttributes().length; i++) {
      entries.add(new DataObjectEntry(this, i));
    }
    return entries;
  }

}
