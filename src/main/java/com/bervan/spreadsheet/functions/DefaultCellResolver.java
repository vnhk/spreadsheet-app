package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.bervan.spreadsheet.service.SpreadsheetService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultCellResolver implements CellResolver {

    public DefaultCellResolver() {

    }

    @Override
    public FunctionArgument resolve(String cellId, List<SpreadsheetRow> rows) {
        SpreadsheetCell cell = SpreadsheetService.findCellById(rows, cellId);

        if (cell == null) {
            throw new IllegalArgumentException("Cell not found: " + cellId);
        }

        return new CellReferenceArgument(cell);
    }
}