package com.revolsys.record.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.record.io.format.directory.DirectoryRecordStore;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.spring.resource.Resource;

public abstract class AbstractRecordIoFactory extends AbstractIoFactoryWithCoordinateSystem
  implements RecordReaderFactory, RecordStoreFactory {

  private final List<String> urlPatterns = new ArrayList<>();

  public AbstractRecordIoFactory(final String name) {
    super(name);
  }

  @Override
  protected void addMediaTypeAndFileExtension(final String mediaType, final String fileExtension) {
    super.addMediaTypeAndFileExtension(mediaType, fileExtension);
    this.urlPatterns.add("(.+)[\\?|&]format=" + fileExtension + "(&.+)?");
  }

  @Override
  public List<String> getRecordStoreFileExtensions() {
    return Collections.emptyList();
  }

  @Override
  public Class<? extends RecordStore> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return RecordStore.class;
  }

  @Override
  public List<String> getUrlPatterns() {
    return this.urlPatterns;
  }

  @Override
  public void init() {
    super.init();
    RecordStoreFactoryRegistry.register(this);
  }

  @Override
  public RecordStore newRecordStore(final Map<String, ? extends Object> connectionProperties) {
    final String url = (String)connectionProperties.get("url");
    final Resource resource = Resource.getResource(url);
    final File directory = resource.getFile();
    final List<String> fileExtensions = getFileExtensions();
    return new DirectoryRecordStore(directory, fileExtensions);
  }
}
