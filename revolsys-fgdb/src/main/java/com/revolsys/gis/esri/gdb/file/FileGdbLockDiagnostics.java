package com.revolsys.gis.esri.gdb.file;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Describes what has a geodatabase's files open, for the log message written
 * when the ESRI API reports a sharing violation. It is best effort and never
 * throws, so it can't change the result of the operation being diagnosed.
 */
final class FileGdbLockDiagnostics {
  private record FileInfo(Path path, long size, long modifiedMillis) {
  }

  private static final int ERROR_MORE_DATA = 234;

  private static final boolean IS_WINDOWS = System.getProperty("os.name", "")
    .startsWith("Windows");

  private static final int MAX_FILES = 500;

  private static final int MAX_LOCK_FILES = 20;

  private static final int MAX_RECENT_FILES = 10;

  // Layout of the Windows Restart Manager RM_PROCESS_INFO structure
  private static final long PROCESS_INFO_APP_NAME_OFFSET = 12;

  private static final long PROCESS_INFO_APP_NAME_SIZE = 256 * 2;

  private static final long PROCESS_INFO_APP_TYPE_OFFSET = 652;

  private static final long PROCESS_INFO_SERVICE_NAME_OFFSET = 524;

  private static final long PROCESS_INFO_SERVICE_NAME_SIZE = 64 * 2;

  private static final long PROCESS_INFO_SIZE = 668;

  private static final int SESSION_KEY_LENGTH = 32;

  private static void appendLockFiles(final StringBuilder text, final List<FileInfo> files) {
    // The name of an ESRI lock file includes the host name, process id and
    // thread id of whoever has the geodatabase or table open
    text.append("\n  ESRI lock files:");
    int count = 0;
    for (final FileInfo file : files) {
      final String fileName = file.path().getFileName().toString();
      if (fileName.endsWith(".lock")) {
        if (++count <= MAX_LOCK_FILES) {
          text.append("\n    ").append(fileName);
        }
      }
    }
    if (count == 0) {
      text.append(" none");
    } else if (count > MAX_LOCK_FILES) {
      text.append("\n    and ").append(count - MAX_LOCK_FILES).append(" more");
    }
  }

  private static void appendProcesses(final StringBuilder text, final List<FileInfo> files) {
    text.append("\n  Processes with the geodatabase's files open:");
    if (!IS_WINDOWS) {
      text.append(" not available on this operating system");
    } else if (files.isEmpty()) {
      text.append(" no files found");
    } else {
      try {
        final long currentProcessId = ProcessHandle.current().pid();
        final List<String> processes = new ArrayList<>();
        final boolean otherProcess = getProcessesUsingFiles(files, currentProcessId, processes);
        for (final String process : processes) {
          text.append("\n    ").append(process);
        }
        if (!otherProcess) {
          text.append(
            "\n    No other process had a file open when checked (it may have already closed it)");
        }
      } catch (final Throwable e) {
        text.append(" not available (").append(e).append(")");
      }
    }
  }

  private static void appendRecentFiles(final StringBuilder text, final List<FileInfo> files) {
    final long time = System.currentTimeMillis();
    text.append("\n  Most recently modified files (of ").append(files.size()).append("):");
    int count = 0;
    for (final FileInfo file : files) {
      if (++count > MAX_RECENT_FILES) {
        break;
      }
      text.append("\n    ")
        .append(file.path().getFileName())
        .append(' ')
        .append(file.size())
        .append(" bytes, modified ")
        .append(time - file.modifiedMillis())
        .append(" ms ago");
    }
  }

  private static void check(final String functionName, final int result) {
    if (result != 0) {
      throw new IllegalStateException(functionName + " failed with Windows error " + result);
    }
  }

  /**
   * Describe the geodatabase and what has its files open.
   *
   * @param geodatabasePath The geodatabase directory.
   * @return The description, with each item on a new indented line.
   */
  static String describe(final String geodatabasePath) {
    final StringBuilder text = new StringBuilder();
    try {
      text.append("\n  Geodatabase: ").append(geodatabasePath);
      text.append("\n  Thread: ").append(Thread.currentThread().getName());
      text.append(", process id: ").append(ProcessHandle.current().pid());
      if (geodatabasePath != null) {
        final List<FileInfo> files = listFiles(Paths.get(geodatabasePath));
        // Check the processes first, as a virus scanner or indexer only has a
        // file open for a moment
        appendProcesses(text, files);
        appendLockFiles(text, files);
        appendRecentFiles(text, files);
      }
    } catch (final Throwable e) {
      text.append("\n  Unable to get more details: ").append(e);
    }
    return text.toString();
  }

  private static String getApplicationType(final int type) {
    switch (type) {
      case 1:
        return "window";
      case 2:
        return "background window";
      case 3:
        return "service";
      case 4:
        return "explorer";
      case 5:
        return "console";
      case 1000:
        return "critical system process";
      default:
        return "unknown type";
    }
  }

