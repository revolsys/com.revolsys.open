package com.revolsys.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.revolsys.io.FileUtil;
import com.revolsys.parallel.ThreadInterruptedException;

public final class JavaProcess {

  public static Process exec(final File logFile,
    final List<String> javaArguments, final Class<?> klass) {
    return exec(logFile, javaArguments, klass, Collections.<String> emptyList());
  }

  public static Process exec(File logFile, final List<String> javaArguments,
    final Class<?> klass, final List<String> programArguments) {
    final ProcessBuilder builder = processBuilder(javaArguments, klass,
      programArguments);

    logFile = FileUtil.getFile(logFile);
    logFile.getParentFile().mkdirs();
    builder.redirectErrorStream(true);
    builder.redirectOutput(logFile);
    try {
      return builder.start();
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to start " + builder.command(), e);
    }
  }

  public static Process exec(final List<String> javaArguments,
    final Class<?> klass) {
    return exec(javaArguments, klass, Collections.<String> emptyList());
  }

  public static Process exec(final List<String> javaArguments,
    final Class<?> klass, final List<String> programArguments) {
    final ProcessBuilder builder = processBuilder(javaArguments, klass,
      programArguments);

    try {
      return builder.start();
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to start " + builder.command(), e);
    }
  }

  public static Process exec(final List<String> javaArguments,
    final Class<?> klass, final String... programArguments) {
    return exec(javaArguments, klass, Arrays.asList(programArguments));
  }

  public static int execAndWait(final List<String> javaArguments,
    final Class<?> klass, final List<String> programArguments) {
    final Process process = exec(javaArguments, klass, programArguments);
    try {
      process.waitFor();
    } catch (final InterruptedException e) {
      throw new ThreadInterruptedException(e);
    }
    return process.exitValue();
  }

  protected static ProcessBuilder processBuilder(
    final List<String> javaArguments, final Class<?> klass,
    final List<String> programArguments) {
    final String javaHome = System.getProperty("java.home");
    final String javaBin = javaHome + File.separator + "bin" + File.separator
      + "java";
    final String classpath = System.getProperty("java.class.path");
    final String className = klass.getCanonicalName();

    final List<String> params = new ArrayList<String>();
    params.add(javaBin);
    params.add("-cp");
    params.add(classpath);
    if (javaArguments != null) {
      params.addAll(javaArguments);
    }
    params.add(className);
    if (programArguments != null) {
      params.addAll(programArguments);
    }
    final ProcessBuilder builder = new ProcessBuilder(params);
    return builder;
  }

  private JavaProcess() {
  }

}
