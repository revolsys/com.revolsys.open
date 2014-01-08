package com.revolsys.process;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.parallel.process.AbstractProcess;
import com.revolsys.util.CompareUtil;

public class CopyRecords extends AbstractProcess {

  private DataObjectStore targetDataStore;

  private DataObjectStore sourceDataStore;

  private String typePath;

  private boolean hasSequence;

  private Map<String, Boolean> orderBy = new HashMap<String, Boolean>();

  public CopyRecords() {
  }

  public CopyRecords(final DataObjectStore sourceDataStore,
    final String typePath, final DataObjectStore targetDataStore,
    final boolean hasSequence) {
    this(sourceDataStore, typePath, new HashMap<String, Boolean>(),
      targetDataStore, hasSequence);
  }

  public CopyRecords(final DataObjectStore sourceDataStore,
    final String typePath, final Map<String, Boolean> orderBy,
    final DataObjectStore targetDataStore, final boolean hasSequence) {
    this.sourceDataStore = sourceDataStore;
    this.typePath = typePath;
    this.orderBy = orderBy;
    this.targetDataStore = targetDataStore;
    this.hasSequence = hasSequence;
  }

  public Map<String, Boolean> getOrderBy() {
    return orderBy;
  }

  public DataObjectStore getSourceDataStore() {
    return sourceDataStore;
  }

  public DataObjectStore getTargetDataStore() {
    return targetDataStore;
  }

  public String getTypePath() {
    return typePath;
  }

  public boolean isHasSequence() {
    return hasSequence;
  }

  @Override
  public void run() {
    try {
      final Query query = new Query(typePath);
      query.setOrderBy(orderBy);
      final Reader<DataObject> reader = sourceDataStore.query(query);
      try {
        final Writer<DataObject> targetWriter = targetDataStore.createWriter();
        try {
          final DataObjectMetaData targetMetaData = targetDataStore.getMetaData(typePath);
          if (targetMetaData == null) {
            LoggerFactory.getLogger(getClass()).error(
              "Cannot find target table: " + typePath);
          } else {
            if (hasSequence) {
              final String idAttributeName = targetMetaData.getIdAttributeName();
              Object maxId = targetDataStore.createPrimaryIdValue(typePath);
              for (final DataObject sourceRecord : reader) {
                final Object sourceId = sourceRecord.getValue(idAttributeName);
                while (CompareUtil.compare(maxId, sourceId) < 0) {
                  maxId = targetDataStore.createPrimaryIdValue(typePath);
                }
                targetWriter.write(sourceRecord);
              }
            } else {
              for (final DataObject sourceRecord : reader) {
                targetWriter.write(sourceRecord);
              }
            }
          }
        } finally {
          targetWriter.close();
        }
      } finally {
        reader.close();
      }
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to copy records for " + typePath, e);
    }
  }

  public void setHasSequence(final boolean hasSequence) {
    this.hasSequence = hasSequence;
  }

  public void setSourceDataStore(final DataObjectStore sourceDataStore) {
    this.sourceDataStore = sourceDataStore;
  }

  public void setTargetDataStore(final DataObjectStore targetDataStore) {
    this.targetDataStore = targetDataStore;
  }

  public void setTypePath(final String typePath) {
    this.typePath = typePath;
  }

  @Override
  public String toString() {
    return "Copy " + typePath;
  }

}
