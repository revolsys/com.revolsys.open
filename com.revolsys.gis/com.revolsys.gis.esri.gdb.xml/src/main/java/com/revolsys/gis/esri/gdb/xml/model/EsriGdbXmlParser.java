package com.revolsys.gis.esri.gdb.xml.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.revolsys.gis.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.xml.io.XmlProcessor;

public class EsriGdbXmlParser extends XmlProcessor implements
  EsriGeodatabaseXmlConstants {

  private static final Map<String, Class<?>> TAG_NAME_CLASS_MAP = new HashMap<String, Class<?>>();

  static {
    TAG_NAME_CLASS_MAP.put(DATA_ELEMENT.getLocalPart(), DataElement.class);
    TAG_NAME_CLASS_MAP.put(SPATIAL_REFERENCE.getLocalPart(),
      SpatialReference.class);
    TAG_NAME_CLASS_MAP.put(FIELD.getLocalPart(), Field.class);
    TAG_NAME_CLASS_MAP.put(INDEX.getLocalPart(), Index.class);
    TAG_NAME_CLASS_MAP.put(GEOMETRY_DEF.getLocalPart(), GeometryDef.class);
    TAG_NAME_CLASS_MAP.put(CONTROLLER_MEMBERSHIP.getLocalPart(),
      GeometryDef.class);
    TAG_NAME_CLASS_MAP.put(PROPERTY_SET.getLocalPart(), PropertySet.class);
    TAG_NAME_CLASS_MAP.put(EXTENT.getLocalPart(), Extent.class);
  }

  public EsriGdbXmlParser() {
    super("http://www.esri.com/schemas/ArcGIS/10.1", TAG_NAME_CLASS_MAP);
  }

  public List<Field> processFields(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    List<Field> fields = null;
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      QName tagName = parser.getName();
      Object value = process(parser);
      if (tagName.equals(FIELD_ARRAY)) {
        fields = (List<Field>)value;
      }
    }
    return fields;
  }

  public List<Field> processFieldArray(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    List<Field> fields = new ArrayList<Field>();
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object value = process(parser);
      if (value instanceof Field) {
        Field field = (Field)value;
        fields.add(field);
      }
    }
    return fields;
  }

  public List<ControllerMembership> processControllerMemberships(
    final XMLStreamReader parser) throws XMLStreamException, IOException {
    List<ControllerMembership> controllerMemberships = new ArrayList<ControllerMembership>();
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object value = process(parser);
      if (value instanceof ControllerMembership) {
        ControllerMembership controllerMembership = (ControllerMembership)value;
        controllerMemberships.add(controllerMembership);
      }
    }
    return controllerMemberships;
  }

  public List<Index> processIndexes(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    List<Index> indexes = null;
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      QName tagName = parser.getName();
      Object value = process(parser);
      if (tagName.equals(INDEX_ARRAY)) {
        indexes = (List<Index>)value;
      }
    }
    return indexes;
  }

  public List<Index> processIndexArray(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    List<Index> indexes = new ArrayList<Index>();
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object value = process(parser);
      if (value instanceof Index) {
        Index field = (Index)value;
        indexes.add(field);
      }
    }
    return indexes;
  }

  public Set<PropertySet> processExtensionProperties(
    final XMLStreamReader parser) throws XMLStreamException, IOException {
    Set<PropertySet> properties = null;
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      QName tagName = parser.getName();
      Object value = process(parser);
      if (tagName.equals(PROPERTY_ARRAY)) {
        properties = (Set<PropertySet>)value;
      }
    }
    return properties;
  }

  public Set<PropertySet> processPropertyArray(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    Set<PropertySet> properties = new LinkedHashSet<PropertySet>();
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object value = process(parser);
      if (value instanceof PropertySet) {
        PropertySet propertySet = (PropertySet)value;
        properties.add(propertySet);
      }
    }
    return properties;
  }
}
