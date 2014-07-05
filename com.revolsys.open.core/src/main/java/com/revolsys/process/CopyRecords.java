package com.revolsys.process;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.data.io.RecordStore;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.parallel.process.AbstractProcess;
import com.revolsys.util.CompareUtil;

public class CopyRecords extends AbstractProcess {

  private RecordStore targetDataStore;

  private RecordStore sourceDataStore;

  private String typePath;

  private boolean hasSequence;

  private Map<String, Boolean> orderBy = new HashMap<String, Boolean>();

  public CopyRecords() {
  }

  public CopyRecords(final RecordStore sourceDataStore,
    final String typePath, final RecordStore targetDataStore,
    final boolean hasSequence) {
    this(sourceDataStore, typePath, new HashMap<String, Boolean>(),
      targetDataStore, hasSequence);
  }

  public CopyRecords(final RecordStore sourceDataStore,
    final String typePath, final Map<String, Boolean> orderBy,
    final RecordStore targetDataStore, final boolean hasSequence) {
    this.sourceDataStore = sourceDataStore;
    this.typePath = typePath;
    this.orderBy = orderBy;
    this.targetDataStore = targetDataStore;
    this.hasSequence = hasSequence;
  }

  public Map<String, Boolean> getOrderBy() {
    return orderBy;
  }

  public RecordStore getSourceDataStore() {
    return sourceDataStore;
  }

  public RecordStore getTargetDataStore() {
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
      final Reader<Record> reader = sourceDataStore.query(query);
      try {
        final Writer<Record> targetWriter = targetDataStore.createWriter();
        try {
          final RecordDefinition targetMetaData = targetDataStore.getRecordDefinition(typePath);
          if (targetMetaData == null) {
            LoggerFactory.getLogger(getClass()).error(
              "Cannot find target table: " + typePath);
          } else {
            if (hasSequence) {
              final String idAttributeName = targetMetaData.getIdAttributeName();
              Object maxId = targetDataStore.createPrimaryIdValue(typePath);
              for (final Record sourceRecord : reader) {
                final Object sourceId = sourceRecord.getValue(idAttributeName);
                while (CompareUtil.compare(maxId, sourceId) < 0) {
                  maxId = targetDataStore.createPrimaryIdValue(typePath);
                }
                targetWriter.write(sourceRecord);
              }
            } else {
              for (final Record sourceRecord : reader) {
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

  public void setSourceDataStore(final RecordStore sourceDataStore) {
    this.sourceDataStore = sourceDataStore;
  }

  public void setTargetDataStore(final RecordStore targetDataStore) {
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
