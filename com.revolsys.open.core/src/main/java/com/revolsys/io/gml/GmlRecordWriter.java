package com.revolsys.io.gml;

import java.io.Writer;

import javax.xml.namespace.QName;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.RecordProperties;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.io.PathUtil;
import com.revolsys.io.gml.type.GmlFieldType;
import com.revolsys.io.gml.type.GmlFieldTypeRegistry;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;

public class GmlRecordWriter extends AbstractRecordWriter implements
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

  public GmlRecordWriter(final RecordDefinition recordDefinition,
    final Writer out) {
    this.recordDefinition = recordDefinition;
    this.out = new XmlWriter(out);
    this.qualifiedName = recordDefinition.getProperty(RecordProperties.QUALIFIED_NAME);
    if (this.qualifiedName == null) {
      this.qualifiedName = new QName(recordDefinition.getName());
    }
    this.namespaceUri = this.qualifiedName.getNamespaceURI();
    this.out.setPrefix(this.qualifiedName);
  }

  private void box(final GeometryFactory geometryFactory,
    final BoundingBox areaBoundingBox) {
    this.out.startTag(BOX);
    srsName(this.out, geometryFactory);
    this.out.startTag(COORDINATES);
    this.out.text(areaBoundingBox.getMinX());
    this.out.text(",");
    this.out.text(areaBoundingBox.getMinY());
    this.out.text(" ");
    this.out.text(areaBoundingBox.getMaxX());
    this.out.text(",");
    this.out.text(areaBoundingBox.getMaxY());
    this.out.endTag(COORDINATES);
    this.out.endTag(BOX);
  }

  @Override
  public void close() {
    if (!this.opened) {
      writeHeader();
    }

    writeFooter();
    this.out.close();
  }

  private void envelope(final GeometryFactory geometryFactory,
    final BoundingBox areaBoundingBox) {
    this.out.startTag(ENVELOPE);
    srsName(this.out, geometryFactory);
    this.out.element(LOWER_CORNER, areaBoundingBox.getMinX() + " "
        + areaBoundingBox.getMinY());
    this.out.element(UPPER_CORNER, areaBoundingBox.getMaxX() + " "
        + areaBoundingBox.getMaxY());
    this.out.endTag(ENVELOPE);
  }

  @Override
  public void flush() {
    this.out.flush();
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
    if (!this.opened) {
      writeHeader();
    }
    this.out.startTag(FEATURE_MEMBER);
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
    this.out.startTag(qualifiedName);

    for (final Attribute attribute : recordDefinition.getAttributes()) {
      final String attributeName = attribute.getName();
      final Object value = object.getValue(attributeName);
      if (isWritable(value)) {
        this.out.startTag(this.namespaceUri, attributeName);
        final DataType type = attribute.getType();
        final GmlFieldType fieldType = this.fieldTypes.getFieldType(type);
        if (fieldType != null) {
          fieldType.writeValue(this.out, value);
        }
        this.out.endTag();
      }
    }

    this.out.endTag(qualifiedName);
    this.out.endTag(FEATURE_MEMBER);
  }

  public void writeFooter() {
    this.out.endTag(FEATURE_COLLECTION);
    this.out.endDocument();
  }

  private void writeHeader() {
    this.out.setIndent(isIndent());
    this.opened = true;
    this.out.startDocument("UTF-8", "1.0");

    this.out.startTag(FEATURE_COLLECTION);
    if (this.geometryFactory != null) {
      this.out.startTag(BOUNDED_BY);
      box(this.geometryFactory, this.geometryFactory.getCoordinateSystem()
        .getAreaBoundingBox());
      this.out.endTag(BOUNDED_BY);
    }
  }

}
