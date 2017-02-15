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
import com.revolsys.elevation.gridded.compactbinary.CompactBinaryGriddedElevation;
import com.revolsys.elevation.gridded.compactbinary.CompactBinaryGriddedElevationModelFile;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.util.Dates;
import com.revolsys.util.Exceptions;
import com.revolsys.util.WrappedException;

public class CompactBinaryGriddedElevationModelTest extends GriddedElevationModelTest {

  @Override
  public List<String> getFileExtensions() {
    return Arrays.asList(CompactBinaryGriddedElevation.FILE_EXTENSION,
      CompactBinaryGriddedElevation.FILE_EXTENSION_GZ,
      CompactBinaryGriddedElevation.FILE_EXTENSION_ZIP);
  }

  @Test
  public void test200RandomAccess() {
    final long time = System.currentTimeMillis();
    final GriddedElevationModel model = GriddedElevationModelTest
      .newIntArrayModelNaNOnDiagonal(3005);
    final String filePath = "target/test/elevation/nanDiagonal.demcb";
    writeModel(model, filePath);
    try (
      final CompactBinaryGriddedElevationModelFile actualModel = new CompactBinaryGriddedElevationModelFile(
        Paths.get(filePath))) {
      assertModelEquals(model, actualModel);
    }
    System.out
      .println("test200RandomAccess\t" + Dates.toEllapsedTime(time, System.currentTimeMillis()));
  }

  @Test
  public void test201RandomAccessMissingError() {
    final String filePath = "target/test/elevation/missingError.demcb";
    try (
      final CompactBinaryGriddedElevationModelFile actualModel = new CompactBinaryGriddedElevationModelFile(
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
    final String filePath = "target/test/elevation/missingCreateEmpty.demcb";
    final Path path = Paths.get(filePath);
    Files.deleteIfExists(path);
    final GeometryFactory geometryFactory = GeometryFactory.fixed(3005, 3, 1000.0, 1000.0, 1000.0);
    final GriddedElevationModel expectedModel = GriddedElevationModelTest
      .newIntArrayModelEmpty(3005);
    try (
      final CompactBinaryGriddedElevationModelFile actualModel = new CompactBinaryGriddedElevationModelFile(
        path, geometryFactory, 0, 0, 255, 255, 1)) {
      assertModelEquals(expectedModel, actualModel);
    }

    final GriddedElevationModel actualModel = GriddedElevationModel.newGriddedElevationModel(path);
    assertModelEquals(expectedModel, actualModel);
  }

  @Test
  public void test203RandomAccessMissingCreateValues() throws IOException {
    final String filePath = "target/test/elevation/missingCreateEmpty.demcb";
    final Path path = Paths.get(filePath);
    Files.deleteIfExists(path);
    final GeometryFactory geometryFactory = GeometryFactory.fixed(3005, 3, 1000.0, 1000.0, 1000.0);
    final GriddedElevationModel expectedModel = GriddedElevationModelTest
      .newIntArrayModelNaNOnDiagonal(3005);
    try (
      final CompactBinaryGriddedElevationModelFile actualModel = new CompactBinaryGriddedElevationModelFile(
        path, geometryFactory, 0, 0, 255, 255, 1)) {
      actualModel.setElevations(expectedModel);
      assertModelEquals(expectedModel, actualModel);
    }

    final GriddedElevationModel actualModel = GriddedElevationModel.newGriddedElevationModel(path);
    assertModelEquals(expectedModel, actualModel);
  }

}
