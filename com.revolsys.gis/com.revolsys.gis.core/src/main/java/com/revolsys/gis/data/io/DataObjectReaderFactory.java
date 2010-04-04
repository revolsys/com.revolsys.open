package com.revolsys.gis.data.io;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.io.IoFactory;

public interface DataObjectReaderFactory extends IoFactory {
  com.revolsys.gis.data.io.Reader<DataObject> createDataObjectReader(
    File file);

  com.revolsys.gis.data.io.Reader<DataObject> createDataObjectReader(
    File file,
    DataObjectFactory factory);

  com.revolsys.gis.data.io.Reader<DataObject> createDataObjectReader(
    InputStream in);

  com.revolsys.gis.data.io.Reader<DataObject> createDataObjectReader(
    InputStream in,
    Charset charset);

  com.revolsys.gis.data.io.Reader<DataObject> createDataObjectReader(
    InputStream in,
    Charset charset,
    DataObjectFactory factory);

  com.revolsys.gis.data.io.Reader<DataObject> createDataObjectReader(
    InputStream in,
    DataObjectFactory factory);

  com.revolsys.gis.data.io.Reader<DataObject> createDataObjectReader(
    Reader in);

  com.revolsys.gis.data.io.Reader<DataObject> createDataObjectReader(
    Reader in,
    DataObjectFactory factory);

  com.revolsys.gis.data.io.Reader<DataObject> createDataObjectReader(
    URL url);

  com.revolsys.gis.data.io.Reader<DataObject> createDataObjectReader(
    URL url,
    DataObjectFactory factory);

}
