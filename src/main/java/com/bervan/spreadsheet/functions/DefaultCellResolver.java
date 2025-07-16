package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetCell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DefaultCellResolver implements CellResolver {


    public DefaultCellResolver() {

    }

    @Override
    public FunctionArgument resolve(String cellId, List<SpreadsheetRow> rows) {
        Map<String, SpreadsheetCell> cellMap = rows.stream().map(SpreadsheetRow::getCells)
                .flatMap(Collection::parallelStream)
                .collect(Collectors.toMap(SpreadsheetCell::getCellId, Function.identity()));

        SpreadsheetCell cell = cellMap.get(cellId);
        if (cell == null) {
            throw new IllegalArgumentException("Cell not found: " + cellId);
        }

        return new CellReferenceArgument(cell);
    }
}