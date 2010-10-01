package com.revolsys.gis.gml;

import java.io.Writer;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.gml.type.GmlFieldType;
import com.revolsys.gis.gml.type.GmlFieldTypeRegistry;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.xml.io.XmlWriter;

public class GmlDataObjectWriter extends AbstractWriter<DataObject> implements
  GmlConstants {
  private final GmlFieldTypeRegistry fieldTypes = GmlFieldTypeRegistry.INSTANCE;

  private final DataObjectMetaData metaData;

  private boolean opened;

  private final XmlWriter out;

  private GeometryFactory geometryFactory;

  public GmlDataObjectWriter(
    final DataObjectMetaData metaData,
    final Writer out) {
    this.metaData = metaData;
    this.out = new XmlWriter(out);
  }

  @Override
  public void close() {
    if (!opened) {
      writeHeader();
    }

    writeFooter();
    out.close();
  }

  @Override
  public void flush() {
    out.flush();
  }

  @Override
  public void setProperty(
    String name,
    Object value) {
    if (name.equals(IoConstants.GEOMETRY_FACTORY)) {
      this.geometryFactory = (GeometryFactory)value;
    }
    super.setProperty(name, value);
  }

  public void write(
    final DataObject object) {
    if (!opened) {
      writeHeader();
    }
    final DataObjectMetaData metaData = object.getMetaData();
    final QName typeName = metaData.getName();
    final String namespaceUri = typeName.getNamespaceURI();
    out.startTag(typeName);

    for (final Attribute attribute : metaData.getAttributes()) {
      final String attributeName = attribute.getName();
      out.startTag(namespaceUri, attributeName);
      final Object value = object.getValue(attributeName);
      final DataType type = attribute.getType();
      final GmlFieldType fieldType = fieldTypes.getFieldType(type);
      if (fieldType != null) {
        fieldType.writeValue(out, value);
      }
      out.endTag();
    }

    out.endTag(typeName);
  }

  public void writeFooter() {
    out.endTag(FEATURE_MEMBERS);
    out.endTag(FEATURE_COLLECTION);
    out.endDocument();
  }

  private void writeHeader() {
    opened = true;
    out.startDocument();
    out.startTag(FEATURE_COLLECTION);
    if (geometryFactory != null) {
      out.startTag(BOUNDED_BY);
      boundingBox(geometryFactory, geometryFactory.getCoordinateSystem()
        .getAreaBoundingBox());
      out.endTag(BOUNDED_BY);
    }
    out.startTag(FEATURE_MEMBERS);
  }

  private void boundingBox(
    GeometryFactory geometryFactory,
    BoundingBox areaBoundingBox) {
    out.startTag(ENVELOPE);
    srsName(out, geometryFactory);
    out.element(LOWER_CORNER,
      areaBoundingBox.getMinX() + " " + areaBoundingBox.getMinY());
    out.element(UPPER_CORNER,
      areaBoundingBox.getMaxX() + " " + areaBoundingBox.getMaxY());
    out.endTag(ENVELOPE);
  }

  public static final void srsName(
    XmlWriter out,
    GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    final int csId = coordinateSystem.getId();
    out.attribute(SRS_NAME, "urn:ogc:def:crs:EPSG:6.6:" + csId);
  }

}
