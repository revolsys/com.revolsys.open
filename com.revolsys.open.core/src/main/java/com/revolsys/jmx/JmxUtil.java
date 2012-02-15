package com.revolsys.jmx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Submit a job for the business application mapTileByLocation using a URL as
 * the location of the batch job requests data. Wait for job processing to be
 * completed. Get the results, extract the map names, remove duplicates, sort
 * the resulting list and display the short list of map names.
 */
public class JmxUtil {

  public static void close(final JMXConnector jmxConnector) {
    if (jmxConnector != null) {
      try {
        jmxConnector.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static JMXConnector getJmxConnector(final String connectorString) {
    return getJmxConnector(connectorString, "", "");
  }

  public static JMXConnector getJmxConnector(
    final String connectorString,
    final String userName,
    final String password) {
    final HashMap<String, String[]> environment = new HashMap<String, String[]>();
    final String[] jmxCredentials = new String[] {
      userName, password
    };
    environment.put(JMXConnector.CREDENTIALS, jmxCredentials);

    // Get JMXServiceURL of JMX Connector (must be known in advance)
    JMXServiceURL jmxServiceUrl;
    JMXConnector jmxConnector = null;

    try {
      jmxServiceUrl = new JMXServiceURL(connectorString);
      jmxConnector = JMXConnectorFactory.connect(jmxServiceUrl, environment);
    } catch (final MalformedURLException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return jmxConnector;
  }

  public static MBeanAttributeInfo getMBeanAttribute(
    final MBeanServerConnection connection,
    final String nameString,
    final String attributeName) {
    MBeanAttributeInfo attribute = null;
    ObjectName objectName;
    try {
      objectName = new ObjectName(nameString);
      MBeanInfo mBeanInfo;
      mBeanInfo = connection.getMBeanInfo(objectName);
      final MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
      for (int i = 0; i < attributes.length; i++) {
        final MBeanAttributeInfo thisAttribute = attributes[i];
        if (thisAttribute.getName().equals(attributeName)) {
          attribute = thisAttribute;
          break;
        }
      }
    } catch (final InstanceNotFoundException e) {
      e.printStackTrace();
    } catch (final IntrospectionException e) {
      e.printStackTrace();
    } catch (final ReflectionException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    } catch (final MalformedObjectNameException e) {
      e.printStackTrace();
    } catch (final NullPointerException e) {
      e.printStackTrace();
    }
    return attribute;
  }

  @SuppressWarnings("unchecked")
  public static Map<String, MBeanAttributeInfo[]> getMBeanAttributes(
    final MBeanServer mBeanServer,
    final String objectNameString) {
    Map<String, MBeanAttributeInfo[]> attributesMap = null;
    Set<ObjectName> objectNames;
    try {
      objectNames = mBeanServer.queryNames(new ObjectName(objectNameString),
        null);
      attributesMap = new TreeMap<String, MBeanAttributeInfo[]>();
      for (final Iterator<ObjectName> iterator = objectNames.iterator(); iterator.hasNext();) {
        final ObjectName objectName = iterator.next();
        MBeanInfo mBeanInfo;
        mBeanInfo = mBeanServer.getMBeanInfo(objectName);
        final MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
        attributesMap.put(objectName.getCanonicalName(), attributes);
      }
    } catch (final InstanceNotFoundException e) {
      e.printStackTrace();
    } catch (final IntrospectionException e) {
      e.printStackTrace();
    } catch (final ReflectionException e) {
      e.printStackTrace();
    } catch (final MalformedObjectNameException e) {
      e.printStackTrace();
    } catch (final NullPointerException e) {
      e.printStackTrace();
    }
    return attributesMap;
  }

  @SuppressWarnings("unchecked")
  public static Map<String, MBeanAttributeInfo[]> getMBeanAttributes(
    final MBeanServerConnection connection,
    final String objectNameString) {
    Map<String, MBeanAttributeInfo[]> attributesMap = null;
    Set<ObjectName> objectNames;
    try {
      objectNames = connection.queryNames(new ObjectName(objectNameString),
        null);
      attributesMap = new TreeMap<String, MBeanAttributeInfo[]>();
      for (final Iterator<ObjectName> iterator = objectNames.iterator(); iterator.hasNext();) {
        final ObjectName objectName = iterator.next();
        MBeanInfo mBeanInfo;
        mBeanInfo = connection.getMBeanInfo(objectName);
        final MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
        attributesMap.put(objectName.getCanonicalName(), attributes);
      }
    } catch (final InstanceNotFoundException e) {
      e.printStackTrace();
    } catch (final IntrospectionException e) {
      e.printStackTrace();
    } catch (final ReflectionException e) {
      e.printStackTrace();
    } catch (final MalformedObjectNameException e) {
      e.printStackTrace();
    } catch (final NullPointerException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return attributesMap;
  }

  public static Object getMBeanAttributeValue(
    final MBeanServerConnection connection,
    final ObjectName objName,
    final String attributeName) {
    Object attributeValue = "Unavailable";
    try {
      attributeValue = connection.getAttribute(objName, attributeName);
    } catch (final AttributeNotFoundException e) {
      // e.printStackTrace();
    } catch (final MBeanException e) {
      // e.printStackTrace();
    } catch (final InstanceNotFoundException e) {
      // e.printStackTrace();
    } catch (final ReflectionException e) {
      // e.printStackTrace();
    } catch (final IOException e) {
      // e.printStackTrace();
    } catch (final NullPointerException e) {
      // e.printStackTrace();
    } catch (final Exception e) {
      // e.printStackTrace();
    }

    return attributeValue;
  }

  public static Object getMBeanAttributeValue(
    final MBeanServerConnection connection,
    final String nameString,
    final String attributeName) {
    Object attributeValue = null;
    ObjectName objName;
    try {
      objName = new ObjectName(nameString);
      attributeValue = getMBeanAttributeValue(connection, objName,
        attributeName);
    } catch (final MalformedObjectNameException e) {
      e.printStackTrace();
    } catch (final NullPointerException e) {
      e.printStackTrace();
    }

    return attributeValue;
  }

  public static MBeanServerConnection getMbeanServerConnection(
    final JMXConnector jmxConnector) {

    MBeanServerConnection mBeanServerConnection = null;
    if (jmxConnector != null) {
      try {
        mBeanServerConnection = jmxConnector.getMBeanServerConnection();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
    return mBeanServerConnection;
  }

  public static void printAttributesMapValues(
    final MBeanServerConnection connection,
    final Map<String, MBeanAttributeInfo[]> attributesMap) {
    for (final String objectNameStr : attributesMap.keySet()) {
      System.out.println("\nobjectName=" + objectNameStr);
      final MBeanAttributeInfo[] attributes = attributesMap.get(objectNameStr);
      ObjectName objectName;
      try {
        objectName = new ObjectName(objectNameStr);
        for (int i = 0; i < attributes.length; i++) {
          final MBeanAttributeInfo attribute = attributes[i];
          final String attributeName = attribute.getName();
          System.out.print(" name=" + attributeName);
          final String attributeType = attribute.getType();
          System.out.print(" type=" + attributeType);
          if (attribute.isReadable()) {
            Object attributeValue = null;
            attributeValue = JmxUtil.getMBeanAttributeValue(connection,
              objectName, attributeName);
            if (attributeValue != null) {
              System.out.print(" value=" + attributeValue);
            }
          }
          System.out.println("");
        }
      } catch (final MalformedObjectNameException e) {
        e.printStackTrace();
      } catch (final NullPointerException e) {
        e.printStackTrace();
      }
    }
  }

  public static void printAttributeValue(
    final MBeanServerConnection connection,
    final String objectNameString,
    final MBeanAttributeInfo attribute) {
    final String attributeName = attribute.getName();
    System.out.println("objectName="
      + objectNameString
      + " "
      + attributeName
      + "="
      + JmxUtil.getMBeanAttributeValue(connection, objectNameString,
        attributeName));

  }
}
