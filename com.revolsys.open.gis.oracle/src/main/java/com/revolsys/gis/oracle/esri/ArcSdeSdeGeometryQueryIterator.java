package com.revolsys.gis.oracle.esri;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.springframework.jdbc.object.SqlQuery;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.revolsys.collection.AbstractIterator;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcQueryIterator;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ArcSdeSdeGeometryQueryIterator extends AbstractIterator<DataObject> {

	private static SeConnection sdeConnection;

	private static Geometry toGeometry(final SeShape shape) {

		try {
			int type = shape.getType();
			SeCoordinateReference coordRef = shape.getCoordRef();
			int srid = (int) coordRef.getSrid().longValue();
			double scaleXy = coordRef.getXYUnits();
			double scaleZ = coordRef.getZUnits();
			int numAxis = 2;
			if (shape.is3D()) {
				numAxis = 3;
			}
			if (shape.isMeasured()) {
				numAxis = 4;
			}
			GeometryFactory geometryFactory = GeometryFactory.getFactory(srid,
					numAxis, scaleXy, scaleZ);

			final int numParts = shape.getNumParts();
			final double[][][] allCoordinates = shape.getAllCoords();
			switch (type) {

			case SeShape.TYPE_NIL:
				return geometryFactory.createEmptyGeometry();
			case SeShape.TYPE_POINT:
			case SeShape.TYPE_MULTI_POINT:
				List<Point> points = new ArrayList<Point>();
				for (int partIndex = 0; partIndex < numParts; partIndex++) {
					final int numRings = shape.getNumSubParts(partIndex + 1);
					for (int ringIndex = 0; ringIndex < numRings; ringIndex++) {
						CoordinatesList coordinates = getCoordinates(shape,
								allCoordinates, partIndex, ringIndex, numAxis);
						Point point = geometryFactory.createPoint(coordinates);
						if (!point.isEmpty()) {
							points.add(point);
						}
					}
				}
				if (points.size() == 1) {
					return points.get(0);
				} else {
					return geometryFactory.createMultiPoint(points);
				}
			case SeShape.TYPE_MULTI_LINE:
			case SeShape.TYPE_LINE:
				List<LineString> lines = new ArrayList<LineString>();
				for (int partIndex = 0; partIndex < numParts; partIndex++) {
					final int numRings = shape.getNumSubParts(partIndex + 1);
					for (int ringIndex = 0; ringIndex < numRings; ringIndex++) {
						CoordinatesList coordinates = getCoordinates(shape,
								allCoordinates, partIndex, ringIndex, numAxis);
						LineString line = geometryFactory
								.createLineString(coordinates);
						if (!line.isEmpty()) {
							lines.add(line);
						}
					}
				}
				if (lines.size() == 1) {
					return lines.get(0);
				} else {
					return geometryFactory.createMultiLineString(lines);
				}
			case SeShape.TYPE_POLYGON:
			case SeShape.TYPE_MULTI_POLYGON:
				List<Polygon> polygons = new ArrayList<Polygon>();
				for (int partIndex = 0; partIndex < numParts; partIndex++) {
					final int numRings = shape.getNumSubParts(partIndex + 1);
					List<CoordinatesList> rings = new ArrayList<CoordinatesList>();
					for (int ringIndex = 0; ringIndex < numRings; ringIndex++) {
						CoordinatesList coordinates = getCoordinates(shape,
								allCoordinates, partIndex, ringIndex, numAxis);
						rings.add(coordinates);
					}
					if (!rings.isEmpty()) {
						Polygon polygon = geometryFactory.createPolygon(rings);
						polygons.add(polygon);
					}
				}
				if (polygons.size() == 1) {
					return polygons.get(0);
				} else {
					return geometryFactory.createMultiPolygon(polygons);
				}

			default:
				throw new IllegalArgumentException("Shape not supported:"
						+ shape.asText(1000));
			}

		} catch (final SeException e) {
			throw new RuntimeException("Unable to read shape", e);
		}
	}

	private static CoordinatesList getCoordinates(SeShape shape,
			double[][][] allCoordinates, int partIndex, int ringIndex,
			int numAxis) throws SeException {
		final int numCoords = shape.getNumPoints(partIndex + 1, ringIndex + 1);
		CoordinatesList coordinates = new DoubleCoordinatesList(numCoords,
				numAxis);
		for (int coordinateIndex = 0; coordinateIndex < numCoords; coordinateIndex++) {

			double x = allCoordinates[partIndex][ringIndex][coordinateIndex
					* numAxis];
			double y = allCoordinates[partIndex][ringIndex][coordinateIndex
					* numAxis + 1];
			coordinates.setX(coordinateIndex, x);
			coordinates.setY(coordinateIndex, y);
		}
		return coordinates;
	}

	public static DataObject getNextObject(final JdbcDataObjectStore dataStore,
			final DataObjectMetaData metaData,
			final List<Attribute> attributes,
			final DataObjectFactory dataObjectFactory, final SeRow row) {
		final DataObject object = dataObjectFactory.createDataObject(metaData);
		if (object != null) {
			object.setState(DataObjectState.Initalizing);
			for (int columnIndex = 0; columnIndex < attributes.size(); columnIndex++) {
				try {
					SeColumnDefinition columnDefinition = row
							.getColumnDef(columnIndex);
					final int type = columnDefinition.getType();
					if (row.getIndicator(columnIndex) != SeRow.SE_IS_NULL_VALUE) {

						String name = columnDefinition.getName();
						switch (type) {

						case SeColumnDefinition.TYPE_INT16:
							object.setValue(name, row.getShort(columnIndex));
							break;

						case SeColumnDefinition.TYPE_DATE:
							object.setValue(name, row.getTime(columnIndex));
							break;

						case SeColumnDefinition.TYPE_INT32:
							object.setValue(name, row.getInteger(columnIndex));
							break;

						case SeColumnDefinition.TYPE_FLOAT32:
							object.setValue(name, row.getFloat(columnIndex));
							break;

						case SeColumnDefinition.TYPE_FLOAT64:
							object.setValue(name, row.getDouble(columnIndex));
							break;

						case SeColumnDefinition.TYPE_STRING:
							object.setValue(name, row.getString(columnIndex));
							break;

						case SeColumnDefinition.TYPE_SHAPE:
							final SeShape shape = row.getShape(columnIndex);
							object.setValue(name, toGeometry(shape));
							break;
						}
					}
				} catch (final SeException e) {
					throw new RuntimeException("Unable to get value "
							+ columnIndex + " from result set", e);
				}
				columnIndex++;
			}
			object.setState(DataObjectState.Persisted);
			dataStore.addStatistic("query", object);
		}
		return object;
	}

	public static SeQuery getResultSet(final DataObjectMetaData metaData,
			final PreparedStatement statement, final Query query)
			throws SeException {
		JdbcUtils.setPreparedStatementParameters(statement, query);

		SeSqlConstruct sqlConstruct = null;
		final SeQuery seQuery = new SeQuery(sdeConnection, null, sqlConstruct);
		seQuery.prepareQuery();
		seQuery.execute();

		return seQuery;
	}

	private Connection connection;

	private final int currentQueryIndex = -1;

	private DataObjectFactory dataObjectFactory;

	private JdbcDataObjectStore dataStore;

	private final int fetchSize = 10;

	private DataObjectMetaData metaData;

	private List<Query> queries;

	private SeQuery resultSet;

	private PreparedStatement statement;

	private List<Attribute> attributes = new ArrayList<Attribute>();

	private Query query;

	private Statistics statistics;

	public ArcSdeSdeGeometryQueryIterator(final JdbcDataObjectStore dataStore,
			final Query query, final Map<String, Object> properties) {
		super();
		// TODO sdeConnection
		this.dataObjectFactory = query.getProperty("dataObjectFactory");
		if (this.dataObjectFactory == null) {
			this.dataObjectFactory = dataStore.getDataObjectFactory();
		}
		this.dataStore = dataStore;
		this.query = query;
		this.statistics = (Statistics) properties.get(Statistics.class
				.getName());
	}

	@Override
	@PreDestroy
	public void doClose() {
		closeSeQuery(resultSet);

		attributes = null;
		sdeConnection = null;
		dataObjectFactory = null;
		dataStore = null;
		metaData = null;
		queries = null;
		query = null;
		resultSet = null;
		statement = null;
		statistics = null;
	}

	@Override
	protected void doInit() {
		this.resultSet = getResultSet();
	}

	public JdbcDataObjectStore getDataStore() {
		return dataStore;
	}

	protected String getErrorMessage() {
		if (queries == null) {
			return null;
		} else {
			return queries.get(currentQueryIndex).getSql();
		}
	}

	public DataObjectMetaData getMetaData() {
		if (metaData == null) {
			hasNext();
		}
		return metaData;
	}

	@Override
	protected DataObject getNext() throws NoSuchElementException {
		try {
			if (resultSet != null) {
				SeRow row = resultSet.fetch();
				if (row != null) {
					final DataObject object = getNextObject(dataStore,
							metaData, attributes, dataObjectFactory, row);
					if (statistics != null) {
						statistics.add(object);
					}
					return object;
				}
			}
			close();
			throw new NoSuchElementException();
		} catch (final SeException e) {
			close();
			throw new RuntimeException(getErrorMessage(), e);
		} catch (final RuntimeException e) {
			close();
			throw e;
		} catch (final Error e) {
			close();
			throw e;
		}
	}

	protected SeQuery getResultSet() {
		final String tableName = query.getTypeName();
		metaData = query.getMetaData();
		if (metaData == null) {
			if (tableName != null) {
				metaData = dataStore.getMetaData(tableName);
				query.setMetaData(metaData);
			}
		}
		try {
		
			resultSet = getResultSet(metaData, statement, query);

			final List<String> attributeNames = new ArrayList<String>(
					query.getAttributeNames());
			if (attributeNames.isEmpty()) {
				this.attributes.addAll(metaData.getAttributes());
			} else {
				for (final String attributeName : attributeNames) {
					if (attributeName.equals("*")) {
						this.attributes.addAll(metaData.getAttributes());
					} else {
						final Attribute attribute = metaData
								.getAttribute(attributeName);
						if (attribute != null) {
							attributes.add(attribute);
						}
					}
				}
			}

			final String typePath = query.getTypeNameAlias();
			if (typePath != null) {
				final DataObjectMetaDataImpl newMetaData = ((DataObjectMetaDataImpl) metaData)
						.clone();
				newMetaData.setName(typePath);
				this.metaData = newMetaData;
			}
		} catch (final SeException e) {
			closeSeQuery(resultSet);
			throw new RuntimeException("Error performing query", e);
		}
		return resultSet;
	}

	public static void closeSeQuery(SeQuery seQuery) {
		try {
			seQuery.close();
		} catch (SeException e) {
			LoggerFactory.getLogger(ArcSdeSdeGeometryQueryIterator.class).error(
					"Unable to close query", e);
		}
	}

	protected String getSql(final Query query) {
		return JdbcUtils.getSelectSql(query);
	}

	protected void setQuery(final Query query) {
		this.query = query;
	}
}
