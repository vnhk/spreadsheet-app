package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.Cell;
import com.bervan.spreadsheet.model.Cell;
import com.bervan.spreadsheet.model.SpreadsheetRow;
import org.apache.commons.math3.exception.NotANumberException;

import java.util.ArrayList;
import java.util.List;

public interface SpreadsheetFunction {
    default String calculateOld(List<String> relatedCells, List<SpreadsheetRow> rows) {
        throw new RuntimeException("Deprecated!");
    }

    String calculate(List<String> relatedCells, List<List<Cell>> rows);

    String getInfo();

    String getName();

    default List<Object> getParams(List<String> allParams, List<List<Cell>> rows) {
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

        List<Cell> collect = rows.stream()
                .flatMap(List::stream)
                .filter(e -> cells.contains(e.cellId))
                .toList();

        result.addAll(collect);

        return result;
    }

    default List<Object> getParams_careAboutOrder(List<String> allParams, List<List<Cell>> rows) {
        List<Object> relatedCells = new ArrayList<>();
        for (String cellId : allParams) {
            relatedCells.add(getParam(cellId, rows));
        }

        return relatedCells;
    }

    private static Object getParam(String param, List<List<Cell>> rows) {
        if (!startsWithCapitalsAndEndsWithNumber(param)) {
            return param;
        }

        return rows.stream()
                .flatMap(List::stream)
                .filter(e -> param.contains(e.cellId))
                .toList();
    }

    private static boolean startsWithCapitalsAndEndsWithNumber(String input) {
        return input.matches("^[A-Z]+\\d+$");
    }

    default double getDouble(List<Object> params, int i) {
        try {
            Object param = params.get(i);
            double val;
            if (param instanceof Cell) {
                val = Double.parseDouble(((Cell) param).value);
            } else {
                val = Double.parseDouble(String.valueOf(param));
            }
            return val;
        } catch (Exception e) {
            throw new NotANumberException();
        }
    }

    default String getString(List<Object> params, int i) {
        try {
            Object param = params.get(i);
            String val;
            if (param instanceof Cell) {
                val = ((Cell) param).value;
            } else {
                val = String.valueOf(param);
            }
            return val;
        } catch (Exception e) {
            throw new RuntimeException("Invalid function value");
        }
    }
}
