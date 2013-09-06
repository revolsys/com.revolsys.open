package com.revolsys.gis.esri.gdb.file;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactory;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.gis.esri.gdb.file.capi.CapiFileGdbDataObjectStore;
import com.revolsys.io.FileUtil;

public class FileGdbDataObjectStoreFactory implements DataObjectStoreFactory {

  private static final List<String> URL_PATTERNS = Arrays.asList(
    "file:/(//)?.*.gdb/?", "folderconnection:/(//)?.*.gdb/?");

  public static FileGdbDataObjectStore create(final File file) {
    FileGdbDataObjectStore dataObjectStore;
    dataObjectStore = new CapiFileGdbDataObjectStore(file);
    return dataObjectStore;
  }

  @Override
  public FileGdbDataObjectStore createDataObjectStore(
    final Map<String, ? extends Object> connectionProperties) {
    final Map<String, Object> properties = new LinkedHashMap<String, Object>(
      connectionProperties);
    final String url = (String)properties.remove("url");
    final File file = FileUtil.getUrlFile(url);

    final FileGdbDataObjectStore dataObjectStore = create(file);
    DataObjectStoreFactoryRegistry.setConnectionProperties(dataObjectStore,
      properties);
    return dataObjectStore;
  }

  @Override
  public Class<? extends DataObjectStore> getDataObjectStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return DataObjectStore.class;
  }

  @Override
  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }

}
