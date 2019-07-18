package com.revolsys.core.test.elevation.gridded.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jeometry.common.date.Dates;
import org.junit.Assert;
import org.junit.Test;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.elevation.gridded.usgsdem.UsgsGriddedElevationModel;
import com.revolsys.geometry.model.GeometryFactory;

public abstract class GriddedElevationModelTest {

  protected static void assertModelEquals(final GriddedElevationModel expectedModel,
    final GriddedElevationModel actualModel) {
    for (int gridY = 0; gridY < expectedModel.getGridHeight(); gridY++) {
      for (int gridX = 0; gridX < expectedModel.getGridWidth(); gridX++) {
        final double expectedElevation = expectedModel.getValue(gridX, gridY);
        final double actualElevation = actualModel.getValue(gridX, gridY);
        final String message = "Elevation (" + gridX + "," + gridY + ")";
        if (Double.isInfinite(actualElevation) && expectedElevation == 1) {
          actualModel.getValue(gridX, gridY);
        }
        Assert.assertEquals(message, expectedElevation, actualElevation, 0);
      }
    }
  }

  /**
  1. Write the model to the file specified by filePath.
  2. Read the model from the written file.
  3. Compare the read model with the original model
  
   * @param model The model to test.
   * @param filePath The file to write to.
   */
  protected static void assertWriteRead(final GriddedElevationModel model, final String filePath) {
    final Path path = writeModel(model, filePath);
    final GriddedElevationModel actualModel = GriddedElevationModel.newGriddedElevationModel(path);
    assertModelEquals(model, actualModel);
  }

  protected static GriddedElevationModel newIntArrayModelEmpty(final int coordinateSystemId) {
    final GeometryFactory geometryFactory = GeometryFactory.fixed3d(coordinateSystemId, 1000.0,
      1000.0, 1000.0);
    final GriddedElevationModel model = new IntArrayScaleGriddedElevationModel(geometryFactory, 0,
      0, 255, 255, 1);

    return model;
  }

  /**
  Create a new {@link UsgsGriddedElevationModel}.
  The elevation for each cell is set to gridX.gridY (e.g. 10.34).
  Except where gridX == gridY where NaN is used.

   * @param coordinateSystemId The coordinate system id.
   * @return The model
   */
  protected static GriddedElevationModel newIntArrayModelNaNOnDiagonal(
    final int coordinateSystemId) {
    final GeometryFactory geometryFactory = GeometryFactory.fixed3d(coordinateSystemId, 1000.0,
      1000.0, 1000.0);
    final GriddedElevationModel model = new IntArrayScaleGriddedElevationModel(geometryFactory, 0,
      0, 255, 255, 1);
    for (int gridY = 0; gridY < model.getGridHeight() - 1; gridY++) {
      for (int gridX = 0; gridX < model.getGridWidth() - 1; gridX++) {
        double elevation;
        if (gridX == gridY) {
          elevation = Double.NaN;
        } else {
          elevation = gridX + gridY / 1000.0;
        }
        model.setValue(gridX, gridY, elevation);
      }
    }
    return model;
  }

  protected static Path writeModel(final GriddedElevationModel model, final String filePath) {
    final Path path = Paths.get(filePath);
    com.revolsys.io.file.Paths.createParentDirectories(path);
    model.writeGriddedElevationModel(path);
    return path;
  }

  public abstract List<String> getFileExtensions();

  @Test
  public void test001WriteReadAllFileExtensions() {

    for (final String fileExtension : getFileExtensions()) {
      final long time = System.currentTimeMillis();
      final GriddedElevationModel model = GriddedElevationModelTest
        .newIntArrayModelNaNOnDiagonal(3005);
      final String filePath = "target/test/elevation/nanDiagonal." + fileExtension;
      assertWriteRead(model, filePath);
      System.out.println("test001WriteReadAllFileExtensions " + fileExtension + "\t"
        + Dates.toEllapsedTime(time, System.currentTimeMillis()));
    }
  }

}
