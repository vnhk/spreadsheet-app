package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.SpreadsheetCell;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DefaultCellResolver implements CellResolver {

    private final Map<String, SpreadsheetCell> cellMap; // ex. A1 → SpreadsheetCell

    public DefaultCellResolver(List<SpreadsheetCell> cells) {
        this.cellMap = cells.stream()
                .collect(Collectors.toMap(SpreadsheetCell::getCellId, Function.identity()));
    }

    @Override
    public FunctionArgument resolve(String cellId) {
        SpreadsheetCell cell = cellMap.get(cellId);
        if (cell == null) {
            throw new IllegalArgumentException("Cell not found: " + cellId);
        }

        return new ConstantArgument(cell.getValue());
    }
}