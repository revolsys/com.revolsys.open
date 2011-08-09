package com.revolsys.gis.esri.gdb.xml.model;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.xml.XmlConstants;
import com.revolsys.xml.XsiConstants;
import com.revolsys.xml.io.XmlWriter;

public class EsriGdbXmlSerializer implements EsriGeodatabaseXmlConstants {

  private static final Logger LOG = LoggerFactory.getLogger(EsriGdbXmlSerializer.class);

  public static String toString(final Object object) {
    final StringWriter writer = new StringWriter();
    final EsriGdbXmlSerializer serializer = new EsriGdbXmlSerializer(null,
      writer);
    serializer.serialize(object);
    writer.flush();
    return writer.toString();
  }

  private final Map<Class<?>, Set<QName>> classPropertyTagNamesMap = new HashMap<Class<?>, Set<QName>>();

  private final Map<Class<?>, QName> classTagNameMap = new HashMap<Class<?>, QName>();

  private final Map<Class<?>, QName> classXsiTagNameMap = new HashMap<Class<?>, QName>();

  private final Map<QName, QName> tagNameXsiTagNameMap = new HashMap<QName, QName>();

  private final Map<QName, QName> tagNameChildTagNameMap = new HashMap<QName, QName>();

  private final Map<QName, QName> tagNameListElementTagNameMap = new HashMap<QName, QName>();

  private final Map<Class<?>, Map<QName, Method>> classPropertyMethodMap = new HashMap<Class<?>, Map<QName, Method>>();

  private XmlWriter out;

  private boolean writeNamespaces;

  private boolean writeFirstNamespace;

  private boolean writeNull;

  private final Map<Class<?>, QName> classTypeNameMap = new LinkedHashMap<Class<?>, QName>();

  private final Set<QName> xsiTypeTypeNames = new HashSet<QName>();

