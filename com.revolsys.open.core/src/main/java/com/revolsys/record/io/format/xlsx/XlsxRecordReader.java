package com.revolsys.record.io.format.xlsx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.SpreadsheetML.SharedStrings;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTSst;
import org.xlsx4j.sml.CTXstringWhitespace;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.Worksheet;

import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.logging.Logs;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.format.csv.AbstractRecordReader;
import com.revolsys.spring.resource.Resource;

public class XlsxRecordReader extends AbstractRecordReader {
  private Resource resource;

  private List<Row> rows = Collections.emptyList();

  private int rowIndex = 0;

  private List<CTRst> sharedStringList = Collections.emptyList();

  public XlsxRecordReader(final Resource resource) {
    this(resource, ArrayRecord.FACTORY);
  }

  public XlsxRecordReader(final Resource resource,
    final RecordFactory<? extends Record> recordFactory) {
    super(recordFactory);
    this.resource = resource;
  }

  @Override
  protected void closeDo() {
    super.closeDo();
    this.resource = null;
    this.rows = Collections.emptyList();
    this.sharedStringList = Collections.emptyList();
  }

  @Override
  protected Record getNext() {
    final List<String> row = readNextRow();
    if (row != null && row.size() > 0) {
      return parseRecord(row);
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  protected void initDo() {
    super.initDo();
    try (
      InputStream in = this.resource.newBufferedInputStream()) {

      final SpreadsheetMLPackage spreadsheetPackage = (SpreadsheetMLPackage)SpreadsheetMLPackage
        .load(in);
      WorksheetPart worksheetPart = null;
      for (final Part part : spreadsheetPackage.getParts().getParts().values()) {
        if (part instanceof WorksheetPart) {
          if (worksheetPart == null) {
            worksheetPart = (WorksheetPart)part;
          }
        } else if (part instanceof SharedStrings) {
          final SharedStrings sharedStrings = (SharedStrings)part;
          final CTSst contents = sharedStrings.getContents();
          this.sharedStringList = contents.getSi();
        }
      }
      if (worksheetPart != null) {
        final Worksheet worksheet = worksheetPart.getContents();
        final SheetData sheetData = worksheet.getSheetData();
        this.rows = sheetData.getRow();
        final List<String> line = readNextRow();
        final String filename = this.resource.getFilename();
        newRecordDefinition(filename, line);
      }
    } catch (final IOException | Docx4JException e) {
      Logs.error(this, "Unable to open " + this.resource, e);
    } catch (final NoSuchElementException e) {
    }
  }

  @Override
  protected GeometryFactory loadGeometryFactory() {
    return EsriCoordinateSystems.getGeometryFactory(this.resource);
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   *
   * @return a string array with each comma-separated element as a separate
   *         entry.
   * @throws IOException if bad things happen during the read
   */
  private List<String> readNextRow() {
    if (this.rowIndex < this.rows.size()) {
      final List<String> values = new ArrayList<>();
      final Row row = this.rows.get(this.rowIndex);
      for (final Cell cell : row.getC()) {
        String value = null;
        final String cellValue = cell.getV();

        final STCellType cellType = cell.getT();
        switch (cellType) {
          case S:
            final int stringIndex = Integer.parseInt(cellValue);
            final CTRst sharedString = this.sharedStringList.get(stringIndex);
            final CTXstringWhitespace text = sharedString.getT();
            value = text.getValue();
          break;
          default:
            if (cellValue == null) {
              final CTRst is = cell.getIs();
              value = is.getT().getValue();
            } else {
              value = cellValue;
            }
          break;
        }
        values.add(value);
      }
      this.rowIndex++;
      return values;
    } else {
      throw new NoSuchElementException();
    }
  }
}
