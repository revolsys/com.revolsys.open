package com.revolsys.jump.feature.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.jump.feature.filter.operator.Operator;
import com.revolsys.jump.util.StringUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;

public class NameValueFeatureFilter implements FeatureFilter {
  private List<NameValue> values = new ArrayList<NameValue>();

  public NameValueFeatureFilter() {
  }

  public NameValueFeatureFilter(final NameValue value) {
    this.values.add(value);
  }

  public NameValueFeatureFilter(final List<NameValue> values) {
    this.values.addAll(values);
  }

  public void add(final String name, final Operator operator, final Object value) {
    values.add(new NameValue(name, operator, value));
  }

  public void add(final NameValue nameValue) {
    values.add(nameValue);
  }

  public boolean accept(final Feature feature) {
    FeatureSchema schema = feature.getSchema();
    if (values != null && !values.isEmpty()) {
      for (NameValue nameValue : values) {
        String name = (String)nameValue.getName();
        Operator operator = nameValue.getOperator();
        Object featureValue = null;
        try {
          featureValue = feature.getAttribute(name);
        } catch (IllegalArgumentException e) {
          try {
            featureValue = getAttributeByPath(schema, feature, name);
          } catch (IllegalArgumentException e2) {
          }
        }
        Object matchValue = nameValue.getValue();
        if (!operator.match(featureValue, matchValue)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public Object getAttributeByPath(final FeatureSchema schema, final Feature feature,
    final String path) {
    String[] propertyPath = path.split("\\.");
    if (propertyPath.length == 0) {
      System.out.println(path);
      return null;
    } else {
      Object propertyValue = feature;
      for (int i = 0; i < propertyPath.length && propertyValue != null; i++) {
        String propertyName = propertyPath[i];
        if (propertyValue instanceof Feature) {
          Feature dataObject = (Feature)propertyValue;
          propertyValue = dataObject.getAttribute(propertyName);
        } else if (propertyValue instanceof Geometry) {
          Geometry geometry = (Geometry)propertyValue;
          Object userData = geometry.getUserData();
          if (userData instanceof Map) {
            Map<String, Object> attrs = (Map<String, Object>)userData;
            propertyValue = attrs.get(propertyName);
          }
        } else {
          propertyValue = null;
        }
      }
      return propertyValue;
    }
  }

  /**
   * @return the values
   */
  public List<NameValue> getValues() {
    return values;
  }

  /**
   * @param values the values to set
   */
  public void setValues(final List<NameValue> values) {
    this.values.clear();
    this.values.addAll(values);
  }

  public String toString() {
    return StringUtil.toString(values);
  }
}
