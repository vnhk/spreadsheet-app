package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.Cell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import org.apache.commons.math3.exception.NotANumberException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface SpreadsheetFunction {
    String calculate(List<String> relatedCells, List<SpreadsheetRow> rows);

    default List<Object> getParams(List<String> allParams, List<SpreadsheetRow> rows) {
        List<String> cells = new ArrayList<>();
        List<String> notCells = new ArrayList<>();

        for (String param : allParams) {
            if (SpreadsheetFunction.startsWithCapitalsAndEndsWithNumber(param)) {
                cells.add(param);
            } else {
                notCells.add(param);
            }
        }

        List<Object> result = new ArrayList<>(notCells);

        List<Cell> collect = rows.stream().map(SpreadsheetRow::getCells)
                .flatMap(Collection::stream)
                .filter(e -> cells.contains(e.cellId))
                .toList();

        result.addAll(collect);

        return result;
    }

    default List<Object> getParams_careAboutOrder(List<String> allParams, List<SpreadsheetRow> rows) {
        List<Object> relatedCells = new ArrayList<>();
        for (String cellId : allParams) {
            relatedCells.add(getParam(cellId, rows));
        }

        return relatedCells;
    }

    private static Object getParam(String param, List<SpreadsheetRow> rows) {
        if (!startsWithCapitalsAndEndsWithNumber(param)) {
            return param;
        }

        return rows.stream().map(SpreadsheetRow::getCells)
                .flatMap(Collection::stream)
                .filter(e -> param.equals(e.cellId))
                .findFirst().get();
    }

    private static boolean startsWithCapitalsAndEndsWithNumber(String input) {
        return input.matches("^[A-Z]+\\d+$");
    }

    default double getDouble(List<Object> params, int i) {
        Object param = params.get(i);
        double val;
        if (param instanceof Cell) {
            val = Double.parseDouble(((Cell) param).value);
        } else if (param instanceof String || param == null) {
            throw new NotANumberException();
        } else {
            val = Double.parseDouble(String.valueOf(param));
        }
        return val;
    }
}
