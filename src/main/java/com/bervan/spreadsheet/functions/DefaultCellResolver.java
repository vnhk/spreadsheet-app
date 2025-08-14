package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import com.bervan.spreadsheet.service.SpreadsheetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DefaultCellResolver implements CellResolver {

    public DefaultCellResolver() {

    }

    @Override
    public FunctionArgument resolve(String cellId, List<SpreadsheetRow> rows) {
        SpreadsheetCell cell = SpreadsheetService.findCellById(rows, cellId);

        if (cell == null) {
            log.warn("Cell not found: " + cellId);
            throw new IllegalArgumentException("Cell not found: " + cellId);
        }

        return new CellReferenceArgument(cell);
    }
}