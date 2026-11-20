package com.revolsys.gis.esri.gdb.file;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.jeometry.common.logging.Logs;

import com.revolsys.esri.filegdb.jni.EnumRows;
import com.revolsys.esri.filegdb.jni.EsriFileGdb;
import com.revolsys.esri.filegdb.jni.Geodatabase;
import com.revolsys.esri.filegdb.jni.Table;
import com.revolsys.esri.filegdb.jni.VectorOfWString;
import com.revolsys.io.BaseCloseable;
import com.revolsys.record.io.format.esri.gdb.xml.model.Domain;
import com.revolsys.record.io.format.esri.gdb.xml.model.EsriGdbXmlSerializer;

public class GeodatabaseReference {

  // The ESRI API reports the Windows ERROR_SHARING_VIOLATION (a file is open
  // in another process) as "Unknown error (-2147024864)"
  private static final String SHARING_VIOLATION_CODE = "-2147024864";

  private static final int SHARING_VIOLATION_RETRY_COUNT = 2;

  private static final long SHARING_VIOLATION_RETRY_WAIT_MILLIS = 1000;

  private int referenceCount = 0;

  private Geodatabase geodatabase;

  private String fileName;

  public synchronized void alterDomain(final Domain domain) {
    final String domainDefinition = EsriGdbXmlSerializer.toString(domain);
    final Geodatabase geodatabase = getValue();
    if (geodatabase != null) {
      try {
        geodatabase.alterDomain(domainDefinition);
      } finally {
        disconnect();
      }
    }
  }

  public synchronized void close() {
    if (!isClosed()) {
      this.referenceCount = Integer.MIN_VALUE;
      closeGeodatabase();
    }
  }

  private void closeGeodatabase() {
    final Geodatabase geodatabase = this.geodatabase;
    if (geodatabase != null) {
      this.geodatabase = null;
      final int closeResult = EsriFileGdb.CloseGeodatabase(geodatabase);
      if (closeResult != 0) {
        Logs.error(this, "Error closing: " + this.fileName + " ESRI Error=" + closeResult);
      }
    }
  }

  public synchronized void closeTable(final Table table, final String path) {
    final Geodatabase geodatabase = getValue();
    if (geodatabase != null) {
      try {
        geodatabase.closeTable(table);
        table.delete();
      } catch (final Exception e) {
        Logs.error(this, "Unable to close table: " + path, e);
      } finally {
        disconnect();
      }
    }
  }

  public synchronized void compactGeodatabase() {
    final Geodatabase geodatabase = getValue();
    if (geodatabase != null) {
      try {
        geodatabase.CompactDatabase();
      } finally {
        disconnect();
      }
    }
  }

  /**
   * Keep the geodatabase open until the returned connection is closed. Each
   * connection releases its reference at most once, even if closed repeatedly.
   *
   * @return The connection.
   */
  public synchronized BaseCloseable connect() {
    if (isClosed()) {
      throw new IllegalStateException("Resource closed");
    } else if (getValue() == null) {
      // No reference was taken, so there is nothing to release
      return () -> {
      };
    } else {
      final AtomicBoolean open = new AtomicBoolean(true);
      return () -> {
        if (open.compareAndSet(true, false)) {
          disconnect();
        }
      };
    }
  }

  public synchronized void createDomain(final String domainDef) {
    final Geodatabase geodatabase = getValue();
    if (geodatabase != null) {
      try {
        geodatabase.createDomain(domainDef);
      } finally {
        disconnect();
      }
    }
  }

  public synchronized void createFeatureDataset(final String featureDatasetDef) {
    final Geodatabase geodatabase = getValue();
    if (geodatabase != null) {
      try {
        retrySharingViolation("create feature dataset", () -> {
          geodatabase.createFeatureDataset(featureDatasetDef);
          return null;
        });
      } finally {
        disconnect();
      }
    }
  }

