package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.context.HashMapContext;

import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMap;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.prep.PreparedGeometry;
import com.revolsys.jts.geom.prep.PreparedGeometryFactory;
import com.revolsys.jts.operation.buffer.Buffer;
import com.revolsys.jts.operation.buffer.BufferParameters;
import com.revolsys.jts.simplify.DouglasPeuckerSimplifier;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.util.JexlUtil;

public class CreateObjectsWithinDistanceOfGeometry extends
  BaseInOutProcess<DataObject, DataObject> {

  private Map<String, Object> attributes = new HashMap<String, Object>();

  private double distance;

  private Channel<DataObject> geometryIn;

  private List<DataObject> geometryObjects = new ArrayList<DataObject>();

  private Map<DataObjectMetaData, Map<DataObjectMetaData, PreparedGeometry>> metaDataGeometryMap = new HashMap<DataObjectMetaData, Map<DataObjectMetaData, PreparedGeometry>>();

  private String typePathTemplate;

  private Expression typePathTemplateExpression;

  private boolean writeOriginal;

  @Override
  protected void destroy() {
    super.destroy();
    if (geometryIn != null) {
      geometryIn.readDisconnect();
      geometryIn = null;
    }
    attributes = null;
    geometryObjects = null;
    metaDataGeometryMap = null;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public double getDistance() {
    return distance;
  }

  public Channel<DataObject> getGeometryIn() {
    if (geometryIn == null) {
      setGeometryIn(new Channel<DataObject>());
    }
    return geometryIn;
  }

  public List<DataObject> getGeometryObjects() {
    return geometryObjects;
  }

  private final Map<DataObjectMetaData, PreparedGeometry> getMetaDataGeometries(
    final DataObjectMetaData metaData) {
    Map<DataObjectMetaData, PreparedGeometry> metaDataGeometries = metaDataGeometryMap.get(metaData);
    if (metaDataGeometries == null) {
      final PreparedGeometryFactory preparedGeometryFactory = new PreparedGeometryFactory();
      metaDataGeometries = new LinkedHashMap<DataObjectMetaData, PreparedGeometry>();
      DataObjectMetaData newMetaData;
      PreparedGeometry preparedGeometry;
      for (final DataObject object : geometryObjects) {
        Geometry geometry = object.getGeometryValue();
        if (geometry != null) {
          final JexlContext context = new HashMapContext();
          final Map<String, Object> vars = new HashMap<String, Object>(
            attributes);
          vars.putAll(new DataObjectMap(object));
          vars.put("typePath", metaData.getPath());
          context.setVars(vars);
          final String typePath = (String)JexlUtil.evaluateExpression(context,
            typePathTemplateExpression);
          newMetaData = new DataObjectMetaDataImpl(typePath,
            metaData.getAttributes());
          if (distance > 0) {
            final BufferParameters parameters = new BufferParameters(1, 3, 2,
              1.0D);
            geometry = Buffer.buffer(geometry, distance, parameters);
          }
          geometry = DouglasPeuckerSimplifier.simplify(geometry, 2D);
          preparedGeometry = preparedGeometryFactory.create(geometry);
          metaDataGeometries.put(newMetaData, preparedGeometry);
        }
      }

      metaDataGeometryMap.put(metaData, metaDataGeometries);
    }
    return metaDataGeometries;
  }

  public String getTypeNameTemplate() {
    return typePathTemplate;
  }

  private void initializeGeometries(final Channel<DataObject> geometryIn) {
    if (geometryIn != null) {
      for (final DataObject object : geometryIn) {
        geometryObjects.add(object);
      }
    }
  }

  public boolean isWriteOriginal() {
    return writeOriginal;
  }

  @Override
  protected void preRun(final Channel<DataObject> in,
    final Channel<DataObject> out) {
    initializeGeometries(geometryIn);
  }

  @Override
  protected void process(final Channel<DataObject> in,
    final Channel<DataObject> out, final DataObject object) {
    if (writeOriginal) {
      out.write(object);
    }
    final DataObjectMetaData metaData = object.getMetaData();
    final Geometry geometryValue = object.getGeometryValue();
    final Map<DataObjectMetaData, PreparedGeometry> metaDataGeometries = getMetaDataGeometries(metaData);
    for (final Entry<DataObjectMetaData, PreparedGeometry> metaDataGeometry : metaDataGeometries.entrySet()) {
      final DataObjectMetaData newMetaData = metaDataGeometry.getKey();
      final PreparedGeometry intersectsGeometry = metaDataGeometry.getValue();
      if (intersectsGeometry.intersects(geometryValue)) {
        final DataObject newObject = new ArrayDataObject(newMetaData, object);
        out.write(newObject);
      }
    }
  }

  public void setAttributes(final Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public void setDistance(final double distance) {
    this.distance = distance;
  }

  public void setGeometryIn(final Channel<DataObject> geometryIn) {
    this.geometryIn = geometryIn;
    geometryIn.readConnect();
  }

  public void setGeometryObjects(final List<DataObject> geometryObjects) {
    this.geometryObjects = geometryObjects;
  }

  public void setTypeNameTemplate(final String typePathTemplate) {
    this.typePathTemplate = typePathTemplate;
    try {
      typePathTemplateExpression = JexlUtil.createExpression(typePathTemplate,
        "%\\{([^\\}]+)\\}");
    } catch (final Exception e) {
      throw new IllegalArgumentException((new StringBuilder()).append(
        "Invalid type name template: ")
        .append(typePathTemplate)
        .toString(), e);
    }
  }

  public void setWriteOriginal(final boolean writeOriginal) {
    this.writeOriginal = writeOriginal;
  }
}
