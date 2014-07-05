package com.revolsys.io.gml;

import java.io.Writer;

import javax.xml.namespace.QName;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.RecordProperties;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.io.PathUtil;
import com.revolsys.io.gml.type.GmlFieldType;
import com.revolsys.io.gml.type.GmlFieldTypeRegistry;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;

public class GmlRecordWriter extends AbstractWriter<Record> implements
  GmlConstants {
  public static final void srsName(final XmlWriter out,
    final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    final int csId = coordinateSystem.getId();
    out.attribute(SRS_NAME, "EPSG:" + csId);
  }

  private final GmlFieldTypeRegistry fieldTypes = GmlFieldTypeRegistry.INSTANCE;

  private GeometryFactory geometryFactory;

  private final RecordDefinition recordDefinition;

  private boolean opened;

  private final XmlWriter out;

  private QName qualifiedName;

  private final String namespaceUri;

  public GmlRecordWriter(final RecordDefinition recordDefinition, final Writer out) {
    this.recordDefinition = recordDefinition;
    this.out = new XmlWriter(out);
    qualifiedName = recordDefinition.getProperty(RecordProperties.QUALIFIED_NAME);
    if (qualifiedName == null) {
      qualifiedName = new QName(recordDefinition.getTypeName());
    }
    namespaceUri = qualifiedName.getNamespaceURI();
    this.out.setPrefix(qualifiedName);
  }

  private void box(final GeometryFactory geometryFactory,
    final BoundingBox areaBoundingBox) {
    out.startTag(BOX);
    srsName(out, geometryFactory);
    out.startTag(COORDINATES);
    out.text(areaBoundingBox.getMinX());
    out.text(",");
    out.text(areaBoundingBox.getMinY());
    out.text(" ");
    out.text(areaBoundingBox.getMaxX());
    out.text(",");
    out.text(areaBoundingBox.getMaxY());
    out.endTag(COORDINATES);
    out.endTag(BOX);
  }

  @Override
  public void close() {
    if (!opened) {
      writeHeader();
    }

    writeFooter();
    out.close();
  }

  private void envelope(
    final GeometryFactory geometryFactory,
    final BoundingBox areaBoundingBox) {
    out.startTag(ENVELOPE);
    srsName(out, geometryFactory);
    out.element(LOWER_CORNER,
      areaBoundingBox.getMinX() + " " + areaBoundingBox.getMinY());
    out.element(UPPER_CORNER,
      areaBoundingBox.getMaxX() + " " + areaBoundingBox.getMaxY());
    out.endTag(ENVELOPE);
  }

  @Override
  public void flush() {
    out.flush();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if (name.equals(IoConstants.GEOMETRY_FACTORY)) {
      this.geometryFactory = (com.revolsys.jts.geom.GeometryFactory)value;
    }
    super.setProperty(name, value);
  }

  @Override
  public void write(final Record object) {
    if (!opened) {
      writeHeader();
    }
    out.startTag(FEATURE_MEMBER);
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    QName qualifiedName = recordDefinition.getProperty(RecordProperties.QUALIFIED_NAME);
    if (qualifiedName == null) {
      final String typeName = recordDefinition.getPath();
      final String path = PathUtil.getPath(typeName);
      final String name = PathUtil.getName(typeName);
      qualifiedName = new QName(path, name);
      recordDefinition.setProperty(RecordProperties.QUALIFIED_NAME,
        qualifiedName);
    }
    out.startTag(qualifiedName);

    for (final Attribute attribute : recordDefinition.getAttributes()) {
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

    out.endTag(qualifiedName);
    out.endTag(FEATURE_MEMBER);
  }

  public void writeFooter() {
    out.endTag(FEATURE_COLLECTION);
    out.endDocument();
  }

  private void writeHeader() {
    opened = true;
    out.startDocument("UTF-8", "1.0");

    out.startTag(FEATURE_COLLECTION);
    if (geometryFactory != null) {
      out.startTag(BOUNDED_BY);
      box(geometryFactory, geometryFactory.getCoordinateSystem()
        .getAreaBoundingBox());
      out.endTag(BOUNDED_BY);
    }
  }

}
