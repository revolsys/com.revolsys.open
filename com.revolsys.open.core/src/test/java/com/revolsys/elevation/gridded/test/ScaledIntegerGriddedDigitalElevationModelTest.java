package com.revolsys.elevation.gridded.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.scaledint.ScaledIntegerGriddedDigitalElevation;
import com.revolsys.elevation.gridded.scaledint.ScaledIntegerGriddedDigitalElevationModelFile;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.util.Dates;
import com.revolsys.util.Exceptions;
import com.revolsys.util.WrappedException;

public class ScaledIntegerGriddedDigitalElevationModelTest extends GriddedElevationModelTest {

  @Override
  public List<String> getFileExtensions() {
    return Arrays.asList(ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION,
      ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION_GZ,
      ScaledIntegerGriddedDigitalElevation.FILE_EXTENSION_ZIP);
  }

  @Test
  public void test200RandomAccess() {
    final long time = System.currentTimeMillis();
    final GriddedElevationModel model = GriddedElevationModelTest
      .newIntArrayModelNaNOnDiagonal(3005);
    final String filePath = "target/test/elevation/nanDiagonal.sigdem";
    writeModel(model, filePath);
    try (
      final ScaledIntegerGriddedDigitalElevationModelFile actualModel = new ScaledIntegerGriddedDigitalElevationModelFile(
        Paths.get(filePath))) {
      assertModelEquals(model, actualModel);
    }
    System.out
      .println("test200RandomAccess\t" + Dates.toEllapsedTime(time, System.currentTimeMillis()));
  }

  @Test
  public void test201RandomAccessMissingError() {
    final String filePath = "target/test/elevation/missingError.sigdem";
    try (
      final ScaledIntegerGriddedDigitalElevationModelFile actualModel = new ScaledIntegerGriddedDigitalElevationModelFile(
        Paths.get(filePath))) {
      Assert.fail("Missing file should throw an exception");
    } catch (final WrappedException e) {
      final Throwable cause = Exceptions.unwrap(e);
      if (cause instanceof NoSuchFileException) {
        // Expected result
      } else {
        throw e;
      }
    }
  }

  @Test
  public void test203RandomAccessMissingCreateEmpty() throws IOException {
    final String filePath = "target/test/elevation/missingCreateEmpty.sigdem";
    final Path path = Paths.get(filePath);
    Files.deleteIfExists(path);
    final GeometryFactory geometryFactory = GeometryFactory.fixed3d(3005, 1000.0, 1000.0, 1000.0);
    final GriddedElevationModel expectedModel = GriddedElevationModelTest
      .newIntArrayModelEmpty(3005);
    try (
      final ScaledIntegerGriddedDigitalElevationModelFile actualModel = new ScaledIntegerGriddedDigitalElevationModelFile(
        path, geometryFactory, 0, 0, 255, 255, 1)) {
      assertModelEquals(expectedModel, actualModel);
    }

    final GriddedElevationModel actualModel = GriddedElevationModel.newGriddedElevationModel(path);
    assertModelEquals(expectedModel, actualModel);
  }

  @Test
  public void test203RandomAccessMissingCreateValues() throws IOException {
    final String filePath = "target/test/elevation/missingCreateEmpty.sigdem";
    final Path path = Paths.get(filePath);
    Files.deleteIfExists(path);
    final GeometryFactory geometryFactory = GeometryFactory.fixed3d(3005, 1000.0, 1000.0, 1000.0);
    final GriddedElevationModel expectedModel = GriddedElevationModelTest
      .newIntArrayModelNaNOnDiagonal(3005);
    try (
      final ScaledIntegerGriddedDigitalElevationModelFile actualModel = new ScaledIntegerGriddedDigitalElevationModelFile(
        path, geometryFactory, 0, 0, 255, 255, 1)) {
      actualModel.setValues(expectedModel);
      assertModelEquals(expectedModel, actualModel);
    }

    final GriddedElevationModel actualModel = GriddedElevationModel.newGriddedElevationModel(path);
    assertModelEquals(expectedModel, actualModel);
  }

}