  private EsriGdbXmlSerializer() {
    addTagNameXsiTagName(METADATA, XML_PROPERTY_SET);
    addTagNameChildTagName(METADATA, XML_DOC);

    addClassProperties(SpatialReference.class, SPATIAL_REFERENCE, null, WKT,
      X_ORIGIN, Y_ORIGIN, XY_SCALE, Z_ORIGIN, Z_SCALE, M_ORIGIN, M_SCALE,
      XY_TOLERANCE, Z_TOLERANCE, M_TOLERANCE, HIGH_PRECISION, WKID);

    addClassProperties(GeographicCoordinateSystem.class, SPATIAL_REFERENCE,
      GEOGRAPHIC_COORDINATE_SYSTEM);

    addClassProperties(ProjectedCoordinateSystem.class, SPATIAL_REFERENCE,
      PROJECTED_COORDINATE_SYSTEM);

    addClassProperties(GeometryDef.class, GEOMETRY_DEF, GEOMETRY_DEF,
      AVG_NUM_POINTS, GEOMETRY_TYPE, HAS_M, HAS_Z, SPATIAL_REFERENCE,
      GRID_SIZE_0, GRID_SIZE_1, GRID_SIZE_2);

    addClassProperties(Domain.class, DOMAIN, null, DOMAIN_NAME, FIELD_TYPE,
      MERGE_POLICY, SPLIT_POLICY, DESCRIPTION, OWNER, CODED_VALUES);

    addClassProperties(CodedValueDomain.class, DOMAIN, CODED_VALUE_DOMAIN);
    addTagNameXsiTagName(CODED_VALUES, ARRAY_OF_CODED_VALUE);

    addClassProperties(CodedValue.class, CODED_VALUE, CODED_VALUE, NAME, CODE);

    addClassProperties(Field.class, FIELD, FIELD, NAME, TYPE, IS_NULLABLE,
      LENGTH, PRECISION, SCALE, REQUIRED, EDIATBLE, DOMAIN_FIXED, GEOMETRY_DEF,
      ALIAS_NAME, MODEL_NAME, DEFAULT_VALUE, DOMAIN);

    addTagNameXsiTagName(FIELD_ARRAY, ARRAY_OF_FIELD);

    addTagNameXsiTagName(FIELDS, FIELDS);
    addTagNameChildTagName(FIELDS, FIELD_ARRAY);

    addClassProperties(Index.class, INDEX, INDEX, NAME, IS_UNIQUE,
      IS_ASCENDING, FIELDS);

    addTagNameXsiTagName(INDEX_ARRAY, ARRAY_OF_INDEX);

    addTagNameXsiTagName(INDEXES, INDEXES);
    addTagNameChildTagName(INDEXES, INDEX_ARRAY);

    addClassProperties(PropertySetProperty.class, PROPERTY_SET_PROPERTY, null,
      KEY, VALUE);

    addTagNameXsiTagName(PROPERTY_ARRAY, ARRAY_OF_PROPERTY_SET_PROPERTY);

    addTagNameXsiTagName(EXTENSION_PROPERTIES, PROPERTY_SET);
    addTagNameChildTagName(EXTENSION_PROPERTIES, PROPERTY_ARRAY);

    addTagNameListElementTagName(RELATIONSHIP_CLASS_NAMES, NAME);
    addTagNameXsiTagName(RELATIONSHIP_CLASS_NAMES, NAMES);

    addTagNameListElementTagName(SUBTYPES, SUBTYPE);

    addClassProperties(Subtype.class, SUBTYPE, null, SUBTYPE_NAME,
      SUBTYPE_CODE, FIELD_INFOS);

    addClassProperties(SubtypeFieldInfo.class, SUBTYPE_FIELD_INFO, null,
      FIELD_NAME, DOMAIN_NAME, DEFAULT_VALUE);

    addClassProperties(EnvelopeN.class, ENVELOPE, ENVELOPE_N, X_MIN, Y_MIN,
      X_MAX, Y_MAX, Z_MIN, Z_MAX, M_MIN, M_MAX, SPATIAL_REFERENCE);
    addTagNameXsiTagName(CONTROLLER_MEMBERSHIPS, ARRAY_OF_CONTROLLER_MEMBERSHIP);

    addClassProperties(DataElement.class, DATA_ELEMENT, DATA_ELEMENT,
      CATALOG_PATH, NAME, CHILDREN_EXPANDED, FULL_PROPS_RETRIEVED,
      METADATA_RETRIEVED, METADATA, CHILDREN);

    addClassProperties(DEDataset.class, DATA_ELEMENT, DE_DATASET, DATASET_TYPE,
      DSID, VERSIONED, CAN_VERSION, CONFIGURATION_KEYWORD);

    addClassProperties(DEGeoDataset.class, DATA_ELEMENT, DE_GEO_DATASET,
      EXTENT, SPATIAL_REFERENCE);

    addClassProperties(DEFeatureDataset.class, DATA_ELEMENT,
      DE_FEATURE_DATASET, EXTENT, SPATIAL_REFERENCE);

    addClassProperties(DETable.class, DATA_ELEMENT, DE_TABLE, HAS_OID,
      OBJECT_ID_FIELD_NAME, FIELDS, INDEXES, CLSID, EXTCLSID,
      RELATIONSHIP_CLASS_NAMES, ALIAS_NAME, MODEL_NAME, HAS_GLOBAL_ID,
      GLOBAL_ID_FIELD_NAME, RASTER_FIELD_NAME, EXTENSION_PROPERTIES,
      SUBTYPE_FIELD_NAME, DEFAULT_SUBTYPE_CODE, SUBTYPES,
      CONTROLLER_MEMBERSHIPS);

    addClassProperties(DEFeatureClass.class, DATA_ELEMENT, DE_FEATURE_CLASS,
      FEATURE_TYPE, SHAPE_TYPE, SHAPE_FIELD_NAME, HAS_M, HAS_Z,
      HAS_SPATIAL_INDEX, AREA_FIELD_NAME, LENGTH_FIELD_NAME, EXTENT,
      SPATIAL_REFERENCE);

    addTagNameXsiTagName(DATASET_DEFINITIONS, ARRAY_OF_DATA_ELEMENT);
    addTagNameXsiTagName(DOMAINS, ARRAY_OF_DOMAIN);

    addClassProperties(WorkspaceDefinition.class, WORKSPACE_DEFINITION,
      WORKSPACE_DEFINITION, WORKSPACE_TYPE, VERSION, DOMAINS,
      DATASET_DEFINITIONS, METADATA);

    addTagNameListElementTagName(WORKSPACE_DATA, DATASET_DATA);
    addTagNameXsiTagName(WORKSPACE_DATA, WORKSPACE_DATA);

    addClassProperties(Workspace.class, WORKSPACE, null, WORKSPACE_DEFINITION,
      WORKSPACE_DATA);

    classTypeNameMap.put(Byte.class, XmlConstants.XS_BYTE);
    classTypeNameMap.put(Short.class, XmlConstants.XS_SHORT);
    classTypeNameMap.put(Integer.class, XmlConstants.XS_INT);
    classTypeNameMap.put(Float.class, XmlConstants.XS_FLOAT);
    classTypeNameMap.put(Double.class, XmlConstants.XS_DOUBLE);
    classTypeNameMap.put(Double.class, XmlConstants.XS_STRING);

    xsiTypeTypeNames.add(CODE);
  }

