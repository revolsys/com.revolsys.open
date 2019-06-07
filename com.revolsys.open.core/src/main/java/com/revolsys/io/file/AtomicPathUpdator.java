package com.revolsys.io.file;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.BaseCloseable;

public class AtomicPathUpdator implements BaseCloseable {
  private static final CopyOption[] MOVE_OPTIONS = {
    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING
  };

  private final Path targetDirectory;

  private final String fileName;

  private Path tempDirectory;

  private Path path;

  private boolean cancelled = false;

  public AtomicPathUpdator(final Path directory, final String fileName) {
    try {
      this.targetDirectory = directory;
      this.fileName = fileName;
      this.tempDirectory = Files.createTempDirectory(fileName);
      this.path = this.tempDirectory.resolve(fileName);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void close() {
    final Path tempDirectory = this.tempDirectory;
    try {
      if (!this.cancelled) {
        try {
          Files.walk(tempDirectory).forEach(tempPath -> {
            if (!tempPath.equals(tempDirectory)) {
              final Path relativePath = tempDirectory.relativize(tempPath);
              final Path targetPath = this.targetDirectory.resolve(relativePath);
              Paths.createParentDirectories(targetPath);
              final Path oldPath = Paths.addExtension(targetPath, "old");
              move(targetPath, oldPath);
              move(tempPath, targetPath);
              try {
                Files.deleteIfExists(oldPath);
              } catch (final IOException e) {
                throw Exceptions.wrap("Error deleting file: " + oldPath, e);
              }
            }
          });
        } catch (final IOException e) {
          throw Exceptions.wrap(e);
        }
      }
    } finally {
      Paths.deleteDirectories(tempDirectory);
    }
  }

  public Path getPath() {
    return this.path;
  }

  public boolean isCancelled() {
    return this.cancelled;
  }

  public boolean isTargetExists() {
    final Path targetFile = this.targetDirectory.resolve(this.fileName);
    return Files.exists(targetFile);
  }

  private void move(final Path sourcePath, final Path targetPath) {
    try {
      if (Files.exists(sourcePath)) {
        Files.move(sourcePath, targetPath, MOVE_OPTIONS);
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Error moving file: " + sourcePath + " to " + targetPath, e);
    }
  }

  public void setCancelled(final boolean cancelled) {
    this.cancelled = cancelled;
  }

  @Override
  public String toString() {
    return this.targetDirectory + "/" + this.fileName;
  }
}
