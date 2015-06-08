package com.revolsys.data.record.schema;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.revolsys.collection.EmptyReference;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.Path;
import com.revolsys.util.Property;

public abstract class AbstractRecordStoreSchemaElement extends AbstractObjectWithProperties
  implements RecordStoreSchemaElement, Comparable<RecordStoreSchemaElement> {

  private final String path;

  private final String name;

  private Reference<RecordStoreSchema> schema;

  public AbstractRecordStoreSchemaElement() {
    this.path = "";
    this.name = "";
  }

  public AbstractRecordStoreSchemaElement(final RecordStoreSchema schema, final String path) {
    if (!Property.hasValue(path)) {
      throw new IllegalArgumentException("Path is required");
    }

    String name = Path.getName(path);
    if (!Property.hasValue(name)) {
      name = "/";
    }
    this.name = name;
    if (schema == null) {
      this.schema = new EmptyReference<>();
      this.path = path;
    } else {
      this.schema = new WeakReference<>(schema);
      this.path = Path.toPath(schema.getPath(), name);
    }
  }

  public AbstractRecordStoreSchemaElement(final String path) {
    this(null, path);
  }

  @Override
  public void close() {
    super.close();
    this.schema = new EmptyReference<>();
  }

  @Override
  public int compareTo(final RecordStoreSchemaElement other) {
    final String otherPath = other.getPath();
    if (otherPath == this.path) {
      return 0;
    } else if (this.path == null) {
      return 1;
    } else if (otherPath == null) {
      return -1;
    } else {
      return this.path.compareTo(otherPath);
    }
  }

  @Override
  public boolean equalPath(final String path) {
    return path.equalsIgnoreCase(getPath());
  }

  @Override
  public boolean equals(final Object other) {
    return other == this;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getPath() {
    return this.path;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends RecordStore> V getRecordStore() {
    final RecordStoreSchema schema = getSchema();
    if (schema == null) {
      return null;
    } else {
      return (V)schema.getRecordStore();
    }
  }

  @Override
  public RecordStoreSchema getSchema() {
    return this.schema.get();
  }

  @Override
  public int hashCode() {
    if (this.path == null) {
      return super.hashCode();
    } else {
      return this.path.hashCode();
    }
  }

  @Override
  public String toString() {
    return getPath();
  }
}
