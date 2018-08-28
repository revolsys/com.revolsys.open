package com.revolsys.core.test.geometry.test.testrunner;

/**
 * An Exception which indicates a problem during reflection
 *
 * @author Martin Davis
 * @version 1.7
 */
public class JTSTestReflectionException extends Exception {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private static String newMessage(final String opName, final Object[] args) {
    String msg = "Could not find Geometry method: " + opName + "(";
    for (int j = 0; j < args.length; j++) {
      if (j > 0) {
        msg += ", ";
      }
      msg += args[j].getClass().getName();
    }
    msg += ")";
    return msg;
  }

  public JTSTestReflectionException(final String message) {
    super(message);
  }

  public JTSTestReflectionException(final String opName, final Object[] args) {
    super(newMessage(opName, args));
  }

}
