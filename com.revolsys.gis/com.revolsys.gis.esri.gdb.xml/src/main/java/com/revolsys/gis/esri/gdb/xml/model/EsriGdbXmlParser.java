package com.revolsys.gis.esri.gdb.xml.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.revolsys.gis.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.xml.io.XmlProcessor;

public class EsriGdbXmlParser extends XmlProcessor implements
  EsriGeodatabaseXmlConstants {

  private static final Map<String, Class<?>> TAG_NAME_CLASS_MAP = new HashMap<String, Class<?>>();

  static {
    TAG_NAME_CLASS_MAP.put(CHILDREN.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(SUBTYPE.getLocalPart(), Subtype.class);
    TAG_NAME_CLASS_MAP.put(FIELD_INFOS.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(SUBTYPE_FIELD_INFO.getLocalPart(),
      SubtypeFieldInfo.class);
    TAG_NAME_CLASS_MAP.put(RELATIONSHIP_CLASS_NAMES.getLocalPart(),
      ArrayList.class);
    TAG_NAME_CLASS_MAP.put(SUBTYPES.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(DATA_ELEMENT.getLocalPart(), DataElement.class);
    TAG_NAME_CLASS_MAP.put(DE_DATASET.getLocalPart(), DEDataset.class);
    TAG_NAME_CLASS_MAP.put(DE_GEO_DATASET.getLocalPart(), DEGeoDataset.class);
    TAG_NAME_CLASS_MAP.put(DE_FEATURE_DATASET.getLocalPart(),
      DEFeatureDataset.class);
    TAG_NAME_CLASS_MAP.put(DE_FEATURE_CLASS.getLocalPart(),
      DEFeatureClass.class);
    TAG_NAME_CLASS_MAP.put(DE_TABLE.getLocalPart(), DETable.class);
    TAG_NAME_CLASS_MAP.put(ENVELOPE.getLocalPart(), Envelope.class);
    TAG_NAME_CLASS_MAP.put(ENVELOPE_N.getLocalPart(), EnvelopeN.class);
    TAG_NAME_CLASS_MAP.put(SPATIAL_REFERENCE.getLocalPart(),
      SpatialReference.class);
    TAG_NAME_CLASS_MAP.put(FIELD_ARRAY.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(FIELDS.getLocalPart(), null);
    TAG_NAME_CLASS_MAP.put(FIELD.getLocalPart(), Field.class);
    TAG_NAME_CLASS_MAP.put(INDEX_ARRAY.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(INDEXES.getLocalPart(), null);
    TAG_NAME_CLASS_MAP.put(METADATA.getLocalPart(), null);
    TAG_NAME_CLASS_MAP.put(INDEX.getLocalPart(), Index.class);
    TAG_NAME_CLASS_MAP.put(GEOMETRY_DEF.getLocalPart(), GeometryDef.class);
    TAG_NAME_CLASS_MAP.put(CONTROLLER_MEMBERSHIP.getLocalPart(),
      GeometryDef.class);
    TAG_NAME_CLASS_MAP.put(PROPERTY_ARRAY.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(PROPERTY_SET.getLocalPart(), null);
    TAG_NAME_CLASS_MAP.put(PROPERTY_SET_PROPERTY.getLocalPart(),
      PropertySetProperty.class);
    TAG_NAME_CLASS_MAP.put(EXTENT.getLocalPart(), EnvelopeN.class);
    TAG_NAME_CLASS_MAP.put(GEOGRAPHIC_COORDINATE_SYSTEM.getLocalPart(),
      GeographicCoordinateSystem.class);
    TAG_NAME_CLASS_MAP.put(PROJECTED_COORDINATE_SYSTEM.getLocalPart(),
      ProjectedCoordinateSystem.class);
  }

  public EsriGdbXmlParser() {
    super("http://www.esri.com/schemas/ArcGIS/10.1", TAG_NAME_CLASS_MAP);
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

}
