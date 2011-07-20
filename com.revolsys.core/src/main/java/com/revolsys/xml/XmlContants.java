/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.xml;

import javax.xml.namespace.QName;

/**
 * The XmlConstants class defines some useful constants for XML namespaces.
 * 
 * @author Paul Austin
 * @version 1.0
 */
public final class XmlContants {
  /** The XML Namespace prefix for XML. */
  public static final String XML_NS_PREFIX = "xml";

  /** The XML Namespace URI for XML. */
  public static final String XML_NS_URI = "http://www.w3.org/XML/1998/namespace";

  /** The XML Namespace prefix for XML Namespaces. */
  public static final String XMLNS_NS_PREFIX = "xmlns";

  /** The XML Namespace URI for XML Namespaces. */
  public static final String XMLNS_NS_URI = "http://www.w3.org/2000/xmlns/";

  /** The XML Namespace prefix for XML Namespaces. */
  public static final String XML_SCHEMA_NAMESPACE_PREFIX = "xs";

  /** The XML Namespace URI for XML Namespaces. */
  public static final String XML_SCHEMA_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema";

  public static final QName XML_LANG = new QName(XML_NS_URI, "lang",
    XML_NS_PREFIX);

  public static final QName XML_SCHEMA = new QName(XML_SCHEMA_NAMESPACE_URI, "schema",
    XML_SCHEMA_NAMESPACE_PREFIX);
}
