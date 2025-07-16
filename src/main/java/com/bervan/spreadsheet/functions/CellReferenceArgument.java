package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.Spreadsheet;
import com.bervan.spreadsheet.model.SpreadsheetCell;

import java.util.Optional;

public class CellReferenceArgument implements FunctionArgument {
    private final String cellId;
    private final Spreadsheet spreadsheet;

    public CellReferenceArgument(String cellId, Spreadsheet spreadsheet) {
        this.cellId = cellId;
        this.spreadsheet = spreadsheet;
    }

    @Override
    public double asDouble() {
        String value = asText();
        return Double.parseDouble(value);
    }

    @Override
    public String asText() {
        return getCell().getValue().toString();
    }

    private SpreadsheetCell getCell() {
        Optional<SpreadsheetCell> cell = spreadsheet.getCell(cellId);
        if (cell.isEmpty()) {
            throw new RuntimeException("Cell not found in spreadsheet!");
        }
        return cell.get();
    }

}