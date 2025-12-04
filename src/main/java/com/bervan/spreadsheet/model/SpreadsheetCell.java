package com.bervan.spreadsheet.model;

import com.bervan.logging.JsonLogger;
import com.bervan.spreadsheet.utils.SpreadsheetUtils;
import lombok.Getter;

@Getter
public class SpreadsheetCell {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "spreadsheets");
    private String cellId;
    private int rowNumber;
    private int columnNumber;
    private Object value;
    private String formula;
    private CellType cellType;

    public SpreadsheetCell() {

    }

    public SpreadsheetCell(int rowNumber, int columnNumber, Object value) {
        if (rowNumber == 0) {
            log.warn("Row index cannot be equal to 0");
        }
        if (columnNumber == 0) {
            log.warn("Column number cannot be equal to 0");
        }
        this.rowNumber = rowNumber;
        this.columnNumber = columnNumber;
        setValueRelatedFields(value);
        this.cellId = calcCellId();
    }

    public SpreadsheetCell(Object value, int columnNumber, int rowNumber) {
        this(rowNumber, columnNumber, value);
    }

    private String calcCellId() {
        return SpreadsheetUtils.getColumnHeader(columnNumber) + rowNumber;
    }

    private void setValueRelatedFields(Object value) {
        this.value = value;
        this.cellType = detectCellType(value);

        if (cellType == CellType.FORMULA) {
            formula = value.toString();
        }
    }

    private CellType detectCellType(Object value) {
        if (value == null) {
            return CellType.EMPTY;
        }

        String valueString = value.toString();

        if (valueString.isEmpty()) {
            return CellType.EMPTY;
        }

        if (valueString.startsWith("=")) {
            return CellType.FORMULA;
        }
        try {
            Double.parseDouble(valueString);
            return CellType.NUMBER;
        } catch (NumberFormatException e) {
            return CellType.TEXT;
        }
    }

    public void setNewValueAndCellRelatedFields(Object value) {
        setValueRelatedFields(value);
    }

    public void updateValue(Object value) {
        this.value = value;
    }

    public boolean hasFormula() {
        return cellType == CellType.FORMULA && formula != null && !formula.isEmpty();
    }

    public void updateColumnNumber(int newColumnNumber) {
        this.columnNumber = newColumnNumber;
        this.cellId = calcCellId();
    }

    public void updateRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
        this.cellId = calcCellId();
    }

    public enum CellType {
        TEXT,
        NUMBER,
        FORMULA,
        DATE,        // not handled yet
        BOOLEAN,     // not handled yet
        ERROR,       // not handled yet
        EMPTY        // optional: for null/empty values
    }
}
