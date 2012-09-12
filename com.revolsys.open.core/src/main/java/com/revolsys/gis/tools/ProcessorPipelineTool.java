/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.gis.tools;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.revolsys.io.FileUtil;
import com.revolsys.logging.log4j.ThreadLocalFileAppender;
import com.revolsys.parallel.process.ProcessNetwork;

public class ProcessorPipelineTool {
  private static final String EXCLUDE_PATTERN = "exclude";

  private static final String EXCLUDE_PATTERN_OPTION = "x";

  private static final Logger log = Logger.getLogger(ProcessorPipelineTool.class);

  private static final String LOG_DIRECTORY = "logDirectory";

  private static final String LOG_DIRECTORY_OPTION = "l";

  private static final String OUTPUT_DIRECTORY = "outputDirectory";

  private static final String OUTPUT_DIRECTORY_OPTION = "o";

  private static final String SCRIPT = "script";

  private static final String SCRIPT_OPTION = "s";

  private static final String SOURCE_DIRECTORY = "sourceDirectory";

  private static final String SOURCE_DIRECTORY_OPTION = "V";

  private static final String SOURCE_FILE_EXTENSION_OPTION = "e";

  private static final String SOURCE_FLE_EXTENSION = "sourceFileExtension";

  private static Throwable getBeanExceptionCause(final BeanCreationException e) {
    Throwable cause = e.getCause();
    while (cause instanceof BeanCreationException
      || cause instanceof MethodInvocationException
      || cause instanceof PropertyAccessException) {
      final Throwable newCause = cause.getCause();
      if (newCause != null) {
        cause = newCause;
      }
    }
    return cause;
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final ProcessorPipelineTool app = new ProcessorPipelineTool();
    app.start(args);
  }

  private CommandLine commandLine;

  private String excludePattern;

  private File logDirectory;

  private final Options options = new Options();

  private File scriptFile;

  private File sourceDirectory;

  private File sourceFile;

  private String sourceFileExtension;

  private File targetDirectory;

  private File targetFile;

  public ProcessorPipelineTool() {
    createOptions();
  }

  private void createOptions() {
    final Option script = new Option(SCRIPT_OPTION, SCRIPT, true,
      "the script file that defines the processor pipeline");
    script.setRequired(true);
    options.addOption(script);

    final Option sourceDirectory = new Option(SOURCE_DIRECTORY_OPTION,
      SOURCE_DIRECTORY, true, "the location of the source files to process");
    sourceDirectory.setRequired(false);
    options.addOption(sourceDirectory);

    final Option sourceFileExtension = new Option(SOURCE_FILE_EXTENSION_OPTION,
      SOURCE_FLE_EXTENSION, true,
      "the file extension of the source files (e.g. .saf)");
    sourceFileExtension.setRequired(false);
    options.addOption(sourceFileExtension);

    final Option outputDirectory = new Option(OUTPUT_DIRECTORY_OPTION,
      OUTPUT_DIRECTORY, true, "the directory to write processed files to");
    outputDirectory.setRequired(false);
    options.addOption(outputDirectory);

    final Option logDirectory = new Option(LOG_DIRECTORY_OPTION, LOG_DIRECTORY,
      true, "the directory to write log files to");
    logDirectory.setRequired(false);
    options.addOption(logDirectory);

    final Option excludePattern = new Option(EXCLUDE_PATTERN_OPTION,
      EXCLUDE_PATTERN, true,
      "exclude files matching a regular expression (e.g. '.*_back.zip");
    excludePattern.setRequired(false);
    options.addOption(excludePattern);

    final Option property = new Option("D", "property=value", true,
      "use value for given property");
    property.setValueSeparator('=');
    options.addOption(property);
  }

