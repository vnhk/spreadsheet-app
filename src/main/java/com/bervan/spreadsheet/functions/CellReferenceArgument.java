package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetCell;
import lombok.Getter;

public class CellReferenceArgument implements FunctionArgument {
    @Getter
    private final SpreadsheetCell cell;

    public CellReferenceArgument(SpreadsheetCell cell) {
        this.cell = cell;
    }

    @Override
    public double asDouble() {
        String value = asText();
        return Double.parseDouble(value);
    }

    @Override
    public String asText() {
        return asObject().toString();
    }

    @Override
    public Object asObject() {
        return cell.getValue();
    }
}