  public EsriGdbXmlSerializer(final String esriNamespaceUri, final Writer out) {
    this(out);
    if (esriNamespaceUri != null) {
      this.out.setNamespaceAlias(_NAMESPACE_URI, esriNamespaceUri);
    }
  }

  public EsriGdbXmlSerializer(final Writer out) {
    this();
    this.out = new XmlWriter(out);
    this.out.setIndent(true);
    this.out.startDocument("UTF-8");
    this.out.setPrefix(XmlConstants.XML_SCHEMA);
    this.out.setPrefix(XsiConstants.TYPE);
    writeNamespaces = false;
    writeFirstNamespace = true;
  }

  private void addClassProperties(final Class<?> objectClass,
    final QName tagName, final QName xsiTagName,
    final Collection<QName> propertyNames) {
    classTagNameMap.put(objectClass, tagName);
    addClassXsiTagName(objectClass, xsiTagName);
    final Set<QName> allPropertyNames = new LinkedHashSet<QName>();
    addSuperclassPropertyNames(allPropertyNames, objectClass.getSuperclass());
    allPropertyNames.addAll(propertyNames);
    classPropertyTagNamesMap.put(objectClass, allPropertyNames);
  }

  private void addClassProperties(final Class<?> objectClass,
    final QName tagName, final QName xsiTagName, final QName... propertyNames) {
    addClassProperties(objectClass, tagName, xsiTagName,
      Arrays.asList(propertyNames));
  }

  protected void addClassPropertyMethod(final Class<?> objectClass,
    final QName propertyName, final String methodName) {
    Map<QName, Method> classMethods = classPropertyMethodMap.get(objectClass);
    if (classMethods == null) {
      classMethods = new HashMap<QName, Method>();
      classPropertyMethodMap.put(objectClass, classMethods);
    }
    final Method method = JavaBeanUtil.getMethod(EsriGdbXmlSerializer.class,
      methodName, Object.class);
    classMethods.put(propertyName, method);
  }

  private void addClassXsiTagName(final Class<?> objectClass,
    final QName tagName) {
    if (tagName != null) {
      classXsiTagNameMap.put(objectClass, tagName);
    }
  }

  private void addSuperclassPropertyNames(final Set<QName> allPropertyNames,
    final Class<?> objectClass) {
    if (!objectClass.equals(Object.class)) {
      addSuperclassPropertyNames(allPropertyNames, objectClass.getSuperclass());
      final Set<QName> propertyNames = classPropertyTagNamesMap.get(objectClass);
      if (propertyNames != null) {
        allPropertyNames.addAll(propertyNames);
      }
    }

  }

  private void addTagNameChildTagName(final QName tagName,
    final QName xsiTagName) {
    tagNameChildTagNameMap.put(tagName, xsiTagName);
  }

  private void addTagNameListElementTagName(final QName tagName,
    final QName xsiTagName) {
    tagNameListElementTagNameMap.put(tagName, xsiTagName);
  }

  private void addTagNameXsiTagName(final QName tagName, final QName xsiTagName) {
    tagNameXsiTagNameMap.put(tagName, xsiTagName);
  }

  public void close() {
    out.flush();
    out.close();
  }

  private void endTag(final QName tagName) {
    final QName childTagName = tagNameChildTagNameMap.get(tagName);
    if (childTagName != null) {
      endTag(childTagName);
    }

    out.endTag();
  }

