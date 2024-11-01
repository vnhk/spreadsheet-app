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

            if (toParseRelated.contains(",")) {
                //comma separated cells
                commaSeparatedCells(toParseRelated);
            } else if (toParseRelated.contains(":")) {
                //range cells
                colonSeparatedCells(toParseRelated);
            }


        }
    }

    private void commaSeparatedCells(String toParseRelated) {
        String[] split = toParseRelated.split(",");
        for (String s : split) {
            if (s.endsWith(")")) {
                s = s.substring(0, s.length() - 1);
            }
            relatedCells.add(s);
        }
    }

    private void colonSeparatedCells(String toParseRelated) {
        String[] split = toParseRelated.split(":");

        List<String> ranges = new ArrayList<>();
        for (String s : split) {
            if (s.endsWith(")")) {
                s = s.substring(0, s.length() - 1);
            }

            ranges.add(s);
        }

        if (ranges.size() != 2) {
            throw new RuntimeException("Invalid usage of colon separated values!");
        }

        //only 1 row or only 1 column
        String columnOne = ranges.get(0).replaceAll("^([A-Za-z]+).*", "$1");
        String columnTwo = ranges.get(1).replaceAll("^([A-Za-z]+).*", "$1");

        if (columnTwo.equals(columnOne)) {
            //1 column, multiple rows
            Integer start = Integer.parseInt(ranges.get(0).replaceAll(".*?(\\d+)$", "$1"));
            Integer end = Integer.parseInt(ranges.get(1).replaceAll(".*?(\\d+)$", "$1"));

            if (start > end) {
                throw new RuntimeException("Invalid usage of colon separated values!");
            }

            for (; start <= end; start++) {
                relatedCells.add(columnOne + start);
            }

        } else {
            //1 row, multiple columns
            List<String> cells = generateRange(ranges.get(0), ranges.get(1));
            relatedCells.addAll(cells);
        }


    }

    private List<String> generateRange(String start, String end) {
        List<String> result = new ArrayList<>();

        // Extract the initial letter and the constant number part
        String startLetter = start.replaceAll("\\d", "");
        String endLetter = end.replaceAll("\\d", "");
        int startIndex = getColumnIndex(startLetter);
        int endIndex = getColumnIndex(endLetter);
        String numberPart = start.replaceAll("\\D", "");

        // Generate range from startIndex to endIndex
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(getColumnHeader(i) + numberPart);
        }

        return result;
    }


    // Method to convert column header (like "A", "B", ..., "AA") to an index
    private int getColumnIndex(String columnHeader) {
        int index = 0;
        for (int i = 0; i < columnHeader.length(); i++) {
            index = index * 26 + (columnHeader.charAt(i) - 'A' + 1);
        }
        return index - 1;
    }


}
