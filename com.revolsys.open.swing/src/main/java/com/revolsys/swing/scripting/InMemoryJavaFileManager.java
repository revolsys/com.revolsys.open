package com.revolsys.swing.scripting;

import java.io.IOException;
import java.net.URI;
import java.security.SecureClassLoader;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.util.UrlUtil;

public class InMemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

  private final URIJavaFileObject javaFile;

  private InMemoryJavaFile classFile;

  public InMemoryJavaFileManager(final URIJavaFileObject javaFile) {
    super(ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null));
    this.javaFile = javaFile;
    try {
      final String className = UrlUtil.getFileBaseName(javaFile.toUri().toURL());

      this.classFile = new InMemoryJavaFile(new URI("memory:/" + className + ".class"), Kind.CLASS);
    } catch (final Throwable e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public ClassLoader getClassLoader(final Location location) {
    return new SecureClassLoader() {
      @Override
      protected Class<?> findClass(final String name) throws ClassNotFoundException {
        final byte[] data = InMemoryJavaFileManager.this.classFile.getData();
        return super.defineClass(name, data, 0, data.length);
      }
    };
  }

  @Override
  public JavaFileObject getJavaFileForOutput(final Location location, final String className,
    final Kind kind, final FileObject sibling) throws IOException {
    if (sibling == this.javaFile) {
      return this.classFile;
    } else {
      return super.getJavaFileForOutput(location, className, kind, sibling);
    }
  }

}
