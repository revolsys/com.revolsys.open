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

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

import com.revolsys.io.xml.SimpleXmlProcessorContext;
import com.revolsys.io.xml.StaxUtils;
import com.revolsys.io.xml.XmlProcessorContext;

public class XmlConfigLoader {
  private static final Logger log = Logger.getLogger(XmlConfigLoader.class);

  private final URL configFileUrl;

  private final XmlProcessorContext context = new SimpleXmlProcessorContext();

  public XmlConfigLoader(final URL configFileUrl,
    final ServletContext servletContext) {
    if (configFileUrl == null) {
      throw new IllegalArgumentException("A config file must be specified");
    }
    this.configFileUrl = configFileUrl;
    context.setAttribute("javax.servlet.ServletContext", servletContext);
  }

  public synchronized Config loadConfig() throws InvalidConfigException {
    Config config = null;
    try {
      final XMLInputFactory factory = XMLInputFactory.newInstance();
      factory.setXMLReporter(context);
      final XMLStreamReader parser = factory.createXMLStreamReader(configFileUrl.openStream());
      try {
        StaxUtils.skipToStartElement(parser);
        if (parser.getEventType() == XMLStreamConstants.START_ELEMENT) {
          config = (Config)new IafConfigXmlProcessor(context).process(parser);
        }
      } catch (final XMLStreamException e) {
        context.addError(e.getMessage(), e, parser.getLocation());
      } catch (final Throwable t) {
        log.error(t.getMessage(), t);
        context.addError(t.getMessage(), t, parser.getLocation());
      }
    } catch (final IOException e) {
      context.addError(e.getMessage(), e, null);
    } catch (final XMLStreamException e) {
      context.addError(e.getMessage(), e, null);
    }
    if (!context.getErrors().isEmpty()) {
      throw new InvalidConfigException("Configuration file is invalid",
        context.getErrors());
    }
    return config;
  }
}
