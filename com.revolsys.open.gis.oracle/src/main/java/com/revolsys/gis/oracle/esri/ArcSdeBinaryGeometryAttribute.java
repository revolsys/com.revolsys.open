package com.revolsys.gis.oracle.esri;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRow;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.util.ExceptionUtil;

public class ArcSdeBinaryGeometryAttribute extends JdbcAttribute {

  private final ArcSdeSpatialReference spatialReference;

  private final GeometryFactory geometryFactory;

  private final int numAxis;

  private final ArcSdeBinaryGeometryDataStoreExtension extension;

  public ArcSdeBinaryGeometryAttribute(
    final ArcSdeBinaryGeometryDataStoreExtension extension, final String name,
    final DataType type, final boolean required,
    final Map<String, Object> properties,
    final ArcSdeSpatialReference spatialReference, final int numAxis) {
    super(name, type, -1, 0, 0, required, properties);
    this.extension = extension;
    this.spatialReference = spatialReference;
    final GeometryFactory factory = spatialReference.getGeometryFactory();
    this.geometryFactory = GeometryFactory.getFactory(factory.getSRID(),
      numAxis, factory.getScaleXY(), factory.getScaleZ());
    this.numAxis = numAxis;
    setProperty(AttributeProperties.GEOMETRY_FACTORY, this.geometryFactory);
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public int getNumAxis() {
    return this.numAxis;
  }

  public ArcSdeSpatialReference getSpatialReference() {
    return this.spatialReference;
  }

  @Override
  public int setAttributeValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final DataObject object) throws SQLException {
    try {
      final SeConnection connection = extension.createSeConnection();
      try {
        final Integer objectId = object.getInteger("OBJECTID");
        if (objectId != null) {
          final SeObjectId featureId = new SeObjectId(objectId);
          final SeQuery query = new SeQuery(connection);
          try {
            final DataObjectMetaData metaData = getMetaData();
            final String tableName = extension.getTableName(metaData);
            final SeRow row = query.fetchRow(tableName, featureId,
              new String[] {
                metaData.getGeometryAttributeName()
              });
            ArcSdeBinaryGeometryDataStoreExtension.setValueFromRow(object, row,
              0);
          } finally {
            query.close();
          }
        }
      } finally {
        try {
          connection.close();
        } catch (final SeException e) {
        }
      }
    } catch (final SeException e) {
      ExceptionUtil.log(getClass(), "Unable to read geometry", e);
    }
    return columnIndex + 1;

  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    throw new UnsupportedOperationException(
      "Editing ArcSDE binary geometries is not supported");
  }
}
