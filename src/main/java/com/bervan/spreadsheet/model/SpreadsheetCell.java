package com.bervan.spreadsheet.model;

import com.bervan.spreadsheet.utils.SpreadsheetUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class SpreadsheetCell {
    private final String cellId;
    private final int rowIndex;
    private final int columnIndex;
    private Object value;
    private CellType cellType;

    public SpreadsheetCell(int rowIndex, int columnIndex, Object value) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        setValueRelatedFields(value);
        this.cellId = SpreadsheetUtils.getColumnHeader(columnIndex) + rowIndex;
    }

    private void setValueRelatedFields(Object value) {
        this.value = value;
        this.cellType = detectCellType(value);
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

    public void setValue(String value) {
        setValueRelatedFields(value);
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