  @SuppressWarnings("unchecked")
  public boolean processArguments(final String[] args) {
    try {
      final CommandLineParser parser = new PosixParser();
      commandLine = parser.parse(options, args);
      final List<String> arguments = commandLine.getArgList();
      final Option[] options = commandLine.getOptions();
      for (int i = 0; i < options.length; i++) {
        final Option option = options[i];
        final String shortOpt = option.getOpt();
        if (shortOpt != null && shortOpt.equals("D")) {
          final String argument = arguments.remove(0);
          final String[] values = argument.split("=");
          System.setProperty(values[0], values[1]);
        }

      }
      if (commandLine.hasOption(SOURCE_DIRECTORY_OPTION)) {
        sourceDirectory = new File(
          commandLine.getOptionValue(SOURCE_DIRECTORY_OPTION));
        if (!sourceDirectory.isDirectory()) {
          System.err.println("Source directory '"
            + sourceDirectory.getAbsolutePath()
            + "' does not exist or is not a directory");
          return false;
        }
      }
      if (commandLine.hasOption(SOURCE_FILE_EXTENSION_OPTION)) {
        sourceFileExtension = commandLine.getOptionValue(SOURCE_FILE_EXTENSION_OPTION);
      }
      if (commandLine.hasOption(OUTPUT_DIRECTORY_OPTION)) {
        targetDirectory = new File(
          commandLine.getOptionValue(OUTPUT_DIRECTORY_OPTION));
        if (!targetDirectory.isDirectory()) {
          System.err.println("Target directory '"
            + targetDirectory.getAbsolutePath()
            + "' does not exist or is not a directory");
          return false;
        }
      }
      if (commandLine.hasOption(LOG_DIRECTORY_OPTION)) {
        logDirectory = new File(
          commandLine.getOptionValue(LOG_DIRECTORY_OPTION));
        if (!logDirectory.isDirectory()) {
          System.err.println("Log directory '" + logDirectory.getAbsolutePath()
            + "' does not exist or is not a directory");
          return false;
        }
      }
      scriptFile = new File(commandLine.getOptionValue(SCRIPT_OPTION));
      if (!scriptFile.exists()) {
        System.err.println("The script '" + scriptFile + "' does not exist");
        return false;
      }
      excludePattern = commandLine.getOptionValue(EXCLUDE_PATTERN_OPTION);
      if (sourceDirectory != null) {
        if (targetDirectory == null) {
          System.err.println("A " + OUTPUT_DIRECTORY + " must be specified if "
            + SOURCE_DIRECTORY + " is specified");
          return false;
        }
        if (sourceFileExtension == null) {
          System.err.println("A " + SOURCE_FLE_EXTENSION
            + " must be specified if " + SOURCE_DIRECTORY + " is specified");
          return false;
        }
      } else {
        sourceFile = new File(arguments.get(0));
        if (!sourceFile.exists()) {
          System.err.println("The file '" + sourceFile + "' does not exist");
          return false;
        }
        targetFile = new File(arguments.get(1));
        // if (targetFile.isDirectory()) {
        // targetFile = new File(targetFile, sourceFile.getName());
        // }
      }
      return true;
    } catch (final MissingOptionException e) {
      System.err.println("Missing " + e.getMessage() + " argument");
      return false;
    } catch (final ParseException e) {
      System.err.println("Unable to process command line arguments: "
        + e.getMessage());
      return false;
    }
  }

  private void processDirectory(final File sourceDirectory,
    final File targetDirectory, final File logDirectory,
    final String sourceFileExtension) {
    System.out.println("Processing directory '"
      + sourceDirectory.getAbsolutePath() + "'");
    final File[] files = sourceDirectory.listFiles();
    for (int i = 0; i < files.length; i++) {
      final File file = files[i];
      final String fileName = file.getName();
      if (file.isDirectory()) {
        processDirectory(file, new File(targetDirectory, fileName), new File(
          logDirectory, fileName), sourceFileExtension);
      } else if (fileName.endsWith(sourceFileExtension)) {
        processFile(file, new File(targetDirectory, fileName), new File(
          logDirectory, fileName + ".log"));
      }
    }
  }

  private void processFile(final File sourceFile, final File targetFile,
    final File logFile) {
    final long startTime = System.currentTimeMillis();
    if (excludePattern != null) {
      try {
        if (sourceFile.getCanonicalPath().matches(excludePattern)) {
          return;
        }
      } catch (final IOException e) {
        log.error(e.getMessage(), e);
      }
    }

    final ThreadLocalFileAppender localAppender = ThreadLocalFileAppender.getAppender();
    if (localAppender != null) {
      final File parentFile = logFile.getParentFile();
      if (parentFile != null) {
        parentFile.mkdirs();
      }
      localAppender.setLocalFile(logFile.getAbsolutePath());
    }
    log.info("Processing file '" + sourceFile + "' to '" + targetFile + "'");
    System.out.println("Processing file '" + sourceFile + "' to '" + targetFile
      + "'");

    System.setProperty("sourceFile", sourceFile.getAbsolutePath());
    System.setProperty("targetFile", targetFile.getAbsolutePath());
    final BeanFactory beans = new FileSystemXmlApplicationContext("file:"
      + scriptFile.getAbsolutePath());
    try {
      final File parentFile = targetFile.getParentFile();
      if (parentFile != null) {
        parentFile.mkdirs();
      }
      final Object bean = beans.getBean("pipeline");
      final ProcessNetwork pipeline = (ProcessNetwork)bean;
      pipeline.startAndWait();
    } catch (final BeanCreationException e) {
      final Throwable cause = getBeanExceptionCause(e);
      cause.printStackTrace();
    }
    final long endTime = System.currentTimeMillis();
    final long time = endTime - startTime;
    long seconds = time / 1000;
    final long minutes = seconds / 60;
    seconds = seconds % 60;
    log.info(minutes + " minutes " + seconds + " seconds");
    System.out.println(minutes + " minutes " + seconds + " seconds");

  }

  private void run() {
    if (sourceFile != null) {
      final String baseName = FileUtil.getFileNamePrefix(targetFile);
      if (logDirectory == null) {
        final File parentDirectory = targetFile.getParentFile();
        if (parentDirectory == null) {
          logDirectory = new File(baseName);
        } else {
          logDirectory = new File(parentDirectory, baseName);
        }
      }
      logDirectory.mkdirs();
      final File logFile = new File(logDirectory, baseName + ".log");

      processFile(sourceFile, targetFile, logFile);
    } else {
      processDirectory(sourceDirectory, targetDirectory, logDirectory,
        sourceFileExtension);
    }
  }

  public void start(final String[] args) {
    if (processArguments(args)) {
      run();
    } else {
      final HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("processorPipeline", options);
    }

  }
}
