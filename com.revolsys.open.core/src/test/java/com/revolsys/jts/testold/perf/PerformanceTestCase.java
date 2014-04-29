package com.revolsys.jts.testold.perf;

/**
 * A base class for classes implementing performance tests
 * to be run by the {@link PerformanceTestRunner}.
 * <p>
 * In a subclass of this class,
 * all public methods which start with <code>run</code> are 
 * executed as performance tests.
 * <p>
 * Multiple test runs with different run sizes may be made.
 * Within each run, each <code>run</code> method is executed 
 * the specified number of iterations.
 * The time to run the method is printed for each one.
 * 
 * @author Martin Davis
 *
 */
public abstract class PerformanceTestCase {
  private final String name;

  private int[] runSize = new int[] {
    1
  };

  private int runIter = 1;

  public PerformanceTestCase(final String name) {
    this.name = name;
  }

  /**
   * Ends a test run.
   * 
   * @throws Exception
   */
  public void endRun() throws Exception {

  }

  public String getName() {
    return this.name;
  }

  public int getRunIterations() {
    return this.runIter;
  }

  public int[] getRunSize() {
    return this.runSize;
  }

  /**
   * Sets the number of iterations to execute the test methods in each test run.
   * 
   * @param runIter the number of iterations to execute.
   */
  protected void setRunIterations(final int runIter) {
    this.runIter = runIter;
  }

  /**
   * Sets the size(s) for the runs of the test.
   * 
   * @param runSize a list of the sizes for the test runs
   */
  protected void setRunSize(final int[] runSize) {
    this.runSize = runSize;
  }

  /**
   * Sets up any fixtures needed for the test runs.
   * 
   * @throws Exception
   */
  public void setUp() throws Exception {

  }

  /**
   * Starts a test run with the given size.
   * 
   * @param size
   * @throws Exception
   */
  public void startRun(final int size) throws Exception {

  }

  /**
   * Tear down any fixtures made for the testing.
   * 
   * @throws Exception
   */
  public void tearDown() throws Exception {

  }

}