  /**
   * Use the Windows Restart Manager to list the processes that have any of the
   * files open.
   *
   * @param files The files to check.
   * @param currentProcessId The id of this process.
   * @param processes The list to add a description of each process to.
   * @return True if a process other than this one has a file open.
   */
  private static boolean getProcessesUsingFiles(final List<FileInfo> files,
    final long currentProcessId, final List<String> processes) throws Throwable {
    boolean otherProcess = false;
    try (
      Arena arena = Arena.ofConfined()) {
      final Linker linker = Linker.nativeLinker();
      final SymbolLookup library = SymbolLookup.libraryLookup("rstrtmgr", arena);
      final MethodHandle startSession = linker.downcallHandle(
        library.find("RmStartSession").orElseThrow(), FunctionDescriptor.of(ValueLayout.JAVA_INT,
          ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
      final MethodHandle registerResources = linker.downcallHandle(
        library.find("RmRegisterResources").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT,
          ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
          ValueLayout.ADDRESS));
      final MethodHandle getList = linker.downcallHandle(library.find("RmGetList").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS,
          ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
      final MethodHandle endSession = linker.downcallHandle(
        library.find("RmEndSession").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));

      final MemorySegment sessionHandle = arena.allocate(ValueLayout.JAVA_INT);
      final MemorySegment sessionKey = arena.allocate(ValueLayout.JAVA_CHAR,
        SESSION_KEY_LENGTH + 1);
      check("RmStartSession", (int)startSession.invokeExact(sessionHandle, 0, sessionKey));
      final int session = sessionHandle.get(ValueLayout.JAVA_INT, 0);
      try {
        final int fileCount = files.size();
        final MemorySegment fileNames = arena.allocate(ValueLayout.ADDRESS, fileCount);
        for (int i = 0; i < fileCount; i++) {
          final String fileName = files.get(i).path().toString();
          final MemorySegment fileNameValue = arena.allocateFrom(fileName,
            StandardCharsets.UTF_16LE);
          fileNames.setAtIndex(ValueLayout.ADDRESS, i, fileNameValue);
        }
        check("RmRegisterResources", (int)registerResources.invokeExact(session, fileCount,
          fileNames, 0, MemorySegment.NULL, 0, MemorySegment.NULL));

        final MemorySegment neededCount = arena.allocate(ValueLayout.JAVA_INT);
        final MemorySegment count = arena.allocate(ValueLayout.JAVA_INT);
        final MemorySegment rebootReasons = arena.allocate(ValueLayout.JAVA_INT);
        int capacity = 16;
        MemorySegment processInfos = null;
        int result = ERROR_MORE_DATA;
        for (int i = 0; i < 3 && result == ERROR_MORE_DATA; i++) {
          processInfos = arena.allocate(PROCESS_INFO_SIZE * capacity, 4);
          count.set(ValueLayout.JAVA_INT, 0, capacity);
          result = (int)getList.invokeExact(session, neededCount, count, processInfos,
            rebootReasons);
          capacity = neededCount.get(ValueLayout.JAVA_INT, 0) + 8;
        }
        check("RmGetList", result);

        final int processCount = count.get(ValueLayout.JAVA_INT, 0);
        for (int i = 0; i < processCount; i++) {
          final long offset = i * PROCESS_INFO_SIZE;
          final long processId = Integer
            .toUnsignedLong(processInfos.get(ValueLayout.JAVA_INT, offset));
          final String applicationName = processInfos
            .asSlice(offset + PROCESS_INFO_APP_NAME_OFFSET, PROCESS_INFO_APP_NAME_SIZE)
            .getString(0, StandardCharsets.UTF_16LE);
          final String serviceName = processInfos
            .asSlice(offset + PROCESS_INFO_SERVICE_NAME_OFFSET, PROCESS_INFO_SERVICE_NAME_SIZE)
            .getString(0, StandardCharsets.UTF_16LE);
          final int applicationType = processInfos.get(ValueLayout.JAVA_INT,
            offset + PROCESS_INFO_APP_TYPE_OFFSET);

          final StringBuilder process = new StringBuilder();
          process.append("pid ").append(processId).append(": ").append(applicationName);
          if (processId == currentProcessId) {
            // The Restart Manager always reports the calling process as critical
            process.append(" (this process)");
          } else {
            otherProcess = true;
            process.append(" [").append(getApplicationType(applicationType));
            if (!serviceName.isEmpty()) {
              process.append(' ').append(serviceName);
            }
            process.append(']');
          }
          final String command = ProcessHandle.of(processId)
            .flatMap(handle -> handle.info().command())
            .orElse(null);
          if (command != null) {
            process.append(' ').append(command);
          }
          processes.add(process.toString());
        }
      } finally {
        endSession.invoke(session);
      }
    }
    return otherProcess;
  }

  private static List<FileInfo> listFiles(final Path directory) {
    final List<FileInfo> files = new ArrayList<>();
    try (
      Stream<Path> paths = Files.list(directory)) {
      for (final Path path : (Iterable<Path>)paths::iterator) {
        try {
          final BasicFileAttributes attributes = Files.readAttributes(path,
            BasicFileAttributes.class);
          if (attributes.isRegularFile()) {
            files.add(new FileInfo(path, attributes.size(),
              attributes.lastModifiedTime().toMillis()));
          }
        } catch (final Exception e) {
          // The file was deleted or can't be read, so leave it out
        }
      }
    } catch (final Exception e) {
      // The directory doesn't exist or can't be read, so there are no files
    }
    files.sort(Comparator.comparingLong(FileInfo::modifiedMillis).reversed());
    if (files.size() > MAX_FILES) {
      return new ArrayList<>(files.subList(0, MAX_FILES));
    } else {
      return files;
    }
  }

  private FileGdbLockDiagnostics() {
  }
}