  private Method getClassPropertyMethod(final Class<?> objectClass,
    final QName propertyName) {
    final Map<QName, Method> propertyMethodMap = classPropertyMethodMap.get(objectClass);
    if (propertyMethodMap == null) {
      return null;
    } else {
      return propertyMethodMap.get(propertyName);
    }
  }

  public void serialize(final Object object) {
    final Class<? extends Object> objectClass = object.getClass();
    QName tagName = classTagNameMap.get(objectClass);
    if (tagName == null) {

      final Package classPackage = objectClass.getPackage();
      final String packageName = classPackage.getName();
      final String className = objectClass.getSimpleName();
      tagName = new QName(packageName, className);
    }
    if (!startTag(tagName)) {
      writeXsiTypeAttribute(tagName, objectClass);
    }
    serializeObjectProperties(tagName, object);
    endTag(tagName);
  }

  @SuppressWarnings("rawtypes")
  private void serializeObjectProperties(final QName tagName,
    final Object object) {
    if (object != null) {
      final Class<? extends Object> objectClass = object.getClass();
      final Collection<QName> propertyTagNames = classPropertyTagNamesMap.get(objectClass);
      if (propertyTagNames == null) {
        if (object instanceof List) {
          final Collection list = (Collection)object;
          if (list.isEmpty()) {
            out.closeStartTag();
            out.setElementHasContent();
          } else {
            final QName listElementTagName = tagNameListElementTagNameMap.get(tagName);
            if (listElementTagName == null) {
              for (final Object value : list) {
                serialize(value);
              }
            } else {
              for (final Object value : list) {
                if (!startTag(listElementTagName)) {
                  writeXsiTypeAttribute(listElementTagName, value);
                }
                serializeObjectProperties(listElementTagName, value);
                endTag(listElementTagName);
              }
            }
          }
        } else {
          out.text(object);
        }
      } else {
        for (final QName propertyTagName : propertyTagNames) {
          String proptertyName = propertyTagName.getLocalPart();
          if (proptertyName.length() > 1
            && Character.isLowerCase(proptertyName.charAt(1))) {
            proptertyName = CaseConverter.toLowerFirstChar(proptertyName);
          }
          final Object value = JavaBeanUtil.getProperty(object, proptertyName);
          if (writeNull || value != null) {
            final Method method = getClassPropertyMethod(objectClass,
              propertyTagName);
            if (method == null) {
              if (!startTag(propertyTagName)) {
                writeXsiTypeAttribute(propertyTagName, value);
              }
              serializeObjectProperties(propertyTagName, value);
              endTag(propertyTagName);
            } else {
              JavaBeanUtil.invokeMethod(method, this, value);
            }
          }
        }
      }
    }
  }

  private boolean startTag(final QName tagName) {
    if (writeNamespaces || writeFirstNamespace) {
      out.startTag(tagName);
      writeFirstNamespace = false;
    } else {
      out.startTag(null, tagName.getLocalPart());
    }
    final QName xsiTagName = tagNameXsiTagNameMap.get(tagName);
    boolean hasXsi = false;
    if (xsiTagName != null) {
      out.xsiTypeAttribute(xsiTagName);
      hasXsi = true;
    }
    final QName childTagName = tagNameChildTagNameMap.get(tagName);
    if (childTagName != null) {
      startTag(childTagName);
    }
    return hasXsi;
  }

  private void writeXsiTypeAttribute(final QName tagName,
    final Class<? extends Object> objectClass) {
    QName xsiTagName = classXsiTagNameMap.get(objectClass);
    if (xsiTagName == null) {
      if (xsiTypeTypeNames.contains(tagName)) {
        xsiTagName = classTypeNameMap.get(objectClass);
        if (xsiTagName == null) {
          LOG.error("No xsi:type configuration for class " + objectClass);
        }
      }
    }
    if (xsiTagName != null) {
      out.xsiTypeAttribute(xsiTagName);
    }
  }

  private void writeXsiTypeAttribute(final QName tagName, final Object value) {
    if (value != null) {
      final Class<?> valueClass = value.getClass();
      writeXsiTypeAttribute(tagName, valueClass);
    }
  }
}
