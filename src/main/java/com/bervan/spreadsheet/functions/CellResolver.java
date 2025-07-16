package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetRow;

import java.util.List;

public interface CellResolver {
    FunctionArgument resolve(String cellId, List<SpreadsheetRow> rows);
}