package com.revolsys.swing.scripting;

import java.awt.Window;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.function.Consumer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject.Kind;
import javax.tools.ToolProvider;

import com.revolsys.io.FileUtil;
import com.revolsys.io.filter.FileNameExtensionFilter;
import com.revolsys.process.JavaProcess;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.logging.LoggingEventPanel;
import com.revolsys.util.PreferencesUtil;

public class ScriptRunner {
  private static final String[] NULL_MAIN_ARGS = new String[0];

  public static void main(final String[] args) {
    if (args.length > 0) {
      final String fileName = args[0];
      final File scriptFile = FileUtil.getFile(fileName);
      runScript(scriptFile);
    }
  }

  public static void runScript(final File scriptFile) {
    final ScriptRunner scriptRunner = new ScriptRunner(scriptFile);
    scriptRunner.run();
  }

  public static void runScript(final Window window) {
    runScript(window, ScriptRunner::runScript);
  }

  public static void runScript(final Window window, final Consumer<File> action) {
    final JFileChooser fileChooser = SwingUtil.newFileChooser("Select Script",
      "com.revolsys.swing.tools.script", "directory");

    final FileNameExtensionFilter javaFilter = new FileNameExtensionFilter("Java", "java");
    fileChooser.addChoosableFileFilter(javaFilter);

    fileChooser.setMultiSelectionEnabled(false);
    final int returnVal = Dialogs.showOpenDialog(fileChooser);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      final File selectedFile = fileChooser.getSelectedFile();
      final File scriptFile = FileUtil.getFile(selectedFile);
      PreferencesUtil.setUserString("com.revolsys.swing.tools.script", "directory",
        scriptFile.getParent());
      action.accept(scriptFile);
    }
  }

  public static void runScriptProcess(final Window window) {
    final JavaProcess process = new JavaProcess();
    runScriptProcess(window, null, process);
  }

  public static void runScriptProcess(final Window window, final File logDirectory,
    final JavaProcess javaProcess) {
    runScript(window, scriptFile -> {
      javaProcess.setProgramClass(ScriptRunner.class);
      javaProcess.addProgramArgument(0, scriptFile.getAbsolutePath());
      final String baseName = FileUtil.getBaseName(scriptFile);
      if (logDirectory != null) {
        final String logFileName = baseName + ".log";
        final File logFile = FileUtil.getFile(logDirectory, logFileName);
        logDirectory.mkdirs();
        javaProcess.setLogFile(logFile);
      }
      final ProcessBuilder processBuilder = javaProcess.newBuilder();
      new ProcessWindow(baseName, processBuilder);
    });
  }

  private final File scriptFile;

  public ScriptRunner(final File scriptFile) {
    this.scriptFile = FileUtil.getFile(scriptFile);
  }

  public void run() {
    if (this.scriptFile == null) {
    } else if (this.scriptFile.exists() && this.scriptFile.isFile()) {
      final String fileExtension = FileUtil.getFileNameExtension(this.scriptFile);
      try {
        if ("java".equals(fileExtension)) {
          final String scriptClassName = FileUtil.getBaseName(this.scriptFile);

          final URIJavaFileObject scriptJavaFile = new URIJavaFileObject(this.scriptFile,
            Kind.SOURCE);
          final JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
          final JavaFileManager fileManager = new InMemoryJavaFileManager(scriptJavaFile);
          final CompilationTask compilationTask = javaCompiler.getTask(null, fileManager, null,
            null, null, Collections.singleton(scriptJavaFile));
          compilationTask.call();

          final ClassLoader classLoader = fileManager.getClassLoader(null);

          final Class<?> scriptClass;
          try {
            scriptClass = classLoader.loadClass(scriptClassName);
          } catch (final ClassNotFoundException e) {
            showErrorDialog("must contain the class:<br />"//
              + "<code>public class " + scriptClassName + "</code><br />" //
              + "in the default package.");
            return;
          }

          final Method mainMethod;
          try {
            mainMethod = scriptClass.getDeclaredMethod("main", String[].class);
          } catch (final NoSuchMethodException e) {
            showErrorDialog("must contain the method:<br />"
              + "<code>public static void main(String[] args)</code><br />" //
              + " in the class:<br />" //
              + "<code>" + scriptClassName + "</code>");
            return;
          }
          try {
            mainMethod.invoke(null, (Object)NULL_MAIN_ARGS);
          } catch (final InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            showErrorDialog(scriptClass, targetException);
          } catch (final Throwable e) {
            showErrorDialog(scriptClass, e);
            return;

          }
        }
      } catch (final Throwable e) {
        showErrorDialog(getClass(), e);
      }
    } else {
      showErrorDialog("Does not exist");
    }
  }

  private void showErrorDialog(final Class<?> scriptClass, final Throwable e) {
    LoggingEventPanel.showDialog("Error running script file:<br /> " //
      + "<code>" + this.scriptFile + "</code><br />"//
      + e.getMessage(), e);
  }

  private void showErrorDialog(final String message) {
    Dialogs.showMessageDialog("<html>Script file:<br /> " + "<code>" + this.scriptFile
      + "</code><br />" + message + "</html>", "Script Error", JOptionPane.ERROR_MESSAGE);
  }
}