  public synchronized void createTable(final String catalogPath, final String tableDefinition,
    final String parentPath) {
    final Geodatabase geodatabase = getValue();
    if (geodatabase != null) {
      try {
        // The wait between retries is outside the lock on Geodatabase.class so
        // it doesn't stop other geodatabases creating tables
        final Table table = retrySharingViolation("create table " + catalogPath, () -> {
          synchronized (Geodatabase.class) {
            return geodatabase.createTable(tableDefinition, parentPath);
          }
        });
        geodatabase.closeTable(table);
        table.delete();
      } finally {
        disconnect();
      }
    }
  }

  public synchronized int deleteGeodatabase() {
    close();
    return EsriFileGdb.DeleteGeodatabase(this.fileName);
  }

  protected synchronized void disconnect() {
    if (!isClosed()) {
      synchronized (this) {
        final boolean wasOpen = this.referenceCount > 0;
        this.referenceCount--;
        if (this.referenceCount <= 0) {
          this.referenceCount = 0;
          if (wasOpen) {
            // System.out.println("CL\tg\t" + this);
            closeGeodatabase();
          }
        }
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  public synchronized VectorOfWString getChildDatasets(final String catalogPath,
    final String datasetType) {
    final Geodatabase geodatabase = getValue();
    if (geodatabase != null) {
      try {
        final boolean pathExists = isPathExists(catalogPath);
        if (pathExists) {
          return geodatabase.getChildDatasets(catalogPath, datasetType);
        }
      } finally {
        disconnect();
      }
    }
    return null;
  }

  public synchronized String getDomainDefinition(final String domainName) {
    final Geodatabase geodatabase = getValue();
    if (geodatabase != null) {
      try {
        return geodatabase.getDomainDefinition(domainName);
      } finally {
        disconnect();
      }
    }
    return null;
  }

  public synchronized String getTableDefinition(final String path) {
    final Geodatabase geodatabase = getValue();
    if (geodatabase != null) {
      try {
        return geodatabase.getTableDefinition(path);
      } finally {
        disconnect();
      }
    }
    return null;
  }

  public synchronized Geodatabase getValue() {
    if (isClosed()) {
      throw new IllegalStateException("Value closed");
    } else {
      if (this.geodatabase == null && this.fileName != null) {
        try {
          // System.out.println("OP\tg\t" + this);
          this.geodatabase = retrySharingViolation("open geodatabase",
            () -> EsriFileGdb.openGeodatabase(this.fileName));
        } catch (final FileGdbException e) {
          final String message = e.getMessage();
          if (!"The system cannot find the path specified. (-2147024893)".equals(message)) {
            throw e;
          }
        }
      }
      // Only count a reference once the geodatabase is open, so a failed open
      // doesn't leave a reference that is never released
      if (this.geodatabase != null) {
        this.referenceCount++;
      }
      return this.geodatabase;
    }
  }

  public synchronized boolean hasChildDataset(final String parentCatalogPath,
    final String datasetType, final String childCatalogPath) {
    final Geodatabase geodatabase = getValue();
    if (geodatabase != null) {
      try {
        final VectorOfWString childDatasets = geodatabase.getChildDatasets(parentCatalogPath,
          datasetType);
        for (int i = 0; i < childDatasets.size(); i++) {
          final String catalogPath = childDatasets.get(i);
          if (catalogPath.equals(childCatalogPath)) {
            return true;
          }
        }
        return false;
      } catch (final RuntimeException e) {
        if ("-2147211775\tThe item was not found.".equals(e.getMessage())) {
          return false;
        } else {
          throw e;
        }
      } finally {
        disconnect();
      }
    }
    return false;
  }

  public boolean isClosed() {
    return this.referenceCount == Integer.MIN_VALUE;
  }

  public synchronized boolean isPathExists(String path) {
    if (path == null) {
      return false;
    } else if ("\\".equals(path)) {
      return true;
    } else {
      final boolean pathExists = true;

      path = path.replaceAll("[\\/]+", "\\");
      path = path.replaceAll("\\$", "");
      int index = 0;
      while (index != -1) {
        final String parentPath = path.substring(0, index + 1);
        final int nextIndex = path.indexOf(index + 1, '\\');
        String currentPath;
        if (nextIndex == -1) {
          currentPath = path;
        } else {
          currentPath = path.substring(0, nextIndex);
        }
        boolean found = false;
        final Geodatabase geodatabase = getValue();
        if (geodatabase != null) {
          try {
            final VectorOfWString children = geodatabase.getChildDatasets(parentPath,
              "Feature Dataset");
            for (int i = 0; i < children.size(); i++) {
              final String childPath = children.get(i);
              if (childPath.equals(currentPath)) {
                found = true;
              }
            }
          } finally {
            disconnect();
          }
        }
        if (!found) {
          return false;
        }
        index = nextIndex;
      }
      return pathExists;
    }
  }

  private boolean isSharingViolation(final FileGdbException e) {
    final String message = e.getMessage();
    return message != null && message.contains(SHARING_VIOLATION_CODE);
  }

  public synchronized Table openTable(final String path) {
    final Geodatabase geodatabase = getValue();
    if (geodatabase != null) {
      try {
        return retrySharingViolation("open table " + path, () -> geodatabase.openTable(path));
      } finally {
        disconnect();
      }
    }
    return null;
  }

  public synchronized EnumRows query(final String sql, final boolean recycling) {
    final Geodatabase geodatabase = getValue();
    if (geodatabase != null) {
      try {
        return geodatabase.query(sql, recycling);
      } finally {
        disconnect();
      }
    }
    return null;
  }

  /**
   * Run the operation, trying again if it fails because one of the
   * geodatabase's files is open in another process. Each failure is logged
   * with details of what has the files open.
   *
   * @param action The description of the operation for the log message.
   * @param operation The operation.
   * @return The result of the operation.
   */
  private <V> V retrySharingViolation(final String action, final Supplier<V> operation) {
    final int attemptCount = SHARING_VIOLATION_RETRY_COUNT + 1;
    for (int attempt = 1;; attempt++) {
      try {
        final V result = operation.get();
        if (attempt > 1) {
          Logs.error(this, "Sharing violation resolved: " + action + " worked on attempt " + attempt
            + " of " + attemptCount + "\n  Geodatabase: " + this.fileName);
        }
        return result;
      } catch (final FileGdbException e) {
        if (!isSharingViolation(e)) {
          if (attempt > 1) {
            // The attempt that had the sharing violation can leave the item
            // partly created, so trying again fails as it already exists
            Logs.error(this,
              "Sharing violation not resolved: " + action + " failed with a different error on attempt "
                + attempt + " of " + attemptCount + ", the earlier attempt may have partly worked: "
                + e.getMessage() + "\n  Geodatabase: " + this.fileName);
          }
          throw e;
        }
        final boolean retry = attempt < attemptCount;
        final StringBuilder message = new StringBuilder(
          "Sharing violation (a file is open in another process): unable to ");
        message.append(action);
        message.append(", attempt ").append(attempt).append(" of ").append(attemptCount);
        if (retry) {
          message.append(", retrying in ")
            .append(SHARING_VIOLATION_RETRY_WAIT_MILLIS)
            .append(" ms");
        } else {
          message.append(", not retrying");
        }
        message.append(FileGdbLockDiagnostics.describe(this.fileName));
        Logs.error(this, message.toString(), e);
        if (!retry) {
          throw e;
        }
        try {
          Thread.sleep(SHARING_VIOLATION_RETRY_WAIT_MILLIS);
        } catch (final InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          throw e;
        }
      }
    }
  }

  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }

  @Override
  public String toString() {
    return this.fileName;
  }

}
