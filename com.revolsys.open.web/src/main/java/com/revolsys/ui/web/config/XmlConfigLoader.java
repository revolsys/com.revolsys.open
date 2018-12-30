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

import java.net.URL;

import javax.servlet.ServletContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.record.io.format.xml.SimpleXmlProcessorContext;
import com.revolsys.record.io.format.xml.StaxReader;
import com.revolsys.record.io.format.xml.XmlProcessorContext;

public class XmlConfigLoader {
  private static final Logger log = LoggerFactory.getLogger(XmlConfigLoader.class);

  private final URL configFileUrl;

  private final XmlProcessorContext context = new SimpleXmlProcessorContext();

  public XmlConfigLoader(final URL configFileUrl, final ServletContext servletContext) {
    if (configFileUrl == null) {
      throw new IllegalArgumentException("A config file must be specified");
    }
    this.configFileUrl = configFileUrl;
    this.context.setAttribute("javax.servlet.ServletContext", servletContext);
  }

  public synchronized Config loadConfig() throws InvalidConfigException {
    Config config = null;
    try {
      final XMLInputFactory factory = XMLInputFactory.newInstance();
      factory.setXMLReporter(this.context);
      final StaxReader parser = StaxReader.newXmlReader(this.configFileUrl.openStream());
      try {
        parser.skipToStartElement();
        if (parser.getEventType() == XMLStreamConstants.START_ELEMENT) {
          config = (Config)new IafConfigXmlProcessor(this.context).process(parser);
        }
      } catch (final Throwable t) {
        log.error(t.getMessage(), t);
        this.context.addError(t.getMessage(), t, parser.getLocation());
      }
    } catch (final Throwable e) {
      this.context.addError(e.getMessage(), e, null);
    }
    if (!this.context.getErrors().isEmpty()) {
      throw new InvalidConfigException("Configuration file is invalid", this.context.getErrors());
    }
    return config;
  }
}
