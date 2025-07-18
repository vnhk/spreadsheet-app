package com.bervan.spreadsheet.model;

import com.bervan.spreadsheet.utils.SpreadsheetUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class SpreadsheetCell {
    private final String cellId;
    private final int rowNumber;
    private final int columnNumber;
    private Object value;
    private String formula;
    private CellType cellType;

    public SpreadsheetCell(int rowNumber, int columnNumber, Object value) {
        if (rowNumber == 0) {
            throw new IllegalArgumentException("Row index cannot be equal to 0");
        }
        if (columnNumber == 0) {
            throw new IllegalArgumentException("Column number cannot be equal to 0");
        }
        this.rowNumber = rowNumber;
        this.columnNumber = columnNumber;
        setValueRelatedFields(value);
        this.cellId = SpreadsheetUtils.getColumnHeader(columnNumber) + rowNumber;
    }

    public SpreadsheetCell(Object value, int columnNumber, int rowNumber) {
        this(rowNumber, columnNumber, value);
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

    public void setValue(Object value) {
        setValueRelatedFields(value);
    }

    public boolean hasFormula() {
        return formula != null && !formula.isEmpty() && cellType == CellType.FORMULA;
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
