/*
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
package com.revolsys.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.io.filter.PatternFilenameFilter;
import com.revolsys.util.ExceptionUtil;

/**
 * The FileUtil class is a utility class for performing common tasks with
 * classes from the java.io package.
 * 
 * @author Paul Austin
 */
public final class FileUtil {
  /** Files or directories to be deleted on exit. */
  private static final List<File> deleteFilesOnExit = new ArrayList<File>();

  /** The thread that deletes files on exit. */
  private static Thread deleteFilesOnExitThread;

  /** The logger to record errors to. */
  private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

  /** The file path separator for UNIX based systems. */
  public static final char UNIX_FILE_SEPARATOR = '/';

  /** The file path separator for Windows based systems. */
  public static final char WINDOWS_FILE_SEPARATOR = '\\';

  public static void closeSilent(final EndianInput in) {
    try {
      in.close();
    } catch (final IOException e) {
    }

  }

  /**
   * Close the input stream without throwing an I/O exception if the close
   * failed. The error will be logged instead.
   * 
   * @param in The input stream to close.
   */
  public static void closeSilent(final InputStream in) {
    if (in != null) {
      try {
        in.close();
      } catch (final IOException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Close the output stream without throwing an I/O exception if the close
   * failed. The error will be logged instead.
   * 
   * @param out The output stream to close.
   */
  public static void closeSilent(final OutputStream out) {
    if (out != null) {
      try {
        out.close();
      } catch (final IOException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Close the reader without throwing an I/O exception if the close failed. The
   * error will be logged instead.
   * 
   * @param in The reader to close.
   */
  public static void closeSilent(final Reader in) {
    if (in != null) {
      try {
        in.close();
      } catch (final IOException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Close the writer without throwing an I/O exception if the close failed. The
   * error will be logged instead.
   * 
   * @param out The writer to close.
   */
  public static void closeSilent(final Writer out) {
    if (out != null) {
      try {
        out.close();
      } catch (final IOException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Convert the path containing UNIX or Windows file separators to the local
   * {@link File#separator} character.
   * 
   * @param path The path to convert.
   * @return The converted path.
   */
  public static String convertPath(final String path) {
    final char separator = File.separatorChar;
    if (separator == WINDOWS_FILE_SEPARATOR) {
      return path.replace(UNIX_FILE_SEPARATOR, separator);
    } else if (separator == UNIX_FILE_SEPARATOR) {
      return path.replace(WINDOWS_FILE_SEPARATOR, separator);
    } else {
      return path.replace(UNIX_FILE_SEPARATOR, separator).replace(
        WINDOWS_FILE_SEPARATOR, separator);
    }
  }

  public static void copy(final File src, final File dest) throws IOException {
    if (src.isDirectory()) {
      dest.mkdirs();
      final File[] files = src.listFiles();
      if (files != null) {
        for (final File file : files) {
          final String name = file.getName();
          final File destFile = new File(dest, name);
          copy(file, destFile);
        }
      }
    } else {
      final FileInputStream in = new FileInputStream(src);
      File destFile;
      if (dest.isDirectory()) {
        final String name = src.getName();
        destFile = new File(dest, name);
      } else {
        destFile = dest;
      }
      copy(in, destFile);
    }
  }

  /**
   * Copy the contents of the file to the output stream. The output stream will
   * need to be closed manually after invoking this method.
   * 
   * @param file The file to read the contents from.
   * @param out The output stream to write the contents to.
   * @throws IOException If an I/O error occurs.
   */
  public static long copy(final File file, final OutputStream out)
    throws IOException {
    final FileInputStream in = new FileInputStream(file);
    try {
      return copy(in, out);
    } finally {
      in.close();
    }
  }

  /**
   * Copy the contents of the file to the writer. The writer will need to be
   * closed manually after invoking this method.
   * 
   * @param file The file to read the contents from.
   * @param out The writer to write the contents to.
   * @throws IOException If an I/O error occurs.
   */
  public static long copy(final File file, final Writer out) throws IOException {
    final FileReader in = new FileReader(file);
    try {
      return copy(in, out);
    } finally {
      in.close();
    }
  }

  /**
   * Copy the contents of the input stream to the file. The input stream will
   * need to be closed manually after invoking this method.
   * 
   * @param in The input stream to read the contents from.
   * @param file The file to write the contents to.
   * @throws IOException If an I/O error occurs.
   */
  public static long copy(final InputStream in, final File file) {
    try {
      final FileOutputStream out = new FileOutputStream(file);
      try {
        return copy(in, out);
      } finally {
        closeSilent(out);
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to open file: " + file, e);
    }
  }

  /**
   * Writes the content of a zip entry to a file using NIO.
   * 
   * @param zin input stream from zip file
   * @param file file path where this entry will be saved
   * @param sz file size
   * @throws IOException if an i/o error
   */
  public static void copy(final InputStream zin, final File file, final long sz)
    throws IOException {

    ReadableByteChannel rc = null;
    FileOutputStream out = null;

    try {
      rc = Channels.newChannel(zin);
      out = new FileOutputStream(file);
      final FileChannel fc = out.getChannel();

      // read into the buffer
      long count = 0;
      int attempts = 0;
      while (count < sz) {
        final long written = fc.transferFrom(rc, count, sz);
        count += written;

        if (written == 0) {
          attempts++;
          if (attempts > 100) {
            throw new IOException("Error writing to file " + file);
          }
        } else {
          attempts = 0;
        }
      }

      out.close();
      out = null;
    } finally {
      if (out != null) {
        FileUtil.closeSilent(out);
      }
    }
  }

  /**
   * Copy the contents of the input stream to the output stream. The input
   * stream and output stream will need to be closed manually after invoking
   * this method.
   * 
   * @param in The input stream to read the contents from.
   * @param out The output stream to write the contents to.
   */
  public static long copy(final InputStream in, final OutputStream out) {
    try {
      final byte[] buffer = new byte[4906];
      long numBytes = 0;
      int count;
      while ((count = in.read(buffer)) > -1) {
        out.write(buffer, 0, count);
        numBytes += count;
      }
      return numBytes;
    } catch (final IOException e) {
      return (Long)ExceptionUtil.throwUncheckedException(e);
    }
  }

  /**
   * Copy the contents of the reader to the file. The reader will need to be
   * closed manually after invoking this method.
   * 
   * @param in The reader to read the contents from.
   * @param file The file to write the contents to.
   * @throws IOException If an I/O error occurs.
   */
  public static void copy(final Reader in, final File file) throws IOException {
    final FileWriter out = new FileWriter(file);
    try {
      copy(in, out);
    } finally {
      in.close();
    }
  }

  /**
   * Copy the contents of the reader to the writer. The reader and writer will
   * need to be closed manually after invoking this method.
   * 
   * @param in The reader to read the contents from.
   * @param out The writer to write the contents to.
   * @throws IOException If an I/O error occurs.
   */
  public static long copy(final Reader in, final Writer out) {
    try {
      final char[] buffer = new char[4906];
      long numBytes = 0;
      int count;
      while ((count = in.read(buffer)) > -1) {
        out.write(buffer, 0, count);
        numBytes += count;
      }
      return numBytes;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Create a new temporary directory.
   * 
   * @param prefix The file name prefix.
   * @param suffix The file name suffix.
   * @return The temportary directory.
   * @throws IOException If there was an exception creating the directory.
   */
  public static File createTempDirectory(final String prefix,
    final String suffix) {
    try {
      final File file = File.createTempFile(prefix, suffix);
      if (!file.delete()) {
        throw new IOException("Cannot delete temporary file");
      }
      if (!file.mkdirs()) {
        throw new IOException("Cannot create temporary directory");
      }
      file.deleteOnExit();
      return file;
    } catch (final Exception e) {
      return ExceptionUtil.throwUncheckedException(e);
    }
  }

  public static File createTempFile(final String prefix, final String suffix) {
    try {
      final File file = File.createTempFile(prefix, suffix);
      deleteFileOnExit(file);
      return file;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Delete a directory and all the files and sub directories below the
   * directory.
   * 
   * @param directory The directory to delete.
   */
  public static boolean deleteDirectory(final File directory) {
    return deleteDirectory(directory, true);
  }

  /**
   * Delete all the files and sub directories below the directory. If the
   * deleteRoot flag is true the directory will also be deleted.
   * 
   * @param directory The directory to delete.
   * @param deleteRoot Flag indicating if the directory should also be deleted.
   * @throws IOException If a file or directory could not be deleted.
   */
  public static boolean deleteDirectory(final File directory,
    final boolean deleteRoot) {
    boolean deleted = true;
    final File[] files = directory.listFiles();
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        final File file = files[i];
        if (file.exists()) {
          if (file.isDirectory()) {
            if (!deleteDirectory(file, true)) {
              deleted = false;
            }
          } else {
            if (!file.delete() && file.exists()) {
              deleted = false;
              LOG.error("Cannot delete file: " + getCanonicalPath(file));
            }
          }
        }
      }
    }
    if (deleteRoot) {
      if (!directory.delete() && directory.exists()) {
        deleted = false;
        LOG.error("Cannot delete directory: " + getCanonicalPath(directory));
      }
    }
    return deleted;
  }

  public static void deleteDirectory(final File directory,
    final FilenameFilter filter) {
    final File[] files = directory.listFiles();
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        final File file = files[i];
        if (file.exists() && filter.accept(directory, file.getName())) {
          if (file.isDirectory()) {
            deleteDirectory(file, true);
          } else {
            if (!file.delete() && file.exists()) {
              LOG.error("Cannot delete file: " + getCanonicalPath(file));
            }
          }
        }
      }
    }
  }

  /**
   * Add the file to be deleted on exit. If the file is a directory the
   * directory and it's contents will be deleted.
   * 
   * @param file The file or directory to delete.
   */
  public static void deleteFileOnExit(final File file) {
    synchronized (deleteFilesOnExit) {
      if (deleteFilesOnExitThread == null) {
        deleteFilesOnExitThread = new Thread(new Runnable() {
          @Override
          public void run() {
            synchronized (deleteFilesOnExit) {
              for (final File file : deleteFilesOnExit) {
                if (file.exists()) {
                  if (file.isFile()) {
                    LOG.debug("Deleting file: " + file.getAbsolutePath());
                    file.delete();
                  } else {
                    LOG.debug("Deleting directory: " + file.getAbsolutePath());
                    deleteDirectory(file);
                  }
                }
              }
            }
          }
        });
        Runtime.getRuntime().addShutdownHook(deleteFilesOnExitThread);
      }
      deleteFilesOnExit.add(file);
    }
  }

  /**
   * Delete the files that match the java regular expression.
   * 
   * @param directory The directory.
   * @param pattern The regular expression to match
   */
  public static void deleteFiles(final File directory, final String pattern) {
    final File[] files = directory.listFiles(new PatternFilenameFilter(pattern));
    for (int i = 0; i < files.length; i++) {
      final File file = files[i];
      file.delete();
    }
  }

  public static void deleteFilesOlderThan(final File directory, final Date date) {
    final long time = date.getTime();
    deleteFilesOlderThan(directory, time);
  }

  public static void deleteFilesOlderThan(final File directory, final long age) {
    if (directory.exists() && directory.isDirectory()) {
      for (final File file : directory.listFiles()) {
        if (file.isDirectory()) {
          deleteFilesOlderThan(file, age);
        } else if (file.isFile()) {
          if (file.lastModified() < age) {
            if (!file.delete()) {
              LOG.error("Unable to delete file: " + file);
            }
          }
        }
      }
    }
  }

  public static String getBaseName(final File file) {
    final String fileName = file.getName();
    return getBaseName(fileName);
  }

  public static String getBaseName(final String name) {
    final String fileName = getFileName(name);
    final int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex != -1) {
      return fileName.substring(0, dotIndex);
    } else {
      return fileName;
    }
  }

  private static String getCanonicalPath(final File file) {
    try {
      return file.getCanonicalPath();
    } catch (final IOException e) {
      return file.getAbsolutePath();
    }
  }

  public static File getFile(final Resource resource) throws IOException {
    if (resource instanceof FileSystemResource) {
      final FileSystemResource fileResource = (FileSystemResource)resource;
      return fileResource.getFile();
    } else {
      final String fileName = resource.getFilename();
      final String ext = getFileNameExtension(fileName);
      final File file = File.createTempFile(fileName, "." + ext);
      copy(resource.getInputStream(), file);
      file.deleteOnExit();
      return file;
    }

  }

  public static String getFileName(final String fileName) {
    int slashIndex = fileName.lastIndexOf('/');
    if (slashIndex != -1) {
      return fileName.substring(slashIndex + 1);
    } else {
      slashIndex = fileName.lastIndexOf('\\');
      if (slashIndex != -1) {
        return fileName.substring(slashIndex + 1);
      } else {
        return fileName;
      }
    }
  }

  public static String getFileNameExtension(final File file) {
    final String fileName = file.getName();
    return getFileNameExtension(fileName);
  }

  public static String getFileNameExtension(final String fileName) {
    final int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex != -1) {
      return fileName.substring(dotIndex + 1);
    } else {
      return "";
    }
  }

  public static String getFileNamePrefix(final File file) {
    final String fileName = file.getName();
    return getBaseName(fileName);
  }

  public static File getFileWithExtension(final File file,
    final String extension) {
    final File parentFile = file.getParentFile();
    final String baseName = FileUtil.getFileNamePrefix(file);
    final String newFileName = baseName + "." + extension;
    if (parentFile == null) {
      return new File(newFileName);
    } else {
      return new File(parentFile, newFileName);
    }
  }

  public static FileInputStream getInputStream(final File file) {
    try {
      return new FileInputStream(file);
    } catch (final FileNotFoundException e) {
      throw new RuntimeException("Unble to open file: " + file, e);
    }
  }

  /**
   * Return the relative path of the file from the parentDirectory. For example
   * the relative path of c:\Data\Files\file1.txt and c:\Data would be
   * Files\file1.txt.
   * 
   * @param parentDirectory The parent directory.
   * @param file The file to return the relative path for.
   * @return The relative path.
   * @throws IOException If an I/O error occurs.
   */
  public static String getRelativePath(final File parentDirectory,
    final File file) throws IOException {
    final String parentPath = getCanonicalPath(parentDirectory);
    final String filePath = getCanonicalPath(file);
    if (filePath.startsWith(parentPath)) {
      return filePath.substring(parentPath.length() + 1);
    }
    return filePath;
  }

  public static String getString(final InputStream in) {
    try {
      final Reader reader = new InputStreamReader(in);
      final StringWriter out = new StringWriter();
      copy(reader, out);
      return out.toString();
    } finally {
      closeSilent(in);
    }
  }

  public static Writer getWriter(final File file) {
    try {
      return new FileWriter(file);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to open file " + file, e);
    }
  }

  public static URL toUrl(final File file) {
    try {
      return file.toURI().toURL();
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("Cannot get file url " + file, e);
    }
  }

  /**
   * Construct a new FileUtil.
   */
  private FileUtil() {
  }
}
