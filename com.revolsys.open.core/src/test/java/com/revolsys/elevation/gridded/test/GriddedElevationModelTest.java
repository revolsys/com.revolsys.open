package com.revolsys.elevation.gridded.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.IntArrayScaleGriddedElevationModel;
import com.revolsys.geometry.model.GeometryFactory;

public abstract class GriddedElevationModelTest {

  /**
  1. Write the model to the file specified by filePath.
  2. Read the model from the written file.
  3. Compare the read model with the original model

   * @param model The model to test.
   * @param filePath The file to write to.
   */
  public static void assertWriteRead(final GriddedElevationModel model, final String filePath) {
    final Path path = Paths.get(filePath);
    com.revolsys.io.file.Paths.createParentDirectories(path);
    model.writeGriddedElevationModel(path);
    final GriddedElevationModel actualModel = GriddedElevationModel.newGriddedElevationModel(path);
    for (int gridY = 0; gridY < model.getGridHeight(); gridY++) {
      for (int gridX = 0; gridX < model.getGridWidth(); gridX++) {
        final double expectedElevation = model.getElevation(gridX, gridY);
        final double actualElevation = actualModel.getElevation(gridX, gridY);
        final String message = "Elevation (" + gridX + "," + gridY + ")";
        Assert.assertEquals(message, expectedElevation, actualElevation, 0);
      }
    }
  }

  /**
  Create a new {@link IntArrayScaleGriddedElevationModel}.
  The elevation for each cell is set to gridX.gridY (e.g. 10.34).
  Except where gridX == gridY where NaN is used.
  
   * @param coordinateSystemId The coordinate system id.
   * @return The model
   */
  public static GriddedElevationModel newIntArrayModelNaNOnDiagonal(final int coordinateSystemId) {
    final GriddedElevationModel model = new IntArrayScaleGriddedElevationModel(
      GeometryFactory.fixed(coordinateSystemId, 3, 1000, 100), 0, 0, 255, 255, 1);
    for (int gridY = 0; gridY < model.getGridHeight() - 1; gridY++) {
      for (int gridX = 0; gridX < model.getGridWidth() - 1; gridX++) {
        double elevation;
        if (gridX == gridY) {
          elevation = Double.NaN;
        } else {
          elevation = gridX + gridY / 1000.0;
        }
        model.setElevation(gridX, gridY, elevation);
      }
    }
    return model;
  }

  public abstract List<String> getFileExtensions();

  @Test
  public void test001WriteReadAllFileExtensions() {
    for (final String fileExtension : getFileExtensions()) {
      final GriddedElevationModel model = GriddedElevationModelTest
        .newIntArrayModelNaNOnDiagonal(3005);
      final String filePath = "target/test/elevation/nanDiagonal." + fileExtension;
      GriddedElevationModelTest.assertWriteRead(model, filePath);
    }
  }

}
