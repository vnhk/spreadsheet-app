package com.bervan.spreadsheet.functions;

import com.bervan.spreadsheet.model.Cell;
import com.bervan.spreadsheet.model.SpreadsheetRow;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface SpreadsheetFunction {
    default String calculateOld(List<String> relatedCells, List<SpreadsheetRow> rows) {
        List<List<Cell>> collect = rows.stream().map(SpreadsheetRow::getCells).collect(Collectors.toList());
        return calculate(relatedCells, collect);
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
                .filter(e -> cells.contains(e.getCellId()))
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
                .filter(e -> param.contains(e.getCellId()))
                .findFirst().orElseThrow();
    }

    private static boolean startsWithCapitalsAndEndsWithNumber(String input) {
        return input.matches("^[A-Z]+\\d+$");
    }

    default double getDouble(List<Object> params, int i) {
        try {
            Object param = params.get(i);
            double val;
            if (param instanceof Cell) {
                val = Double.parseDouble(((Cell) param).getValue());
            } else {
                val = Double.parseDouble(String.valueOf(param));
            }
            return val;
        } catch (Exception e) {
            throw new NumberFormatException();
        }
    }

    default String getString(List<Object> params, int i) {
        try {
            Object param = params.get(i);
            String val;
            if (param instanceof Cell) {
                val = ((Cell) param).getValue();
            } else {
                val = String.valueOf(param);
            }
            return val;
        } catch (Exception e) {
            throw new RuntimeException("Invalid function value");
        }
    }
}
