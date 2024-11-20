package com.bervan.spreadsheet.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpreadsheetRow {
    public int number = 0;
    public UUID rowId = UUID.randomUUID();
    public final List<Cell> cells = new ArrayList<>();

    public SpreadsheetRow() {

    }

    public SpreadsheetRow(int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            cells.add(new Cell("", i, number));
        }
    }

    public SpreadsheetRow(SpreadsheetRow rowToCopy) {
        this.number = rowToCopy.number;
        for (Cell cell : rowToCopy.getCells()) {
            addCell(cell.columnNumber, cell);
        }
    }

    public void addCell(int columnNumber) {
        cells.add(new Cell("", columnNumber, number));
    }

    public void removeAllCells() {
        cells.removeAll(cells);
    }

    public void addCell(int index, Cell cellO) {
        Cell cell;
        if (cellO.isFunction) {
            cell = new Cell(cellO.getFunctionValue(), index, number);
        } else {
            cell = new Cell(cellO.value, index, number);
        }
        cells.add(index, cell);
    }

    public void removeCell(int index) {
        if (index >= 0 && index < cells.size()) {
            cells.remove(index);
        }
    }

    public Cell getCell(int index) {
        return cells.get(index);
    }

    public Cell setCell(int index, String value) {
        Cell cell = new Cell(value, index, number);
        cells.set(index, cell);

        return cell;
    }

    public List<Cell> getCells() {
        return cells;
    }
}