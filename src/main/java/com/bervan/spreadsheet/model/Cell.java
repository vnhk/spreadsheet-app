package com.bervan.spreadsheet.model;

import java.util.ArrayList;
import java.util.List;

public class Cell {
    public String cellId;
    //displayValue
    public String value;
    //internalValueIfFunction to be visible on click
    public String functionValue;
    public String function;
    public boolean isFunction;
    public List<String> relatedCells = new ArrayList<>();

    private String getColumnHeader(int columnIndex) {
        StringBuilder label = new StringBuilder();
        while (columnIndex >= 0) {
            label.insert(0, (char) ('A' + (columnIndex % 26)));
            columnIndex = columnIndex / 26 - 1;
        }
        return label.toString();
    }

    Cell(String value, int columnNumber, int rowNumber) {
        this.value = value;
        this.cellId = getColumnHeader(columnNumber) + rowNumber;

        if (value.startsWith("=")) {
            functionValue = value;
            isFunction = true;
            function = value.split("=")[1].split("\\(")[0];
            String toParseRelated = value.split("=")[1].split("\\(")[1];
            String[] split = toParseRelated.split(",");
            for (String s : split) {
                if (s.endsWith(")")) {
                    s = s.substring(0, s.length() - 1);
                }
                relatedCells.add(s);
            }
        }
    }

}
