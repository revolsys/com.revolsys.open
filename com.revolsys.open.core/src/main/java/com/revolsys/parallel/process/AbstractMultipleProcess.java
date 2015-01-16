package com.revolsys.parallel.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.revolsys.util.CollectionUtil;

public abstract class AbstractMultipleProcess extends AbstractProcess {

  private final List<Process> processes = new ArrayList<Process>();

  public AbstractMultipleProcess() {
  }

  public AbstractMultipleProcess(final Collection<? extends Process> processes) {
    this.processes.addAll(processes);
  }

  public AbstractMultipleProcess(final Process... processes) {
    this(Arrays.asList(processes));
  }

  public void addProcess(final Process process) {
    this.processes.add(process);
  }

  public List<Process> getProcesses() {
    return this.processes;
  }

  @Override
  public String toString() {
    return CollectionUtil.toString("\n  ", this.processes);
  }

}
