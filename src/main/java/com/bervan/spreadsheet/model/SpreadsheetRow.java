package com.bervan.spreadsheet.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SpreadsheetRow {
    public int rowNumber = 1;
    public UUID rowId = UUID.randomUUID();
    public List<SpreadsheetCell> cells = new ArrayList<>();

    public SpreadsheetRow() {

    }

    public SpreadsheetRow(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public SpreadsheetCell getCell(int index) {
        return cells.get(index);
    }

    public List<SpreadsheetCell> getCells() {
        return cells;
    }

    public void setCells(List<SpreadsheetCell> cells) {
        this.cells = cells;
    }

    public void addCell(SpreadsheetCell... cell) {
        this.cells.addAll(Arrays.stream(cell).toList());
    }
}