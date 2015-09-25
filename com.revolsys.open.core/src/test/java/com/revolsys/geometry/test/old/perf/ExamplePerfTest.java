package com.revolsys.geometry.test.old.perf;

/**
 * An example of the usage of the {@PerformanceTestRunner}.
 *
 * @author Martin Davis
 *
 */
public class ExamplePerfTest extends PerformanceTestCase {

  public static void main(final String args[]) {
    PerformanceTestRunner.run(ExamplePerfTest.class);
  }

  public ExamplePerfTest(final String name) {
    super(name);
    setRunSize(new int[] {
      10, 20
    });
    setRunIterations(10);
  }

  public void runExample1() {
    // System.out.println("Iter # " + this.iter++);
    // do test work here
  }

  public void runExample2() {
    // System.out.println("Iter # " + this.iter++);
    // do test work here
  }

  @Override
  public void setUp() {
    // read data and allocate resources here
  }

  @Override
  public void startRun(final int size) {
  }

  @Override
  public void tearDown() {
    // deallocate resources here
  }
}
