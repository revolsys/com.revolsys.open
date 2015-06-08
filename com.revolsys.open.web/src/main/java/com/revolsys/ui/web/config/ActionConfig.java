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

import java.util.HashMap;

import com.revolsys.ui.web.exception.ActionInitException;

public class ActionConfig {
  /** The IafAction subclass instance. */
  private IafAction action;

  /** The application configuration. */
  private final Config config;

  /** The initialization parameters for the action. */
  private final HashMap parameters = new HashMap();

  /** The name of the IafAction subclass. */
  private String type;

  /**
   * Construct a new ActionConfig.
   *
   * @param config The application configuration.
   * @param type The name of the IafAction subclass.
   */
  public ActionConfig(final Config config, final String type) {
    this.config = config;
    this.type = type;
  }

  /**
   * Add a new parameter to the action.
   *
   * @param parameter The parameter.
   */
  public void addParameter(final Parameter parameter) {
    this.parameters.put(parameter.getName(), parameter.getValue());
  }

  /**
   * Add a new parameter to the action.
   *
   * @param name The parameter name.
   * @param value The parameter value.
   */
  public void addParameter(final String name, final String value) {
    this.parameters.put(name, value);
  }

  /**
   * Compare this action config with another. Two action configs are equal if
   * the type and parameters are equal.
   *
   * @param o The object to compare to.
   * @return True if the two objects are equal.
   */
  @Override
  public boolean equals(final Object o) {
    if (o instanceof ActionConfig) {
      final ActionConfig a = (ActionConfig)o;
      if (a.type.equals(this.type) && a.parameters.equals(this.parameters)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the action for this config. If this is the first call a new instance of
   * the class defined by "type" will be created and initialized.
   *
   * @return The action instance.
   * @throws ActionInitException If the action could not be initialized.
   */
  public IafAction getAction() throws ActionInitException {
    if (this.action == null) {
      try {
        this.action = (IafAction)Class.forName(this.type).newInstance();
        this.action.init(this);
      } catch (final ClassNotFoundException cnfe) {
        throw new ActionInitException("Unable to find class: " + this.type, cnfe);
      } catch (final InstantiationException ie) {
        throw new ActionInitException("Unable to instantiate class: " + this.type, ie);
      } catch (final IllegalAccessException ie) {
        throw new ActionInitException("Unable to access class: " + this.type, ie);
      }
    }
    return this.action;
  }

  /**
   * Get the parameter value as an boolean. See
   * {@link Boolean#valueOf(java.lang.String)} for more details.
   *
   * @param name The parameter name.
   * @return The parameter value.
   */
  public boolean getBooleanParameter(final String name) {
    final String value = getStringParameter(name);
    return Boolean.valueOf(value).booleanValue();
  }

  /**
   * Get the application configuration.
   *
   * @return The application configuration.
   */
  public Config getConfig() {
    return this.config;
  }

  /**
   * Get the parameter value as an int. If it is not a valid number
   * Integer.MIN_VALUE will be returned.
   *
   * @param name The parameter name.
   * @return The parameter value.
   */
  public int getIntParameter(final String name) {
    try {
      return Integer.parseInt(getStringParameter(name));
    } catch (final NumberFormatException nfe) {
      return Integer.MIN_VALUE;
    }
  }

  /**
   * Get the parameter value as an long. If it is not a valid number
   * Long.MIN_VALUE will be returned.
   *
   * @param name The parameter name.
   * @return The parameter value.
   */
  public long getLongParameter(final String name) {
    try {
      return Long.parseLong(getStringParameter(name));
    } catch (final NumberFormatException nfe) {
      return Long.MIN_VALUE;
    }
  }

  /**
   * Get the parameter value.
   *
   * @param name The parameter name.
   * @return The parameter value.
   */
  public Object getParameter(final String name) {
    return this.parameters.get(name);
  }

  /**
   * Get the parameter value as a String.
   *
   * @param name The parameter name.
   * @return The parameter value.
   */
  public String getStringParameter(final String name) {
    final Object value = this.parameters.get(name);
    if (value != null) {
      return value.toString();
    } else {
      return null;
    }
  }

  /**
   * @return Returns the type.
   */
  public String getType() {
    return this.type;
  }

  /**
   * Get the hash code for the object.
   *
   * @return The hash code.
   */
  @Override
  public int hashCode() {
    return this.type.hashCode() + (this.parameters.hashCode() << 2);
  }

  /**
   * @param type The type to set.
   */
  public void setType(final String type) {
    this.type = type;
  }

  /**
   * Get the string representation of the object.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return new StringBuilder(this.type).append(" ").append(this.parameters).toString();
  }
}
