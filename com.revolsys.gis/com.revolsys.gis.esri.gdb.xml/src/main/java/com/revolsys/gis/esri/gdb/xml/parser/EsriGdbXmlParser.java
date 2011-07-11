package com.revolsys.gis.esri.gdb.xml.parser;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.beanutils.BeanUtils;

import com.revolsys.util.CaseConverter;
import com.revolsys.xml.io.StaxUtils;
import com.revolsys.xml.io.XmlProcessor;

public class EsriGdbXmlParser extends XmlProcessor {

  public EsriGdbXmlParser() {
    super("http://www.esri.com/schemas/ArcGIS/10.1");
  }

  public DataElement processDataElement(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    DataElement dataElement = new DataElement();
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object object = process(parser);
      if (object instanceof SpatialReference) {
        SpatialReference spatialReference = (SpatialReference)object;
        dataElement.setSpatialReference(spatialReference);
      }
    }
    return dataElement;
  }

  public SpatialReference processSpatialReference(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    SpatialReference spatialReference = new SpatialReference();
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      String tagName = parser.getName().getLocalPart();
      String value = StaxUtils.getElementText(parser);
      try {
        BeanUtils.setProperty(spatialReference, tagName, value);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
    return spatialReference;
  }

}
