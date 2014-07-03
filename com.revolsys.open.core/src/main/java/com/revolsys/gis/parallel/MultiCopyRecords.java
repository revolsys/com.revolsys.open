package com.revolsys.gis.parallel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.data.io.DataObjectStore;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.parallel.process.AbstractMultipleProcess;
import com.revolsys.parallel.process.Parallel;
import com.revolsys.parallel.process.Process;
import com.revolsys.parallel.process.ProcessNetwork;
import com.revolsys.parallel.process.Sequential;
import com.revolsys.process.CopyRecords;
import com.revolsys.util.CollectionUtil;

public class MultiCopyRecords implements Process {
  private DataObjectStore targetDataStore;

  private DataObjectStore sourceDataStore;

  private Process process;

  private String name;

  private ProcessNetwork processNetwork;

  private Map<String, Object> processDefinition;

  @SuppressWarnings("unchecked")
  protected Process createProcess(final Map<String, Object> processDefinition) {
    if (processDefinition == null) {
      return null;
    } else {
      final String type = (String)processDefinition.get("type");
      if ("copyRecords".equals(type)) {
        final String typePath = (String)processDefinition.get("typePath");
        if (StringUtils.hasText(typePath)) {
          final boolean hasSequence = CollectionUtil.getBool(processDefinition,
            "hasSequence");
          final Map<String, Boolean> orderBy = CollectionUtil.get(
            processDefinition, "orderBy",
            Collections.<String, Boolean> emptyMap());
          final CopyRecords copy = new CopyRecords(sourceDataStore, typePath,
            orderBy, targetDataStore, hasSequence);
          return copy;
        } else {
          LoggerFactory.getLogger(getClass()).error(
            "Parameter 'typePath' required for type='copyRecords'");
        }
      } else if ("sequential".equals(type)) {
        final List<Map<String, Object>> processList = (List<Map<String, Object>>)processDefinition.get("processes");
        if (processList == null) {
          LoggerFactory.getLogger(getClass()).error(
            "Parameter 'processes' required for type='sequential'");
        } else {
          final Sequential processes = new Sequential();
          createProcesses(processes, processList);
          return processes;
        }
      } else if ("parallel".equals(type)) {
        final List<Map<String, Object>> processList = (List<Map<String, Object>>)processDefinition.get("processes");
        if (processList == null) {
          LoggerFactory.getLogger(getClass()).error(
            "Parameter 'processes' required for type='parallel'");
        } else {
          final Parallel processes = new Parallel();
          createProcesses(processes, processList);
          return processes;
        }

      } else {
        LoggerFactory.getLogger(getClass()).error(
          "Parameter type=" + type
            + " not in 'copyRecords', 'sequential', 'copyRecords'");
      }
      return null;
    }
  }

  private void createProcesses(final AbstractMultipleProcess processes,
    final List<Map<String, Object>> processDefinitions) {
    for (final Map<String, Object> processDefinition : processDefinitions) {
      final Process process = createProcess(processDefinition);
      if (process != null) {
        processes.addProcess(process);
      }
    }
  }

  @Override
  public String getBeanName() {
    return name;
  }

  @Override
  public ProcessNetwork getProcessNetwork() {
    return this.processNetwork;
  }

  public DataObjectStore getSourceDataStore() {
    return sourceDataStore;
  }

  public DataObjectStore getTargetDataStore() {
    return targetDataStore;
  }

  @Override
  public void run() {
    process = createProcess(processDefinition);
    if (process != null) {
      if (processNetwork != null) {
        processNetwork.addProcess(process);
      } else {
        process.run();
      }
    }
  }

  @Override
  public void setBeanName(final String name) {
    this.name = name;
  }

  public void setProcessDefinition(final Map<String, Object> processDefinition) {
    this.processDefinition = processDefinition;
  }

  public void setProcessDefinitionResource(final Resource resource) {
    final Map<String, Object> processDefinition = JsonMapIoFactory.toMap(resource);
    setProcessDefinition(processDefinition);
  }

  @Override
  public void setProcessNetwork(final ProcessNetwork processNetwork) {
    this.processNetwork = processNetwork;
    if (processNetwork != null) {
      processNetwork.addProcess(this);
    }
  }

  public void setSourceDataStore(final DataObjectStore sourceDataStore) {
    this.sourceDataStore = sourceDataStore;
  }

  public void setTargetDataStore(final DataObjectStore targetDataStore) {
    this.targetDataStore = targetDataStore;
  }
}
