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
package com.revolsys.ui.web.config;

import javax.xml.namespace.QName;

public class IafConfigXmlConstants {
  private static final String NAMESPACE = "urn:x-revolsys-com:iaf:core:config";

  private static final String NS_PREFIX = "iafc";

  private static final QName ACTION_TAG = new QName(NAMESPACE, "IafAction", NS_PREFIX);

  private static final QName AREA_TAG = new QName(NAMESPACE, "Area", NS_PREFIX);

  private static final QName ARGUMENT_TAG = new QName(NAMESPACE, "Argument", NS_PREFIX);

  private static final QName COMPONENT_INCLUDE_TAG = new QName(NAMESPACE, "ComponentInclude",
    NS_PREFIX);

  private static final QName COMPONENT_TAG = new QName(NAMESPACE, "Component", NS_PREFIX);

  private static final QName DYNAMIC_MENU_TAG = new QName(NAMESPACE, "DynamicMenu", NS_PREFIX);

  private static final QName ELEMENT_COMPONENT_TAG = new QName(NAMESPACE, "ElementComponent",
    NS_PREFIX);

  private static final QName FIELD_TAG = new QName(NAMESPACE, "Field", NS_PREFIX);

  private static final QName IAF_CONFIG_TAG = new QName(NAMESPACE, "rsWebUiConfig", NS_PREFIX);

  private static final QName JAVA_COMPONENT_TAG = new QName(NAMESPACE, "JavaComponent", NS_PREFIX);

  private static final QName LAYOUT_INCLUDE_TAG = new QName(NAMESPACE, "LayoutInclude", NS_PREFIX);

  private static final QName LAYOUT_TAG = new QName(NAMESPACE, "Layout", NS_PREFIX);

  private static final QName MENU_INCLUDE_TAG = new QName(NAMESPACE, "MenuInclude", NS_PREFIX);

  private static final QName MENU_ITEM_TAG = new QName(NAMESPACE, "MenuItem", NS_PREFIX);

  private static final QName MENU_TAG = new QName(NAMESPACE, "Menu", NS_PREFIX);

  private static final QName ON_LOAD_TAG = new QName(NAMESPACE, "OnLoad", NS_PREFIX);

  private static final QName PAGE_TAG = new QName(NAMESPACE, "Page", NS_PREFIX);

  private static final QName PARAMETER_TAG = new QName(NAMESPACE, "Parameter", NS_PREFIX);

  private static final QName PROPERTY_TAG = new QName(NAMESPACE, "WebProperty", NS_PREFIX);

  private static final QName SCRIPT_TAG = new QName(NAMESPACE, "Script", NS_PREFIX);

  private static final QName STYLE_TAG = new QName(NAMESPACE, "Style", NS_PREFIX);

}
