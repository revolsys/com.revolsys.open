package com.revolsys.process;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.list.Lists;
import com.revolsys.io.FileUtil;
import com.revolsys.parallel.ThreadInterruptedException;
import com.revolsys.util.Exceptions;

public final class JavaProcess implements Runnable {
  private List<String> javaArguments = new ArrayList<>();

  private List<String> programArguments = new ArrayList<>();

  private Class<?> programClass;

  private File logFile;

  public JavaProcess() {
  }

  public JavaProcess(final Class<?> programClass) {
    this(null, programClass, null);
  }

  public JavaProcess(final List<String> javaArguments, final Class<?> programClass,
    final List<String> programArguments) {
    this.javaArguments = Lists.array(javaArguments);
    this.programArguments = Lists.array(programArguments);
    this.programClass = programClass;
  }

  public JavaProcess(final List<String> javaArguments, final List<String> programArguments) {
    this(javaArguments, null, programArguments);
  }

  public JavaProcess addJavaArgument(final int index, final String argument) {
    this.javaArguments.add(index, argument);
    return this;
  }

  public JavaProcess addJavaArgument(final String argument) {
    this.javaArguments.add(argument);
    return this;
  }

  public JavaProcess addProgramArgument(final int index, final String argument) {
    this.programArguments.add(index, argument);
    return this;
  }

  public JavaProcess addProgramArgument(final String argument) {
    this.programArguments.add(argument);
    return this;
  }

  public List<String> getJavaArguments() {
    return this.javaArguments;
  }

  public File getLogFile() {
    return this.logFile;
  }

  public List<String> getProgramArguments() {
    return this.programArguments;
  }

  public Class<?> getProgramClass() {
    return this.programClass;
  }

  public ProcessBuilder newBuilder() {
    final String javaHome = System.getProperty("java.home");
    final String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

    final List<String> params = new ArrayList<String>();
    params.add(javaBin);

    params.add("-cp");
    final String classpath = System.getProperty("java.class.path");
    params.add(classpath);

    final String libraryPath = System.getProperty("java.library.path");
    params.add("-Djava.library.path=" + libraryPath);

    final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    final List<String> inputArguments = runtimeMXBean.getInputArguments();
    for (final String inputArgument : inputArguments) {
      if (!inputArgument.startsWith("-agentlib")) {
        params.add(inputArgument);
      }
    }

    params.addAll(this.javaArguments);

    final String className = this.programClass.getCanonicalName();
    params.add(className);

    params.addAll(this.programArguments);

    final ProcessBuilder builder = new ProcessBuilder(params);
    if (this.logFile != null) {
      this.logFile = FileUtil.getFile(this.logFile);
      this.logFile.getParentFile().mkdirs();
      builder.redirectErrorStream(true);
      builder.redirectOutput(this.logFile);
    }
    return builder;
  }

  @Override
  public void run() {
    if (this.programClass == null) {
      LoggerFactory.getLogger(getClass()).error("programClass cannot be null");
    } else {
      try {
        final int exitValue = startAndWait();
        if (exitValue != 0) {
          JOptionPane.showMessageDialog(
            null, "<html>Error ruunning process, check log file for details<br /><code>"
              + this.logFile + "</code></html>",
            "Error running process", JOptionPane.ERROR_MESSAGE);
        }
      } catch (final Throwable e) {
        Exceptions.log(this.programClass, e);
      }
    }
  }

  public JavaProcess setJavaArguments(final List<String> javaArguments) {
    this.javaArguments = Lists.array(javaArguments);
    return this;
  }

  public JavaProcess setLogFile(final File logFile) {
    this.logFile = logFile;
    return this;
  }

  public JavaProcess setProgramArguments(final List<String> programArguments) {
    this.programArguments = Lists.array(programArguments);
    return this;
  }

  public JavaProcess setProgramClass(final Class<?> programClass) {
    this.programClass = programClass;
    return this;
  }

  public Process start() {
    final ProcessBuilder processBuilder = newBuilder();
    try {
      return processBuilder.start();
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to start " + processBuilder.command(), e);
    }
  }

  public int startAndWait() {
    final Process process = start();
    try {
      process.waitFor();
    } catch (final InterruptedException e) {
      throw new ThreadInterruptedException(e);
    }
    return process.exitValue();
  }

  public Thread startThread() {
    if (this.programClass == null) {
      LoggerFactory.getLogger(getClass()).error("programClass cannot be null");
      return null;
    } else {
      final Thread thread = new Thread(this, this.programClass.getName());
      thread.start();
      return thread;
    }
  }

}
